export type GreetingStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED' | 'EXPIRED';

export interface GreetingResponse {
  id: number;
  senderUserId: number;
  receiverUserId: number;
  senderDogId: number;
  receiverWalkId: number;
  status: GreetingStatus;
  createdAt: number;
  expiresAt: number;
}

export interface MessageResponse {
  id: number;
  greetingId: number;
  senderUserId: number;
  body: string;
  createdAt: number;
}
