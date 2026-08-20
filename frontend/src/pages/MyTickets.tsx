import { Link } from 'react-router-dom';
import { Button } from '@/components/Button';
import { useQuery } from '@tanstack/react-query';
import { bookingApi } from '@/features/booking/api';
import { concertApi } from '@/features/concerts/api';
import { Ticket, Calendar, Clock, CreditCard } from 'lucide-react';
import dayjs from 'dayjs';

function TicketCard({ booking }: { booking: any }) {
  const { data: concert, isLoading } = useQuery({
    queryKey: ['concert', booking.concertId],
    queryFn: () => concertApi.getConcertById(booking.concertId),
    enabled: !!booking.concertId
  });

  if (isLoading) return <div className="bg-surface p-4 rounded-xl border border-border animate-pulse h-32"></div>;
  if (!concert) return null;

  return (
    <div className="bg-surface border border-border rounded-xl p-6 flex flex-col md:flex-row gap-6 relative overflow-hidden">
      {booking.status === 'COMPLETED' && (
        <div className="absolute top-0 right-0 bg-green-500/20 text-green-400 px-4 py-1 rounded-bl-xl font-bold text-xs">
          ĐÃ THANH TOÁN
        </div>
      )}
      {booking.status === 'PENDING' && (
        <div className="absolute top-0 right-0 bg-yellow-500/20 text-yellow-400 px-4 py-1 rounded-bl-xl font-bold text-xs">
          CHỜ THANH TOÁN
        </div>
      )}
      {booking.status === 'CANCELLED' && (
        <div className="absolute top-0 right-0 bg-red-500/20 text-red-400 px-4 py-1 rounded-bl-xl font-bold text-xs">
          ĐÃ HỦY
        </div>
      )}

      <div className="flex-1 space-y-4">
        <h3 className="text-2xl font-bold text-white">{concert.name}</h3>
        
        <div className="grid grid-cols-2 gap-4 text-sm text-textSecondary mb-4">
          <div className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-primary" />
            <span>{dayjs(concert.eventDate).format('DD/MM/YYYY')}</span>
          </div>
          <div className="flex items-center gap-2">
            <Clock className="w-4 h-4 text-primary" />
            <span>{dayjs(concert.eventDate).format('HH:mm')}</span>
          </div>
          <div className="flex items-center gap-2 col-span-2">
            <CreditCard className="w-4 h-4 text-primary" />
            <span>Tổng tiền: <strong className="text-white">{booking.totalAmount.toLocaleString('vi-VN')} đ</strong></span>
          </div>
        </div>

        {booking.items && booking.items.length > 0 && (
          <div className="mt-4">
            <p className="text-xs font-bold text-textSecondary mb-2 uppercase">Chi tiết vé</p>
            <div className="space-y-2 max-h-32 overflow-y-auto pr-2">
              {booking.items.map((item: any) => (
                <div key={item.id} className="flex justify-between items-center text-sm border border-border rounded-lg p-2 bg-background">
                  <div>
                    <span className="font-bold text-primary">{item.categoryName}</span>
                    {item.seatNumber ? (
                      <span className="text-white ml-2">- Ghế {item.seatNumber}</span>
                    ) : (
                      <span className="text-textSecondary ml-2">x {item.quantity} (Vé đứng)</span>
                    )}
                  </div>
                  <span className="font-bold text-white">{(item.price * item.quantity).toLocaleString('vi-VN')} đ</span>
                </div>
              ))}
            </div>
          </div>
        )}
        
        <div className="mt-4 pt-4 border-t border-border border-dashed">
          <p className="text-xs text-textSecondary">Mã đơn hàng: <span className="font-mono text-white">{booking.id}</span></p>
          <p className="text-xs text-textSecondary">Ngày tạo: {dayjs(booking.createdAt).format('DD/MM/YYYY HH:mm')}</p>
        </div>
      </div>
      
      {booking.status === 'COMPLETED' && (
        <div className="flex flex-col justify-center items-center p-4 bg-background rounded-lg border border-border border-dashed min-w-[150px]">
          <Ticket className="w-10 h-10 text-primary mb-2 opacity-50" />
          <span className="text-xs text-textSecondary text-center">Mã QR sẽ hiển thị ở đây trước sự kiện 24h</span>
        </div>
      )}
      {booking.status === 'PENDING' && (
        <div className="flex flex-col justify-center items-center p-4 bg-background rounded-lg border border-primary/30 min-w-[150px]">
          <Link to={"/checkout/" + booking.id}>
            <Button className="w-full">Thanh Toán Ngay</Button>
          </Link>
          <span className="text-xs text-textSecondary text-center mt-2">Đơn sẽ bị hủy sau 5 phút nếu không thanh toán</span>
        </div>
      )}
    </div>
  );
}

export function MyTickets() {
  const { data: bookings, isLoading } = useQuery({
    queryKey: ['my-bookings'],
    queryFn: bookingApi.getMyBookings
  });

  return (
    <div className="container mx-auto px-4 py-10 max-w-4xl">
      <h1 className="text-3xl font-bold mb-8 text-white">Vé Của Tôi</h1>
      
      {isLoading ? (
        <div className="text-center py-20 text-textSecondary">Đang tải danh sách vé...</div>
      ) : bookings?.length === 0 ? (
        <div className="text-center py-20 bg-surface rounded-xl border border-border">
          <Ticket className="w-12 h-12 text-textSecondary mx-auto mb-4 opacity-50" />
          <p className="text-lg text-textSecondary">Bạn chưa có vé nào.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {bookings?.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()).map(booking => (
            <TicketCard key={booking.id} booking={booking} />
          ))}
        </div>
      )}
    </div>
  );
}
