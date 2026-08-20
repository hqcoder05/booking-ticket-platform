import { Link, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminApi } from '@/features/admin/api';
import { Button } from '@/components/Button';
import dayjs from 'dayjs';
import toast from 'react-hot-toast';
import { Trash2 } from 'lucide-react';

export function AdminDashboard() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { data: concerts, isLoading } = useQuery({
    queryKey: ['admin-concerts'],
    queryFn: adminApi.getAllConcerts, 
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => adminApi.deleteConcert(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin-concerts'] });
      toast.success('Xóa sự kiện thành công!');
    },
    onError: (err: any) => {
      toast.error('Không thể xóa: ' + (err.response?.data?.message || err.message));
    }
  });

  const handleDelete = (id: string) => {
    toast((t) => (
      <div>
        <p className="font-bold mb-2">Bạn có chắc chắn muốn xóa sự kiện này?</p>
        <div className="flex gap-2 justify-end mt-4">
          <Button variant="outline" size="sm" onClick={() => toast.dismiss(t.id)}>Hủy</Button>
          <Button size="sm" className="bg-error hover:bg-error/80 text-white" onClick={() => {
            deleteMutation.mutate(id);
            toast.dismiss(t.id);
          }}>Xóa</Button>
        </div>
      </div>
    ), { duration: 5000 });
  };

  return (
    <div className="container mx-auto px-4 py-10">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold">Bảng Điều Khiển Quản Trị Viên</h1>
        <Link to="/admin/concerts/new">
          <Button>+ Tạo sự kiện mới</Button>
        </Link>
      </div>

      <div className="bg-surface border border-border rounded-xl overflow-hidden">
        <table className="w-full text-left text-sm text-textSecondary">
          <thead className="bg-surfaceElevated text-xs uppercase text-white font-bold">
            <tr>
              <th className="px-6 py-4">Tên Sự Kiện</th>
              <th className="px-6 py-4">Thời Gian</th>
              <th className="px-6 py-4">Trạng Thái</th>
              <th className="px-6 py-4">Hành Động</th>
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={4} className="text-center py-10">Đang tải...</td></tr>
            ) : (
              concerts?.map((concert) => (
                <tr key={concert.id} className="border-b border-border hover:bg-surfaceElevated transition-colors">
                  <td className="px-6 py-4 font-medium text-white">{concert.name}</td>
                  <td className="px-6 py-4">{dayjs(concert.eventDate).format('DD/MM/YYYY HH:mm')}</td>
                  <td className="px-6 py-4">
                    <span className="px-3 py-1 rounded-full text-xs font-bold bg-primary/20 text-primary">
                      {concert.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 flex items-center gap-2">
                    <Button variant="outline" size="sm" onClick={() => navigate(`/admin/concerts/${concert.id}/edit`)}>
                      Sửa
                    </Button>
                    <button onClick={() => handleDelete(concert.id)} className="text-textSecondary hover:text-error transition-colors p-2">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))
            )}
            {concerts?.length === 0 && !isLoading && (
              <tr><td colSpan={4} className="text-center py-10">Chưa có sự kiện nào</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
