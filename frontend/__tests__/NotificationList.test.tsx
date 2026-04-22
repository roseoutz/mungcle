import React from 'react';
import { render, waitFor } from '@testing-library/react-native';
import { NotificationList } from '../src/features/notifications/components/NotificationList';
import * as notificationsApi from '../src/features/notifications/services/notifications.api';

// expo-router mock
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn() }),
}));

// API mock
jest.mock('../src/features/notifications/services/notifications.api');

const mockListNotifications = notificationsApi.listNotifications as jest.MockedFunction<
  typeof notificationsApi.listNotifications
>;

describe('NotificationList', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  it('loading 상태: ActivityIndicator 표시', () => {
    // 응답을 지연시켜 로딩 상태 유지
    mockListNotifications.mockReturnValue(new Promise(() => {}));
    const { getByLabelText } = render(<NotificationList />);
    expect(getByLabelText('불러오는 중')).toBeTruthy();
  });

  it('error 상태: 에러 메시지 표시', async () => {
    mockListNotifications.mockRejectedValue(new Error('네트워크 오류'));
    const { findByText } = render(<NotificationList />);
    expect(await findByText('연결할 수 없어요')).toBeTruthy();
  });

  it('empty 상태: 빈 메시지 표시', async () => {
    mockListNotifications.mockResolvedValue({ notifications: [] });
    const { findByText } = render(<NotificationList />);
    expect(await findByText('아직 인사가 없어요')).toBeTruthy();
  });

  it('success 상태: 알림 목록 렌더링', async () => {
    mockListNotifications.mockResolvedValue({
      notifications: [
        {
          id: 1,
          userId: 10,
          type: 'GREETING_RECEIVED',
          payload: {},
          read: false,
          createdAt: Date.now() - 5000,
        },
        {
          id: 2,
          userId: 10,
          type: 'MESSAGE_RECEIVED',
          payload: { message: '산책 같이해요' },
          read: true,
          createdAt: Date.now() - 10000,
        },
      ],
    });

    const { findByText } = render(<NotificationList />);
    expect(await findByText('인사 수신')).toBeTruthy();
    expect(await findByText('새 메시지')).toBeTruthy();
  });

  it('success 상태: nextCursor 없으면 hasMore=false (loadMore 미호출)', async () => {
    mockListNotifications.mockResolvedValue({
      notifications: [
        {
          id: 1,
          userId: 10,
          type: 'GREETING_RECEIVED',
          payload: {},
          read: true,
          createdAt: Date.now(),
        },
      ],
      // nextCursor 없음
    });

    render(<NotificationList />);

    await waitFor(() => {
      // listNotifications는 초기 load 시 1회만 호출
      expect(mockListNotifications).toHaveBeenCalledTimes(1);
    });
  });
});
