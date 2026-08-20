import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/features/admin/api';
import { Button } from '@/components/Button';
import { MapPin, Plus, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';

export function Venues() {
  const queryClient = useQueryClient();
  const [isCreating, setIsCreating] = useState(false);
  const [formData, setFormData] = useState({ name: '', address: '', city: '', capacity: 1000 });

  const { data: venues, isLoading } = useQuery({
    queryKey: ['admin-venues'],
    queryFn: adminApi.getAllVenues
  });

  const createMutation = useMutation({
    mutationFn: () => adminApi.createVenue(formData),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-venues'] });
      setIsCreating(false);
      setFormData({ name: '', address: '', city: '', capacity: 1000 });
      toast.success('Tạo địa điểm thành công!');
    },
    onError: (err: any) => {
      toast.error('Lỗi: ' + (err.response?.data?.message || err.message));
    }
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.deleteVenue(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-venues'] });
      toast.success('Xóa địa điểm thành công!');
    },
    onError: (err: any) => {
      toast.error('Không thể xóa: ' + (err.response?.data?.message || err.message));
    }
  });

  const handleDelete = (id: string) => {
    toast((t) => (
      <div>
        <p className="font-bold mb-2">Bạn có chắc chắn muốn xóa địa điểm này?</p>
        <div className="flex gap-2 justify-end mt-4">
          <Button variant="outline" size="sm" onClick={() => toast.dismiss(t.id)}>
            Hủy
          </Button>
          <Button size="sm" className="bg-error hover:bg-error/80 text-white" onClick={() => {
            deleteMutation.mutate(id);
            toast.dismiss(t.id);
          }}>
            Xóa
          </Button>
        </div>
      </div>
    ), { duration: 5000 });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    createMutation.mutate();
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Quản lý Địa điểm</h1>
          <p className="text-textSecondary">Danh sách các nhà hát, sân vận động, rạp</p>
        </div>
        <Button onClick={() => setIsCreating(!isCreating)} className="flex items-center gap-2">
          <Plus className="w-5 h-5" />
          {isCreating ? 'Hủy' : 'Thêm Địa Điểm'}
        </Button>
      </div>

      {isCreating && (
        <form onSubmit={handleSubmit} className="bg-surface p-6 rounded-xl border border-border mb-8 space-y-4">
          <h3 className="text-xl font-bold text-white mb-4">Tạo Địa Điểm Mới</h3>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-textSecondary mb-1">Tên rạp/sân vận động</label>
              <input type="text" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Thành phố</label>
              <input type="text" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.city} onChange={e => setFormData({...formData, city: e.target.value})} />
            </div>
            <div className="col-span-2">
              <label className="block text-sm text-textSecondary mb-1">Địa chỉ cụ thể</label>
              <input type="text" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.address} onChange={e => setFormData({...formData, address: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Sức chứa (Capacity)</label>
              <input type="number" required min="1" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.capacity} onChange={e => setFormData({...formData, capacity: parseInt(e.target.value)})} />
            </div>
          </div>
          <div className="flex justify-end pt-4">
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Đang lưu...' : 'Lưu Địa Điểm'}
            </Button>
          </div>
        </form>
      )}

      {isLoading ? (
        <div className="text-textSecondary">Đang tải...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {venues?.map(venue => (
            <div key={venue.id} className="bg-surface p-6 rounded-xl border border-border hover:border-primary/50 transition-colors">
              <div className="flex items-start justify-between mb-4">
                <div className="p-3 bg-primary/20 rounded-lg text-primary">
                  <MapPin className="w-6 h-6" />
                </div>
                <div className="flex items-center gap-2">
                  <span className="bg-surfaceElevated px-3 py-1 rounded-full text-xs font-bold text-textSecondary">
                    Sức chứa: {venue.capacity.toLocaleString('vi-VN')}
                  </span>
                  <button onClick={() => handleDelete(venue.id)} className="text-textSecondary hover:text-error transition-colors p-2">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
              <h3 className="text-xl font-bold text-white mb-2">{venue.name}</h3>
              <p className="text-textSecondary text-sm mb-1">{venue.address}</p>
              <p className="text-primary text-sm font-bold">{venue.city}</p>
            </div>
          ))}
          {venues?.length === 0 && (
            <div className="col-span-3 text-center p-12 bg-surface rounded-xl border border-border">
              <p className="text-textSecondary">Chưa có địa điểm nào.</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
