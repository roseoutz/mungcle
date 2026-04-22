export type NotificationType =
  | 'GREETING_RECEIVED'
  | 'GREETING_ACCEPTED'
  | 'MESSAGE_RECEIVED'
  | 'WALK_EXPIRED';

export interface NotificationResponse {
  id: number;
  userId: number;
  type: NotificationType;
  payload: Record<string, unknown>;
  read: boolean;
  createdAt: number;
}

export interface NotificationsPage {
  notifications: NotificationResponse[];
  nextCursor?: number;
}
