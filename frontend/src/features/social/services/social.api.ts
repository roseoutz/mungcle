import { apiClient } from '../../../shared/utils/api';
import type { GreetingResponse, MessageResponse } from '../types/social.types';

export async function createGreeting(
  senderDogId: number,
  receiverWalkId: number,
): Promise<GreetingResponse> {
  return apiClient<GreetingResponse>('/api/greetings', {
    method: 'POST',
    body: JSON.stringify({ senderDogId, receiverWalkId }),
  });
}

export async function listGreetings(
  status?: string,
  direction?: string,
): Promise<GreetingResponse[]> {
  const params = new URLSearchParams();
  if (status) params.set('status', status);
  if (direction) params.set('direction', direction);
  const query = params.toString();
  return apiClient<GreetingResponse[]>(`/api/greetings${query ? `?${query}` : ''}`);
}

export async function getGreeting(greetingId: number): Promise<GreetingResponse> {
  return apiClient<GreetingResponse>(`/api/greetings/${greetingId}`);
}

export async function respondGreeting(
  greetingId: number,
  accept: boolean,
): Promise<GreetingResponse> {
  return apiClient<GreetingResponse>(`/api/greetings/${greetingId}/respond`, {
    method: 'POST',
    body: JSON.stringify({ accept }),
  });
}

export async function sendMessage(greetingId: number, body: string): Promise<MessageResponse> {
  return apiClient<MessageResponse>(`/api/greetings/${greetingId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ body }),
  });
}

export async function listMessages(greetingId: number): Promise<MessageResponse[]> {
  return apiClient<MessageResponse[]>(`/api/greetings/${greetingId}/messages`);
}
