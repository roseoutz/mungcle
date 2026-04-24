import React, { useEffect, useRef, useState } from 'react';
import { Animated, StyleSheet, Text } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';

// 네트워크 상태 감지 — expo-network 없이 fetch probe 방식 사용
async function checkOnline(): Promise<boolean> {
  try {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 5000);
    await fetch('https://clients3.google.com/generate_204', {
      method: 'HEAD',
      signal: controller.signal,
      cache: 'no-store',
    });
    clearTimeout(timeout);
    return true;
  } catch {
    return false;
  }
}

const POLL_INTERVAL_MS = 5000;

// 오프라인 상태일 때 상단에 표시되는 배너 — 연결 복구 시 자동으로 사라짐
export function OfflineBanner() {
  const [isOffline, setIsOffline] = useState(false);
  const translateY = useRef(new Animated.Value(-60)).current;

  useEffect(() => {
    let mounted = true;

    async function poll() {
      const online = await checkOnline();
      if (!mounted) return;
      setIsOffline(!online);
    }

    poll();
    const interval = setInterval(poll, POLL_INTERVAL_MS);
    return () => {
      mounted = false;
      clearInterval(interval);
    };
  }, []);

  useEffect(() => {
    Animated.timing(translateY, {
      toValue: isOffline ? 0 : -60,
      duration: 300,
      useNativeDriver: true,
    }).start();
  }, [isOffline, translateY]);

  return (
    <Animated.View
      style={[styles.banner, { transform: [{ translateY }] }]}
      accessibilityLabel="오프라인 상태입니다"
      accessibilityLiveRegion="polite"
      pointerEvents="none"
    >
      <Text style={styles.text}>인터넷 연결이 없어요</Text>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  banner: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    backgroundColor: colors.warning,
    paddingVertical: spacing.sm,
    paddingHorizontal: spacing.md,
    alignItems: 'center',
    zIndex: 999,
  },
  text: {
    ...typography.caption,
    color: colors.surface,
    fontWeight: '600',
  },
});
