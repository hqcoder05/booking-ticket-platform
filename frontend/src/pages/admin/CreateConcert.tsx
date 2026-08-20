import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Button } from '@/components/Button';
import { adminApi } from '@/features/admin/api';
import toast from 'react-hot-toast';

export function CreateConcert() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    eventDate: '',
    venueId: '',
    stageLayout: 'FRONT',
  });

  const { data: venues, isLoading: isLoadingVenues } = useQuery({
    queryKey: ['admin-venues'],
    queryFn: adminApi.getAllVenues
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.venueId) {
      toast.error('Vui lòng chọn địa điểm!');
      return;
    }
    setLoading(true);
    try {
      await adminApi.createConcert({
        name: formData.name,
        eventDate: new Date(formData.eventDate).toISOString(),
        venueId: formData.venueId
      });
      toast.success(`Tạo sự kiện thành công! Cần thêm hạng vé.`);
      navigate('/admin');
    } catch (error: any) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-10 max-w-2xl">
      <h1 className="text-3xl font-bold mb-8 text-white">Tạo Sự Kiện Mới</h1>
      
      <form onSubmit={handleSubmit} className="bg-surface p-8 rounded-xl border border-border space-y-6">
        <div>
          <label className="block text-sm font-bold text-textSecondary mb-2">Tên sự kiện</label>
          <input 
            type="text" required
            className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary"
            value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})}
          />
        </div>

        <div>
          <label className="block text-sm font-bold text-textSecondary mb-2">Thời gian diễn ra</label>
          <input 
            type="datetime-local" required
            className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary"
            value={formData.eventDate} onChange={e => setFormData({...formData, eventDate: e.target.value})}
          />
        </div>

        <div className="border-t border-border pt-6 mt-6">
          <h3 className="text-xl font-bold mb-4">Địa điểm</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-bold text-textSecondary mb-2">Chọn địa điểm đã có</label>
              {isLoadingVenues ? (
                <div className="text-sm text-textSecondary">Đang tải danh sách địa điểm...</div>
              ) : (
                <select 
                  required
                  className="w-full bg-background border border-border rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary"
                  value={formData.venueId}
                  onChange={e => setFormData({...formData, venueId: e.target.value})}
                >
                  <option value="">-- Chọn địa điểm --</option>
                  {venues?.map(v => (
                    <option key={v.id} value={v.id}>{v.name} ({v.city}) - Sức chứa: {v.capacity}</option>
                  ))}
                </select>
              )}
            </div>
            <p className="text-xs text-textSecondary mt-2">
              (Nếu địa điểm chưa tồn tại, vui lòng vào tab &quot;Địa điểm&quot; để tạo mới trước khi tạo sự kiện).
            </p>
          </div>
        </div>

        <div className="flex justify-end pt-6 gap-4">
          <Button type="button" variant="outline" onClick={() => navigate('/admin')}>Hủy</Button>
          <Button type="submit" disabled={loading || isLoadingVenues}>
            {loading ? 'Đang xử lý...' : 'Tạo Sự Kiện'}
          </Button>
        </div>
      </form>
    </div>
  );
}
