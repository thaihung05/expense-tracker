import { useCallback, useState } from 'react'
import { Link, NavLink, Outlet } from 'react-router'
import { useAuth } from '../lib/auth'
import { EditorContext } from './editor'
import type { EditorTarget } from './editor'
import { Icon } from './Icon'
import type { IconName } from './Icon'
import { MonthSwitcher } from './MonthSwitcher'
import { TransactionSheet } from './TransactionSheet'

const NAV: { to: string; label: string; icon: IconName }[] = [
  { to: '/', label: 'Tổng quan', icon: 'home' },
  { to: '/giao-dich', label: 'Giao dịch', icon: 'list' },
  { to: '/bao-cao', label: 'Báo cáo', icon: 'chart' },
  { to: '/ngan-sach', label: 'Ngân sách', icon: 'target' },
]

/** Khung chung: thanh tren (chon thang, tai khoan), noi dung trang, thanh dieu huong duoi. */
export function Layout() {
  const { user } = useAuth()
  const [target, setTarget] = useState<EditorTarget | null>(null)
  const openEditor = useCallback((t?: EditorTarget) => setTarget(t ?? { draft: {} }), [])
  const closeEditor = useCallback(() => setTarget(null), [])

  return (
    <EditorContext.Provider value={openEditor}>
      <div className="app">
        <header className="topbar">
          <MonthSwitcher />
          <Link to="/cai-dat" className="avatar" aria-label="Tài khoản và cài đặt">
            {user?.displayName.trim().charAt(0).toUpperCase() ?? <Icon name="user" />}
          </Link>
        </header>

        <main className="page">
          <Outlet />
        </main>

        <nav className="tabbar" aria-label="Điều hướng chính">
          {NAV.slice(0, 2).map((item) => <Tab key={item.to} {...item} />)}
          <button className="tabbar__add" onClick={() => openEditor()} aria-label="Thêm giao dịch">
            <Icon name="plus" size={26} />
          </button>
          {NAV.slice(2).map((item) => <Tab key={item.to} {...item} />)}
        </nav>

        {target && <TransactionSheet target={target} onClose={closeEditor} />}
      </div>
    </EditorContext.Provider>
  )
}

function Tab({ to, label, icon }: { to: string; label: string; icon: IconName }) {
  return (
    <NavLink to={to} end={to === '/'} className={({ isActive }) => `tab${isActive ? ' tab--on' : ''}`}>
      <Icon name={icon} />
      <span>{label}</span>
    </NavLink>
  )
}
