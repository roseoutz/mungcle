import { useCallback, useState } from 'react';
import { ApiError } from '../../../shared/utils/api';
import { createGreeting, respondGreeting } from '../services/social.api';
import type { GreetingResponse } from '../types/social.types';

interface GreetingState {
  loading: boolean;
  error: ApiError | null;
  data: GreetingResponse | null;
}

// 인사 전송 및 응답 상태를 관리하는 훅
export function useGreeting() {
  const [state, setState] = useState<GreetingState>({
    loading: false,
    error: null,
    data: null,
  });

  const send = useCallback(async (senderDogId: number, receiverWalkId: number) => {
    setState({ loading: true, error: null, data: null });
    try {
      const data = await createGreeting(senderDogId, receiverWalkId);
      setState({ loading: false, error: null, data });
      return data;
    } catch (err) {
      const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
      setState({ loading: false, error, data: null });
      return null;
    }
  }, []);

  const respond = useCallback(async (greetingId: number, accept: boolean) => {
    setState({ loading: true, error: null, data: null });
    try {
      const data = await respondGreeting(greetingId, accept);
      setState({ loading: false, error: null, data });
      return data;
    } catch (err) {
      const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
      setState({ loading: false, error, data: null });
      return null;
    }
  }, []);

  return { ...state, send, respond };
}
