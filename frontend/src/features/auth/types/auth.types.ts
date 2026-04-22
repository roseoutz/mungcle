export type SocialProvider = 'KAKAO' | 'NAVER' | 'APPLE' | 'GOOGLE';

export interface AuthResponse {
  accessToken: string;
  user: UserInfo;
}

export interface UserInfo {
  id: number;
  nickname: string;
  neighborhood: string;
  profilePhotoUrl: string;
}

export interface LoginEmailParams {
  email: string;
  password: string;
}

export interface RegisterEmailParams {
  email: string;
  password: string;
  nickname: string;
}
