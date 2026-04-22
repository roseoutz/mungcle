import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { TextField } from '../src/shared/components/TextField';

describe('TextField', () => {
  it('label을 렌더링함', () => {
    const { getByText } = render(
      <TextField label="이메일" value="" onChangeText={() => {}} />,
    );
    expect(getByText('이메일')).toBeTruthy();
  });

  it('error 메시지 표시', () => {
    const { getByText } = render(
      <TextField
        label="이메일"
        value=""
        onChangeText={() => {}}
        error="올바른 이메일을 입력해주세요"
      />,
    );
    expect(getByText('올바른 이메일을 입력해주세요')).toBeTruthy();
  });

  it('error 없을 때 에러 메시지 미표시', () => {
    const { queryByText } = render(
      <TextField label="이름" value="" onChangeText={() => {}} />,
    );
    expect(queryByText('올바른 이메일을 입력해주세요')).toBeNull();
  });

  it('onChangeText 호출됨', () => {
    const onChangeText = jest.fn();
    const { getByLabelText } = render(
      <TextField label="이름" value="" onChangeText={onChangeText} />,
    );
    fireEvent.changeText(getByLabelText('이름'), '홍길동');
    expect(onChangeText).toHaveBeenCalledWith('홍길동');
  });
});
