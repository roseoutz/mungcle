import React, { useCallback, useEffect } from 'react';
import { Alert, FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';
import { AddDogCard } from '../../src/features/dogs/components/AddDogCard';
import { DogListCard } from '../../src/features/dogs/components/DogListCard';
import type { Dog } from '../../src/features/dogs/types/dogs.types';
import { getMyDogs } from '../../src/features/dogs';
import { useApi } from '../../src/shared/hooks/useApi';
import { EmptyState } from '../../src/shared/components/EmptyState';
import { ResponsiveContainer } from '../../src/shared/components/ResponsiveContainer';

const MAX_DOGS = 5;

// 스켈레톤 카드 — 로딩 대체 UI
function SkeletonDogCard() {
  return (
    <View style={styles.skeleton}>
      <View style={styles.skeletonAvatar} />
      <View style={styles.skeletonLines}>
        <View style={[styles.skeletonLine, { width: '50%' }]} />
        <View style={[styles.skeletonLine, { width: '70%' }]} />
      </View>
    </View>
  );
}

export default function DogsScreen() {
  const { data: dogs, loading, error, execute: fetchDogs } = useApi(getMyDogs);

  useEffect(() => {
    fetchDogs();
  }, [fetchDogs]);

  const handleAdd = useCallback(() => {
    // 강아지 등록 화면으로 이동 (추후 구현)
    Alert.alert('강아지 추가', '강아지 등록 화면으로 이동합니다.');
  }, []);

  const handleEdit = useCallback((dog: Dog) => {
    Alert.alert('수정', `${dog.name} 수정 화면으로 이동합니다.`);
  }, []);

  const handleDelete = useCallback((dogId: number) => {
    Alert.alert('삭제', `강아지(${dogId})를 삭제하시겠습니까?`, [
      { text: '취소', style: 'cancel' },
      { text: '삭제', style: 'destructive', onPress: () => fetchDogs() },
    ]);
  }, [fetchDogs]);

  const handlePress = useCallback((dog: Dog) => {
    Alert.alert('상세', `${dog.name} 상세 화면으로 이동합니다.`);
  }, []);

  const renderItem = useCallback(
    ({ item }: { item: Dog }) => (
      <DogListCard dog={item} onEdit={handleEdit} onDelete={handleDelete} onPress={handlePress} />
    ),
    [handleEdit, handleDelete, handlePress],
  );

  const keyExtractor = useCallback((item: Dog) => String(item.id), []);

  const dogList = dogs ?? [];
  const canAdd = dogList.length < MAX_DOGS;

  const ListHeader = (
    <Text style={styles.title}>내 강아지</Text>
  );

  const ListFooter = canAdd ? <AddDogCard onPress={handleAdd} /> : null;

  if (loading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <Text style={styles.title}>내 강아지</Text>
        <View style={styles.content}>
          <SkeletonDogCard />
          <SkeletonDogCard />
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <Text style={styles.title}>내 강아지</Text>
        <EmptyState
          icon="⚠️"
          message="강아지 목록을 불러오지 못했어요."
          ctaLabel="다시 시도"
          onCta={fetchDogs}
        />
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <ResponsiveContainer>
        <FlatList
          data={dogList}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          ListHeaderComponent={ListHeader}
          ListFooterComponent={ListFooter}
          ListEmptyComponent={
            <EmptyState
              icon="🐶"
              message="등록된 강아지가 없어요."
              ctaLabel="강아지 추가하기"
              onCta={handleAdd}
            />
          }
          contentContainerStyle={styles.list}
          showsVerticalScrollIndicator={false}
        />
      </ResponsiveContainer>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    paddingHorizontal: spacing.md,
  },
  list: {
    paddingHorizontal: spacing.md,
    paddingBottom: spacing.xl,
  },
  title: {
    ...typography.heading,
    color: colors.text,
    paddingHorizontal: spacing.md,
    paddingTop: spacing.md,
    paddingBottom: spacing.sm,
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
    width: 64,
    height: 64,
    borderRadius: 32,
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
