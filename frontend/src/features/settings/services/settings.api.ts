import { apiClient } from '../../../shared/utils/api';
import type {
  BlockInfo,
  UpdateUserRequest,
  UserDetailResponse,
  UserResponse,
} from '../types/settings.types';

export async function getMyProfile(): Promise<UserDetailResponse> {
  return apiClient<UserDetailResponse>('/api/users/me');
}

export async function updateProfile(
  data: Partial<UpdateUserRequest>,
): Promise<UserResponse> {
  return apiClient<UserResponse>('/api/users/me', {
    method: 'PATCH',
    body: JSON.stringify(data),
  });
}

export async function deleteAccount(): Promise<void> {
  return apiClient<void>('/api/users/me', { method: 'DELETE' });
}

export async function listBlocks(): Promise<{ blocks: BlockInfo[] }> {
  return apiClient<{ blocks: BlockInfo[] }>('/api/blocks');
}

export async function unblock(userId: number): Promise<void> {
  return apiClient<void>(`/api/blocks/${userId}`, { method: 'DELETE' });
}

export async function createReport(reportedId: number, reason: string): Promise<void> {
  return apiClient<void>('/api/reports', {
    method: 'POST',
    body: JSON.stringify({ reportedId, reason }),
  });
}
