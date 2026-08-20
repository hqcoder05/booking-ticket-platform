import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authApi } from '@/features/auth/api';
import { Button } from '@/components/Button';
import { Ticket } from 'lucide-react';
import toast from 'react-hot-toast';

export function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await authApi.login({ email, password });
      localStorage.setItem('accessToken', res.accessToken);
      localStorage.setItem('userRole', res.user.role);
      
      toast.success('Đăng nhập thành công!');

      if (res.user.role === 'ADMIN' || res.user.role === 'OPERATOR') {
        navigate('/admin');
      } else {
        navigate('/');
      }
    } catch (err: any) {
      toast.error('Đăng nhập thất bại: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 relative overflow-hidden">
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] bg-primary/20 blur-[120px] rounded-full mix-blend-screen pointer-events-none"></div>
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] bg-[#A50064]/20 blur-[120px] rounded-full mix-blend-screen pointer-events-none"></div>

      <div className="w-full max-w-md glass-panel border border-border rounded-2xl p-8 relative z-10">
        <div className="flex flex-col items-center mb-8">
          <div className="w-12 h-12 bg-primary/20 rounded-full flex items-center justify-center mb-4">
            <Ticket className="w-6 h-6 text-primary" />
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">Đăng Nhập</h1>
          <p className="text-textSecondary text-center">Chào mừng bạn quay lại Ticket.Master</p>
        </div>
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-textSecondary mb-1.5">Email</label>
            <input 
              type="email" required placeholder="name@example.com"
              className="w-full bg-surface border border-border rounded-xl px-4 py-3.5 text-white placeholder-textSecondary/50 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
              value={email} onChange={e => setEmail(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-textSecondary mb-1.5">Mật khẩu</label>
            <input 
              type="password" required placeholder="••••••••"
              className="w-full bg-surface border border-border rounded-xl px-4 py-3.5 text-white placeholder-textSecondary/50 focus:outline-none focus:border-primary focus:ring-1 focus:ring-primary transition-all"
              value={password} onChange={e => setPassword(e.target.value)}
            />
          </div>
          <Button type="submit" className="w-full h-12 text-lg mt-4" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Đăng Nhập'}
          </Button>
        </form>
        <div className="mt-6 text-center text-sm text-textSecondary">
          Chưa có tài khoản? <Link to="/register" className="text-primary font-bold hover:underline">Đăng ký ngay</Link>
        </div>
      </div>
    </div>
  );
}
