import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import * as SecureStore from 'expo-secure-store';
import { loginEmail, loginKakao, registerEmail } from '../services/auth.api';
import type { UserInfo } from '../types/auth.types';

interface AuthContextValue {
  isAuthenticated: boolean;
  user: UserInfo | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, nickname: string) => Promise<void>;
  kakaoLogin: (token: string) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(true);

  // 앱 시작 시 저장된 토큰으로 인증 상태 복원
  useEffect(() => {
    async function restoreSession() {
      try {
        const token = await SecureStore.getItemAsync('accessToken');
        const userJson = await SecureStore.getItemAsync('user');
        if (token && userJson) {
          setUser(JSON.parse(userJson) as UserInfo);
        }
      } catch {
        // 토큰 복원 실패 시 로그아웃 상태 유지
      } finally {
        setLoading(false);
      }
    }
    restoreSession();
  }, []);

  const saveSession = useCallback(async (accessToken: string, userInfo: UserInfo) => {
    await SecureStore.setItemAsync('accessToken', accessToken);
    await SecureStore.setItemAsync('user', JSON.stringify(userInfo));
    setUser(userInfo);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await loginEmail(email, password);
    await saveSession(response.accessToken, response.user);
  }, [saveSession]);

  const register = useCallback(async (email: string, password: string, nickname: string) => {
    const response = await registerEmail(email, password, nickname);
    await saveSession(response.accessToken, response.user);
  }, [saveSession]);

  const kakaoLogin = useCallback(async (token: string) => {
    const response = await loginKakao(token);
    await saveSession(response.accessToken, response.user);
  }, [saveSession]);

  const logout = useCallback(async () => {
    await SecureStore.deleteItemAsync('accessToken');
    await SecureStore.deleteItemAsync('user');
    setUser(null);
  }, []);

  const value: AuthContextValue = {
    isAuthenticated: user !== null,
    user,
    loading,
    login,
    register,
    kakaoLogin,
    logout,
  };

  return React.createElement(AuthContext.Provider, { value }, children);
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
