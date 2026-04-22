import { apiClient } from '../../../shared/utils/api';
import type { GreetingResponse } from '../types/social.types';

export async function createGreeting(
  senderDogId: number,
  receiverWalkId: number,
): Promise<GreetingResponse> {
  return apiClient<GreetingResponse>('/api/greetings', {
    method: 'POST',
    body: JSON.stringify({ senderDogId, receiverWalkId }),
  });
}

export async function respondGreeting(
  greetingId: number,
  accept: boolean,
): Promise<GreetingResponse> {
  return apiClient<GreetingResponse>(`/api/greetings/${greetingId}/respond`, {
    method: 'PATCH',
    body: JSON.stringify({ accept }),
  });
}
