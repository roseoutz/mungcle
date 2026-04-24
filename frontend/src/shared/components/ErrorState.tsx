import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';
import { Button } from './Button';

type ErrorVariant = 'network' | 'server' | 'notFound' | 'generic';

interface ErrorStateProps {
  variant?: ErrorVariant;
  // 메시지 오버라이드 — 미제공 시 variant 기본 메시지 사용
  message?: string;
  onRetry?: () => void;
}

const VARIANT_CONFIG: Record<ErrorVariant, { icon: string; title: string; message: string }> = {
  network: {
    icon: '📡',
    title: '연결할 수 없어요',
    message: '인터넷 연결을 확인하고 다시 시도해주세요',
  },
  server: {
    icon: '🔧',
    title: '서버 오류',
    message: '서버에 문제가 생겼어요. 잠시 후 다시 시도해주세요',
  },
  notFound: {
    icon: '🔍',
    title: '찾을 수 없어요',
    message: '요청하신 정보를 찾을 수 없어요',
  },
  generic: {
    icon: '⚠️',
    title: '오류가 발생했어요',
    message: '잠시 후 다시 시도해주세요',
  },
};

// 데이터 화면 공통 에러 상태 컴포넌트 — variant별 아이콘/메시지 + 재시도 버튼
export function ErrorState({ variant = 'generic', message, onRetry }: ErrorStateProps) {
  const config = VARIANT_CONFIG[variant];

  return (
    <View style={styles.container} accessibilityRole="none">
      <Text style={styles.icon} accessibilityElementsHidden>
        {config.icon}
      </Text>
      <Text style={styles.title}>{config.title}</Text>
      <Text style={styles.message}>{message ?? config.message}</Text>
      {onRetry ? (
        <View style={styles.retryButton}>
          <Button
            variant="outline"
            size="md"
            onPress={onRetry}
            accessibilityLabel="다시 시도"
          >
            다시 시도
          </Button>
        </View>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.xl,
    paddingHorizontal: spacing.lg,
  },
  icon: {
    fontSize: 48,
    marginBottom: spacing.md,
  },
  title: {
    ...typography.subheading,
    color: colors.text,
    textAlign: 'center',
    marginBottom: spacing.sm,
  },
  message: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
    marginBottom: spacing.lg,
  },
  retryButton: {
    minWidth: 120,
  },
});
