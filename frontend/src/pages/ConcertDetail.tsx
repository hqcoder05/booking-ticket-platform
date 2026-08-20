import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { SeatMap } from '@/features/seats/SeatMap';
import { Button } from '@/components/Button';
import { Calendar, MapPin, Clock, Info } from 'lucide-react';
import { motion } from 'framer-motion';
import { useConcertDetail, useConcertSeats } from '@/features/concerts/queries';
import { bookingApi } from '@/features/booking/api';
import dayjs from 'dayjs';
import toast from 'react-hot-toast';

export function ConcertDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [ticketType, setTicketType] = useState<'RESERVED' | 'STANDING'>('RESERVED');
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);
  const [standingQuantities, setStandingQuantities] = useState<Record<string, number>>({});
  const [isBooking, setIsBooking] = useState(false);
  
  const { data: concert, isLoading: concertLoading } = useConcertDetail(id!);
  const { data: seats, isLoading: seatsLoading } = useConcertSeats(id!);

  // Automatically switch to STANDING if there are no SEATED categories
  useEffect(() => {
    if (concert) {
      const hasSeated = concert.ticketCategories.some(c => c.type === 'SEATED');
      if (!hasSeated) {
        setTicketType('STANDING');
      }
    }
  }, [concert]);

  useEffect(() => {
    if (seats && seats.length > 0) {
      const validSelected = selectedSeatIds.filter(seatId => {
        const seat = seats.find(s => s.id === seatId);
        return seat && seat.status === 'AVAILABLE';
      });
      if (validSelected.length !== selectedSeatIds.length) {
        setSelectedSeatIds(validSelected);
        toast.error('Một số ghế bạn chọn đã bị người khác đặt mất. Vui lòng chọn lại.');
      }
    }
  }, [seats]);

  if (concertLoading) return <div className="p-20 text-center text-textSecondary">Đang tải thông tin...</div>;
  if (!concert) return <div className="p-20 text-center text-error">Không tìm thấy sự kiện.</div>;

  // Enhance seats with category details
  const enhancedSeats = seats?.map(seat => {
    const category = concert.ticketCategories.find(c => c.id === seat.ticketCategoryId);
    
    // SeatNumber typically looks like "A1", we can parse row and number
    const match = seat.seatNumber.match(/([A-Z]+)(\d+)/);
    const row = match ? match[1] : 'A';
    const num = match ? parseInt(match[2]) : 1;

    return {
      id: seat.id,
      ticketCategoryId: category?.id || '',
      row,
      number: num,
      status: seat.status,
      type: category?.name || 'STANDARD',
      price: category?.price || 0
    };
  }) || [];

  const handleToggleSeat = (seatId: string) => {
    setSelectedSeatIds(prev => 
      prev.includes(seatId) ? prev.filter(id => id !== seatId) : [...prev, seatId]
    );
  };

  const selectedSeatDetails = enhancedSeats.filter(s => selectedSeatIds.includes(s.id));
  const standingCategories = concert.ticketCategories.filter(c => c.type === 'STANDING');
  
  const selectedSeatsSubtotal = selectedSeatDetails.reduce((sum, seat) => sum + seat.price, 0);
  const standingSubtotal = standingCategories.reduce((sum, cat) => sum + (cat.price * (standingQuantities[cat.id] || 0)), 0);
  const subtotal = selectedSeatsSubtotal + standingSubtotal;

  const hasStanding = standingCategories.length > 0;

  const handleCheckoutClick = async () => {
    const totalStandingTickets = Object.values(standingQuantities).reduce((a, b) => a + b, 0);
    if (selectedSeatIds.length === 0 && totalStandingTickets === 0) {
      toast.error('Vui lòng chọn ít nhất 1 vé để đặt!');
      return;
    }
    const token = localStorage.getItem('accessToken');
    if (!token) {
      toast.error('Vui lòng đăng nhập để đặt vé!');
      navigate('/login');
      return;
    }

    setIsBooking(true);
    try {
      const standingTickets = Object.entries(standingQuantities)
        .filter(([_, quantity]) => quantity > 0)
        .map(([ticketCategoryId, quantity]) => ({
          ticketCategoryId,
          quantity
        }));
      
      const idempotencyKey = crypto.randomUUID();
      const booking = await bookingApi.createBooking({
        concertId: concert.id,
        idempotencyKey,
        seatIds: selectedSeatIds,
        standingTickets
      }, idempotencyKey);
      
      toast.success('Đã giữ vé! Vui lòng thanh toán.');
      navigate(`/checkout/${booking.id}`);
    } catch (error: any) {
      toast.error('Đặt vé thất bại: ' + (error.response?.data?.message || error.message));
    } finally {
      setIsBooking(false);
    }
  };

  return (
    <div className="flex-1 flex flex-col pb-20">
      {/* Detail Hero */}
      <div className="h-[400px] relative">
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/60 to-transparent z-10" />
        <div className="absolute inset-0 bg-gradient-to-br from-[#A50064]/30 via-[#FF4081]/10 to-background" />
        <div className="absolute top-1/4 right-1/4 w-96 h-96 bg-primary/20 rounded-full blur-[100px] mix-blend-screen" />
        <div className="absolute bottom-0 left-0 w-full z-20">
          <div className="container mx-auto px-4 pb-12">
            <h1 className="text-4xl md:text-6xl font-bold mb-4">{concert.name}</h1>
            <div className="flex flex-wrap items-center gap-6 text-textSecondary text-lg">
              <div className="flex items-center gap-2"><Calendar className="text-primary w-5 h-5" /> {dayjs(concert.eventDate).format('dddd, DD/MM/YYYY')}</div>
              <div className="flex items-center gap-2"><Clock className="text-primary w-5 h-5" /> {dayjs(concert.eventDate).format('HH:mm')}</div>
              <div className="flex items-center gap-2"><MapPin className="text-primary w-5 h-5" /> {concert.venue.name}, {concert.venue.city}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="container mx-auto px-4 mt-8">
        <div className="flex flex-col lg:flex-row gap-8 relative">
          
          {/* Main Content Area */}
          <div className="flex-1 min-w-0">
            <div className="mb-8 flex gap-4 border-b border-border">
              <button 
                className={`pb-4 px-2 font-bold transition-colors ${ticketType === 'RESERVED' ? 'text-primary border-b-2 border-primary' : 'text-textSecondary hover:text-white'}`}
                onClick={() => setTicketType('RESERVED')}
              >
                Ghế ngồi (Reserved)
              </button>
              {hasStanding && (
                <button 
                  className={`pb-4 px-2 font-bold transition-colors ${ticketType === 'STANDING' ? 'text-primary border-b-2 border-primary' : 'text-textSecondary hover:text-white'}`}
                  onClick={() => setTicketType('STANDING')}
                >
                  Khu vực đứng (Free Standing)
                </button>
              )}
            </div>

            <motion.div
              key={ticketType}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="min-h-[500px]"
            >
              {ticketType === 'RESERVED' ? (
                <div>
                  <h3 className="text-xl font-bold mb-6">Vui lòng chọn ghế</h3>
                  {seatsLoading ? <p>Đang tải sơ đồ ghế...</p> : (
                    <SeatMap seats={enhancedSeats} selectedSeats={selectedSeatIds} onToggleSeat={handleToggleSeat} stageLayout={concert.stageLayout} />
                  )}
                </div>
              ) : (
                <div className="bg-surface rounded-xl p-8 border border-border">
                  <div className="flex items-start gap-4 mb-8 bg-surfaceElevated p-4 rounded-lg border border-primary/20">
                    <Info className="w-6 h-6 text-primary shrink-0" />
                    <div>
                      <h4 className="font-bold text-white">Khu vực đứng tự do</h4>
                      <p className="text-sm text-textSecondary mt-1">Đây là khu vực không có ghế cố định. Hãy đến sớm để chọn được vị trí tốt nhất.</p>
                    </div>
                  </div>

                  {standingCategories.map(cat => (
                    <div key={cat.id} className="flex justify-between items-center p-6 border border-border rounded-lg bg-background mb-4">
                      <div>
                        <h3 className="text-xl font-bold text-white mb-1">{cat.name}</h3>
                        <p className="text-primary font-bold">{cat.price.toLocaleString('vi-VN')} ₫</p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-textSecondary mb-3">Còn trống {cat.availableQuantity} / {cat.totalQuantity}</p>
                          <div className="flex items-center gap-4 bg-surface rounded-full px-4 py-2 border border-border">
                            <button 
                              className="text-textSecondary hover:text-white"
                              onClick={() => setStandingQuantities(prev => ({
                                ...prev,
                                [cat.id]: Math.max(0, (prev[cat.id] || 0) - 1)
                              }))}
                            >-</button>
                            <span className="font-bold w-4 text-center">{standingQuantities[cat.id] || 0}</span>
                            <button 
                              className="text-textSecondary hover:text-white"
                              onClick={() => setStandingQuantities(prev => ({
                                ...prev,
                                [cat.id]: Math.min(cat.availableQuantity, (prev[cat.id] || 0) + 1)
                              }))}
                            >+</button>
                          </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </motion.div>
          </div>

          {/* Sticky Summary */}
          <div className="lg:w-[400px] shrink-0">
            <div className="sticky top-24 glass-panel rounded-xl p-6 shadow-2xl">
              <h3 className="text-xl font-bold mb-6 border-b border-border pb-4">Tóm tắt đơn hàng</h3>
              
              {selectedSeatIds.length === 0 && Object.values(standingQuantities).reduce((a, b) => a + b, 0) === 0 ? (
                <div className="min-h-[150px] flex flex-col justify-center items-center text-textSecondary">
                  <p>Chưa có vé nào được chọn.</p>
                  <p className="text-sm mt-2">Vui lòng chọn ghế hoặc vé đứng để tiếp tục.</p>
                </div>
              ) : (
                <div>
                  <div className="space-y-4 mb-6 max-h-[300px] overflow-y-auto pr-2">
                    {selectedSeatDetails.map(seat => (
                      <div key={seat.id} className="flex justify-between items-start">
                        <div>
                          <p className="font-bold text-white">{seat.type} - Ghế {seat.row}{seat.number}</p>
                          <p className="text-xs text-textSecondary">Ghế ngồi</p>
                        </div>
                        <p className="font-bold">{seat.price.toLocaleString('vi-VN')} đ</p>
                      </div>
                    ))}
                    {standingCategories.filter(cat => standingQuantities[cat.id] > 0).map(cat => (
                      <div key={cat.id} className="flex justify-between items-start">
                        <div>
                          <p className="font-bold text-white">{cat.name} x {standingQuantities[cat.id]}</p>
                          <p className="text-xs text-textSecondary">Vé đứng tự do</p>
                        </div>
                        <p className="font-bold">{(cat.price * standingQuantities[cat.id]).toLocaleString('vi-VN')} đ</p>
                      </div>
                    ))}
                  </div>
                  
                  <div className="border-t border-border pt-4 mb-4">
                    <div className="flex justify-between text-textSecondary mb-2">
                      <span>Tạm tính</span>
                      <span>{subtotal.toLocaleString('vi-VN')} đ</span>
                    </div>
                    <div className="flex justify-between text-xl font-bold text-white mt-4 pt-4 border-t border-border">
                      <span>Tổng cộng</span>
                      <span className="text-primary">{subtotal.toLocaleString('vi-VN')} đ</span>
                    </div>
                  </div>

                </div>
              )}

              <Button 
                className="w-full text-lg h-14 rounded-xl" 
                disabled={(selectedSeatIds.length === 0 && Object.values(standingQuantities).reduce((a, b) => a + b, 0) === 0) || isBooking}
                onClick={handleCheckoutClick}
              >
                {isBooking ? 'Đang xử lý...' : 'Thanh Toán Ngay'}
              </Button>
            </div>
          </div>
          
        </div>
      </div>

    </div>
  );
}






