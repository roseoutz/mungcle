import { fireEvent, render, waitFor } from '@testing-library/react-native';
import React from 'react';
import { WalkToggle } from '../src/features/walks/components/WalkToggle';

// expo-location 모킹
jest.mock('expo-location', () => ({
  requestForegroundPermissionsAsync: jest.fn().mockResolvedValue({ status: 'granted' }),
  getCurrentPositionAsync: jest.fn().mockResolvedValue({
    coords: { latitude: 37.5665, longitude: 126.978 },
  }),
  Accuracy: { Balanced: 3 },
}));

// walks API 모킹
jest.mock('../src/features/walks/services/walks.api', () => ({
  startWalk: jest.fn().mockResolvedValue({
    id: 1, dogId: 1, userId: 1, type: 'OPEN', gridCell: '37.5664_126.9780',
    status: 'ACTIVE', startedAt: Date.now() / 1000, endsAt: Date.now() / 1000 + 3600,
  }),
  stopWalk: jest.fn().mockResolvedValue({
    id: 1, dogId: 1, userId: 1, type: 'OPEN', gridCell: '37.5664_126.9780',
    status: 'ENDED', startedAt: Date.now() / 1000, endsAt: Date.now() / 1000,
  }),
  getMyActiveWalks: jest.fn().mockResolvedValue([]),
}));

describe('WalkToggle', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('OFF 상태에서 "산책 시작" 버튼 렌더링', () => {
    const { getByLabelText } = render(<WalkToggle dogId={1} />);
    expect(getByLabelText('산책 시작하기')).toBeTruthy();
  });

  it('dogId가 null이면 버튼 비활성화', () => {
    const { getByLabelText } = render(<WalkToggle dogId={null} />);
    const button = getByLabelText('산책 시작하기');
    expect(button.props.accessibilityState?.disabled).toBe(true);
  });

  it('버튼 누르면 startWalk 호출 후 OPEN 상태로 전환', async () => {
    const { startWalk } = require('../src/features/walks/services/walks.api');
    const { getByLabelText } = render(<WalkToggle dogId={1} />);

    fireEvent.press(getByLabelText('산책 시작하기'));

    await waitFor(() => {
      expect(startWalk).toHaveBeenCalledTimes(1);
    });

    await waitFor(() => {
      expect(getByLabelText('산책 종료하기')).toBeTruthy();
    });
  });

  it('OPEN 상태에서 카운트다운(00:) 텍스트 표시', async () => {
    const { getByLabelText, getByText } = render(<WalkToggle dogId={1} />);
    fireEvent.press(getByLabelText('산책 시작하기'));

    await waitFor(() => {
      expect(getByLabelText('산책 종료하기')).toBeTruthy();
    });

    // 카운트다운 형식 확인 (60:00 또는 59:59 형태)
    const countdownPattern = /\d{2}:\d{2}/;
    const allTexts = getByText(countdownPattern);
    expect(allTexts).toBeTruthy();
  });
});
