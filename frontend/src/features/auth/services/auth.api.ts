import { apiClient } from '../../../shared/utils/api';
import type { AuthResponse } from '../types/auth.types';

export async function loginEmail(email: string, password: string): Promise<AuthResponse> {
  return apiClient<AuthResponse>('/api/auth/email/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
}

export async function registerEmail(
  email: string,
  password: string,
  nickname: string,
): Promise<AuthResponse> {
  return apiClient<AuthResponse>('/api/auth/email/register', {
    method: 'POST',
    body: JSON.stringify({ email, password, nickname }),
  });
}

export async function loginKakao(token: string): Promise<AuthResponse> {
  return apiClient<AuthResponse>('/api/auth/kakao/login', {
    method: 'POST',
    body: JSON.stringify({ token }),
  });
}
