export type WalkType = 'OPEN' | 'SOLO';
export type WalkStatus = 'ACTIVE' | 'ENDED';

export interface WalkResponse {
  id: number;
  dogId: number;
  userId: number;
  type: string;
  gridCell: string;
  status: string;
  startedAt: number;
  endsAt: number;
}

export interface NearbyWalkCard {
  walkId: number;
  dog: {
    id: number;
    name: string;
    breed: string;
    size: string;
    temperaments: string[];
    sociability: number;
    photoUrl?: string;
    vaccinationRegistered: boolean;
  };
  owner: {
    id: number;
    nickname: string;
    neighborhood: string;
    profilePhotoUrl?: string;
  };
  gridDistance: number;
  startedAt: number;
}

export interface PatternResponse {
  dogId: number;
  typicalHour: number;
  countLast14Days: number;
}
