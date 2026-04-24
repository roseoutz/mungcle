import { fireEvent, render } from '@testing-library/react-native';
import React from 'react';
import { ErrorState } from '../src/shared/components/ErrorState';

describe('ErrorState', () => {
  it('기본(generic) 변형 — 기본 타이틀/메시지 렌더링', () => {
    const { getByText } = render(<ErrorState />);
    expect(getByText('오류가 발생했어요')).toBeTruthy();
    expect(getByText('잠시 후 다시 시도해주세요')).toBeTruthy();
  });

  it('network 변형 — 네트워크 메시지 렌더링', () => {
    const { getByText } = render(<ErrorState variant="network" />);
    expect(getByText('연결할 수 없어요')).toBeTruthy();
  });

  it('server 변형 — 서버 오류 메시지 렌더링', () => {
    const { getByText } = render(<ErrorState variant="server" />);
    expect(getByText('서버 오류')).toBeTruthy();
  });

  it('notFound 변형 — 찾을 수 없음 메시지 렌더링', () => {
    const { getByText } = render(<ErrorState variant="notFound" />);
    expect(getByText('찾을 수 없어요')).toBeTruthy();
  });

  it('message prop으로 기본 메시지 오버라이드 가능', () => {
    const { getByText } = render(<ErrorState message="커스텀 에러 메시지" />);
    expect(getByText('커스텀 에러 메시지')).toBeTruthy();
  });

  it('onRetry가 있으면 다시 시도 버튼 렌더링', () => {
    const onRetry = jest.fn();
    const { getByLabelText } = render(<ErrorState onRetry={onRetry} />);
    expect(getByLabelText('다시 시도')).toBeTruthy();
  });

  it('onRetry가 없으면 버튼 미렌더링', () => {
    const { queryByLabelText } = render(<ErrorState />);
    expect(queryByLabelText('다시 시도')).toBeNull();
  });

  it('다시 시도 버튼 클릭 시 onRetry 호출', () => {
    const onRetry = jest.fn();
    const { getByLabelText } = render(<ErrorState onRetry={onRetry} />);
    fireEvent.press(getByLabelText('다시 시도'));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });
});
