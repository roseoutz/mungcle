import React, { useEffect } from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';
import { NotificationList, useNotifications } from '../../src/features/notifications';
import { ResponsiveContainer } from '../../src/shared/components/ResponsiveContainer';

export default function NotificationsScreen() {
  const {
    notifications,
    loading,
    error,
    hasMore,
    refresh,
    loadMore,
    handleMarkRead,
    handleMarkAllRead,
  } = useNotifications();

  useEffect(() => {
    refresh();
  }, [refresh]);

  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <ResponsiveContainer maxWidth={720}>
        <View style={styles.header}>
          <Text style={styles.title}>알림</Text>
          <TouchableOpacity
            style={styles.markAllButton}
            onPress={handleMarkAllRead}
            accessibilityLabel="모두 읽음 처리"
            accessibilityRole="button"
          >
            <Text style={styles.markAllText}>모두 읽음</Text>
          </TouchableOpacity>
        </View>
        <NotificationList
          notifications={notifications}
          loading={loading}
          error={error ? error.message : null}
          hasMore={hasMore}
          onRefresh={refresh}
          onLoadMore={loadMore}
          onMarkRead={handleMarkRead}
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
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: spacing.lg,
    paddingVertical: spacing.md,
  },
  title: {
    ...typography.heading,
    color: colors.text,
  },
  markAllButton: {
    minHeight: 44,
    paddingHorizontal: spacing.md,
    alignItems: 'center',
    justifyContent: 'center',
  },
  markAllText: {
    ...typography.body,
    color: colors.primary,
  },
});
