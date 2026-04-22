import { Stack } from 'expo-router';
import React from 'react';
import { colors } from '../../src/constants/theme';

export default function AuthLayout() {
  return (
    <Stack
      screenOptions={{
        headerStyle: { backgroundColor: colors.background },
        headerTintColor: colors.text,
        headerTitleStyle: { fontWeight: '600' },
        contentStyle: { backgroundColor: colors.background },
      }}
    >
      <Stack.Screen name="login" options={{ title: '로그인', headerShown: false }} />
      <Stack.Screen name="email-login" options={{ title: '이메일 로그인' }} />
      <Stack.Screen name="register" options={{ title: '회원가입' }} />
    </Stack>
  );
}
