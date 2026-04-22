export interface BlockInfo {
  blockedUserId: number;
  blockedNickname: string;
  createdAt: number;
}

export interface UserDetailResponse {
  id: number;
  nickname: string;
  neighborhood: string;
  profilePhotoUrl: string;
  email?: string;
}

export interface UserResponse {
  id: number;
  nickname: string;
  neighborhood: string;
  profilePhotoUrl: string;
}

export interface UpdateUserRequest {
  nickname?: string;
  neighborhood?: string;
  profilePhotoUrl?: string;
}
