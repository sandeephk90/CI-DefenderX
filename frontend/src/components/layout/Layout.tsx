import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import clsx from 'clsx'

const NAV = [
  { to: '/dashboard',  label: 'Dashboard',  icon: '▦' },
  { to: '/endpoints',  label: 'Endpoints',  icon: '⬡' },
  { to: '/threats',    label: 'Threats',    icon: '⚠' },
  { to: '/reports',    label: 'Reports',    icon: '📋' },
]

export default function Layout() {
  const { username, role, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="flex h-screen overflow-hidden">
      {/* Sidebar */}
      <aside className="w-60 flex flex-col bg-gray-900 border-r border-gray-800 shrink-0">
        <div className="px-5 py-4 border-b border-gray-800">
          <div className="flex items-center gap-2">
            <span className="text-brand-500 text-2xl">🛡</span>
            <div>
              <div className="text-sm font-bold text-white tracking-wide">CI-DefenderX</div>
              <div className="text-[10px] text-gray-500 uppercase tracking-widest">Threat Protection</div>
            </div>
          </div>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {NAV.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-colors',
                  isActive
                    ? 'bg-brand-600 text-white'
                    : 'text-gray-400 hover:bg-gray-800 hover:text-gray-100'
                )
              }
            >
              <span className="text-base">{item.icon}</span>
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="px-4 py-3 border-t border-gray-800">
          <div className="text-xs text-gray-400 mb-1 truncate">{username}</div>
          <div className="text-[10px] text-gray-600 uppercase mb-2">{role}</div>
          <button onClick={handleLogout} className="btn-ghost w-full text-left text-xs px-2 py-1">
            Sign out
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
