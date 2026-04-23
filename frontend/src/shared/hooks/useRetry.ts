import { useCallback, useRef, useState } from 'react';

interface UseRetryOptions {
  maxRetries?: number;
  // 첫 재시도 대기 시간(ms) — 이후 지수 증가
  initialDelayMs?: number;
}

interface UseRetryResult<T> {
  data: T | null;
  error: Error | null;
  isLoading: boolean;
  retryCount: number;
  retry: () => void;
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export function useRetry<T>(
  asyncFn: () => Promise<T>,
  options: UseRetryOptions = {},
): UseRetryResult<T> {
  const { maxRetries = 3, initialDelayMs = 1000 } = options;

  const [data, setData] = useState<T | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [retryCount, setRetryCount] = useState(0);
  // 최신 asyncFn 참조를 유지 — 클로저 stale 방지
  const asyncFnRef = useRef(asyncFn);
  asyncFnRef.current = asyncFn;

  const execute = useCallback(
    async (attempt: number) => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await asyncFnRef.current();
        setData(result);
        setError(null);
      } catch (err) {
        const caughtError = err instanceof Error ? err : new Error(String(err));
        if (attempt < maxRetries) {
          // 지수 백오프: initialDelayMs * 2^attempt (최대 30초)
          const delay = Math.min(initialDelayMs * Math.pow(2, attempt), 30_000);
          await sleep(delay);
          setRetryCount(attempt + 1);
          await execute(attempt + 1);
          return;
        }
        setError(caughtError);
        setData(null);
      } finally {
        setIsLoading(false);
      }
    },
    [maxRetries, initialDelayMs],
  );

  const retry = useCallback(() => {
    setRetryCount(0);
    execute(0);
  }, [execute]);

  return { data, error, isLoading, retryCount, retry };
}
