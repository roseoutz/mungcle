import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { colors, spacing } from '../../src/constants/theme';
import { typography } from '../../src/constants/typography';
import { NotificationList, useNotifications } from '../../src/features/notifications';

function MarkAllButton() {
  const { handleMarkAllRead } = useNotifications();
  return (
    <TouchableOpacity
      style={styles.markAllButton}
      onPress={handleMarkAllRead}
      accessibilityLabel="모두 읽음 처리"
      accessibilityRole="button"
    >
      <Text style={styles.markAllText}>모두 읽음</Text>
    </TouchableOpacity>
  );
}

export default function NotificationsScreen() {
  return (
    <SafeAreaView style={styles.container} edges={['top']}>
      <View style={styles.header}>
        <Text style={styles.title}>알림</Text>
        <MarkAllButton />
      </View>
      <NotificationList />
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
