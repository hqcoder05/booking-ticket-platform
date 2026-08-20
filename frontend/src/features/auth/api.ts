import { api } from '@/services/api';
import type { ApiResponse } from '@/services/api';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: {
    id: string;
    email: string;
    role: string;
  };
}

export const authApi = {
  login: async (request: any): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/login', request);
    return response.data.result;
  },
  register: async (request: any): Promise<AuthResponse> => {
    const response = await api.post<ApiResponse<AuthResponse>>('/auth/register', request);
    return response.data.result;
  }
};
