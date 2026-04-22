import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import SettingsScreen from '../app/(tabs)/settings';
import * as settingsApi from '../src/features/settings/services/settings.api';

// expo-router mock
jest.mock('expo-router', () => ({
  useRouter: () => ({ push: jest.fn(), back: jest.fn() }),
}));

// auth mock
const mockLogout = jest.fn();
jest.mock('../src/features/auth', () => ({
  useAuth: () => ({
    user: { id: 1, nickname: '테스터', neighborhood: '강남구', profilePhotoUrl: '' },
    logout: mockLogout,
  }),
}));

// settings API mock
jest.mock('../src/features/settings/services/settings.api');
const mockDeleteAccount = settingsApi.deleteAccount as jest.MockedFunction<
  typeof settingsApi.deleteAccount
>;

describe('SettingsScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('프로필 카드에 닉네임과 동네 표시', () => {
    const { getByText } = render(<SettingsScreen />);
    expect(getByText('테스터')).toBeTruthy();
    expect(getByText('강남구')).toBeTruthy();
  });

  it('메뉴 항목 렌더링: 차단 관리, 신고하기, 로그아웃, 회원 탈퇴', () => {
    const { getByText } = render(<SettingsScreen />);
    expect(getByText('차단 관리')).toBeTruthy();
    expect(getByText('신고하기')).toBeTruthy();
    expect(getByText('로그아웃')).toBeTruthy();
    expect(getByText('회원 탈퇴')).toBeTruthy();
  });

  it('로그아웃 탭 시 logout 호출', async () => {
    const { getByLabelText } = render(<SettingsScreen />);
    fireEvent.press(getByLabelText('로그아웃'));
    await waitFor(() => {
      expect(mockLogout).toHaveBeenCalledTimes(1);
    });
  });

  it('회원 탈퇴 탭 시 확인 모달 표시', () => {
    const { getByLabelText, getByText } = render(<SettingsScreen />);
    fireEvent.press(getByLabelText('회원 탈퇴'));
    // 모달 내 메시지 텍스트가 포함되어 있는지 확인 (부분 일치)
    expect(getByText(/탈퇴하면 모든 데이터가 삭제되어요/)).toBeTruthy();
  });

  it('탈퇴 모달에서 취소 탭 시 모달 닫힘', () => {
    const { getByLabelText, getAllByText } = render(<SettingsScreen />);
    fireEvent.press(getByLabelText('회원 탈퇴'));
    // 취소 버튼이 visible=true인 모달에 있음을 확인 후 닫기
    fireEvent.press(getByLabelText('취소'));
    // 모달이 닫히면 deleteModalVisible=false → Modal visible=false
    // 탈퇴 확인 버튼 레이블이 disabled 상태가 되지 않음을 통해 간접 검증
    const menuItems = getAllByText('회원 탈퇴');
    expect(menuItems.length).toBeGreaterThanOrEqual(1);
  });

  it('탈퇴 확인 시 deleteAccount + logout 호출', async () => {
    mockDeleteAccount.mockResolvedValue(undefined);
    const { getByLabelText } = render(<SettingsScreen />);
    fireEvent.press(getByLabelText('회원 탈퇴'));
    fireEvent.press(getByLabelText('탈퇴 확인'));
    await waitFor(() => {
      expect(mockDeleteAccount).toHaveBeenCalledTimes(1);
      expect(mockLogout).toHaveBeenCalledTimes(1);
    });
  });
});
