export interface StandingTicketRequest {
  ticketCategoryId: string;
  quantity: number;
}

export interface BookingRequest {
  concertId: string;
  idempotencyKey: string;
  voucherCode?: string;
  standingTickets?: StandingTicketRequest[];
  seatIds?: string[];
}

export interface BookingResponse {
  id: string;
  userId: string;
  concertId: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'REFUNDED';
  totalAmount: number;
  createdAt: string;
  items?: any[];
}

export interface PaymentInitiateRequest {
  bookingId: string;
  method: 'CREDIT_CARD' | 'MOMO' | 'BANK_TRANSFER';
}

export interface PaymentResponse {
  id: string;
  bookingId: string;
  method: string;
  status: string;
}
