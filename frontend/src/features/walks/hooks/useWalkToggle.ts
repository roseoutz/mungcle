import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../../../shared/utils/api';
import { getMyActiveWalks, startWalk, stopWalk } from '../services/walks.api';
import type { WalkResponse } from '../types/walks.types';

type WalkToggleState = 'OFF' | 'OPEN' | 'ACTIVE';

interface WalkToggleData {
  state: WalkToggleState;
  activeWalk: WalkResponse | null;
  secondsRemaining: number;
  loading: boolean;
  error: ApiError | null;
}

const WALK_DURATION_SECONDS = 60 * 60; // 60분

// 산책 시작/종료 토글 상태를 관리하는 훅
export function useWalkToggle(dogId: number | null) {
  const [data, setData] = useState<WalkToggleData>({
    state: 'OFF',
    activeWalk: null,
    secondsRemaining: WALK_DURATION_SECONDS,
    loading: false,
    error: null,
  });
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  // 기존 활성 산책 복원
  useEffect(() => {
    async function restoreActiveWalk() {
      try {
        const walks = await getMyActiveWalks();
        if (walks.length > 0) {
          const walk = walks[0];
          const now = Date.now() / 1000;
          const remaining = Math.max(0, walk.endsAt - now);
          setData({
            state: walk.type === 'OPEN' ? 'OPEN' : 'ACTIVE',
            activeWalk: walk,
            secondsRemaining: Math.floor(remaining),
            loading: false,
            error: null,
          });
        }
      } catch {
        // 복원 실패 시 무시 — OFF 상태 유지
      }
    }
    restoreActiveWalk();
  }, []);

  // 카운트다운 타이머
  useEffect(() => {
    if (data.state !== 'OFF' && data.activeWalk) {
      timerRef.current = setInterval(() => {
        setData((prev) => {
          const next = prev.secondsRemaining - 1;
          if (next <= 0) {
            if (timerRef.current) clearInterval(timerRef.current);
            return { ...prev, state: 'OFF', activeWalk: null, secondsRemaining: WALK_DURATION_SECONDS };
          }
          return { ...prev, secondsRemaining: next };
        });
      }, 1000);
    } else {
      if (timerRef.current) clearInterval(timerRef.current);
    }
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [data.state, data.activeWalk]);

  const toggle = useCallback(
    async (lat: number, lng: number) => {
      if (!dogId) return;

      if (data.state === 'OFF') {
        // OFF → OPEN
        setData((prev) => ({ ...prev, loading: true, error: null }));
        try {
          const walk = await startWalk(dogId, lat, lng, true);
          setData({
            state: 'OPEN',
            activeWalk: walk,
            secondsRemaining: WALK_DURATION_SECONDS,
            loading: false,
            error: null,
          });
        } catch (err) {
          const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
          setData((prev) => ({ ...prev, loading: false, error }));
        }
      } else if (data.state === 'OPEN' || data.state === 'ACTIVE') {
        // OPEN/ACTIVE → OFF
        if (!data.activeWalk) return;
        setData((prev) => ({ ...prev, loading: true, error: null }));
        try {
          await stopWalk(data.activeWalk.id);
          setData({
            state: 'OFF',
            activeWalk: null,
            secondsRemaining: WALK_DURATION_SECONDS,
            loading: false,
            error: null,
          });
        } catch (err) {
          const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
          setData((prev) => ({ ...prev, loading: false, error }));
        }
      }
    },
    [dogId, data.state, data.activeWalk],
  );

  return { ...data, toggle };
}
