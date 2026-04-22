import React, { useCallback } from 'react';
import { FlatList, StyleSheet, View } from 'react-native';
import { colors, spacing } from '../../../constants/theme';
import { EmptyState } from '../../../shared/components/EmptyState';
import type { NearbyWalkCard, PatternResponse } from '../types/walks.types';
import { NearbyDogCard } from './NearbyDogCard';
import { TimePatternCard } from './TimePatternCard';

interface NearbyListProps {
  walks: NearbyWalkCard[];
  patterns: PatternResponse[];
  loading: boolean;
  error: Error | null;
  onGreet: (walkId: number) => void;
  onCardPress: (walkId: number) => void;
  onRetry: () => void;
}

// 스켈레톤 카드 — 로딩 상태 대체 UI
function SkeletonCard() {
  return (
    <View style={styles.skeleton}>
      <View style={styles.skeletonAvatar} />
      <View style={styles.skeletonLines}>
        <View style={[styles.skeletonLine, { width: '60%' }]} />
        <View style={[styles.skeletonLine, { width: '40%' }]} />
        <View style={[styles.skeletonLine, { width: '80%' }]} />
      </View>
    </View>
  );
}

// 주변 강아지 목록 — loading/empty/error/success 4상태
export function NearbyList({
  walks,
  patterns,
  loading,
  error,
  onGreet,
  onCardPress,
  onRetry,
}: NearbyListProps) {
  const renderItem = useCallback(
    ({ item }: { item: NearbyWalkCard }) => (
      <NearbyDogCard item={item} onGreet={onGreet} onPress={onCardPress} />
    ),
    [onGreet, onCardPress],
  );

  const keyExtractor = useCallback((item: NearbyWalkCard) => String(item.walkId), []);

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <SkeletonCard />
        <SkeletonCard />
        <SkeletonCard />
      </View>
    );
  }

  if (error) {
    return (
      <EmptyState
        icon="⚠️"
        message="주변 산책 정보를 불러오지 못했어요."
        ctaLabel="다시 시도"
        onCta={onRetry}
      />
    );
  }

  if (walks.length === 0) {
    return (
      <>
        <EmptyState
          icon="🐕"
          message="근처에 산책 중인 강아지가 없어요."
        />
        {patterns.length > 0 && <TimePatternCard patterns={patterns} />}
      </>
    );
  }

  return (
    <FlatList
      data={walks}
      renderItem={renderItem}
      keyExtractor={keyExtractor}
      contentContainerStyle={styles.list}
      showsVerticalScrollIndicator={false}
    />
  );
}

const styles = StyleSheet.create({
  list: {
    paddingBottom: spacing.xl,
  },
  loadingContainer: {
    paddingHorizontal: spacing.md,
    gap: spacing.sm,
  },
  skeleton: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: spacing.md,
    flexDirection: 'row',
    gap: spacing.md,
    marginBottom: spacing.sm,
  },
  skeletonAvatar: {
    width: 56,
    height: 56,
    borderRadius: 28,
    backgroundColor: colors.border,
  },
  skeletonLines: {
    flex: 1,
    gap: spacing.sm,
    justifyContent: 'center',
  },
  skeletonLine: {
    height: 12,
    borderRadius: 6,
    backgroundColor: colors.border,
  },
});
