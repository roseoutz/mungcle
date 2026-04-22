import React, { useState } from 'react';
import {
  Alert,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter, useLocalSearchParams } from 'expo-router';
import { createReport } from '../../src/features/settings';
import { colors, spacing, borderRadius, touchTarget } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

const REPORT_REASONS = [
  '욕설/혐오 발언',
  '스팸/광고',
  '사기/허위 정보',
  '부적절한 콘텐츠',
  '기타',
];

export default function ReportScreen() {
  const router = useRouter();
  const { userId } = useLocalSearchParams<{ userId: string }>();
  const [selectedReason, setSelectedReason] = useState<string | null>(null);
  const [detail, setDetail] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // userId가 없으면 신고 대상을 특정할 수 없으므로 뒤로 이동
  if (!userId) {
    router.back();
    return null;
  }

  const reportedUserId = Number(userId);

  async function handleSubmit() {
    if (!selectedReason) {
      Alert.alert('선택 필요', '신고 사유를 선택해주세요.');
      return;
    }
    setSubmitting(true);
    try {
      const reason = detail.trim()
        ? `${selectedReason}: ${detail.trim()}`
        : selectedReason;
      await createReport(reportedUserId, reason);
      Alert.alert('신고 완료', '신고가 접수되었어요. 검토 후 처리될 예정이에요.', [
        { text: '확인', onPress: () => router.back() },
      ]);
    } catch {
      Alert.alert('오류', '신고 접수 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.');
    } finally {
      setSubmitting(false);
    }
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
        <Text style={styles.title}>신고하기</Text>
        <View style={styles.placeholder} />
      </View>

      <ScrollView contentContainerStyle={styles.form}>
        <Text style={styles.sectionLabel}>신고 사유를 선택해주세요</Text>
        {REPORT_REASONS.map((reason) => (
          <TouchableOpacity
            key={reason}
            style={[styles.reasonItem, selectedReason === reason && styles.reasonItemSelected]}
            onPress={() => setSelectedReason(reason)}
            accessibilityLabel={reason}
            accessibilityRole="radio"
            accessibilityState={{ selected: selectedReason === reason }}
          >
            <View style={[styles.radio, selectedReason === reason && styles.radioSelected]} />
            <Text style={styles.reasonText}>{reason}</Text>
          </TouchableOpacity>
        ))}

        <Text style={[styles.sectionLabel, styles.sectionLabelTop]}>
          추가 설명 (선택)
        </Text>
        <TextInput
          style={styles.textarea}
          value={detail}
          onChangeText={setDetail}
          placeholder="구체적인 내용을 입력해주세요"
          placeholderTextColor={colors.textMuted}
          multiline
          numberOfLines={4}
          maxLength={500}
          accessibilityLabel="추가 설명 입력"
        />
        <Text style={styles.charCount}>{detail.length}/500</Text>

        <TouchableOpacity
          style={[styles.submitButton, (!selectedReason || submitting) && styles.submitButtonDisabled]}
          onPress={handleSubmit}
          disabled={!selectedReason || submitting}
          accessibilityLabel="신고 제출"
          accessibilityRole="button"
        >
          <Text style={styles.submitText}>
            {submitting ? '제출 중...' : '신고 제출'}
          </Text>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
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
  placeholder: {
    minWidth: touchTarget.min,
  },
  form: {
    padding: spacing.lg,
  },
  sectionLabel: {
    ...typography.caption,
    color: colors.textMuted,
    marginBottom: spacing.md,
  },
  sectionLabelTop: {
    marginTop: spacing.lg,
  },
  reasonItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    padding: spacing.md,
    marginBottom: spacing.sm,
    minHeight: touchTarget.min,
    borderWidth: 1,
    borderColor: colors.border,
  },
  reasonItemSelected: {
    borderColor: colors.primary,
    backgroundColor: '#F0F7F2',
  },
  radio: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: colors.border,
    marginRight: spacing.md,
  },
  radioSelected: {
    borderColor: colors.primary,
    backgroundColor: colors.primary,
  },
  reasonText: {
    ...typography.body,
    color: colors.text,
  },
  textarea: {
    ...typography.body,
    color: colors.text,
    backgroundColor: colors.surface,
    borderRadius: borderRadius.button,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    borderWidth: 1,
    borderColor: colors.border,
    minHeight: 100,
    textAlignVertical: 'top',
  },
  charCount: {
    ...typography.micro,
    color: colors.textMuted,
    textAlign: 'right',
    marginTop: spacing.xs,
  },
  submitButton: {
    backgroundColor: colors.primary,
    borderRadius: borderRadius.button,
    paddingVertical: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: spacing.lg,
    minHeight: touchTarget.min,
  },
  submitButtonDisabled: {
    opacity: 0.4,
  },
  submitText: {
    ...typography.body,
    color: colors.surface,
    fontWeight: '600',
  },
});
