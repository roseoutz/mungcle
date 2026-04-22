import { fireEvent, render } from '@testing-library/react-native';
import React from 'react';
import { EmptyState } from '../src/shared/components/EmptyState';

describe('EmptyState', () => {
  it('메시지 텍스트가 렌더링됨', () => {
    const { getByText } = render(<EmptyState message="근처에 강아지가 없어요." />);
    expect(getByText('근처에 강아지가 없어요.')).toBeTruthy();
  });

  it('icon이 있으면 렌더링됨', () => {
    const { getByText } = render(<EmptyState icon="🐕" message="비어있어요." />);
    expect(getByText('🐕')).toBeTruthy();
  });

  it('icon이 없으면 렌더링되지 않음', () => {
    const { queryByText } = render(<EmptyState message="비어있어요." />);
    // 이모지가 없으므로 null
    expect(queryByText('🐕')).toBeNull();
  });

  it('ctaLabel과 onCta가 있으면 버튼 렌더링', () => {
    const onCta = jest.fn();
    const { getByLabelText } = render(
      <EmptyState message="비어있어요." ctaLabel="다시 시도" onCta={onCta} />,
    );
    expect(getByLabelText('다시 시도')).toBeTruthy();
  });

  it('CTA 버튼 누르면 onCta 호출', () => {
    const onCta = jest.fn();
    const { getByLabelText } = render(
      <EmptyState message="비어있어요." ctaLabel="다시 시도" onCta={onCta} />,
    );
    fireEvent.press(getByLabelText('다시 시도'));
    expect(onCta).toHaveBeenCalledTimes(1);
  });

  it('ctaLabel만 있고 onCta가 없으면 버튼 미렌더링', () => {
    const { queryByText } = render(
      <EmptyState message="비어있어요." ctaLabel="다시 시도" />,
    );
    expect(queryByText('다시 시도')).toBeNull();
  });
});
