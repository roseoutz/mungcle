import { apiClient } from '../../../shared/utils/api';
import type { CreateDogParams, Dog } from '../types/dogs.types';

export async function getMyDogs(): Promise<Dog[]> {
  return apiClient<Dog[]>('/api/dogs');
}

export async function createDog(params: CreateDogParams): Promise<Dog> {
  return apiClient<Dog>('/api/dogs', {
    method: 'POST',
    body: JSON.stringify(params),
  });
}

export async function getDog(id: number): Promise<Dog> {
  return apiClient<Dog>(`/api/dogs/${id}`);
}
