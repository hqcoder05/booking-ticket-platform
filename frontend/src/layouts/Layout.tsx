import { Outlet, Link } from 'react-router-dom';
import { Ticket } from 'lucide-react';

export function Layout() {
  const token = localStorage.getItem('accessToken');
  const role = localStorage.getItem('userRole');
  
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('userRole');
    window.location.reload();
  };

  return (
    <div className="min-h-screen flex flex-col">
      <header className="sticky top-0 z-50 glass-panel border-b border-border">
        <div className="container mx-auto px-4 h-16 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2 text-primary font-bold text-xl tracking-tight">
            <Ticket className="w-6 h-6" />
            <span>TICKET.MASTER</span>
          </Link>
          <nav className="flex gap-6 items-center">
            {role === 'ADMIN' && (
              <Link to="/admin" className="text-primary font-bold hover:text-white transition-colors">Admin Portal</Link>
            )}
            <Link to="/" className="text-textSecondary hover:text-white transition-colors">Sự kiện</Link>
            {token ? (
              <>
                <Link to="/my-tickets" className="text-textSecondary hover:text-white transition-colors">Vé của tôi</Link>
                <button onClick={handleLogout} className="text-red-400 hover:text-red-300 transition-colors">Đăng xuất</button>
              </>
            ) : (
              <Link to="/login" className="bg-primary/20 text-primary px-4 py-2 rounded-lg font-bold hover:bg-primary/30 transition-colors">Đăng nhập</Link>
            )}
          </nav>
        </div>
      </header>
      
      <main className="flex-1 flex flex-col">
        <Outlet />
      </main>
      
      <footer className="border-t border-border bg-surface mt-auto">
        <div className="container mx-auto px-4 py-8 text-center text-textSecondary text-sm">
          &copy; 2026 Ticket.Master Platform. Premium Entertainment.
        </div>
      </footer>
    </div>
  );
}
