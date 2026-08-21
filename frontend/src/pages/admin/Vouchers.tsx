import { useState, useEffect } from 'react';
import { api } from '@/services/api';
import type { ApiResponse } from '@/services/api';
import { Button } from '@/components/Button';
import { Plus, Trash2, Tag, Percent, DollarSign } from 'lucide-react';
import toast from 'react-hot-toast';

interface Voucher {
  id: string;
  code: string;
  discountType: string;
  discountValue: number;
  maxUsage: number;
  currentUsage: number;
}

export function Vouchers() {
  const [vouchers, setVouchers] = useState<Voucher[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState({
    code: '',
    discountType: 'PERCENTAGE',
    discountValue: '',
    maxUsage: '',
  });

  const fetchVouchers = async () => {
    try {
      const res = await api.get<ApiResponse<Voucher[]>>('/operation/vouchers');
      setVouchers(res.data.result);
    } catch (err: any) {
      toast.error('Lỗi tải vouchers: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchVouchers(); }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/operation/vouchers', {
        code: form.code.toUpperCase(),
        discountType: form.discountType,
        discountValue: parseFloat(form.discountValue),
        maxUsage: parseInt(form.maxUsage),
        currentUsage: 0,
      });
      toast.success('Tạo voucher thành công!');
      setShowForm(false);
      setForm({ code: '', discountType: 'PERCENTAGE', discountValue: '', maxUsage: '' });
      fetchVouchers();
    } catch (err: any) {
      toast.error('Lỗi: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDelete = async (id: string, code: string) => {
    if (!confirm(`Bạn chắc chắn muốn xóa voucher "${code}"?`)) return;
    try {
      await api.delete(`/operation/vouchers/${id}`);
      toast.success('Đã xóa voucher!');
      fetchVouchers();
    } catch (err: any) {
      toast.error('Lỗi: ' + (err.response?.data?.message || err.message));
    }
  };

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold">Quản lý Voucher</h1>
          <p className="text-textSecondary mt-1">Tạo và quản lý mã giảm giá cho sự kiện</p>
        </div>
        <Button onClick={() => setShowForm(!showForm)} className="flex items-center gap-2">
          <Plus className="w-5 h-5" /> Tạo Voucher
        </Button>
      </div>

      {/* Create Form */}
      {showForm && (
        <div className="glass-panel border border-border rounded-xl p-6 mb-8">
          <h2 className="text-lg font-bold mb-4 flex items-center gap-2">
            <Tag className="w-5 h-5 text-primary" /> Tạo Voucher mới
          </h2>
          <form onSubmit={handleCreate} className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm text-textSecondary mb-1">Mã Voucher</label>
              <input
                type="text" required placeholder="VD: SUMMER2027"
                className="w-full bg-surface border border-border rounded-lg px-4 py-3 text-white focus:border-primary focus:outline-none"
                value={form.code} onChange={e => setForm({...form, code: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Loại giảm giá</label>
              <select
                className="w-full bg-surface border border-border rounded-lg px-4 py-3 text-white focus:border-primary focus:outline-none"
                value={form.discountType} onChange={e => setForm({...form, discountType: e.target.value})}
              >
                <option value="PERCENTAGE">Giảm theo %</option>
                <option value="FIXED_AMOUNT">Giảm số tiền cố định (VNĐ)</option>
              </select>
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">
                {form.discountType === 'PERCENTAGE' ? 'Phần trăm giảm (%)' : 'Số tiền giảm (VNĐ)'}
              </label>
              <input
                type="number" required min="1" placeholder={form.discountType === 'PERCENTAGE' ? '10' : '50000'}
                className="w-full bg-surface border border-border rounded-lg px-4 py-3 text-white focus:border-primary focus:outline-none"
                value={form.discountValue} onChange={e => setForm({...form, discountValue: e.target.value})}
              />
            </div>
            <div>
              <label className="block text-sm text-textSecondary mb-1">Giới hạn lượt sử dụng</label>
              <input
                type="number" required min="1" placeholder="100"
                className="w-full bg-surface border border-border rounded-lg px-4 py-3 text-white focus:border-primary focus:outline-none"
                value={form.maxUsage} onChange={e => setForm({...form, maxUsage: e.target.value})}
              />
            </div>
            <div className="md:col-span-2 flex gap-3 pt-2">
              <Button type="submit">Tạo Voucher</Button>
              <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 text-textSecondary hover:text-white transition-colors">
                Hủy
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Voucher Table */}
      {loading ? (
        <p className="text-textSecondary">Đang tải...</p>
      ) : vouchers.length === 0 ? (
        <div className="text-center py-16 text-textSecondary">
          <Tag className="w-12 h-12 mx-auto mb-4 opacity-50" />
          <p>Chưa có voucher nào. Bấm "Tạo Voucher" để bắt đầu.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-border text-textSecondary text-sm">
                <th className="text-left py-3 px-4">Mã</th>
                <th className="text-left py-3 px-4">Loại</th>
                <th className="text-right py-3 px-4">Giá trị</th>
                <th className="text-center py-3 px-4">Đã dùng / Tối đa</th>
                <th className="text-center py-3 px-4">Trạng thái</th>
                <th className="text-right py-3 px-4">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {vouchers.map(v => (
                <tr key={v.id} className="border-b border-border/50 hover:bg-surfaceElevated transition-colors">
                  <td className="py-4 px-4">
                    <span className="font-mono font-bold text-primary bg-primary/10 px-3 py-1 rounded-lg">{v.code}</span>
                  </td>
                  <td className="py-4 px-4">
                    <span className="flex items-center gap-1.5 text-sm">
                      {v.discountType === 'PERCENTAGE' ? (
                        <><Percent className="w-4 h-4 text-green-400" /> Giảm %</>
                      ) : (
                        <><DollarSign className="w-4 h-4 text-blue-400" /> Giảm tiền</>
                      )}
                    </span>
                  </td>
                  <td className="py-4 px-4 text-right font-semibold">
                    {v.discountType === 'PERCENTAGE'
                      ? `${v.discountValue}%`
                      : `${v.discountValue.toLocaleString('vi-VN')}đ`
                    }
                  </td>
                  <td className="py-4 px-4 text-center">
                    <span className="text-sm">{v.currentUsage} / {v.maxUsage}</span>
                  </td>
                  <td className="py-4 px-4 text-center">
                    {v.currentUsage >= v.maxUsage ? (
                      <span className="text-xs bg-red-500/20 text-red-400 px-2 py-1 rounded-full">Hết lượt</span>
                    ) : (
                      <span className="text-xs bg-green-500/20 text-green-400 px-2 py-1 rounded-full">Còn hiệu lực</span>
                    )}
                  </td>
                  <td className="py-4 px-4 text-right">
                    <button
                      onClick={() => handleDelete(v.id, v.code)}
                      className="text-red-400 hover:text-red-300 transition-colors p-2 rounded-lg hover:bg-red-500/10"
                      title={v.currentUsage > 0 ? 'Không thể xóa (đã có người dùng)' : 'Xóa voucher'}
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
