import { render } from '@testing-library/react-native';
import React from 'react';
import { Chip } from '../src/shared/components/Chip';

describe('Chip', () => {
  it('레이블 텍스트가 렌더링됨', () => {
    const { getByText } = render(<Chip label="FRIENDLY" />);
    expect(getByText('FRIENDLY')).toBeTruthy();
  });

  it('FRIENDLY 칩은 녹색 배경색 적용', () => {
    const { getByText } = render(<Chip label="FRIENDLY" />);
    const text = getByText('FRIENDLY');
    // 텍스트 색상이 녹색 계열인지 확인
    expect(text.props.style).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ color: '#16A34A' }),
      ]),
    );
  });

  it('CALM 칩은 파란색 텍스트 적용', () => {
    const { getByText } = render(<Chip label="CALM" />);
    const text = getByText('CALM');
    expect(text.props.style).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ color: '#2563EB' }),
      ]),
    );
  });

  it('CAUTION 칩은 빨간색 텍스트 적용', () => {
    const { getByText } = render(<Chip label="CAUTION" />);
    const text = getByText('CAUTION');
    expect(text.props.style).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ color: '#DC2626' }),
      ]),
    );
  });

  it('알 수 없는 성향은 기본 회색 적용', () => {
    const { getByText } = render(<Chip label="UNKNOWN_TRAIT" />);
    const text = getByText('UNKNOWN_TRAIT');
    expect(text.props.style).toEqual(
      expect.arrayContaining([
        expect.objectContaining({ color: '#6B7280' }),
      ]),
    );
  });

  it('accessibilityLabel이 설정됨', () => {
    const { getByLabelText } = render(<Chip label="PLAYFUL" accessibilityLabel="활발한 성향" />);
    expect(getByLabelText('활발한 성향')).toBeTruthy();
  });
});
