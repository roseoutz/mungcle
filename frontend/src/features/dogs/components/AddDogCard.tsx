import React from 'react';
import { StyleSheet, Text, TouchableOpacity } from 'react-native';
import { colors, spacing, touchTarget } from '../../../constants/theme';
import { typography } from '../../../constants/typography';

interface AddDogCardProps {
  onPress: () => void;
}

// 점선 테두리 강아지 추가 카드
export function AddDogCard({ onPress }: AddDogCardProps) {
  return (
    <TouchableOpacity
      style={styles.card}
      onPress={onPress}
      accessibilityLabel="강아지 추가하기"
      accessibilityRole="button"
    >
      <Text style={styles.icon}>+</Text>
      <Text style={styles.label}>강아지 추가</Text>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    borderWidth: 1.5,
    borderColor: colors.border,
    borderStyle: 'dashed',
    borderRadius: 12,
    padding: spacing.md,
    marginBottom: spacing.sm,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: touchTarget.min * 2,
    gap: spacing.xs,
  },
  icon: {
    fontSize: 28,
    color: colors.textMuted,
  },
  label: {
    ...typography.body,
    color: colors.textMuted,
  },
});
