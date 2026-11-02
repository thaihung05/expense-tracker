import { useState } from 'react'
import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { download } from '../lib/api'
import { formatMoney, formatMonth, formatShort, formatShortDate } from '../lib/format'
import { useMonth } from '../lib/month'
import { useSummary } from '../lib/queries'
import { useToast } from '../lib/toast'
import type { CategoryTotal, DailyPoint } from '../lib/types'
import { Icon } from '../components/Icon'

export function ReportsPage() {
  const { month } = useMonth()
  const { data, isPending, isError, error } = useSummary(month)
  const [showTable, setShowTable] = useState(false)
  const toast = useToast()

  async function exportCsv() {
    try {
      await download(`/api/transactions/export?month=${month}`, `thu-chi-${month}.csv`)
    } catch {
      toast('Không tải được file, thử lại sau', 'error')
    }
  }

  if (isPending) return <p className="muted">Đang tải…</p>
  if (isError) return <p className="form-error" role="alert">{error.message}</p>

  const spentDays = data.daily.filter((d) => d.expense > 0)
  const biggestDay = spentDays.reduce<DailyPoint | null>((max, d) => (!max || d.expense > max.expense ? d : max), null)

  return (
    <>
      <header className="page-head page-head--row">
        <div>
          <h1>Báo cáo</h1>
          <p className="muted">{formatMonth(month)}</p>
        </div>
        <button className="btn btn--ghost" onClick={exportCsv}>
          <Icon name="download" size={18} /> Xuất CSV
        </button>
      </header>

      <div className="stats">
        <Stat label="Tổng thu" value={data.totalIncome} tone="income" />
        <Stat label="Tổng chi" value={data.totalExpense} tone="expense" />
        <Stat label="Còn lại" value={data.balance} />
      </div>

      <section className="card" aria-labelledby="daily-title">
        <header className="card__head">
          <h2 id="daily-title">Chi tiêu theo ngày</h2>
          <button className="link-btn" onClick={() => setShowTable((v) => !v)} aria-pressed={showTable}>
            {showTable ? 'Xem biểu đồ' : 'Xem dạng bảng'}
          </button>
        </header>
        {spentDays.length === 0 ? (
          <p className="muted">Chưa có khoản chi nào trong tháng.</p>
        ) : showTable ? (
          <table className="table">
            <thead><tr><th scope="col">Ngày</th><th scope="col" className="num">Đã chi</th></tr></thead>
            <tbody>
              {spentDays.map((d) => (
                <tr key={d.date}><td>{formatShortDate(d.date)}</td><td className="num mono">{formatMoney(d.expense)}</td></tr>
              ))}
            </tbody>
          </table>
        ) : (
          <>
            <div className="chart" role="img"
              aria-label={`Biểu đồ cột chi tiêu từng ngày. Ngày chi nhiều nhất ${biggestDay ? formatShortDate(biggestDay.date) : ''}: ${biggestDay ? formatMoney(biggestDay.expense) : ''}`}>
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={data.daily} margin={{ top: 8, right: 4, left: 0, bottom: 0 }} barCategoryGap={2}>
                  <CartesianGrid vertical={false} stroke="var(--line)" strokeDasharray="0" />
                  <XAxis dataKey="date" tickFormatter={(d: string) => String(Number(d.slice(8)))}
                    tickLine={false} axisLine={{ stroke: 'var(--line)' }} interval={4}
                    tick={{ fill: 'var(--muted)', fontSize: 11 }} />
                  <YAxis tickFormatter={formatShort} width={44} tickLine={false} axisLine={false}
                    tick={{ fill: 'var(--muted)', fontSize: 11 }} />
                  <Tooltip cursor={{ fill: 'var(--hover)' }} content={(props) => <DayTooltip {...props} />} />
                  <Bar dataKey="expense" fill="var(--expense)" radius={[4, 4, 0, 0]} maxBarSize={14}
                    isAnimationActive={false} />
                </BarChart>
              </ResponsiveContainer>
            </div>
            {biggestDay && (
              <p className="muted small">
                Chi nhiều nhất ngày {formatShortDate(biggestDay.date)}: <span className="mono">{formatMoney(biggestDay.expense)}</span>
              </p>
            )}
          </>
        )}
      </section>

      <CategoryBreakdown title="Chi theo danh mục" items={data.expenseByCategory} total={data.totalExpense} tone="expense"
        empty="Chưa có khoản chi nào." />
      <CategoryBreakdown title="Thu theo danh mục" items={data.incomeByCategory} total={data.totalIncome} tone="income"
        empty="Chưa có khoản thu nào." />
    </>
  )
}

function Stat({ label, value, tone }: { label: string; value: number; tone?: 'income' | 'expense' }) {
  return (
    <div className="stat">
      <span className="stat__label">{tone && <span className={`dot dot--${tone}`} aria-hidden />} {label}</span>
      <span className="stat__value mono">{formatMoney(value)}</span>
    </div>
  )
}

/** Thanh ngang: do dai = ti le trong tong. Nhan ten + so tien luon hien, khong chi dua vao mau. */
function CategoryBreakdown({ title, items, total, tone, empty }: {
  title: string; items: CategoryTotal[]; total: number; tone: 'income' | 'expense'; empty: string
}) {
  return (
    <section className="card" aria-label={title}>
      <header className="card__head"><h2>{title}</h2></header>
      {items.length === 0 ? (
        <p className="muted">{empty}</p>
      ) : (
        <ul className="hbars">
          {items.map((c) => {
            const pct = total > 0 ? Math.round((c.total / total) * 100) : 0
            return (
              <li key={c.categoryId} className="hbar">
                <div className="hbar__label">
                  <span><span aria-hidden>{c.icon}</span> {c.name}</span>
                  <span className="mono">{formatMoney(c.total)} <span className="muted">· {pct}%</span></span>
                </div>
                <div className="hbar__track" aria-hidden>
                  <div className={`hbar__fill hbar__fill--${tone}`} style={{ width: `${Math.max(pct, 2)}%` }} />
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}

function DayTooltip({ active, payload }: { active?: boolean; payload?: ReadonlyArray<{ payload?: DailyPoint }> }) {
  const point = payload?.[0]?.payload
  if (!active || !point) return null
  return (
    <div className="tooltip">
      <strong>{formatShortDate(point.date)}</strong>
      <span><span className="dot dot--expense" aria-hidden /> Chi <span className="mono">{formatMoney(point.expense)}</span></span>
      {point.income > 0 && (
        <span><span className="dot dot--income" aria-hidden /> Thu <span className="mono">{formatMoney(point.income)}</span></span>
      )}
    </div>
  )
}
