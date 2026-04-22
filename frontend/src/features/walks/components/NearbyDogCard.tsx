import React, { useCallback } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, spacing } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import { Avatar } from '../../../shared/components/Avatar';
import { Button } from '../../../shared/components/Button';
import { Chip } from '../../../shared/components/Chip';
import { SociabilityDots } from '../../../shared/components/SociabilityDots';
import type { NearbyWalkCard } from '../types/walks.types';

interface NearbyDogCardProps {
  item: NearbyWalkCard;
  onGreet: (walkId: number) => void;
  onPress: (walkId: number) => void;
}

// 주변 강아지 카드 — 사진/이름/견종/성향 칩/사회성 + 인사하기 버튼
export function NearbyDogCard({ item, onGreet, onPress }: NearbyDogCardProps) {
  const { dog, owner, walkId } = item;

  const handleGreet = useCallback(() => {
    onGreet(walkId);
  }, [onGreet, walkId]);

  const handlePress = useCallback(() => {
    onPress(walkId);
  }, [onPress, walkId]);

  return (
    <TouchableOpacity
      style={styles.card}
      onPress={handlePress}
      accessibilityLabel={`${dog.name} 카드, 상세보기`}
      accessibilityRole="button"
    >
      <View style={styles.row}>
        <Avatar
          uri={dog.photoUrl}
          size={56}
          fallback={dog.name}
          accessibilityLabel={`${dog.name} 프로필 사진`}
        />
        <View style={styles.info}>
          <View style={styles.nameRow}>
            <Text style={styles.name}>{dog.name}</Text>
            {dog.vaccinationRegistered && (
              <Text style={styles.vaccine} accessibilityLabel="백신 등록됨">
                💉
              </Text>
            )}
          </View>
          <Text style={styles.breed}>{dog.breed} · {dog.size}</Text>
          <Text style={styles.owner}>{owner.nickname} · {owner.neighborhood}</Text>
          <SociabilityDots
            score={dog.sociability}
            accessibilityLabel={`사회성 ${dog.sociability}점`}
          />
        </View>
      </View>

      {/* 성향 칩 목록 */}
      {dog.temperaments.length > 0 && (
        <View style={styles.chips}>
          {dog.temperaments.map((t) => (
            <Chip key={t} label={t} />
          ))}
        </View>
      )}

      {/* 인사하기 CTA */}
      <View style={styles.ctaRow}>
        <Button
          variant="primary"
          size="sm"
          onPress={handleGreet}
          accessibilityLabel={`${dog.name}에게 인사하기`}
        >
          인사하기
        </Button>
      </View>
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: spacing.md,
    marginHorizontal: spacing.md,
    marginBottom: spacing.sm,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.06,
    shadowRadius: 8,
    elevation: 2,
  },
  row: {
    flexDirection: 'row',
    gap: spacing.md,
  },
  info: {
    flex: 1,
    gap: spacing.xs,
  },
  nameRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.xs,
  },
  name: {
    ...typography.subheading,
    color: colors.text,
  },
  vaccine: {
    fontSize: 14,
  },
  breed: {
    ...typography.caption,
    color: colors.textMuted,
  },
  owner: {
    ...typography.caption,
    color: colors.textMuted,
  },
  chips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
    marginTop: spacing.sm,
  },
  ctaRow: {
    alignItems: 'flex-end',
    marginTop: spacing.sm,
  },
});
