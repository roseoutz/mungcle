import * as Location from 'expo-location';
import { useFocusEffect, useRouter } from 'expo-router';
import React, { useCallback, useEffect, useState } from 'react';
import { Alert, FlatList, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';
import { useAuth } from '../../src/features/auth';
import { getMyDogs } from '../../src/features/dogs';
import { useGreeting } from '../../src/features/social';
import { NearbyDogCard } from '../../src/features/walks/components/NearbyDogCard';
import { TimePatternCard } from '../../src/features/walks/components/TimePatternCard';
import { WalkToggle } from '../../src/features/walks/components/WalkToggle';
import { useNearbyWalks } from '../../src/features/walks/hooks/useNearbyWalks';
import { EmptyState } from '../../src/shared/components/EmptyState';
import { ResponsiveContainer } from '../../src/shared/components/ResponsiveContainer';
import { useResponsive } from '../../src/shared/hooks/useResponsive';
import type { NearbyWalkCard } from '../../src/features/walks/types/walks.types';

// 스켈레톤 카드 — 로딩 대체 UI
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

export default function HomeScreen() {
  const { user } = useAuth();
  const router = useRouter();
  const [coords, setCoords] = useState<{ lat: number; lng: number } | null>(null);
  const { walks, patterns, loading, error, refresh } = useNearbyWalks(
    coords?.lat ?? null,
    coords?.lng ?? null,
  );
  const { send: sendGreeting } = useGreeting();

  // 선택된 강아지 ID — 첫 번째 강아지를 기본으로 사용
  const [selectedDogId, setSelectedDogId] = useState<number | null>(null);
  // 강아지가 한 마리도 없는 경우 true
  const [hasDogs, setHasDogs] = useState<boolean>(true);

  // 화면 포커스 시 강아지 목록 새로고침 (등록 후 복귀 케이스 포함)
  useFocusEffect(
    useCallback(() => {
      async function fetchDogs() {
        try {
          const dogs = await getMyDogs();
          if (dogs.length === 0) {
            setHasDogs(false);
            setSelectedDogId(null);
          } else {
            setHasDogs(true);
            setSelectedDogId(dogs[0].id);
          }
        } catch {
          // 조회 실패 시 기존 상태 유지
        }
      }
      fetchDogs();
    }, []),
  );

  // 위치 권한 요청 및 좌표 획득
  useEffect(() => {
    async function getLocation() {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') return;
      const loc = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.Balanced,
      });
      setCoords({ lat: loc.coords.latitude, lng: loc.coords.longitude });
    }
    getLocation();
  }, []);

  const handleGreet = useCallback(
    async (walkId: number) => {
      if (!selectedDogId) {
        Alert.alert('강아지 등록 필요', '먼저 강아지를 등록해주세요.');
        return;
      }
      const result = await sendGreeting(selectedDogId, walkId);
      if (result) {
        Alert.alert('인사 전송', '인사를 보냈어요! 🐾');
      }
    },
    [selectedDogId, sendGreeting],
  );

  const handleCardPress = useCallback((_walkId: number) => {
    // 상세 화면으로 이동 (추후 구현)
  }, []);

  const renderItem = useCallback(
    ({ item }: { item: NearbyWalkCard }) => (
      <NearbyDogCard item={item} onGreet={handleGreet} onPress={handleCardPress} />
    ),
    [handleGreet, handleCardPress],
  );

  const keyExtractor = useCallback((item: NearbyWalkCard) => String(item.walkId), []);

  // 강아지가 없으면 산책 섹션 대신 등록 안내 표시
  const WalkSection = !hasDogs ? (
    <EmptyState
      icon="🐶"
      message="강아지를 등록해주세요"
      ctaLabel="강아지 등록하기"
      onCta={() => router.push('/(tabs)/dogs')}
    />
  ) : (
    <WalkToggle dogId={selectedDogId} />
  );

  const ListHeader = (
    <View>
      <View style={styles.header}>
        <Text style={styles.greeting}>
          안녕하세요, {user?.nickname ?? '멍클 사용자'}님
        </Text>
        <Text style={styles.sub}>근처 반려견과 산책을 시작해보세요.</Text>
      </View>
      {WalkSection}
      <Text style={styles.sectionTitle}>주변 산책 중인 강아지</Text>
    </View>
  );

  const ListEmpty = loading ? (
    <View style={styles.skeletonContainer}>
      <SkeletonCard />
      <SkeletonCard />
      <SkeletonCard />
    </View>
  ) : error ? (
    <EmptyState
      icon="⚠️"
      message="주변 산책 정보를 불러오지 못했어요."
      ctaLabel="다시 시도"
      onCta={refresh}
    />
  ) : (
    <View>
      <EmptyState icon="🐕" message="근처에 산책 중인 강아지가 없어요." />
      {patterns.length > 0 && <TimePatternCard patterns={patterns} />}
    </View>
  );

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <ResponsiveContainer>
        <FlatList
          data={walks}
          renderItem={renderItem}
          keyExtractor={keyExtractor}
          ListHeaderComponent={ListHeader}
          ListEmptyComponent={ListEmpty}
          contentContainerStyle={styles.list}
          showsVerticalScrollIndicator={false}
          numColumns={1}
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
  list: {
    paddingBottom: spacing.xl,
  },
  header: {
    paddingHorizontal: spacing.lg,
    paddingTop: spacing.md,
    paddingBottom: spacing.sm,
  },
  greeting: {
    ...typography.heading,
    color: colors.text,
    marginBottom: spacing.xs,
  },
  sub: {
    ...typography.body,
    color: colors.textMuted,
  },
  sectionTitle: {
    ...typography.subheading,
    color: colors.text,
    paddingHorizontal: spacing.lg,
    paddingBottom: spacing.sm,
    marginTop: spacing.md,
  },
  skeletonContainer: {
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
