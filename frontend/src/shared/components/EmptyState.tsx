import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';
import { typography } from '../../constants/typography';
import { Button } from './Button';

interface EmptyStateProps {
  icon?: string;
  message: string;
  ctaLabel?: string;
  onCta?: () => void;
}

// 빈 화면 공통 컴포넌트 — icon + 메시지 + 선택적 CTA 버튼
export function EmptyState({ icon, message, ctaLabel, onCta }: EmptyStateProps) {
  return (
    <View style={styles.container} accessibilityRole="none">
      {icon ? <Text style={styles.icon}>{icon}</Text> : null}
      <Text style={styles.message}>{message}</Text>
      {ctaLabel && onCta ? (
        <View style={styles.cta}>
          <Button
            variant="outline"
            size="md"
            onPress={onCta}
            accessibilityLabel={ctaLabel}
          >
            {ctaLabel}
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
  message: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
    marginBottom: spacing.md,
  },
  cta: {
    marginTop: spacing.sm,
  },
});
