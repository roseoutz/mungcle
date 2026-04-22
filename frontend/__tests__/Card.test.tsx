import React from 'react';
import { render } from '@testing-library/react-native';
import { Text } from 'react-native';
import { Card } from '../src/shared/components/Card';

describe('Card', () => {
  it('children을 렌더링함', () => {
    const { getByText } = render(
      <Card>
        <Text>카드 내용</Text>
      </Card>,
    );
    expect(getByText('카드 내용')).toBeTruthy();
  });

  it('여러 children 렌더링', () => {
    const { getByText } = render(
      <Card>
        <Text>첫 번째</Text>
        <Text>두 번째</Text>
      </Card>,
    );
    expect(getByText('첫 번째')).toBeTruthy();
    expect(getByText('두 번째')).toBeTruthy();
  });
});
