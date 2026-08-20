export interface Venue {
  id: string;
  name: string;
  address: string;
  city: string;
  capacity: number;
}

export interface TicketCategory {
  id: string;
  concertId: string;
  name: string;
  type: 'STANDING' | 'SEATED';
  price: number;
  totalQuantity: number;
  availableQuantity: number;
}

export interface Concert {
  id: string;
  venue: Venue;
  name: string;
  eventDate: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED';
  stageLayout?: string;
  ticketCategories: TicketCategory[];
}

export interface Seat {
  id: string;
  ticketCategoryId: string;
  seatNumber: string;
  status: 'AVAILABLE' | 'HELD' | 'BOOKED';
}
