import { Outlet, Link, useLocation, Navigate } from 'react-router-dom';
import { LayoutDashboard, Ticket, Map, Settings, Tag } from 'lucide-react';

export function AdminLayout() {
  const location = useLocation();
  const role = localStorage.getItem('userRole');

  if (role !== 'ADMIN' && role !== 'OPERATOR') {
    return <Navigate to="/" replace />;
  }

  const navItems = [
    { name: 'Sự kiện (Concerts)', path: '/admin', icon: Ticket },
    { name: 'Địa điểm (Venues)', path: '/admin/venues', icon: Map },
    { name: 'Voucher', path: '/admin/vouchers', icon: Tag },
    { name: 'Cài đặt', path: '/admin/settings', icon: Settings },
  ];

  return (
    <div className="min-h-screen flex bg-background text-white">
      {/* Sidebar */}
      <aside className="w-64 bg-surface border-r border-border flex flex-col sticky top-0 h-screen">
        <div className="h-16 flex items-center px-6 border-b border-border">
          <span className="text-xl font-bold text-primary flex items-center gap-2">
            <LayoutDashboard className="w-6 h-6" />
            ADMIN
          </span>
        </div>
        <nav className="flex-1 py-6 px-4 space-y-2">
          {navItems.map(item => {
            const isActive = location.pathname === item.path || (item.path !== '/admin' && location.pathname.startsWith(item.path));
            return (
              <Link 
                key={item.path} 
                to={item.path}
                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
                  isActive ? 'bg-primary/20 text-primary font-bold' : 'text-textSecondary hover:bg-surfaceElevated hover:text-white'
                }`}
              >
                <item.icon className="w-5 h-5" />
                {item.name}
              </Link>
            )
          })}
        </nav>
        <div className="p-4 border-t border-border">
          <Link to="/" className="text-sm text-textSecondary hover:text-white flex items-center gap-2 px-2 py-2">
            ← Trở về trang khách hàng
          </Link>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-auto bg-background">
        <Outlet />
      </main>
    </div>
  );
}
