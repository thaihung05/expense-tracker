import { Link } from 'react-router'
import { QuickEntry } from '../components/QuickEntry'
import { TransactionList } from '../components/TransactionList'
import { useAuth } from '../lib/auth'
import { currentMonth, formatMoney, formatMonth } from '../lib/format'
import { useMonth } from '../lib/month'
import { useBudgets, useRecentTransactions, useSummary } from '../lib/queries'
import { BudgetRow } from './BudgetsPage'

export function HomePage() {
  const { user } = useAuth()
  const { month } = useMonth()
  const summary = useSummary(month)
  const recent = useRecentTransactions(month)
  const budgets = useBudgets(month)
  const isCurrent = month === currentMonth()
  const alerts = (budgets.data ?? []).filter((b) => b.status !== 'OK')
  const s = summary.data

  return (
    <>
      <section className="balance" aria-label="Số dư tháng">
        <p className="balance__eyebrow">
          {isCurrent ? `Chào ${user?.displayName ?? 'bạn'} · còn lại tháng này` : `Còn lại · ${formatMonth(month)}`}
        </p>
        <p className={`balance__value${s && s.balance < 0 ? ' balance__value--neg' : ''}`}>
          {s ? formatMoney(s.balance) : '…'}
        </p>
        <dl className="balance__split">
          <div>
            <dt><span className="dot dot--income" aria-hidden /> Thu</dt>
            <dd className="mono">{s ? formatMoney(s.totalIncome) : '…'}</dd>
          </div>
          <div>
            <dt><span className="dot dot--expense" aria-hidden /> Chi</dt>
            <dd className="mono">{s ? formatMoney(s.totalExpense) : '…'}</dd>
          </div>
        </dl>
      </section>

      {isCurrent && <QuickEntry />}

      {alerts.length > 0 && (
        <section className="card" aria-labelledby="alert-title">
          <header className="card__head">
            <h2 id="alert-title">Ngân sách cần chú ý</h2>
            <Link to="/ngan-sach" className="link-btn">Xem hết</Link>
          </header>
          {alerts.map((b) => <BudgetRow key={b.id} budget={b} />)}
        </section>
      )}

      <section aria-labelledby="recent-title">
        <header className="section-head">
          <h2 id="recent-title">Gần đây</h2>
          {(recent.data?.totalItems ?? 0) > (recent.data?.items.length ?? 0) && (
            <Link to="/giao-dich" className="link-btn">Xem tất cả {recent.data?.totalItems}</Link>
          )}
        </header>
        {recent.isPending ? (
          <p className="muted">Đang tải…</p>
        ) : recent.data && recent.data.items.length > 0 ? (
          <TransactionList items={recent.data.items} />
        ) : (
          <div className="empty">
            <p>Chưa có giao dịch nào trong {formatMonth(month).toLowerCase()}.</p>
            {isCurrent && <p className="muted">Thử gõ “cafe 35k” vào ô phía trên.</p>}
          </div>
        )}
      </section>
    </>
  )
}
