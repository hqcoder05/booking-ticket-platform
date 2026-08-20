import { api } from '@/services/api';
import type { ApiResponse } from '@/services/api';
import type { BookingRequest, BookingResponse, PaymentInitiateRequest, PaymentResponse } from './types';

export const bookingApi = {
  createBooking: async (request: BookingRequest, idempotencyKey: string): Promise<BookingResponse> => {
    const response = await api.post<ApiResponse<BookingResponse>>('/customer/bookings', request, {
      headers: {
        'Idempotency-Key': idempotencyKey
      }
    });
    return response.data.result;
  },

  getMyBookings: async (): Promise<BookingResponse[]> => {
    const response = await api.get<ApiResponse<BookingResponse[]>>('/customer/bookings');
    return response.data.result;
  },

  initiatePayment: async (request: PaymentInitiateRequest): Promise<PaymentResponse> => {
    const response = await api.post<ApiResponse<PaymentResponse>>('/customer/payments/initiate', request);
    return response.data.result;
  },

  completePayment: async (paymentId: string): Promise<PaymentResponse> => {
    const response = await api.put<ApiResponse<PaymentResponse>>(`/customer/payments/${paymentId}/complete`);
    return response.data.result;
  }
};
