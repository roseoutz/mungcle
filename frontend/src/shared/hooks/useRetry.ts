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

  const execute = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    setRetryCount(0);

    // 재귀 대신 for 루프 사용 — finally가 각 호출마다 실행되던 isLoading 깜빡임 방지
    for (let attempt = 0; attempt <= maxRetries; attempt++) {
      try {
        const result = await asyncFnRef.current();
        setData(result);
        setIsLoading(false);
        return;
      } catch (err) {
        if (attempt < maxRetries) {
          // 지수 백오프: initialDelayMs * 2^attempt (최대 30초)
          const delay = Math.min(initialDelayMs * Math.pow(2, attempt), 30_000);
          setRetryCount(attempt + 1);
          await sleep(delay);
        } else {
          setError(err instanceof Error ? err : new Error(String(err)));
          setData(null);
          setIsLoading(false);
        }
      }
    }
  }, [maxRetries, initialDelayMs]);

  const retry = useCallback(() => {
    execute();
  }, [execute]);

  return { data, error, isLoading, retryCount, retry };
}
