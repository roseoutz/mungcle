import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { borderRadius, colors } from '../../constants/theme';
import { typography } from '../../constants/typography';

// 성향 태그 칩 — ACTIVE/FRIENDLY/PLAYFUL→녹색, CALM→파랑, SHY→회색, CAUTION/PROTECTIVE→주황, ENERGETIC→보라
const TEMPERAMENT_COLORS: Record<string, { bg: string; text: string }> = {
  ACTIVE: { bg: '#DCFCE7', text: '#16A34A' },
  FRIENDLY: { bg: '#DCFCE7', text: '#16A34A' },
  PLAYFUL: { bg: '#DCFCE7', text: '#16A34A' },
  CALM: { bg: '#DBEAFE', text: '#2563EB' },
  SHY: { bg: '#F3F4F6', text: '#6B7280' },
  CAUTION: { bg: '#FEE2E2', text: '#DC2626' },
  PROTECTIVE: { bg: '#FEF3C7', text: '#D97706' },
  ENERGETIC: { bg: '#EDE9FE', text: '#7C3AED' },
};

const DEFAULT_COLOR = { bg: '#F3F4F6', text: '#6B7280' };

interface ChipProps {
  label: string;
  accessibilityLabel?: string;
}

export function Chip({ label, accessibilityLabel }: ChipProps) {
  const colorScheme = TEMPERAMENT_COLORS[label] ?? DEFAULT_COLOR;

  return (
    <View
      style={[styles.chip, { backgroundColor: colorScheme.bg }]}
      accessibilityLabel={accessibilityLabel ?? label}
    >
      <Text style={[styles.label, { color: colorScheme.text }]}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  chip: {
    borderRadius: borderRadius.chip,
    paddingHorizontal: 8,
    paddingVertical: 3,
    alignSelf: 'flex-start',
  },
  label: {
    ...typography.micro,
    fontWeight: '600',
    color: colors.text,
  },
});
