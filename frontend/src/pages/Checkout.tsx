import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { bookingApi } from '@/features/booking/api';
import { concertApi } from '@/features/concerts/api';
import { Button } from '@/components/Button';
import { CreditCard, Wallet, Landmark } from 'lucide-react';
import toast from 'react-hot-toast';

export function Checkout() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [paymentMethod, setPaymentMethod] = useState<'CREDIT_CARD' | 'MOMO' | 'BANK_TRANSFER'>('CREDIT_CARD');
  const [processing, setProcessing] = useState(false);

  // In a real app we'd have an endpoint to fetch a single booking.
  // Here we'll fetch all my bookings and filter.
  const { data: bookings, isLoading: bookingsLoading } = useQuery({
    queryKey: ['my-bookings'],
    queryFn: bookingApi.getMyBookings
  });

  const booking = bookings?.find(b => b.id === id);

  const { data: concert, isLoading: concertLoading } = useQuery({
    queryKey: ['concert', booking?.concertId],
    queryFn: () => concertApi.getConcertById(booking!.concertId),
    enabled: !!booking?.concertId
  });

  const handlePayment = async () => {
    if (!booking) return;
    setProcessing(true);
    try {
      // Step 1: Initiate Payment
      const paymentResponse = await bookingApi.initiatePayment({
        bookingId: booking.id,
        method: paymentMethod
      });
      
      // Step 2: Simulate complete payment (in real life this is a callback from Momo/VNPAY)
      await bookingApi.completePayment(paymentResponse.id);
      
      toast.success('Thanh toán thành công!');
      navigate('/my-tickets');
    } catch (err: any) {
      toast.error('Thanh toán thất bại: ' + (err.response?.data?.message || err.message));
    } finally {
      setProcessing(false);
    }
  };

  if (bookingsLoading) return <div className="p-20 text-center text-textSecondary">Đang tải đơn hàng...</div>;
  if (!booking) return <div className="p-20 text-center text-error">Không tìm thấy đơn đặt vé này hoặc bạn không có quyền truy cập.</div>;
  if (booking.status !== 'PENDING') return <div className="p-20 text-center text-error">Đơn đặt vé này đã thanh toán hoặc đã hủy.</div>;

  return (
    <div className="container mx-auto px-4 py-10 max-w-3xl">
      <h1 className="text-3xl font-bold mb-8 text-white">Thanh Toán</h1>
      
      <div className="grid md:grid-cols-2 gap-8">
        <div className="space-y-6">
          <div className="bg-surface border border-border rounded-xl p-6">
            <h2 className="text-xl font-bold mb-4">Phương thức thanh toán</h2>
            <div className="space-y-3">
              <label className={`flex items-center gap-3 p-4 border rounded-lg cursor-pointer transition-colors ${paymentMethod === 'CREDIT_CARD' ? 'border-primary bg-primary/10' : 'border-border hover:bg-surfaceElevated'}`}>
                <input type="radio" name="paymentMethod" value="CREDIT_CARD" checked={paymentMethod === 'CREDIT_CARD'} onChange={() => setPaymentMethod('CREDIT_CARD')} className="hidden" />
                <CreditCard className="w-6 h-6 text-primary" />
                <span className="font-medium">Thẻ Tín dụng / Ghi nợ</span>
              </label>
              <label className={`flex items-center gap-3 p-4 border rounded-lg cursor-pointer transition-colors ${paymentMethod === 'MOMO' ? 'border-primary bg-primary/10' : 'border-border hover:bg-surfaceElevated'}`}>
                <input type="radio" name="paymentMethod" value="MOMO" checked={paymentMethod === 'MOMO'} onChange={() => setPaymentMethod('MOMO')} className="hidden" />
                <Wallet className="w-6 h-6 text-[#A50064]" />
                <span className="font-medium">Ví MoMo</span>
              </label>
              <label className={`flex items-center gap-3 p-4 border rounded-lg cursor-pointer transition-colors ${paymentMethod === 'BANK_TRANSFER' ? 'border-primary bg-primary/10' : 'border-border hover:bg-surfaceElevated'}`}>
                <input type="radio" name="paymentMethod" value="BANK_TRANSFER" checked={paymentMethod === 'BANK_TRANSFER'} onChange={() => setPaymentMethod('BANK_TRANSFER')} className="hidden" />
                <Landmark className="w-6 h-6 text-blue-500" />
                <span className="font-medium">Chuyển khoản ngân hàng</span>
              </label>
            </div>

            <div className="mt-6 border-t border-border pt-6">
              {paymentMethod === 'CREDIT_CARD' && (
                <div className="space-y-4 animate-in fade-in slide-in-from-top-2">
                  <div className="space-y-1">
                    <label className="text-sm text-textSecondary">Số thẻ</label>
                    <input type="text" placeholder="0000 0000 0000 0000" className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary" />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-1">
                      <label className="text-sm text-textSecondary">Ngày hết hạn (MM/YY)</label>
                      <input type="text" placeholder="MM/YY" className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary" />
                    </div>
                    <div className="space-y-1">
                      <label className="text-sm text-textSecondary">CVV</label>
                      <input type="text" placeholder="123" className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary" />
                    </div>
                  </div>
                  <div className="space-y-1">
                    <label className="text-sm text-textSecondary">Tên in trên thẻ</label>
                    <input type="text" placeholder="NGUYEN VAN A" className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary uppercase" />
                  </div>
                </div>
              )}

              {paymentMethod === 'MOMO' && (
                <div className="flex flex-col items-center justify-center space-y-4 animate-in fade-in slide-in-from-top-2">
                  <div className="w-48 h-48 bg-white p-2 rounded-xl">
                    {/* Dummy QR code using a generic placeholder or CSS pattern */}
                    <div className="w-full h-full bg-[#A50064] rounded-lg flex items-center justify-center">
                      <span className="text-white font-bold text-center px-4">Quét mã MoMo<br/>để thanh toán</span>
                    </div>
                  </div>
                  <p className="text-sm text-textSecondary text-center">Sử dụng ứng dụng MoMo hoặc ứng dụng camera hỗ trợ QR code để quét mã.</p>
                </div>
              )}

              {paymentMethod === 'BANK_TRANSFER' && (
                <div className="space-y-4 animate-in fade-in slide-in-from-top-2">
                  <div className="bg-surfaceElevated p-4 rounded-lg border border-border space-y-2">
                    <div className="flex justify-between">
                      <span className="text-textSecondary text-sm">Ngân hàng:</span>
                      <span className="font-bold text-white">Vietcombank</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-textSecondary text-sm">Số tài khoản:</span>
                      <span className="font-bold text-primary text-lg">0123456789</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-textSecondary text-sm">Chủ tài khoản:</span>
                      <span className="font-bold text-white">CÔNG TY GEEKUP</span>
                    </div>
                    <div className="flex justify-between pt-2 border-t border-border mt-2">
                      <span className="text-textSecondary text-sm">Nội dung chuyển khoản:</span>
                      <span className="font-mono bg-background px-2 py-1 rounded text-white">{booking.id.substring(0, 8).toUpperCase()}</span>
                    </div>
                  </div>
                  <p className="text-xs text-textSecondary text-center">Vui lòng chuyển khoản đúng số tiền và nội dung để hệ thống xử lý tự động.</p>
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="space-y-6">
          <div className="bg-surface border border-border rounded-xl p-6">
            <h2 className="text-xl font-bold mb-4">Tóm tắt đơn hàng</h2>
            {concertLoading ? (
              <div className="text-sm text-textSecondary mb-4">Đang tải thông tin sự kiện...</div>
            ) : (
              <div className="mb-4">
                <p className="font-bold text-white text-lg">{concert?.name}</p>
                <p className="text-sm text-textSecondary">Mã đơn: <span className="font-mono">{booking.id.substring(0, 8).toUpperCase()}</span></p>
              </div>
            )}
            
            {booking.items && booking.items.length > 0 && (
              <div className="border-t border-border pt-4 mt-4 space-y-2">
                <p className="text-sm font-bold text-textSecondary">Chi tiết vé</p>
                {booking.items.map((item: any) => (
                  <div key={item.id} className="flex justify-between items-center text-sm">
                    <div>
                      <span className="font-medium text-white">{item.categoryName}</span>
                      {item.seatNumber ? (
                        <span className="text-textSecondary ml-2">- Ghế {item.seatNumber}</span>
                      ) : (
                        <span className="text-textSecondary ml-2">x {item.quantity}</span>
                      )}
                    </div>
                    <span className="font-medium">{(item.price * item.quantity).toLocaleString('vi-VN')} đ</span>
                  </div>
                ))}
              </div>
            )}
            
            <div className="border-t border-border pt-4 mt-4">
              <div className="flex justify-between items-center text-xl font-bold mb-6">
                <span>Tổng thanh toán</span>
                <span className="text-primary">{booking.totalAmount.toLocaleString('vi-VN')} ₫</span>
              </div>
              <Button className="w-full h-14 text-lg" onClick={handlePayment} disabled={processing || concertLoading}>
                {processing ? 'Đang xử lý...' : 'Thanh Toán Ngay'}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
