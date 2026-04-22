import { apiClient } from '../../../shared/utils/api';
import type { NotificationsPage } from '../types/notifications.types';

export async function listNotifications(
  cursor?: number,
  limit?: number,
): Promise<NotificationsPage> {
  const params = new URLSearchParams();
  if (cursor !== undefined) params.set('cursor', String(cursor));
  if (limit !== undefined) params.set('limit', String(limit));
  const query = params.toString();
  return apiClient<NotificationsPage>(`/api/notifications${query ? `?${query}` : ''}`);
}

export async function markRead(notificationId: number): Promise<void> {
  return apiClient<void>(`/api/notifications/${notificationId}/read`, { method: 'POST' });
}

export async function markAllRead(): Promise<void> {
  return apiClient<void>('/api/notifications/read-all', { method: 'POST' });
}
