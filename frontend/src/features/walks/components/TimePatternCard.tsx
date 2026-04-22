import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors, spacing } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import { Card } from '../../../shared/components/Card';
import type { PatternResponse } from '../types/walks.types';

interface TimePatternCardProps {
  patterns: PatternResponse[];
}

// 이 시간대에 자주 산책하는 강아지 수를 보여주는 패턴 카드
export function TimePatternCard({ patterns }: TimePatternCardProps) {
  const currentHour = new Date().getHours();
  const relevantPattern = patterns.find((p) => p.typicalHour === currentHour);

  return (
    <Card style={styles.card}>
      <Text style={styles.title}>이 시간대 산책 패턴</Text>
      {relevantPattern ? (
        <View style={styles.row}>
          <Text style={styles.hour}>{currentHour}시</Text>
          <Text style={styles.count}>
            최근 2주간 {relevantPattern.countLast14Days}번 산책
          </Text>
        </View>
      ) : (
        <Text style={styles.empty}>아직 이 시간대 데이터가 없어요 🐾</Text>
      )}
    </Card>
  );
}

const styles = StyleSheet.create({
  card: {
    marginHorizontal: spacing.md,
    marginBottom: spacing.md,
  },
  title: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.sm,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.sm,
  },
  hour: {
    ...typography.subheading,
    color: colors.primary,
  },
  count: {
    ...typography.body,
    color: colors.text,
  },
  empty: {
    ...typography.body,
    color: colors.textMuted,
  },
});
