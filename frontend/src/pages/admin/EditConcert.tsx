import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/features/admin/api';
import { Button } from '@/components/Button';
import toast from 'react-hot-toast';
import dayjs from 'dayjs';
import { Trash2, Edit2, X, Check } from 'lucide-react';

export function EditConcert() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [loading, setLoading] = useState(false);

  const { data: concert, isLoading: isLoadingConcert } = useQuery({
    queryKey: ['admin-concert', id],
    queryFn: () => adminApi.getAllConcerts().then(res => res.find(c => c.id === id)),
    enabled: !!id
  });

  const { data: venues } = useQuery({
    queryKey: ['admin-venues'],
    queryFn: adminApi.getAllVenues
  });

  const [editingCategoryId, setEditingCategoryId] = useState<string | null>(null);
  const [editCategoryData, setEditCategoryData] = useState({ name: '', price: 0, totalQuantity: 100, type: 'SEATED' });

  const handleDeleteCategory = async (categoryId: string) => {
    if (!confirm('Bạn có chắc chắn muốn xóa hạng vé này?')) return;
    try {
      await adminApi.deleteTicketCategory(id!, categoryId);
      toast.success('Xóa hạng vé thành công!');
      queryClient.invalidateQueries({ queryKey: ['admin-concert', id] });
    } catch (error: any) {
      toast.error('Lỗi: ' + (error.response?.data?.message || error.message));
    }
  };

  const handleUpdateCategory = async (categoryId: string) => {
    try {
      await adminApi.updateTicketCategory(id!, categoryId, editCategoryData);
      toast.success('Cập nhật hạng vé thành công!');
      setEditingCategoryId(null);
      queryClient.invalidateQueries({ queryKey: ['admin-concert', id] });
    } catch (error: any) {
      toast.error('Lỗi: ' + (error.response?.data?.message || error.message));
    }
  };

  const [formData, setFormData] = useState({
    name: '',
    eventDate: '',
    venueId: '',
    stageLayout: 'FRONT',
  });

  const [categoryData, setCategoryData] = useState({
    name: 'STANDARD',
    price: 0,
    totalQuantity: 100,
    type: 'SEATED'
  });

  useEffect(() => {
    if (concert) {
      setFormData({
        name: concert.name,
        eventDate: dayjs(concert.eventDate).format('YYYY-MM-DDTHH:mm'),
        venueId: concert.venue.id,
        stageLayout: concert.stageLayout || 'FRONT',
      });
    }
  }, [concert]);

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await adminApi.updateConcert(id!, {
        name: formData.name,
        eventDate: new Date(formData.eventDate).toISOString(),
        venueId: formData.venueId
      });
      toast.success('Cập nhật sự kiện thành công!');
      queryClient.invalidateQueries({ queryKey: ['admin-concert', id] });
    } catch (error: any) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleAddCategory = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await adminApi.addTicketCategory(id!, categoryData);
      toast.success('Thêm hạng vé thành công!');
      queryClient.invalidateQueries({ queryKey: ['admin-concert', id] });
      setCategoryData({ name: 'STANDARD', price: 0, totalQuantity: 100, type: 'SEATED' });
    } catch (error: any) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    }
  };

  const handlePublish = async () => {
    try {
      await adminApi.publishConcert(id!);
      toast.success('Đã xuất bản sự kiện!');
      queryClient.invalidateQueries({ queryKey: ['admin-concert', id] });
    } catch (error: any) {
      toast.error('Có lỗi: ' + (error.response?.data?.message || error.message));
    }
  };

  if (isLoadingConcert) return <div className="text-white">Đang tải...</div>;
  if (!concert) return <div className="text-error">Không tìm thấy sự kiện.</div>;

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white mb-2">Chỉnh Sửa: {concert.name}</h1>
          <div className="flex gap-2 items-center">
            <span className="text-sm text-textSecondary">Trạng thái:</span>
            <span className={`text-xs font-bold px-2 py-1 rounded-full ${concert.status === 'PUBLISHED' ? 'bg-primary/20 text-primary' : 'bg-yellow-500/20 text-yellow-500'}`}>
              {concert.status}
            </span>
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={() => navigate('/admin')}>Quay Lại</Button>
          {concert.status === 'DRAFT' && (
            <Button onClick={handlePublish} className="bg-green-500 hover:bg-green-600 text-white">Xuất Bản (Bán Vé)</Button>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div className="bg-surface p-6 rounded-xl border border-border">
          <h2 className="text-xl font-bold text-white mb-4">Thông tin chung</h2>
          <form onSubmit={handleUpdate} className="space-y-4">
            <div>
              <label className="block text-sm text-textSecondary mb-1">Tên sự kiện</label>
              <input type="text" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Thời gian</label>
              <input type="datetime-local" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.eventDate} onChange={e => setFormData({...formData, eventDate: e.target.value})} />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Địa điểm</label>
              <select required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                value={formData.venueId} onChange={e => setFormData({...formData, venueId: e.target.value})}>
                <option value="">-- Chọn địa điểm --</option>
                {venues?.map(v => (
                  <option key={v.id} value={v.id}>{v.name} ({v.capacity} ghế)</option>
                ))}
              </select>
            </div>
            <div className="pt-2">
              <Button type="submit" disabled={loading} className="w-full">
                {loading ? 'Đang lưu...' : 'Lưu Thay Đổi'}
              </Button>
            </div>
          </form>
        </div>

        <div className="space-y-6">
          <div className="bg-surface p-6 rounded-xl border border-border">
            <h2 className="text-xl font-bold text-white mb-4">Thêm Hạng Vé Mới</h2>
            <form onSubmit={handleAddCategory} className="space-y-4">
              <div>
                <label className="block text-sm text-textSecondary mb-1">Loại ghế</label>
                <select className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white mb-4"
                  value={categoryData.type} onChange={e => setCategoryData({...categoryData, type: e.target.value, name: e.target.value === 'SEATED' ? 'STANDARD' : 'GA'})}>
                  <option value="SEATED">Ghế ngồi (SEATED)</option>
                  <option value="STANDING">Đứng (STANDING)</option>
                </select>
                
                <label className="block text-sm text-textSecondary mb-1">Tên Hạng Vé</label>
                {categoryData.type === 'SEATED' ? (
                  <select required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                    value={categoryData.name} onChange={e => setCategoryData({...categoryData, name: e.target.value})}>
                    <option value="STANDARD">STANDARD</option>
                    <option value="VIP">VIP</option>
                  </select>
                ) : (
                  <input type="text" required className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                    value={categoryData.name} onChange={e => setCategoryData({...categoryData, name: e.target.value})} placeholder="VD: GA, EARLY BIRD" />
                )}
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-textSecondary mb-1">Giá vé (VND)</label>
                  <input type="number" required min="0" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                    value={categoryData.price} onChange={e => setCategoryData({...categoryData, price: parseInt(e.target.value)})} />
                </div>
                <div>
                  <label className="block text-sm text-textSecondary mb-1">Số lượng</label>
                  <input type="number" required min="1" className="w-full bg-background border border-border rounded-lg px-4 py-2 text-white"
                    value={categoryData.totalQuantity} onChange={e => setCategoryData({...categoryData, totalQuantity: parseInt(e.target.value)})} />
                </div>
              </div>
              <Button type="submit" className="w-full">Thêm Hạng Vé</Button>
            </form>
          </div>

          <div className="bg-surface p-6 rounded-xl border border-border">
            <h2 className="text-xl font-bold text-white mb-4">Các Hạng Vé Hiện Tại</h2>
            {concert.ticketCategories?.length === 0 ? (
              <p className="text-textSecondary">Chưa có hạng vé nào.</p>
            ) : (
              <div className="space-y-3">
                {concert.ticketCategories?.map((cat: any) => (
                  <div key={cat.id} className="bg-background p-3 rounded-lg border border-border">
                    {editingCategoryId === cat.id ? (
                      <div className="space-y-2">
                        {editCategoryData.type === 'SEATED' ? (
                          <select className="w-full bg-surface border border-border rounded px-2 py-1 text-sm text-white" value={editCategoryData.name} onChange={e => setEditCategoryData({...editCategoryData, name: e.target.value})}>
                            <option value="STANDARD">STANDARD</option>
                            <option value="VIP">VIP</option>
                          </select>
                        ) : (
                          <input type="text" className="w-full bg-surface border border-border rounded px-2 py-1 text-sm text-white" value={editCategoryData.name} onChange={e => setEditCategoryData({...editCategoryData, name: e.target.value})} placeholder="Tên hạng vé" />
                        )}
                        <div className="flex gap-2">
                          <input type="number" className="w-1/2 bg-surface border border-border rounded px-2 py-1 text-sm text-white" value={editCategoryData.price} onChange={e => setEditCategoryData({...editCategoryData, price: parseInt(e.target.value)})} placeholder="Giá vé" />
                          <input type="number" className="w-1/2 bg-surface border border-border rounded px-2 py-1 text-sm text-white" value={editCategoryData.totalQuantity} onChange={e => setEditCategoryData({...editCategoryData, totalQuantity: parseInt(e.target.value)})} placeholder="Số lượng" />
                        </div>
                        <div className="flex justify-end gap-2 mt-2">
                          <button onClick={() => setEditingCategoryId(null)} className="p-1 text-textSecondary hover:text-white"><X className="w-4 h-4" /></button>
                          <button onClick={() => handleUpdateCategory(cat.id)} className="p-1 text-success hover:text-success/80"><Check className="w-4 h-4" /></button>
                        </div>
                      </div>
                    ) : (
                      <div className="flex justify-between items-center">
                        <div>
                          <div className="font-bold text-white">{cat.name} ({cat.type})</div>
                          <div className="text-sm text-textSecondary">Số lượng: {cat.totalQuantity}</div>
                        </div>
                        <div className="flex items-center gap-4">
                          <div className="text-primary font-bold">{cat.price.toLocaleString('vi-VN')} đ</div>
                          {concert.status === 'DRAFT' && (
                            <div className="flex gap-1">
                              <button onClick={() => { setEditingCategoryId(cat.id); setEditCategoryData({ name: cat.name, price: cat.price, totalQuantity: cat.totalQuantity, type: cat.type }); }} className="p-1.5 text-textSecondary hover:text-white transition-colors"><Edit2 className="w-4 h-4" /></button>
                              <button onClick={() => handleDeleteCategory(cat.id)} className="p-1.5 text-textSecondary hover:text-error transition-colors"><Trash2 className="w-4 h-4" /></button>
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
