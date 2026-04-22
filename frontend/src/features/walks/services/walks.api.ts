import { apiClient } from '../../../shared/utils/api';
import type { NearbyWalkCard, PatternResponse, WalkResponse } from '../types/walks.types';

// 200m 그리드 스냅 — GPS 좌표를 직접 보내지 않고 gridCell로 변환
function toGridCell(lat: number, lng: number): string {
  const latSnap = Math.round(lat / 0.0018) * 0.0018;
  const lngSnap = Math.round(lng / 0.0025) * 0.0025;
  return `${latSnap.toFixed(4)}_${lngSnap.toFixed(4)}`;
}

export async function startWalk(
  dogId: number,
  lat: number,
  lng: number,
  open: boolean,
): Promise<WalkResponse> {
  return apiClient<WalkResponse>('/api/walks', {
    method: 'POST',
    body: JSON.stringify({
      dogId,
      gridCell: toGridCell(lat, lng),
      type: open ? 'OPEN' : 'SOLO',
    }),
  });
}

export async function stopWalk(walkId: number): Promise<WalkResponse> {
  return apiClient<WalkResponse>(`/api/walks/${walkId}`, {
    method: 'PATCH',
    body: JSON.stringify({ status: 'ENDED' }),
  });
}

export async function getNearbyWalks(
  lat: number,
  lng: number,
): Promise<{ walks: NearbyWalkCard[] }> {
  const gridCell = toGridCell(lat, lng);
  return apiClient<{ walks: NearbyWalkCard[] }>(
    `/api/walks/nearby?gridCell=${encodeURIComponent(gridCell)}`,
  );
}

export async function getMyActiveWalks(): Promise<WalkResponse[]> {
  return apiClient<WalkResponse[]>('/api/walks/me?status=ACTIVE');
}

export async function getNearbyPatterns(
  lat: number,
  lng: number,
): Promise<{ patterns: PatternResponse[] }> {
  const gridCell = toGridCell(lat, lng);
  return apiClient<{ patterns: PatternResponse[] }>(
    `/api/walks/patterns?gridCell=${encodeURIComponent(gridCell)}`,
  );
}
