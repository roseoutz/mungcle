import { useCallback, useState } from 'react';
import { ApiError } from '../utils/api';

interface ApiState<T> {
  data: T | null;
  loading: boolean;
  error: ApiError | null;
}

interface UseApiReturn<T, Args extends unknown[]> extends ApiState<T> {
  execute: (...args: Args) => Promise<T | null>;
  reset: () => void;
}

// API 호출 상태를 관리하는 훅 (loading, error, data)
export function useApi<T, Args extends unknown[]>(
  fn: (...args: Args) => Promise<T>,
): UseApiReturn<T, Args> {
  const [state, setState] = useState<ApiState<T>>({
    data: null,
    loading: false,
    error: null,
  });

  const execute = useCallback(
    async (...args: Args): Promise<T | null> => {
      setState({ data: null, loading: true, error: null });
      try {
        const data = await fn(...args);
        setState({ data, loading: false, error: null });
        return data;
      } catch (err) {
        const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
        setState({ data: null, loading: false, error });
        return null;
      }
    },
    [fn],
  );

  const reset = useCallback(() => {
    setState({ data: null, loading: false, error: null });
  }, []);

  return { ...state, execute, reset };
}
