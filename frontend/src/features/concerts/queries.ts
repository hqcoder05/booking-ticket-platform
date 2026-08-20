import { useQuery } from '@tanstack/react-query';
import { concertApi } from './api';
import type { Concert, Seat } from './types';

export const useConcerts = () => {
  return useQuery<Concert[]>({
    queryKey: ['concerts'],
    queryFn: concertApi.getPublishedConcerts,
  });
};

export const useConcertDetail = (id: string) => {
  return useQuery<Concert>({
    queryKey: ['concert', id],
    queryFn: () => concertApi.getConcertById(id),
    enabled: !!id,
  });
};

export const useConcertSeats = (id: string) => {
  return useQuery<Seat[]>({
    queryKey: ['concert', id, 'seats'],
    queryFn: () => concertApi.getConcertSeats(id),
    enabled: !!id,
    refetchInterval: 5000, // Real-time polling every 5s
  });
};
