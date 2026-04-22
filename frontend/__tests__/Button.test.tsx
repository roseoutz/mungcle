import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { Button } from '../src/shared/components/Button';

describe('Button', () => {
  it('primary 버전 렌더링', () => {
    const { getByText } = render(
      <Button variant="primary" accessibilityLabel="확인">확인</Button>,
    );
    expect(getByText('확인')).toBeTruthy();
  });

  it('outline 버전 렌더링', () => {
    const { getByText } = render(
      <Button variant="outline" accessibilityLabel="취소">취소</Button>,
    );
    expect(getByText('취소')).toBeTruthy();
  });

  it('disabled 상태에서 onPress 미호출', () => {
    const onPress = jest.fn();
    const { getByRole } = render(
      <Button variant="primary" accessibilityLabel="버튼" disabled onPress={onPress}>
        버튼
      </Button>,
    );
    fireEvent.press(getByRole('button'));
    expect(onPress).not.toHaveBeenCalled();
  });

  it('loading 상태에서 ActivityIndicator 표시', () => {
    const { queryByText, getByRole } = render(
      <Button variant="primary" accessibilityLabel="로딩" loading>
        로딩
      </Button>,
    );
    // loading 중에는 텍스트 대신 ActivityIndicator가 렌더링됨
    expect(queryByText('로딩')).toBeNull();
    expect(getByRole('button')).toBeTruthy();
  });

  it('accessibilityLabel이 버튼에 설정됨', () => {
    const { getByLabelText } = render(
      <Button variant="primary" accessibilityLabel="저장하기">저장</Button>,
    );
    expect(getByLabelText('저장하기')).toBeTruthy();
  });
});
