import React, { useEffect } from 'react';
import {
  ActivityIndicator,
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import { colors, spacing } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import { NotificationCard } from './NotificationCard';
import { useNotifications } from '../hooks/useNotifications';

export function NotificationList() {
  const { notifications, loading, error, hasMore, refresh, loadMore, handleMarkRead } =
    useNotifications();

  useEffect(() => {
    refresh();
  }, [refresh]);

  // loading 상태
  if (loading && notifications.length === 0) {
    return (
      <View style={styles.center}>
        <ActivityIndicator size="large" color={colors.primary} accessibilityLabel="불러오는 중" />
      </View>
    );
  }

  // error 상태
  if (error && notifications.length === 0) {
    return (
      <View style={styles.center}>
        <Text style={styles.errorText}>연결할 수 없어요</Text>
        <Text style={styles.errorSub}>잠시 후 다시 시도해주세요</Text>
      </View>
    );
  }

  // empty 상태
  if (!loading && notifications.length === 0) {
    return (
      <View style={styles.center}>
        <Text style={styles.emptyText}>아직 인사가 없어요</Text>
        <Text style={styles.emptySub}>주변 산책 중인 강아지를 찾아보세요</Text>
      </View>
    );
  }

  // success 상태
  return (
    <FlatList
      data={notifications}
      keyExtractor={(item) => String(item.id)}
      renderItem={({ item }) => (
        <NotificationCard notification={item} onMarkRead={handleMarkRead} />
      )}
      contentContainerStyle={styles.list}
      onEndReached={hasMore ? loadMore : undefined}
      onEndReachedThreshold={0.3}
      refreshControl={
        <RefreshControl
          refreshing={loading && notifications.length > 0}
          onRefresh={refresh}
          tintColor={colors.primary}
        />
      }
      ListFooterComponent={
        loading && notifications.length > 0 ? (
          <ActivityIndicator
            style={styles.footer}
            color={colors.primary}
            accessibilityLabel="더 불러오는 중"
          />
        ) : null
      }
    />
  );
}

const styles = StyleSheet.create({
  center: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: spacing.lg,
  },
  errorText: {
    ...typography.subheading,
    color: colors.error,
    textAlign: 'center',
  },
  errorSub: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
    marginTop: spacing.sm,
  },
  emptyText: {
    ...typography.subheading,
    color: colors.text,
    textAlign: 'center',
  },
  emptySub: {
    ...typography.body,
    color: colors.textMuted,
    textAlign: 'center',
    marginTop: spacing.sm,
  },
  list: {
    padding: spacing.md,
  },
  footer: {
    paddingVertical: spacing.md,
  },
});
