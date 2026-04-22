import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../../../shared/utils/api';
import { getNearbyPatterns, getNearbyWalks } from '../services/walks.api';
import type { NearbyWalkCard, PatternResponse } from '../types/walks.types';

const REFRESH_INTERVAL_MS = 30_000;

interface NearbyWalksState {
  walks: NearbyWalkCard[];
  patterns: PatternResponse[];
  loading: boolean;
  error: ApiError | null;
}

// 주변 산책 목록을 30초마다 자동 새로고침하는 훅
export function useNearbyWalks(lat: number | null, lng: number | null) {
  const [state, setState] = useState<NearbyWalksState>({
    walks: [],
    patterns: [],
    loading: false,
    error: null,
  });
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const fetch = useCallback(async () => {
    if (lat === null || lng === null) return;
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const [walksResult, patternsResult] = await Promise.all([
        getNearbyWalks(lat, lng),
        getNearbyPatterns(lat, lng),
      ]);
      setState({
        walks: walksResult.walks,
        patterns: patternsResult.patterns,
        loading: false,
        error: null,
      });
    } catch (err) {
      const error = err instanceof ApiError ? err : new ApiError(0, 'UNKNOWN', String(err));
      setState((prev) => ({ ...prev, loading: false, error }));
    }
  }, [lat, lng]);

  useEffect(() => {
    fetch();
    intervalRef.current = setInterval(fetch, REFRESH_INTERVAL_MS);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [fetch]);

  return { ...state, refresh: fetch };
}
