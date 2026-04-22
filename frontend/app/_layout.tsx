import { Slot, useRouter, useSegments } from 'expo-router';
import * as Notifications from 'expo-notifications';
import React, { useEffect, useRef } from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider, useAuth } from '../src/features/auth';
import { apiClient } from '../src/shared/utils/api';

// 알림 표시 방식 설정: 앱이 포그라운드일 때도 배너 표시
Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowAlert: true,
    shouldPlaySound: true,
    shouldSetBadge: true,
    shouldShowBanner: true,
    shouldShowList: true,
  }),
});

function PushNotificationProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { isAuthenticated } = useAuth();
  const notificationListener = useRef<Notifications.EventSubscription | null>(null);
  const responseListener = useRef<Notifications.EventSubscription | null>(null);

  useEffect(() => {
    if (!isAuthenticated) return;

    // FCM 토큰 등록
    async function registerPushToken() {
      try {
        const { status } = await Notifications.getPermissionsAsync();
        let finalStatus = status;
        if (status !== 'granted') {
          const { status: newStatus } = await Notifications.requestPermissionsAsync();
          finalStatus = newStatus;
        }
        if (finalStatus !== 'granted') return;

        const token = (await Notifications.getExpoPushTokenAsync()).data;
        await apiClient('/api/auth/push-token', {
          method: 'POST',
          body: JSON.stringify({ token }),
        });
      } catch {
        // 토큰 등록 실패는 무시 (알림 기능 비필수)
      }
    }

    registerPushToken();

    // 포그라운드 알림 수신 리스너
    notificationListener.current = Notifications.addNotificationReceivedListener(
      (_notification) => {
        // 포그라운드 알림 수신 — 배지 업데이트 등 필요 시 여기에 처리
      },
    );

    // 알림 탭 응답 리스너 (딥링크 이동)
    responseListener.current = Notifications.addNotificationResponseReceivedListener(
      (response) => {
        const data = response.notification.request.content.data as Record<string, unknown>;
        const type = data?.type as string | undefined;

        if (type === 'GREETING_RECEIVED' || type === 'GREETING_ACCEPTED' || type === 'MESSAGE_RECEIVED') {
          const greetingId = data?.greetingId;
          if (typeof greetingId === 'number') {
            router.push(`/messages/${greetingId}` as Parameters<typeof router.push>[0]);
          }
        }
      },
    );

    return () => {
      notificationListener.current?.remove();
      responseListener.current?.remove();
    };
  }, [isAuthenticated, router]);

  return <>{children}</>;
}

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
        <PushNotificationProvider>
          <RootNavigator />
        </PushNotificationProvider>
      </AuthProvider>
    </SafeAreaProvider>
  );
}
