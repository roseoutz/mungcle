import { fireEvent, render } from '@testing-library/react-native';
import React, { useState } from 'react';
import { Text } from 'react-native';
import { ErrorBoundary } from '../src/shared/components/ErrorBoundary';

// 외부에서 에러 발생 여부를 제어할 수 있는 테스트용 컴포넌트
function ThrowError({ shouldThrow }: { shouldThrow: boolean }) {
  if (shouldThrow) throw new Error('테스트 에러');
  return <Text>정상 콘텐츠</Text>;
}

function ThrowNetworkError({ shouldThrow }: { shouldThrow: boolean }) {
  if (shouldThrow) throw new Error('Network request failed');
  return <Text>정상 콘텐츠</Text>;
}

// console.error 억제 — React error boundary 테스트 시 불필요한 출력 방지
beforeEach(() => {
  jest.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(() => {
  jest.restoreAllMocks();
});

describe('ErrorBoundary', () => {
  it('에러 없으면 자식 컴포넌트를 렌더링함', () => {
    const { getByText } = render(
      <ErrorBoundary>
        <ThrowError shouldThrow={false} />
      </ErrorBoundary>,
    );
    expect(getByText('정상 콘텐츠')).toBeTruthy();
  });

  it('렌더 에러 발생 시 기본 에러 화면을 표시함', () => {
    const { getByText } = render(
      <ErrorBoundary>
        <ThrowError shouldThrow />
      </ErrorBoundary>,
    );
    expect(getByText('오류가 발생했어요')).toBeTruthy();
    expect(getByText('잠시 후 다시 시도해주세요')).toBeTruthy();
  });

  it('네트워크 에러 발생 시 연결 오류 화면을 표시함', () => {
    const { getByText } = render(
      <ErrorBoundary>
        <ThrowNetworkError shouldThrow />
      </ErrorBoundary>,
    );
    expect(getByText('연결할 수 없어요')).toBeTruthy();
  });

  it('다시 시도 버튼 클릭 시 onRetry가 호출됨', () => {
    const onRetry = jest.fn();
    const { getByLabelText } = render(
      <ErrorBoundary onRetry={onRetry}>
        <ThrowError shouldThrow />
      </ErrorBoundary>,
    );
    fireEvent.press(getByLabelText('다시 시도'));
    expect(onRetry).toHaveBeenCalledTimes(1);
  });

  it('커스텀 fallback prop이 있으면 fallback UI를 렌더링함', () => {
    const fallback = jest.fn(() => <Text>커스텀 에러 UI</Text>);
    const { getByText } = render(
      <ErrorBoundary fallback={fallback}>
        <ThrowError shouldThrow />
      </ErrorBoundary>,
    );
    expect(getByText('커스텀 에러 UI')).toBeTruthy();
    // React가 error recovery 과정에서 두 번 호출할 수 있음
    expect(fallback).toHaveBeenCalled();
  });
});
