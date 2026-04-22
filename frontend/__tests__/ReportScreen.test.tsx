import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import ReportScreen from '../app/settings/report';
import * as settingsApi from '../src/features/settings/services/settings.api';

// expo-router mock
const mockBack = jest.fn();
const mockPush = jest.fn();
let mockParams: { userId?: string } = { userId: '42' };

jest.mock('expo-router', () => ({
  useRouter: () => ({ push: mockPush, back: mockBack }),
  useLocalSearchParams: () => mockParams,
}));

// settings API mock
jest.mock('../src/features/settings/services/settings.api');
const mockCreateReport = settingsApi.createReport as jest.MockedFunction<
  typeof settingsApi.createReport
>;

describe('ReportScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockParams = { userId: '42' };
  });

  // success: 신고 사유 선택 후 제출
  it('신고 사유 선택 후 제출 시 createReport 호출 — success', async () => {
    mockCreateReport.mockResolvedValue(undefined);
    jest.spyOn(Alert, 'alert');

    const { getByText, getByLabelText } = render(<ReportScreen />);

    fireEvent.press(getByText('욕설/혐오 발언'));
    fireEvent.press(getByLabelText('신고 제출'));

    await waitFor(() => {
      expect(mockCreateReport).toHaveBeenCalledWith(42, '욕설/혐오 발언');
      expect(Alert.alert).toHaveBeenCalledWith(
        '신고 완료',
        expect.any(String),
        expect.any(Array),
      );
    });
  });

  // error: API 실패 시 에러 알림
  it('createReport 실패 시 에러 Alert 표시 — error', async () => {
    mockCreateReport.mockRejectedValue(new Error('network error'));
    jest.spyOn(Alert, 'alert');

    const { getByText, getByLabelText } = render(<ReportScreen />);

    fireEvent.press(getByText('스팸/광고'));

    await waitFor(async () => {
      fireEvent.press(getByLabelText('신고 제출'));
    });

    await waitFor(() => {
      expect(Alert.alert).toHaveBeenCalledWith('오류', expect.any(String));
    });
  });

  // empty: 사유 미선택 시 제출 버튼 비활성화
  it('사유 미선택 시 제출 버튼이 비활성화됨 — empty', () => {
    const { getByLabelText } = render(<ReportScreen />);
    const submitButton = getByLabelText('신고 제출');
    // disabled prop이 설정되어 있어야 함
    expect(submitButton.props.accessibilityState?.disabled).toBe(true);
    expect(mockCreateReport).not.toHaveBeenCalled();
  });

  // loading: 제출 중 버튼 비활성화
  it('제출 중 버튼이 비활성화됨 — loading', async () => {
    // createReport가 pending인 동안 버튼 상태 확인
    let resolveReport!: () => void;
    mockCreateReport.mockReturnValue(
      new Promise<void>((res) => {
        resolveReport = res;
      }),
    );

    const { getByText, getByLabelText } = render(<ReportScreen />);
    fireEvent.press(getByText('기타'));
    fireEvent.press(getByLabelText('신고 제출'));

    // 제출 중 텍스트로 로딩 상태 확인
    await waitFor(() => {
      expect(getByText('제출 중...')).toBeTruthy();
    });

    resolveReport();
  });

  // userId 없을 때 뒤로가기
  it('userId 파라미터 없으면 router.back 호출', () => {
    mockParams = {};
    render(<ReportScreen />);
    expect(mockBack).toHaveBeenCalledTimes(1);
  });

  // 추가 설명과 함께 신고
  it('추가 설명 입력 시 reason에 포함되어 전송', async () => {
    mockCreateReport.mockResolvedValue(undefined);

    const { getByText, getByLabelText } = render(<ReportScreen />);
    fireEvent.press(getByText('사기/허위 정보'));
    fireEvent.changeText(getByLabelText('추가 설명 입력'), '상세 내용');
    fireEvent.press(getByLabelText('신고 제출'));

    await waitFor(() => {
      expect(mockCreateReport).toHaveBeenCalledWith(42, '사기/허위 정보: 상세 내용');
    });
  });
});
