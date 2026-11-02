import { lazy, Suspense } from 'react'
import { Navigate, Route, Routes } from 'react-router'
import { Layout } from './components/Layout'
import { useAuth } from './lib/auth'
import { MonthProvider } from './lib/month'
import { BudgetsPage } from './pages/BudgetsPage'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { SettingsPage } from './pages/SettingsPage'
import { TransactionsPage } from './pages/TransactionsPage'

// Trang bao cao dung thu vien bieu do kha nang: chi tai khi nguoi dung mo trang nay.
const ReportsPage = lazy(() => import('./pages/ReportsPage').then((m) => ({ default: m.ReportsPage })))

export default function App() {
  const { user, loading } = useAuth()

  if (loading) {
    return <div className="splash" aria-busy="true">Đang mở sổ…</div>
  }

  if (!user) {
    return (
      <Routes>
        <Route path="*" element={<LoginPage />} />
      </Routes>
    )
  }

  return (
    <MonthProvider>
      <Routes>
        <Route element={<Layout />}>
          <Route index element={<HomePage />} />
          <Route path="giao-dich" element={<TransactionsPage />} />
          <Route
            path="bao-cao"
            element={
              <Suspense fallback={<p className="muted">Đang tải…</p>}>
                <ReportsPage />
              </Suspense>
            }
          />
          <Route path="ngan-sach" element={<BudgetsPage />} />
          <Route path="cai-dat" element={<SettingsPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Route>
      </Routes>
    </MonthProvider>
  )
}
