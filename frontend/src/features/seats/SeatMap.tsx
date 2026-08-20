import React from 'react';
import { cn } from '@/utils/cn';

interface SeatProps {
  id: string;
  row: string;
  number: number;
  status: string;
  type: string;
  price: number;
}

interface SeatMapProps {
  seats: SeatProps[];
  selectedSeats: string[];
  onToggleSeat: (seatId: string) => void;
  stageLayout?: string;
}

export function SeatMap({ seats, selectedSeats, onToggleSeat, stageLayout = 'FRONT' }: SeatMapProps) {
  // Extract unique rows to build the grid
  const rows = Array.from(new Set(seats.map(s => s.row))).sort((a, b) => {
    if (a.length !== b.length) return a.length - b.length;
    return a.localeCompare(b);
  });

  const getSeatColor = (type: string, isSelected: boolean, status: string) => {
    if (status === 'SOLD' || status === 'BOOKED') return 'bg-seat-sold cursor-not-allowed opacity-50';
    if (status === 'RESERVED' || status === 'HELD') return 'bg-seat-reserved cursor-not-allowed opacity-50';
    if (isSelected) return 'bg-seat-selected shadow-[0_0_10px_rgba(234,179,8,0.5)]';
    
    switch (type) {
      case 'VIP': return 'bg-seat-vip hover:bg-seat-vip/80';
      case 'STANDARD': return 'bg-blue-500 hover:bg-blue-600';
      default: return 'bg-seat-available hover:bg-gray-400';
    }
  };

  const renderStage = () => {
    if (stageLayout === 'FRONT') {
      return (
        <div className="mb-12 w-full flex justify-center">
          <div className="w-2/3 h-4 bg-gradient-to-b from-primary/50 to-transparent rounded-t-full flex items-center justify-center relative">
            <div className="absolute top-4 text-xs font-bold tracking-widest text-textSecondary uppercase">Sân Khấu</div>
          </div>
        </div>
      );
    } else if (stageLayout === 'ROUND') {
      return (
        <div className="my-12 w-full flex justify-center">
          <div className="w-48 h-48 bg-gradient-to-b from-primary/30 to-primary/10 border-2 border-primary/50 rounded-full flex items-center justify-center relative shadow-[0_0_30px_rgba(234,179,8,0.2)]">
            <div className="text-sm font-bold tracking-widest text-white uppercase">Sân Khấu</div>
          </div>
        </div>
      );
    } else if (stageLayout === 'SURROUNDED') {
      return (
        <div className="my-12 w-full flex justify-center">
          <div className="w-64 h-32 bg-gradient-to-b from-primary/30 to-primary/10 border-2 border-primary/50 rounded-xl flex items-center justify-center relative shadow-[0_0_30px_rgba(234,179,8,0.2)]">
            <div className="text-sm font-bold tracking-widest text-white uppercase">Sân Khấu</div>
          </div>
        </div>
      );
    }
  };

  const halfIndex = Math.ceil(rows.length / 2);

  return (
    <div className="bg-surface rounded-xl py-8 px-12 border border-border overflow-x-auto">
      {stageLayout === 'FRONT' && renderStage()}

      <div className="flex flex-col gap-2 items-center min-w-max">
        {rows.map((row, index) => (
          <React.Fragment key={row}>
            {stageLayout !== 'FRONT' && index === halfIndex && renderStage()}
            <div className="flex gap-1 items-center">
            <span className="w-5 font-bold text-textSecondary text-xs">{row}</span>
            <div className="flex gap-1">
              {seats.filter(s => s.row === row).sort((a, b) => a.number - b.number).map(seat => {
                const isSelected = selectedSeats.includes(seat.id);
                const statusMap: Record<string, string> = {
                  'AVAILABLE': 'Còn trống',
                  'SOLD': 'Đã bán',
                  'BOOKED': 'Đã bán',
                  'RESERVED': 'Đang giữ',
                  'HELD': 'Đang giữ',
                  'SELECTED': 'Đã chọn'
                };
                return (
                  <button
                    key={seat.id}
                    onClick={() => {
                      if (seat.status !== 'SOLD' && seat.status !== 'BOOKED' && seat.status !== 'RESERVED' && seat.status !== 'HELD') {
                        onToggleSeat(seat.id);
                      }
                    }}
                    className={cn(
                      "w-5 h-5 sm:w-6 sm:h-6 rounded-t text-[9px] font-bold transition-all flex items-center justify-center relative group",
                      getSeatColor(seat.type, isSelected, seat.status)
                    )}
                  >
                    {/* Tooltip on hover */}
                    <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 hidden group-hover:flex flex-col bg-background border border-border rounded p-2 text-left z-50 w-32 shadow-xl">
                      <span className="text-white font-bold text-sm">Ghế {seat.row}{seat.number}</span>
                      <span className="text-primary font-bold text-xs">{seat.type}</span>
                      <span className="font-bold text-sm mt-1">{seat.price.toLocaleString('vi-VN')} đ</span>
                      <span className="text-textSecondary text-xs mt-1">{statusMap[isSelected ? 'SELECTED' : seat.status] || 'Không rõ'}</span>
                    </div>
                  </button>
                );
              })}
            </div>
            <span className="w-5 font-bold text-textSecondary text-xs text-right">{row}</span>
            </div>
          </React.Fragment>
        ))}
      </div>
      
      {/* Legend */}
      <div className="mt-12 flex justify-center gap-6 border-t border-border pt-6 text-sm text-textSecondary">
        <div className="flex items-center gap-2"><div className="w-4 h-4 rounded-t bg-seat-vip"></div> VIP</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 rounded-t bg-blue-500"></div> Phổ thông (Standard)</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 rounded-t bg-seat-available"></div> Còn trống</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 rounded-t bg-seat-selected"></div> Đã chọn</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 rounded-t bg-seat-sold"></div> Đã bán / Đang giữ</div>
      </div>
    </div>
  );
}
