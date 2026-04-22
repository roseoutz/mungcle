import { fireEvent, render } from '@testing-library/react-native';
import React from 'react';
import { NearbyDogCard } from '../src/features/walks/components/NearbyDogCard';
import type { NearbyWalkCard } from '../src/features/walks/types/walks.types';

const mockWalk: NearbyWalkCard = {
  walkId: 42,
  dog: {
    id: 1,
    name: '콩이',
    breed: '포메라니안',
    size: 'SMALL',
    temperaments: ['FRIENDLY', 'PLAYFUL'],
    sociability: 5,
    photoUrl: undefined,
    vaccinationRegistered: true,
  },
  owner: {
    id: 10,
    nickname: '박철수',
    neighborhood: '마포구',
    profilePhotoUrl: undefined,
  },
  gridDistance: 1,
  startedAt: Date.now() / 1000,
};

describe('NearbyDogCard', () => {
  it('강아지 이름과 견종이 렌더링됨', () => {
    const { getByText } = render(
      <NearbyDogCard item={mockWalk} onGreet={jest.fn()} onPress={jest.fn()} />,
    );
    expect(getByText('콩이')).toBeTruthy();
    expect(getByText('포메라니안 · SMALL')).toBeTruthy();
  });

  it('성향 칩이 렌더링됨', () => {
    const { getByText } = render(
      <NearbyDogCard item={mockWalk} onGreet={jest.fn()} onPress={jest.fn()} />,
    );
    expect(getByText('FRIENDLY')).toBeTruthy();
    expect(getByText('PLAYFUL')).toBeTruthy();
  });

  it('인사하기 버튼 누르면 onGreet(walkId) 호출', () => {
    const onGreet = jest.fn();
    const { getByLabelText } = render(
      <NearbyDogCard item={mockWalk} onGreet={onGreet} onPress={jest.fn()} />,
    );
    fireEvent.press(getByLabelText('콩이에게 인사하기'));
    expect(onGreet).toHaveBeenCalledWith(42);
  });

  it('카드 누르면 onPress(walkId) 호출', () => {
    const onPress = jest.fn();
    const { getByLabelText } = render(
      <NearbyDogCard item={mockWalk} onGreet={jest.fn()} onPress={onPress} />,
    );
    fireEvent.press(getByLabelText('콩이 카드, 상세보기'));
    expect(onPress).toHaveBeenCalledWith(42);
  });

  it('백신 등록 이모지가 표시됨', () => {
    const { getByLabelText } = render(
      <NearbyDogCard item={mockWalk} onGreet={jest.fn()} onPress={jest.fn()} />,
    );
    expect(getByLabelText('백신 등록됨')).toBeTruthy();
  });
});
