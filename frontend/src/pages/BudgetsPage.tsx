import { useState } from 'react'
import type { FormEvent } from 'react'
import { AmountInput } from '../components/AmountInput'
import { Icon } from '../components/Icon'
import type { IconName } from '../components/Icon'
import { ApiError } from '../lib/api'
import { formatMoney, formatMonth } from '../lib/format'
import { useMonth } from '../lib/month'
import { useBudgets, useCategories, useDeleteBudget, useSaveBudget } from '../lib/queries'
import { useToast } from '../lib/toast'
import type { Budget, BudgetStatus } from '../lib/types'

const STATUS: Record<BudgetStatus, { label: string; icon: IconName }> = {
  OK: { label: 'Trong hạn mức', icon: 'check' },
  WARNING: { label: 'Sắp hết', icon: 'alert' },
  EXCEEDED: { label: 'Vượt hạn mức', icon: 'alert' },
}

/** 1 dong ngan sach: thanh tien do + trang thai (luon co icon + chu, khong chi dua vao mau). */
// eslint-disable-next-line react-refresh/only-export-components
export function BudgetRow({ budget, onEdit }: { budget: Budget; onEdit?: () => void }) {
  const status = STATUS[budget.status]
  const content = (
    <>
      <div className="budget__top">
        <span className="budget__name"><span aria-hidden>{budget.category.icon}</span> {budget.category.name}</span>
        <span className={`status status--${budget.status.toLowerCase()}`}>
          <Icon name={status.icon} size={14} /> {status.label}
        </span>
      </div>
      <div className="meter" role="progressbar" aria-valuemin={0} aria-valuemax={100}
        aria-valuenow={Math.min(budget.percent, 100)} aria-label={`${budget.category.name}: đã dùng ${budget.percent}%`}>
        <div className={`meter__fill meter__fill--${budget.status.toLowerCase()}`}
          style={{ width: `${Math.min(budget.percent, 100)}%` }} />
      </div>
      <div className="budget__numbers">
        <span className="mono">{formatMoney(budget.spent)} / {formatMoney(budget.limit)}</span>
        <span className="mono">
          {budget.remaining >= 0 ? `còn ${formatMoney(budget.remaining)}` : `lố ${formatMoney(-budget.remaining)}`}
        </span>
      </div>
    </>
  )
  return onEdit ? (
    <button className="budget budget--button" onClick={onEdit}>{content}</button>
  ) : (
    <div className="budget">{content}</div>
  )
}

export function BudgetsPage() {
  const { month } = useMonth()
  const budgets = useBudgets(month)
  const { data: categories = [] } = useCategories()
  const save = useSaveBudget()
  const remove = useDeleteBudget()
  const toast = useToast()

  const [categoryId, setCategoryId] = useState<number | ''>('')
  const [limit, setLimit] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)

  const expenseCategories = categories.filter((c) => c.type === 'EXPENSE')
  const editing = budgets.data?.find((b) => b.category.id === categoryId)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!categoryId) return setError('Chọn danh mục')
    if (!limit) return setError('Nhập hạn mức lớn hơn 0')
    try {
      const saved = await save.mutateAsync({ categoryId, limit })
      toast(`Đã đặt hạn mức ${formatMoney(saved.limit)} cho ${saved.category.name}`)
      setCategoryId('')
      setLimit(null)
      setError(null)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Lưu không thành công')
    }
  }

  async function onDelete(budget: Budget) {
    if (!window.confirm(`Bỏ hạn mức cho ${budget.category.name}?`)) return
    await remove.mutateAsync(budget.id)
    toast('Đã bỏ hạn mức')
    if (categoryId === budget.category.id) {
      setCategoryId('')
      setLimit(null)
    }
  }

  return (
    <>
      <header className="page-head">
        <h1>Ngân sách</h1>
        <p className="muted">Hạn mức chi mỗi tháng · đang xem {formatMonth(month).toLowerCase()}</p>
      </header>

      {budgets.data && budgets.data.length > 0 ? (
        <section className="card">
          {budgets.data.map((b) => (
            <BudgetRow key={b.id} budget={b} onEdit={() => { setCategoryId(b.category.id); setLimit(b.limit); setError(null) }} />
          ))}
        </section>
      ) : (
        !budgets.isPending && (
          <div className="empty">
            <p>Chưa đặt hạn mức nào.</p>
            <p className="muted">Đặt hạn mức cho danh mục hay tiêu lố, app sẽ báo khi dùng tới 80%.</p>
          </div>
        )
      )}

      <form className="card" onSubmit={onSubmit} aria-labelledby="budget-form-title">
        <header className="card__head">
          <h2 id="budget-form-title">{editing ? `Sửa hạn mức · ${editing.category.name}` : 'Đặt hạn mức mới'}</h2>
        </header>
        <div className="field-row">
          <label className="field field--grow" htmlFor="budget-category">
            <span className="field__label">Danh mục chi</span>
            <select id="budget-category" value={categoryId}
              onChange={(e) => {
                const id = e.target.value ? Number(e.target.value) : ''
                setCategoryId(id)
                setLimit(budgets.data?.find((b) => b.category.id === id)?.limit ?? null)
              }}>
              <option value="">Chọn danh mục…</option>
              {expenseCategories.map((c) => <option key={c.id} value={c.id}>{c.icon} {c.name}</option>)}
            </select>
          </label>
          <label className="field field--grow" htmlFor="budget-limit">
            <span className="field__label">Hạn mức mỗi tháng</span>
            <AmountInput id="budget-limit" value={limit} onChange={setLimit} />
          </label>
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        <div className="sheet__actions">
          {editing && (
            <button type="button" className="btn btn--ghost btn--danger" onClick={() => onDelete(editing)}>
              <Icon name="trash" size={18} /> Bỏ hạn mức
            </button>
          )}
          <button className="btn btn--primary" type="submit" disabled={save.isPending}>
            {editing ? 'Lưu hạn mức' : 'Đặt hạn mức'}
          </button>
        </div>
      </form>
    </>
  )
}
