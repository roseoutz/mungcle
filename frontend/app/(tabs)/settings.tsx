import React, { useState } from 'react';
import {
  Alert,
  Modal,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { useAuth } from '../../src/features/auth';
import { deleteAccount } from '../../src/features/settings';
import { colors, spacing, borderRadius, touchTarget } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

interface MenuItemProps {
  label: string;
  onPress: () => void;
  accessibilityLabel: string;
  destructive?: boolean;
}

function MenuItem({ label, onPress, accessibilityLabel, destructive = false }: MenuItemProps) {
  return (
    <TouchableOpacity
      style={styles.menuItem}
      onPress={onPress}
      accessibilityLabel={accessibilityLabel}
      accessibilityRole="button"
    >
      <Text style={[styles.menuLabel, destructive && styles.menuLabelDestructive]}>
        {label}
      </Text>
      <Text style={styles.chevron}>›</Text>
    </TouchableOpacity>
  );
}

export default function SettingsScreen() {
  const { user, logout } = useAuth();
  const router = useRouter();
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [deleting, setDeleting] = useState(false);

  async function handleLogout() {
    await logout();
  }

  async function handleDeleteAccount() {
    setDeleting(true);
    try {
      await deleteAccount();
      await logout();
    } catch {
      Alert.alert('오류', '탈퇴 처리 중 오류가 발생했어요. 잠시 후 다시 시도해주세요.');
    } finally {
      setDeleting(false);
      setDeleteModalVisible(false);
    }
  }

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.content}>
        <Text style={styles.title}>설정</Text>

        {/* 프로필 카드 */}
        <TouchableOpacity
          style={styles.profileCard}
          onPress={() => router.push('/settings/edit-profile')}
          accessibilityLabel="프로필 수정"
          accessibilityRole="button"
        >
          <View style={styles.avatarPlaceholder}>
            <Text style={styles.avatarInitial}>
              {user?.nickname?.[0]?.toUpperCase() ?? '?'}
            </Text>
          </View>
          <View style={styles.profileInfo}>
            <Text style={styles.nickname}>{user?.nickname ?? '-'}</Text>
            <Text style={styles.neighborhood}>{user?.neighborhood ?? '-'}</Text>
          </View>
          <Text style={styles.chevron}>›</Text>
        </TouchableOpacity>

        {/* 메뉴 리스트 */}
        <View style={styles.section}>
          <MenuItem
            label="차단 관리"
            onPress={() => router.push('/settings/blocks')}
            accessibilityLabel="차단 관리 화면으로 이동"
          />
          <MenuItem
            label="신고하기"
            onPress={() =>
              router.push({
                pathname: '/settings/report',
                params: { userId: String(user?.id ?? '') },
              })
            }
            accessibilityLabel="신고 화면으로 이동"
          />
        </View>

        <View style={styles.section}>
          <MenuItem
            label="로그아웃"
            onPress={handleLogout}
            accessibilityLabel="로그아웃"
          />
          <MenuItem
            label="회원 탈퇴"
            onPress={() => setDeleteModalVisible(true)}
            accessibilityLabel="회원 탈퇴"
            destructive
          />
        </View>
      </View>

      {/* 회원 탈퇴 확인 모달 */}
      <Modal
        visible={deleteModalVisible}
        transparent
        animationType="fade"
        onRequestClose={() => setDeleteModalVisible(false)}
        accessibilityViewIsModal
      >
        <View style={styles.modalOverlay}>
          <View style={styles.modalContent}>
            <Text style={styles.modalTitle}>회원 탈퇴</Text>
            <Text style={styles.modalMessage}>
              탈퇴하면 모든 데이터가 삭제되어요.{'\n'}정말 탈퇴하시겠어요?
            </Text>
            <View style={styles.modalButtons}>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalButtonCancel]}
                onPress={() => setDeleteModalVisible(false)}
                accessibilityLabel="취소"
                accessibilityRole="button"
                disabled={deleting}
              >
                <Text style={styles.modalButtonCancelText}>취소</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.modalButtonDelete]}
                onPress={handleDeleteAccount}
                accessibilityLabel="탈퇴 확인"
                accessibilityRole="button"
                disabled={deleting}
              >
                <Text style={styles.modalButtonDeleteText}>
                  {deleting ? '처리 중...' : '탈퇴'}
                </Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flex: 1,
    padding: spacing.lg,
  },
  title: {
    ...typography.heading,
    color: colors.text,
    marginBottom: spacing.lg,
  },
  profileCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    padding: spacing.md,
    marginBottom: spacing.lg,
    minHeight: touchTarget.min,
  },
  avatarPlaceholder: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  avatarInitial: {
    ...typography.subheading,
    color: colors.surface,
  },
  profileInfo: {
    flex: 1,
  },
  nickname: {
    ...typography.subheading,
    color: colors.text,
  },
  neighborhood: {
    ...typography.caption,
    color: colors.textMuted,
    marginTop: spacing.xs,
  },
  chevron: {
    ...typography.subheading,
    color: colors.textMuted,
  },
  section: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    marginBottom: spacing.md,
    overflow: 'hidden',
  },
  menuItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.md,
    minHeight: touchTarget.min,
    borderBottomWidth: 1,
    borderBottomColor: colors.border,
  },
  menuLabel: {
    ...typography.body,
    color: colors.text,
  },
  menuLabelDestructive: {
    color: colors.error,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.5)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  modalContent: {
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    padding: spacing.lg,
    width: '80%',
  },
  modalTitle: {
    ...typography.subheading,
    color: colors.text,
    marginBottom: spacing.sm,
  },
  modalMessage: {
    ...typography.body,
    color: colors.textMuted,
    marginBottom: spacing.lg,
  },
  modalButtons: {
    flexDirection: 'row',
    gap: spacing.sm,
  },
  modalButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: spacing.md,
    borderRadius: borderRadius.button,
    minHeight: touchTarget.min,
  },
  modalButtonCancel: {
    backgroundColor: colors.border,
  },
  modalButtonDelete: {
    backgroundColor: colors.error,
  },
  modalButtonCancelText: {
    ...typography.body,
    color: colors.text,
    fontWeight: '600',
  },
  modalButtonDeleteText: {
    ...typography.body,
    color: colors.surface,
    fontWeight: '600',
  },
});
