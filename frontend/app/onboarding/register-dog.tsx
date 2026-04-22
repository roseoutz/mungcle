import { useRouter } from 'expo-router';
import React, { useState } from 'react';
import {
  ScrollView,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { createDog } from '../../src/features/dogs';
import type { DogSize, DogTemperament } from '../../src/features/dogs';
import { Button } from '../../src/shared/components/Button';
import { TextField } from '../../src/shared/components/TextField';
import { colors, spacing, borderRadius } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

const DOG_SIZES: { label: string; value: DogSize }[] = [
  { label: '소형', value: 'SMALL' },
  { label: '중형', value: 'MEDIUM' },
  { label: '대형', value: 'LARGE' },
];

const TEMPERAMENTS: { label: string; value: DogTemperament }[] = [
  { label: '친화적', value: 'FRIENDLY' },
  { label: '활발함', value: 'PLAYFUL' },
  { label: '차분함', value: 'CALM' },
  { label: '수줍음', value: 'SHY' },
  { label: '보호 성향', value: 'PROTECTIVE' },
  { label: '에너지 넘침', value: 'ENERGETIC' },
];

export default function RegisterDogScreen() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [breed, setBreed] = useState('');
  const [size, setSize] = useState<DogSize>('MEDIUM');
  const [temperaments, setTemperaments] = useState<DogTemperament[]>([]);
  const [socialScore, setSocialScore] = useState(3);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function toggleTemperament(value: DogTemperament) {
    setTemperaments((prev) =>
      prev.includes(value)
        ? prev.filter((t) => t !== value)
        : prev.length < 3
          ? [...prev, value]
          : prev,
    );
  }

  async function handleSubmit() {
    if (!name || !breed) {
      setError('이름과 견종을 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await createDog({ name, breed, size, temperaments, socialScore });
      router.replace('/onboarding/set-location');
    } catch {
      setError('강아지 등록에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content} keyboardShouldPersistTaps="handled">
        <Text style={styles.title}>강아지 등록</Text>
        <Text style={styles.subtitle}>반려견 정보를 입력해주세요</Text>

        <TextField label="이름" value={name} onChangeText={setName} placeholder="강아지 이름" />
        <TextField label="견종" value={breed} onChangeText={setBreed} placeholder="예: 골든 리트리버" />

        {/* 크기 선택 */}
        <Text style={styles.sectionLabel}>크기</Text>
        <View style={styles.chipRow}>
          {DOG_SIZES.map((s) => (
            <TouchableOpacity
              key={s.value}
              style={[styles.chip, size === s.value && styles.chipActive]}
              onPress={() => setSize(s.value)}
              accessibilityLabel={`크기: ${s.label}`}
              accessibilityRole="radio"
              accessibilityState={{ selected: size === s.value }}
            >
              <Text style={[styles.chipText, size === s.value && styles.chipTextActive]}>
                {s.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* 성향 선택 (최대 3개) */}
        <Text style={styles.sectionLabel}>성향 (최대 3개)</Text>
        <View style={styles.chipRow}>
          {TEMPERAMENTS.map((t) => (
            <TouchableOpacity
              key={t.value}
              style={[styles.chip, temperaments.includes(t.value) && styles.chipActive]}
              onPress={() => toggleTemperament(t.value)}
              accessibilityLabel={`성향: ${t.label}`}
              accessibilityRole="checkbox"
              accessibilityState={{ checked: temperaments.includes(t.value) }}
            >
              <Text
                style={[
                  styles.chipText,
                  temperaments.includes(t.value) && styles.chipTextActive,
                ]}
              >
                {t.label}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* 사회성 점수 1~5 */}
        <Text style={styles.sectionLabel}>사회성 점수 ({socialScore}/5)</Text>
        <View style={styles.chipRow}>
          {[1, 2, 3, 4, 5].map((score) => (
            <TouchableOpacity
              key={score}
              style={[styles.scoreChip, socialScore === score && styles.chipActive]}
              onPress={() => setSocialScore(score)}
              accessibilityLabel={`사회성 ${score}점`}
              accessibilityRole="radio"
              accessibilityState={{ selected: socialScore === score }}
            >
              <Text style={[styles.chipText, socialScore === score && styles.chipTextActive]}>
                {score}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {error ? <Text style={styles.error}>{error}</Text> : null}

        <Button
          variant="primary"
          size="lg"
          loading={loading}
          onPress={handleSubmit}
          accessibilityLabel="강아지 등록 완료"
        >
          다음
        </Button>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: spacing.lg,
    gap: spacing.sm,
  },
  title: {
    ...typography.heading,
    color: colors.text,
  },
  subtitle: {
    ...typography.body,
    color: colors.textMuted,
    marginBottom: spacing.md,
  },
  sectionLabel: {
    ...typography.caption,
    color: colors.text,
    fontWeight: '500',
    marginTop: spacing.sm,
    marginBottom: spacing.xs,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.xs,
    marginBottom: spacing.sm,
  },
  chip: {
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.xs + 2,
    borderRadius: borderRadius.chip,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    minHeight: 44,
    justifyContent: 'center',
  },
  chipActive: {
    backgroundColor: colors.primary,
    borderColor: colors.primary,
  },
  chipText: {
    ...typography.caption,
    color: colors.text,
  },
  chipTextActive: {
    color: colors.surface,
  },
  scoreChip: {
    width: 44,
    height: 44,
    borderRadius: borderRadius.chip,
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surface,
    alignItems: 'center',
    justifyContent: 'center',
  },
  error: {
    ...typography.caption,
    color: colors.error,
  },
});
