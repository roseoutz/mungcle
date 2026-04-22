export type DogSize = 'SMALL' | 'MEDIUM' | 'LARGE';

export type DogTemperament =
  | 'FRIENDLY'
  | 'PLAYFUL'
  | 'CALM'
  | 'SHY'
  | 'PROTECTIVE'
  | 'ENERGETIC';

export interface Dog {
  id: number;
  name: string;
  breed: string;
  size: DogSize;
  temperaments: DogTemperament[];
  socialScore: number; // 1~5
  profilePhotoUrl: string | null;
}

export interface CreateDogParams {
  name: string;
  breed: string;
  size: DogSize;
  temperaments: DogTemperament[];
  socialScore: number;
  profilePhoto?: string; // base64 or URI
}
