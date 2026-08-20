import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Button } from '@/components/Button';
import { Calendar, MapPin } from 'lucide-react';
import { useConcerts } from '@/features/concerts/queries';
import dayjs from 'dayjs';

export function Home() {
  const { data: concerts, isLoading, error } = useConcerts();

  return (
    <div className="flex-1 flex flex-col">
      {/* Hero Section */}
      <section className="relative h-[600px] flex items-center justify-center overflow-hidden">
        <div className="absolute inset-0 z-0">
          <div className="absolute inset-0 bg-gradient-to-t from-background via-background/80 to-transparent z-10" />
          <div className="absolute inset-0 bg-gradient-to-br from-[#A50064]/40 via-[#FF4081]/20 to-background" />
          {/* Abstract circles for decoration */}
          <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-primary/20 rounded-full blur-[120px] mix-blend-screen" />
          <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#A50064]/30 rounded-full blur-[100px] mix-blend-screen" />
        </div>
        
        <div className="relative z-20 text-center px-4 max-w-4xl mx-auto mt-20">
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
          >
            <span className="text-primary font-bold tracking-widest uppercase text-sm mb-4 block">
              Sự kiện nổi bật
            </span>
            <h1 className="text-5xl md:text-7xl font-extrabold text-white mb-6 leading-tight">
              THE WORLD TOUR 2026
            </h1>
            <p className="text-xl text-textSecondary mb-8 max-w-2xl mx-auto">
              Trải nghiệm hành trình âm nhạc hoành tráng nhất thập kỷ. Sân khấu trực tiếp tại TP. Hồ Chí Minh.
            </p>
            {concerts && concerts.length > 0 ? (
              <Link to={`/concerts/${concerts[0].id}`}>
                <Button size="lg" className="text-lg px-12 h-14 rounded-full shadow-[0_0_30px_rgba(234,179,8,0.3)]">
                  Mua vé ngay
                </Button>
              </Link>
            ) : null}
          </motion.div>
        </div>
      </section>

      {/* Upcoming Events */}
      <section className="py-20 container mx-auto px-4">
        <h2 className="text-3xl font-bold mb-10 border-l-4 border-primary pl-4">Sự kiện sắp tới</h2>
        
        {isLoading && <div className="text-center py-10 text-textSecondary">Đang tải danh sách sự kiện...</div>}
        {error && <div className="text-center py-10 text-error">Lỗi kết nối tới máy chủ. Vui lòng thử lại.</div>}
        {!isLoading && concerts?.length === 0 && (
          <div className="text-center py-10 text-textSecondary">Hiện tại không có sự kiện nào sắp diễn ra.</div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {concerts?.map((concert) => (
            <motion.div 
              key={concert.id}
              whileHover={{ y: -10 }}
              className="bg-surface rounded-xl overflow-hidden border border-border group"
            >
              <div className="h-48 overflow-hidden relative bg-gradient-to-br from-surfaceElevated to-surface flex items-center justify-center">
                {/* Fallback pattern instead of external image */}
                <div className="absolute inset-0 opacity-20" style={{ backgroundImage: 'radial-gradient(circle at 2px 2px, rgba(234, 179, 8, 0.4) 1px, transparent 0)', backgroundSize: '24px 24px' }}></div>
                <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center">
                  <Calendar className="w-8 h-8 text-primary/50" />
                </div>
                <div className="absolute top-4 right-4 bg-background/90 backdrop-blur-sm text-primary px-3 py-1 rounded-full text-xs font-bold border border-primary/30">
                  HOT
                </div>
              </div>
              <div className="p-6">
                <h3 className="text-xl font-bold mb-4">{concert.name}</h3>
                <div className="space-y-2 mb-6 text-sm text-textSecondary">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-4 h-4 text-primary shrink-0" />
                    <span>{dayjs(concert.eventDate).format('DD/MM/YYYY • HH:mm')}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin className="w-4 h-4 text-primary shrink-0" />
                    <span>{concert.venue.name} - {concert.venue.city}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-xs text-textSecondary">Giá từ</p>
                    <p className="text-lg font-bold text-white">
                      {Math.min(...concert.ticketCategories.map(c => c.price)).toLocaleString('vi-VN')} ₫
                    </p>
                  </div>
                  <Link to={`/concerts/${concert.id}`}>
                    <Button variant="outline">Mua vé</Button>
                  </Link>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      </section>
    </div>
  );
}
