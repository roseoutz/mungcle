import React, { useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { getMyProfile, updateProfile } from '../../src/features/settings';
import { colors, spacing, borderRadius, touchTarget } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function EditProfileScreen() {
  const router = useRouter();
  const [nickname, setNickname] = useState('');
  const [neighborhood, setNeighborhood] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getMyProfile()
      .then((profile) => {
        setNickname(profile.nickname);
        setNeighborhood(profile.neighborhood);
      })
      .catch(() => {
        Alert.alert('오류', '프로필을 불러올 수 없어요.');
      })
      .finally(() => setLoading(false));
  }, []);

  async function handleSave() {
    if (!nickname.trim()) {
      Alert.alert('입력 오류', '닉네임을 입력해주세요.');
      return;
    }
    setSaving(true);
    try {
      await updateProfile({ nickname: nickname.trim(), neighborhood: neighborhood.trim() });
      router.back();
    } catch {
      Alert.alert('오류', '프로필 수정 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.');
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <SafeAreaView style={styles.container} edges={['top']}>
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} accessibilityLabel="불러오는 중" />
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <TouchableOpacity
          onPress={() => router.back()}
          style={styles.backButton}
          accessibilityLabel="뒤로 가기"
          accessibilityRole="button"
        >
          <Text style={styles.backText}>‹</Text>
        </TouchableOpacity>
        <Text style={styles.title}>프로필 수정</Text>
        <TouchableOpacity
          onPress={handleSave}
          style={styles.saveButton}
          disabled={saving}
          accessibilityLabel="저장"
          accessibilityRole="button"
        >
          <Text style={[styles.saveText, saving && styles.saveTextDisabled]}>
            {saving ? '저장 중...' : '저장'}
          </Text>
        </TouchableOpacity>
      </View>

      <ScrollView contentContainerStyle={styles.form}>
        <View style={styles.field}>
          <Text style={styles.label}>닉네임</Text>
          <TextInput
            style={styles.input}
            value={nickname}
            onChangeText={setNickname}
            placeholder="닉네임 입력"
            placeholderTextColor={colors.textMuted}
            accessibilityLabel="닉네임 입력"
            maxLength={20}
          />
        </View>

        <View style={styles.field}>
          <Text style={styles.label}>동네</Text>
          <TextInput
            style={styles.input}
            value={neighborhood}
            onChangeText={setNeighborhood}
            placeholder="동네 입력"
            placeholderTextColor={colors.textMuted}
            accessibilityLabel="동네 입력"
            maxLength={50}
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
    backgroundColor: colors.surface,
  },
  backButton: {
    minWidth: touchTarget.min,
    minHeight: touchTarget.min,
    alignItems: 'center',
    justifyContent: 'center',
  },
  backText: {
    fontSize: 28,
    color: colors.primary,
    lineHeight: 32,
  },
  title: {
    ...typography.subheading,
    color: colors.text,
    flex: 1,
    textAlign: 'center',
  },
  saveButton: {
    minWidth: touchTarget.min,
    minHeight: touchTarget.min,
    alignItems: 'center',
    justifyContent: 'center',
  },
  saveText: {
    ...typography.body,
    color: colors.primary,
    fontWeight: '600',
  },
  saveTextDisabled: {
    color: colors.textMuted,
  },
  form: {
    padding: spacing.lg,
  },
  field: {
    marginBottom: spacing.lg,
  },
  label: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.sm,
  },
  input: {
    ...typography.body,
    color: colors.text,
    backgroundColor: colors.surface,
    borderRadius: borderRadius.button,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
    minHeight: touchTarget.min,
  },
});
