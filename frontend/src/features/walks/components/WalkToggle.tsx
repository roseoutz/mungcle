import * as Location from 'expo-location';
import React, { useCallback, useEffect, useState } from 'react';
import { ActivityIndicator, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, spacing, touchTarget } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import { useWalkToggle } from '../hooks/useWalkToggle';

interface WalkToggleProps {
  dogId: number | null;
}

// 초를 mm:ss 형태로 포맷
function formatCountdown(seconds: number): string {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

// OFF → OPEN → (산책 중 + 카운트다운) 3상태 토글
export function WalkToggle({ dogId }: WalkToggleProps) {
  const { state, secondsRemaining, loading, error, toggle } = useWalkToggle(dogId);
  const [locationError, setLocationError] = useState<string | null>(null);

  useEffect(() => {
    setLocationError(null);
  }, [state]);

  const handlePress = useCallback(async () => {
    setLocationError(null);
    const { status } = await Location.requestForegroundPermissionsAsync();
    if (status !== 'granted') {
      setLocationError('위치 권한이 필요합니다.');
      return;
    }
    const loc = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced });
    toggle(loc.coords.latitude, loc.coords.longitude);
  }, [toggle]);

  const isOff = state === 'OFF';
  const isActive = state === 'OPEN' || state === 'ACTIVE';

  return (
    <View style={styles.container}>
      <TouchableOpacity
        style={[styles.button, isActive && styles.buttonActive, loading && styles.buttonLoading]}
        onPress={handlePress}
        disabled={loading || !dogId}
        accessibilityLabel={isOff ? '산책 시작하기' : '산책 종료하기'}
        accessibilityRole="button"
        accessibilityState={{ busy: loading }}
      >
        {loading ? (
          <ActivityIndicator color={colors.surface} size="small" />
        ) : (
          <View style={styles.inner}>
            <Text style={styles.emoji}>{isOff ? '🐾' : '🏃'}</Text>
            <Text style={styles.label}>{isOff ? '산책 시작' : '산책 중'}</Text>
            {isActive && (
              <Text style={styles.countdown}>{formatCountdown(secondsRemaining)}</Text>
            )}
          </View>
        )}
      </TouchableOpacity>

      {(error || locationError) ? (
        <Text style={styles.error}>{locationError ?? error?.message}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    paddingVertical: spacing.md,
  },
  button: {
    backgroundColor: colors.primary,
    borderRadius: 32,
    minHeight: touchTarget.min,
    paddingHorizontal: spacing.xl,
    paddingVertical: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 160,
  },
  buttonActive: {
    backgroundColor: colors.secondary,
  },
  buttonLoading: {
    opacity: 0.6,
  },
  inner: {
    alignItems: 'center',
    gap: spacing.xs,
  },
  emoji: {
    fontSize: 24,
  },
  label: {
    ...typography.subheading,
    color: colors.surface,
  },
  countdown: {
    ...typography.caption,
    color: colors.surface,
    opacity: 0.9,
  },
  error: {
    ...typography.caption,
    color: colors.error,
    marginTop: spacing.sm,
    textAlign: 'center',
  },
});
