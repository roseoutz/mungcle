import { Slot, useRouter, useSegments } from 'expo-router';
import React, { useEffect } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../src/features/auth';

function RootNavigator() {
  const { isAuthenticated, loading } = useAuth();
  const segments = useSegments();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;

    const inAuthGroup = segments[0] === '(auth)';

    if (!isAuthenticated && !inAuthGroup) {
      // 미인증 상태 → 로그인으로
      router.replace('/(auth)/login');
    } else if (isAuthenticated && inAuthGroup) {
      // 인증 상태 → 홈으로
      router.replace('/(tabs)');
    }
  }, [isAuthenticated, loading, segments, router]);

  return <Slot />;
}

export default function RootLayout() {
  return (
    <SafeAreaProvider>
      <AuthProvider>
        <RootNavigator />
      </AuthProvider>
    </SafeAreaProvider>
  );
}
