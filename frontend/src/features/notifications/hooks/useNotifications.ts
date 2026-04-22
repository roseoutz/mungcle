import { useState, useCallback } from 'react';
import { listNotifications, markRead, markAllRead } from '../services/notifications.api';
import type { NotificationResponse } from '../types/notifications.types';

interface UseNotificationsState {
  notifications: NotificationResponse[];
  loading: boolean;
  error: Error | null;
  hasMore: boolean;
}

interface UseNotificationsResult extends UseNotificationsState {
  refresh: () => Promise<void>;
  loadMore: () => Promise<void>;
  handleMarkRead: (id: number) => Promise<void>;
  handleMarkAllRead: () => Promise<void>;
}

const PAGE_SIZE = 20;

export function useNotifications(): UseNotificationsResult {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const [nextCursor, setNextCursor] = useState<number | undefined>(undefined);
  const [hasMore, setHasMore] = useState(true);

  const refresh = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await listNotifications(undefined, PAGE_SIZE);
      setNotifications(page.notifications);
      setNextCursor(page.nextCursor);
      setHasMore(page.nextCursor !== undefined);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('알림을 불러올 수 없어요'));
    } finally {
      setLoading(false);
    }
  }, []);

  const loadMore = useCallback(async () => {
    if (!hasMore || loading) return;
    setLoading(true);
    try {
      const page = await listNotifications(nextCursor, PAGE_SIZE);
      setNotifications((prev) => [...prev, ...page.notifications]);
      setNextCursor(page.nextCursor);
      setHasMore(page.nextCursor !== undefined);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('알림을 불러올 수 없어요'));
    } finally {
      setLoading(false);
    }
  }, [hasMore, loading, nextCursor]);

  const handleMarkRead = useCallback(async (id: number) => {
    await markRead(id);
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true } : n)),
    );
  }, []);

  const handleMarkAllRead = useCallback(async () => {
    await markAllRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  }, []);

  return {
    notifications,
    loading,
    error,
    hasMore,
    refresh,
    loadMore,
    handleMarkRead,
    handleMarkAllRead,
  };
}
