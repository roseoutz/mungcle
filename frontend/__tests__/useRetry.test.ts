import { act, renderHook, waitFor } from '@testing-library/react-native';
import { useRetry } from '../src/shared/hooks/useRetry';

// 지수 백오프 sleep을 건너뛰기 위해 실제 setTimeout을 즉시 실행으로 교체
beforeAll(() => {
  jest
    .spyOn(global, 'setTimeout')
    .mockImplementation((fn: TimerHandler, _delay?: number, ..._args: unknown[]) => {
      if (typeof fn === 'function') (fn as () => void)();
      return 0 as unknown as ReturnType<typeof setTimeout>;
    });
});

afterAll(() => {
  (global.setTimeout as jest.MockedFunction<typeof setTimeout>).mockRestore();
});

afterEach(() => {
  jest.clearAllMocks();
});

describe('useRetry', () => {
  it('성공 시 data를 반환하고 error는 null', async () => {
    const asyncFn = jest.fn().mockResolvedValue({ id: 1 });
    const { result } = renderHook(() => useRetry(asyncFn, { maxRetries: 3 }));

    act(() => { result.current.retry(); });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.data).toEqual({ id: 1 });
    expect(result.current.error).toBeNull();
    expect(asyncFn).toHaveBeenCalledTimes(1);
  });

  it('maxRetries 초과 시 error를 설정하고 data는 null', async () => {
    const asyncFn = jest.fn().mockRejectedValue(new Error('서버 오류'));
    const { result } = renderHook(() =>
      useRetry(asyncFn, { maxRetries: 2, initialDelayMs: 1 }),
    );

    act(() => { result.current.retry(); });

    await waitFor(() => expect(result.current.isLoading).toBe(false), { timeout: 5000 });

    expect(result.current.error).not.toBeNull();
    expect(result.current.data).toBeNull();
    // 최초 1회 + 재시도 2회 = 3회
    expect(asyncFn).toHaveBeenCalledTimes(3);
  });

  it('retry 재호출 시 retryCount가 0으로 초기화됨', async () => {
    const asyncFn = jest.fn().mockResolvedValue('ok');
    const { result } = renderHook(() => useRetry(asyncFn));

    act(() => { result.current.retry(); });

    await waitFor(() => expect(result.current.isLoading).toBe(false));

    expect(result.current.retryCount).toBe(0);
  });

  it('isLoading — 실행 중 true, 완료 후 false', async () => {
    let resolve!: (v: string) => void;
    const asyncFn = jest.fn(
      () => new Promise<string>((res) => { resolve = res; }),
    );
    const { result } = renderHook(() => useRetry(asyncFn));

    act(() => { result.current.retry(); });

    expect(result.current.isLoading).toBe(true);

    await act(async () => { resolve('done'); });

    expect(result.current.isLoading).toBe(false);
    expect(result.current.data).toBe('done');
  });
});
