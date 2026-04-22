import React from 'react';
import { StyleSheet, View } from 'react-native';
import { colors, spacing } from '../../constants/theme';

// 사회성 점수(1~5)를 색상 도트로 표시 — safe(4-5)/caution(3)/warning(1-2)
function getSociabilityColor(score: number): string {
  if (score >= 4) return colors.safe;
  if (score >= 3) return colors.caution;
  return colors.warning;
}

interface SociabilityDotsProps {
  score: number; // 1~5
  accessibilityLabel?: string;
}

export function SociabilityDots({ score, accessibilityLabel }: SociabilityDotsProps) {
  const color = getSociabilityColor(score);
  const clampedScore = Math.min(5, Math.max(1, score));

  return (
    <View
      style={styles.row}
      accessibilityLabel={accessibilityLabel ?? `사회성 ${score}점`}
      accessibilityRole="image"
    >
      {Array.from({ length: 5 }, (_, i) => (
        <View
          key={i}
          style={[
            styles.dot,
            { backgroundColor: i < clampedScore ? color : colors.border },
          ]}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    gap: spacing.xs,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
  },
});
