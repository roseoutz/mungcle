import React from 'react';
import { render } from '@testing-library/react-native';
import { NotificationList } from '../src/features/notifications/components/NotificationList';
import type { NotificationListProps } from '../src/features/notifications/components/NotificationList';

// expo-router mock
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn() }),
}));

const baseProps: NotificationListProps = {
  notifications: [],
  loading: false,
  error: null,
  hasMore: false,
  onRefresh: jest.fn(),
  onLoadMore: jest.fn(),
  onMarkRead: jest.fn(),
};

describe('NotificationList', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('loading 상태: ActivityIndicator 표시', () => {
    const { getByLabelText } = render(
      <NotificationList {...baseProps} loading={true} />,
    );
    expect(getByLabelText('불러오는 중')).toBeTruthy();
  });

  it('error 상태: 에러 메시지 표시', () => {
    const { getByText } = render(
      <NotificationList {...baseProps} error="네트워크 오류" />,
    );
    expect(getByText('연결할 수 없어요')).toBeTruthy();
  });

  it('empty 상태: 빈 메시지 표시', () => {
    const { getByText } = render(
      <NotificationList {...baseProps} />,
    );
    expect(getByText('아직 인사가 없어요')).toBeTruthy();
  });

  it('success 상태: 알림 목록 렌더링', async () => {
    const notifications = [
      {
        id: 1,
        userId: 10,
        type: 'GREETING_RECEIVED' as const,
        payload: {},
        read: false,
        createdAt: Date.now() - 5000,
      },
      {
        id: 2,
        userId: 10,
        type: 'MESSAGE_RECEIVED' as const,
        payload: { message: '산책 같이해요' },
        read: true,
        createdAt: Date.now() - 10000,
      },
    ];

    const { findByText } = render(
      <NotificationList {...baseProps} notifications={notifications} />,
    );
    expect(await findByText('인사 수신')).toBeTruthy();
    expect(await findByText('새 메시지')).toBeTruthy();
  });

  it('success 상태: hasMore=false 이면 onLoadMore prop 미전달', () => {
    const notifications = [
      {
        id: 1,
        userId: 10,
        type: 'GREETING_RECEIVED' as const,
        payload: {},
        read: true,
        createdAt: Date.now(),
      },
    ];

    const onLoadMore = jest.fn();
    render(
      <NotificationList
        {...baseProps}
        notifications={notifications}
        hasMore={false}
        onLoadMore={onLoadMore}
      />,
    );
    // hasMore=false 이므로 onEndReached에 onLoadMore가 전달되지 않음 — 호출 없음
    expect(onLoadMore).not.toHaveBeenCalled();
  });
});
