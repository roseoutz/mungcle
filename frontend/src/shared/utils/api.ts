import * as SecureStore from 'expo-secure-store';

const API_BASE = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:4000';
const API_VERSION_PREFIX = '/v1';

export async function apiClient<T>(path: string, options?: RequestInit): Promise<T> {
  const token = await SecureStore.getItemAsync('accessToken');
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
    'Mungcle-Version': new Date().toISOString().slice(0, 10),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options?.headers,
  };
  const response = await fetch(`${API_BASE}${API_VERSION_PREFIX}${path}`, { ...options, headers });
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new ApiError(response.status, error.code ?? 'UNKNOWN', error.message ?? 'Request failed');
  }
  if (response.status === 204) return undefined as T;
  return response.json();
}

export class ApiError extends Error {
  constructor(
    public statusCode: number,
    public code: string,
    message: string,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}
