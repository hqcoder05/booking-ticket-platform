import { api } from '@/services/api';
import type { ApiResponse } from '@/services/api';
import type { Concert } from '../concerts/types';

export interface Venue {
  id: string;
  name: string;
  address: string;
  city: string;
  capacity: number;
}

export interface ConcertCreateRequest {
  venueId: string;
  name: string;
  eventDate: string;
}

export interface TicketCategoryCreateRequest {
  name: string;
  price: number;
  totalQuantity: number;
  type: string;
}

export const adminApi = {
  getAllConcerts: async (): Promise<Concert[]> => {
    const response = await api.get<ApiResponse<Concert[]>>('/operation/concerts');
    return response.data.result;
  },
  
  createConcert: async (request: ConcertCreateRequest): Promise<Concert> => {
    const response = await api.post<ApiResponse<Concert>>('/operation/concerts', request);
    return response.data.result;
  },

  updateConcert: async (id: string, request: ConcertCreateRequest): Promise<Concert> => {
    const response = await api.put<ApiResponse<Concert>>(`/operation/concerts/${id}`, request);
    return response.data.result;
  },

  publishConcert: async (id: string): Promise<Concert> => {
    const response = await api.put<ApiResponse<Concert>>(`/operation/concerts/${id}/publish`);
    return response.data.result;
  },

  cancelConcert: async (id: string): Promise<Concert> => {
    const response = await api.put<ApiResponse<Concert>>(`/operation/concerts/${id}/cancel`);
    return response.data.result;
  },

  deleteConcert: async (id: string): Promise<void> => {
    await api.delete(`/operation/concerts/${id}`);
  },

  addTicketCategory: async (concertId: string, request: TicketCategoryCreateRequest): Promise<any> => {
    const response = await api.post<ApiResponse<any>>(`/operation/concerts/${concertId}/ticket-categories`, request);
    return response.data.result;
  },

  updateTicketCategory: async (concertId: string, categoryId: string, request: TicketCategoryCreateRequest): Promise<any> => {
    const response = await api.put<ApiResponse<any>>(`/operation/concerts/${concertId}/ticket-categories/${categoryId}`, request);
    return response.data.result;
  },

  deleteTicketCategory: async (concertId: string, categoryId: string): Promise<void> => {
    await api.delete(`/operation/concerts/${concertId}/ticket-categories/${categoryId}`);
  },

  getAllVenues: async (): Promise<Venue[]> => {
    const response = await api.get<ApiResponse<Venue[]>>('/operation/venues');
    return response.data.result;
  },

  createVenue: async (request: Omit<Venue, 'id'>): Promise<Venue> => {
    const response = await api.post<ApiResponse<Venue>>('/operation/venues', request);
    return response.data.result;
  },

  deleteVenue: async (id: string): Promise<void> => {
    await api.delete(`/operation/venues/${id}`);
  }
};
