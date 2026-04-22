import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { colors, spacing } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import { Avatar } from '../../../shared/components/Avatar';
import { Button } from '../../../shared/components/Button';
import { Chip } from '../../../shared/components/Chip';
import { SociabilityDots } from '../../../shared/components/SociabilityDots';
import type { Dog } from '../types/dogs.types';

interface DogListCardProps {
  dog: Dog;
  onEdit: (dog: Dog) => void;
  onDelete: (dogId: number) => void;
  onPress: (dog: Dog) => void;
}

// 강아지 목록 카드 — 사진/이름/견종/성향 칩/사회성 + 수정/삭제
export function DogListCard({ dog, onEdit, onDelete, onPress }: DogListCardProps) {
  return (
    <TouchableOpacity
      style={styles.card}
      onPress={() => onPress(dog)}
      accessibilityLabel={`${dog.name} 카드`}
      accessibilityRole="button"
    >
      <View style={styles.row}>
        <Avatar
          uri={dog.profilePhotoUrl}
          size={64}
          fallback={dog.name}
          accessibilityLabel={`${dog.name} 프로필 사진`}
        />
        <View style={styles.info}>
          <Text style={styles.name}>{dog.name}</Text>
          <Text style={styles.breed}>{dog.breed} · {dog.size}</Text>
          <SociabilityDots
            score={dog.socialScore}
            accessibilityLabel={`사회성 ${dog.socialScore}점`}
          />
        </View>
      </View>

      {dog.temperaments.length > 0 && (
        <View style={styles.chips}>
          {dog.temperaments.map((t) => (
            <Chip key={t} label={t} />
          ))}
        </View>
      )}

      <View style={styles.actions}>
        <Button
          variant="outline"
          size="sm"
          onPress={() => onEdit(dog)}
          accessibilityLabel={`${dog.name} 수정`}
        >
          수정
        </Button>
        <Button
          variant="text"
          size="sm"
          onPress={() => onDelete(dog.id)}
          accessibilityLabel={`${dog.name} 삭제`}
        >
          삭제
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
    justifyContent: 'center',
  },
  name: {
    ...typography.subheading,
    color: colors.text,
  },
  breed: {
    ...typography.caption,
    color: colors.textMuted,
  },
  chips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
    marginTop: spacing.sm,
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: spacing.sm,
    marginTop: spacing.sm,
  },
});
