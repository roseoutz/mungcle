import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { NotificationCard } from '../src/features/notifications/components/NotificationCard';
import type { NotificationResponse } from '../src/features/notifications/types/notifications.types';

// expo-router mock
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn() }),
}));

function makeNotification(
  overrides: Partial<NotificationResponse> = {},
): NotificationResponse {
  return {
    id: 1,
    userId: 100,
    type: 'GREETING_RECEIVED',
    payload: {},
    read: false,
    createdAt: Date.now() - 60000, // 1분 전
    ...overrides,
  };
}

describe('NotificationCard', () => {
  it('GREETING_RECEIVED 타입 렌더링', () => {
    const { getByText } = render(
      <NotificationCard notification={makeNotification()} onMarkRead={jest.fn()} />,
    );
    expect(getByText('인사 수신')).toBeTruthy();
  });

  it('GREETING_ACCEPTED 타입 렌더링', () => {
    const { getByText } = render(
      <NotificationCard
        notification={makeNotification({ type: 'GREETING_ACCEPTED' })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(getByText('인사 매칭')).toBeTruthy();
  });

  it('MESSAGE_RECEIVED 타입 렌더링', () => {
    const { getByText } = render(
      <NotificationCard
        notification={makeNotification({ type: 'MESSAGE_RECEIVED' })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(getByText('새 메시지')).toBeTruthy();
  });

  it('WALK_EXPIRED 타입 렌더링', () => {
    const { getByText } = render(
      <NotificationCard
        notification={makeNotification({ type: 'WALK_EXPIRED' })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(getByText('산책 만료')).toBeTruthy();
  });

  it('읽지 않은 알림에 dot 표시', () => {
    const { getByLabelText } = render(
      <NotificationCard
        notification={makeNotification({ read: false })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(getByLabelText('읽지 않음')).toBeTruthy();
  });

  it('읽은 알림에 dot 미표시', () => {
    const { queryByLabelText } = render(
      <NotificationCard
        notification={makeNotification({ read: true })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(queryByLabelText('읽지 않음')).toBeNull();
  });

  it('탭 시 onMarkRead 호출 (읽지 않은 상태)', () => {
    const onMarkRead = jest.fn();
    const { getByRole } = render(
      <NotificationCard
        notification={makeNotification({ id: 42, read: false })}
        onMarkRead={onMarkRead}
      />,
    );
    fireEvent.press(getByRole('button'));
    expect(onMarkRead).toHaveBeenCalledWith(42);
  });

  it('탭 시 onMarkRead 미호출 (이미 읽은 상태)', () => {
    const onMarkRead = jest.fn();
    const { getByRole } = render(
      <NotificationCard
        notification={makeNotification({ read: true })}
        onMarkRead={onMarkRead}
      />,
    );
    fireEvent.press(getByRole('button'));
    expect(onMarkRead).not.toHaveBeenCalled();
  });

  it('payload.message가 있으면 메시지 텍스트 표시', () => {
    const { getByText } = render(
      <NotificationCard
        notification={makeNotification({ payload: { message: '안녕하세요!' } })}
        onMarkRead={jest.fn()}
      />,
    );
    expect(getByText('안녕하세요!')).toBeTruthy();
  });
});
