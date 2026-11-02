import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../lib/api'
import { formatMoney, todayISO } from '../lib/format'
import { useCategories, useDeleteTransaction, useSaveTransaction } from '../lib/queries'
import { useToast } from '../lib/toast'
import type { CategoryType } from '../lib/types'
import { AmountInput } from './AmountInput'
import { CategoryPicker } from './CategoryPicker'
import type { EditorTarget } from './editor'
import { Icon } from './Icon'

interface Props {
  target: EditorTarget
  onClose: () => void
}

/** Form them / sua giao dich, hien dang "tam truot tu duoi len" tren dien thoai. */
export function TransactionSheet({ target, onClose }: Props) {
  const editing = 'transaction' in target ? target.transaction : null
  const draft = 'draft' in target ? target.draft : {}

  const { data: categories = [] } = useCategories()
  const save = useSaveTransaction()
  const remove = useDeleteTransaction()
  const toast = useToast()

  const [type, setType] = useState<CategoryType>(editing?.type ?? draft.type ?? 'EXPENSE')
  const [amount, setAmount] = useState<number | null>(editing?.amount ?? draft.amount ?? null)
  const [categoryId, setCategoryId] = useState<number | null>(editing?.category.id ?? draft.categoryId ?? null)
  const [occurredOn, setOccurredOn] = useState(editing?.occurredOn ?? draft.occurredOn ?? todayISO())
  const [note, setNote] = useState(editing?.note ?? draft.note ?? '')
  const [error, setError] = useState<string | null>(null)
  const panelRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    document.body.classList.add('no-scroll')
    return () => {
      document.removeEventListener('keydown', onKey)
      document.body.classList.remove('no-scroll')
    }
  }, [onClose])

  const switchType = (next: CategoryType) => {
    setType(next)
    const current = categories.find((c) => c.id === categoryId)
    if (current && current.type !== next) setCategoryId(null)
  }

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!amount) return setError('Nhập số tiền lớn hơn 0')
    if (!categoryId) return setError('Chọn một danh mục')
    setError(null)
    try {
      const saved = await save.mutateAsync({
        id: editing?.id,
        input: { amount, categoryId, occurredOn, note: note.trim() || null, learnKeyword: draft.learnKeyword },
      })
      toast(`${editing ? 'Đã cập nhật' : 'Đã lưu'} ${formatMoney(saved.amount)} · ${saved.category.name}`)
      onClose()
    } catch (err) {
      setError(err instanceof ApiError ? Object.values(err.fieldErrors)[0] ?? err.message : 'Lưu không thành công')
    }
  }

  async function onDelete() {
    if (!editing || !window.confirm('Xóa giao dịch này? Không thể hoàn tác.')) return
    try {
      await remove.mutateAsync(editing.id)
      toast('Đã xóa giao dịch')
      onClose()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Xóa không thành công')
    }
  }

  return (
    <div className="sheet-backdrop" onClick={(e) => e.target === e.currentTarget && onClose()}>
      <div className="sheet" role="dialog" aria-modal="true" aria-labelledby="sheet-title" ref={panelRef}>
        <header className="sheet__head">
          <h2 id="sheet-title">{editing ? 'Sửa giao dịch' : 'Thêm giao dịch'}</h2>
          <button className="icon-btn" onClick={onClose} aria-label="Đóng">
            <Icon name="close" />
          </button>
        </header>

        <form onSubmit={onSubmit} className="sheet__body">
          <div className="segmented" role="tablist" aria-label="Loại giao dịch">
            {(['EXPENSE', 'INCOME'] as const).map((t) => (
              <button
                key={t}
                type="button"
                role="tab"
                aria-selected={type === t}
                className={`segmented__item segmented__item--${t.toLowerCase()}`}
                onClick={() => switchType(t)}
              >
                {t === 'EXPENSE' ? 'Khoản chi' : 'Khoản thu'}
              </button>
            ))}
          </div>

          <label className="field" htmlFor="amount">
            <span className="field__label">Số tiền</span>
            <AmountInput id="amount" value={amount} onChange={setAmount} autoFocus={!editing && !draft.amount} />
          </label>

          <div className="field">
            <span className="field__label">Danh mục</span>
            <CategoryPicker
              categories={categories}
              type={type}
              value={categoryId}
              onChange={(c) => setCategoryId(c.id)}
            />
          </div>

          <div className="field-row">
            <label className="field" htmlFor="date">
              <span className="field__label">Ngày</span>
              <input id="date" type="date" value={occurredOn} max="2100-12-31" required
                onChange={(e) => setOccurredOn(e.target.value)} />
            </label>
            <label className="field field--grow" htmlFor="note">
              <span className="field__label">Ghi chú</span>
              <input id="note" value={note} maxLength={255} placeholder="vd: cafe với bạn"
                onChange={(e) => setNote(e.target.value)} />
            </label>
          </div>

          {error && <p className="form-error" role="alert">{error}</p>}

          <div className="sheet__actions">
            {editing && (
              <button type="button" className="btn btn--ghost btn--danger" onClick={onDelete} disabled={remove.isPending}>
                <Icon name="trash" size={18} /> Xóa
              </button>
            )}
            <button type="submit" className="btn btn--primary" disabled={save.isPending}>
              {save.isPending ? 'Đang lưu…' : editing ? 'Lưu thay đổi' : 'Lưu giao dịch'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
