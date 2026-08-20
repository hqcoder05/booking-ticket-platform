import { api } from '@/services/api';
import type { ApiResponse } from '@/services/api';
import type { Concert, Seat } from './types';

export const concertApi = {
  getPublishedConcerts: async (): Promise<Concert[]> => {
    const response = await api.get<ApiResponse<Concert[]>>('/customer/concerts');
    return response.data.result;
  },

  getConcertById: async (id: string): Promise<Concert> => {
    const response = await api.get<ApiResponse<Concert>>(`/customer/concerts/${id}`);
    return response.data.result;
  },

  getConcertSeats: async (id: string): Promise<Seat[]> => {
    const response = await api.get<ApiResponse<Seat[]>>(`/customer/concerts/${id}/seats`);
    return response.data.result;
  }
};
