import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useRouter } from 'expo-router';
import { listBlocks, unblock } from '../../src/features/settings';
import type { BlockInfo } from '../../src/features/settings';
import { colors, spacing, borderRadius, touchTarget } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';

export default function BlocksScreen() {
  const router = useRouter();
  const [blocks, setBlocks] = useState<BlockInfo[]>([]);
  const [loading, setLoading] = useState(true);
  const [unblocking, setUnblocking] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await listBlocks();
      setBlocks(data.blocks);
    } catch {
      Alert.alert('오류', '차단 목록을 불러올 수 없어요.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  async function handleUnblock(userId: number, nickname: string) {
    Alert.alert(
      '차단 해제',
      `${nickname} 님의 차단을 해제할까요?`,
      [
        { text: '취소', style: 'cancel' },
        {
          text: '해제',
          onPress: async () => {
            setUnblocking(userId);
            try {
              await unblock(userId);
              setBlocks((prev) => prev.filter((b) => b.blockedUserId !== userId));
            } catch {
              Alert.alert('오류', '차단 해제 중 오류가 발생했어요.');
            } finally {
              setUnblocking(null);
            }
          },
        },
      ],
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
        <Text style={styles.title}>차단 관리</Text>
        <View style={styles.placeholder} />
      </View>

      {loading ? (
        <View style={styles.center}>
          <ActivityIndicator size="large" color={colors.primary} accessibilityLabel="불러오는 중" />
        </View>
      ) : blocks.length === 0 ? (
        <View style={styles.center}>
          <Text style={styles.emptyText}>차단한 사용자가 없어요</Text>
        </View>
      ) : (
        <FlatList
          data={blocks}
          keyExtractor={(item) => String(item.blockedUserId)}
          contentContainerStyle={styles.list}
          renderItem={({ item }) => (
            <View style={styles.row}>
              <View style={styles.avatar}>
                <Text style={styles.avatarInitial}>
                  {item.blockedNickname[0]?.toUpperCase() ?? '?'}
                </Text>
              </View>
              <Text style={styles.nicknameText}>{item.blockedNickname}</Text>
              <TouchableOpacity
                style={[
                  styles.unblockButton,
                  unblocking === item.blockedUserId && styles.unblockButtonDisabled,
                ]}
                onPress={() => handleUnblock(item.blockedUserId, item.blockedNickname)}
                disabled={unblocking === item.blockedUserId}
                accessibilityLabel={`${item.blockedNickname} 차단 해제`}
                accessibilityRole="button"
              >
                <Text style={styles.unblockText}>
                  {unblocking === item.blockedUserId ? '처리 중' : '해제'}
                </Text>
              </TouchableOpacity>
            </View>
          )}
        />
      )}
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
  placeholder: {
    minWidth: touchTarget.min,
  },
  list: {
    padding: spacing.md,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: borderRadius.card,
    padding: spacing.md,
    marginBottom: spacing.sm,
    minHeight: touchTarget.min,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  avatarInitial: {
    ...typography.body,
    color: colors.surface,
    fontWeight: '600',
  },
  nicknameText: {
    ...typography.body,
    color: colors.text,
    flex: 1,
  },
  unblockButton: {
    borderRadius: borderRadius.button,
    borderWidth: 1,
    borderColor: colors.primary,
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    minHeight: touchTarget.min,
    alignItems: 'center',
    justifyContent: 'center',
  },
  unblockButtonDisabled: {
    opacity: 0.4,
  },
  unblockText: {
    ...typography.caption,
    color: colors.primary,
    fontWeight: '600',
  },
  emptyText: {
    ...typography.body,
    color: colors.textMuted,
  },
});
