import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useRouter } from 'expo-router';
import { colors, spacing, borderRadius, touchTarget } from '../../../constants/theme';
import { typography } from '../../../constants/typography';
import type { NotificationResponse, NotificationType } from '../types/notifications.types';

interface NotificationCardProps {
  notification: NotificationResponse;
  onMarkRead: (id: number) => void;
}

function getIconAndLabel(type: NotificationType): { icon: string; label: string } {
  switch (type) {
    case 'GREETING_RECEIVED':
      return { icon: '🐾', label: '인사 수신' };
    case 'GREETING_ACCEPTED':
      return { icon: '✅', label: '인사 매칭' };
    case 'MESSAGE_RECEIVED':
      return { icon: '💬', label: '새 메시지' };
    case 'WALK_EXPIRED':
      return { icon: '⏰', label: '산책 만료' };
    default:
      return { icon: '🔔', label: '알림' };
  }
}

function getDeepLinkPath(notification: NotificationResponse): string | null {
  const { type, payload } = notification;
  switch (type) {
    case 'GREETING_RECEIVED':
    case 'GREETING_ACCEPTED':
      if (typeof payload.greetingId === 'number') {
        return `/messages/${payload.greetingId}`;
      }
      return null;
    case 'MESSAGE_RECEIVED':
      if (typeof payload.greetingId === 'number') {
        return `/messages/${payload.greetingId}`;
      }
      return null;
    default:
      return null;
  }
}

function formatTime(createdAt: number): string {
  const diffMs = Date.now() - createdAt;
  const diffMin = Math.floor(diffMs / 60000);
  if (diffMin < 1) return '방금 전';
  if (diffMin < 60) return `${diffMin}분 전`;
  const diffHour = Math.floor(diffMin / 60);
  if (diffHour < 24) return `${diffHour}시간 전`;
  return `${Math.floor(diffHour / 24)}일 전`;
}

export function NotificationCard({ notification, onMarkRead }: NotificationCardProps) {
  const router = useRouter();
  const { icon, label } = getIconAndLabel(notification.type);

  function handlePress() {
    if (!notification.read) {
      onMarkRead(notification.id);
    }
    const path = getDeepLinkPath(notification);
    if (path) {
      router.push(path as Parameters<typeof router.push>[0]);
    }
  }

  return (
    <TouchableOpacity
      style={[styles.container, notification.read ? styles.read : styles.unread]}
      onPress={handlePress}
      accessibilityLabel={`${label} 알림 - ${formatTime(notification.createdAt)}`}
      accessibilityRole="button"
    >
      <View style={styles.iconContainer}>
        <Text style={styles.icon}>{icon}</Text>
      </View>
      <View style={styles.content}>
        <Text style={[styles.label, notification.read ? styles.labelRead : styles.labelUnread]}>
          {label}
        </Text>
        {typeof notification.payload.message === 'string' && (
          <Text style={styles.message} numberOfLines={2}>
            {notification.payload.message}
          </Text>
        )}
        <Text style={styles.time}>{formatTime(notification.createdAt)}</Text>
      </View>
      {!notification.read && <View style={styles.dot} accessibilityLabel="읽지 않음" />}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: spacing.md,
    borderRadius: borderRadius.card,
    marginBottom: spacing.sm,
    minHeight: touchTarget.min,
  },
  read: {
    backgroundColor: colors.surface,
  },
  unread: {
    backgroundColor: '#F0F7F2',
  },
  iconContainer: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.background,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: spacing.md,
  },
  icon: {
    fontSize: 20,
  },
  content: {
    flex: 1,
  },
  label: {
    ...typography.body,
  },
  labelRead: {
    color: colors.textMuted,
  },
  labelUnread: {
    color: colors.text,
    fontWeight: '600',
  },
  message: {
    ...typography.caption,
    color: colors.textMuted,
    marginTop: spacing.xs,
  },
  time: {
    ...typography.micro,
    color: colors.textMuted,
    marginTop: spacing.xs,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    backgroundColor: colors.primary,
    marginLeft: spacing.sm,
  },
});
