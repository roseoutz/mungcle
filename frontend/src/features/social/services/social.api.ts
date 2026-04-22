import { apiClient } from '../../../shared/utils/api';

export interface GreetingResponse {
  id: number;
  senderId: number;
  receiverId: number;
  status: 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED';
  expiresAt: number;
  createdAt: number;
}

export interface MessageResponse {
  id: number;
  greetingId: number;
  senderId: number;
  body: string;
  createdAt: number;
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
