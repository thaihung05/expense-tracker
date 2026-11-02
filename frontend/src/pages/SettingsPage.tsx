import { useState } from 'react'
import type { FormEvent } from 'react'
import { Icon } from '../components/Icon'
import { ApiError } from '../lib/api'
import { useAuth } from '../lib/auth'
import { useCategories, useCreateCategory, useDeleteCategory } from '../lib/queries'
import { useToast } from '../lib/toast'
import type { Category, CategoryType } from '../lib/types'

const ICONS = ['🏷️', '🐶', '👶', '🎁', '💄', '🏋️', '✈️', '☕', '🍺', '📱', '🚗', '💼', '🏦', '💡']

export function SettingsPage() {
  const { user, logout } = useAuth()
  const { data: categories = [] } = useCategories()
  const create = useCreateCategory()
  const remove = useDeleteCategory()
  const toast = useToast()

  const [name, setName] = useState('')
  const [type, setType] = useState<CategoryType>('EXPENSE')
  const [icon, setIcon] = useState(ICONS[0])
  const [error, setError] = useState<string | null>(null)

  async function onCreate(e: FormEvent) {
    e.preventDefault()
    if (!name.trim()) return setError('Nhập tên danh mục')
    try {
      const c = await create.mutateAsync({ name: name.trim(), type, icon })
      toast(`Đã thêm danh mục ${c.name}`)
      setName('')
      setError(null)
    } catch (err) {
      setError(err instanceof ApiError ? Object.values(err.fieldErrors)[0] ?? err.message : 'Không thêm được')
    }
  }

  async function onDelete(c: Category) {
    if (!window.confirm(`Xóa danh mục ${c.name}?`)) return
    try {
      await remove.mutateAsync(c.id)
      toast(`Đã xóa ${c.name}`)
    } catch (err) {
      toast(err instanceof ApiError ? err.message : 'Không xóa được', 'error')
    }
  }

  return (
    <>
      <header className="page-head">
        <h1>Tài khoản</h1>
      </header>

      <section className="card">
        <div className="account">
          <span className="avatar avatar--lg" aria-hidden>{user?.displayName.charAt(0).toUpperCase()}</span>
          <div>
            <p className="account__name">{user?.displayName}</p>
            <p className="muted">{user?.email}</p>
          </div>
        </div>
        <button className="btn btn--ghost btn--block" onClick={logout}>Đăng xuất</button>
      </section>

      <section className="card" aria-labelledby="cat-title">
        <header className="card__head"><h2 id="cat-title">Danh mục</h2></header>
        {(['EXPENSE', 'INCOME'] as const).map((t) => (
          <div key={t} className="cat-group">
            <h3 className="cat-group__title">{t === 'EXPENSE' ? 'Khoản chi' : 'Khoản thu'}</h3>
            <ul className="cat-list">
              {categories.filter((c) => c.type === t).map((c) => (
                <li key={c.id}>
                  <span><span aria-hidden>{c.icon}</span> {c.name}</span>
                  {c.isDefault ? (
                    <span className="muted small">Mặc định</span>
                  ) : (
                    <button className="icon-btn" onClick={() => onDelete(c)} aria-label={`Xóa ${c.name}`}>
                      <Icon name="trash" size={18} />
                    </button>
                  )}
                </li>
              ))}
            </ul>
          </div>
        ))}

        <form onSubmit={onCreate} className="cat-form">
          <h3 className="cat-group__title">Thêm danh mục riêng</h3>
          <div className="segmented segmented--small" role="tablist" aria-label="Loại danh mục">
            {(['EXPENSE', 'INCOME'] as const).map((t) => (
              <button key={t} type="button" role="tab" aria-selected={type === t} className="segmented__item"
                onClick={() => setType(t)}>
                {t === 'EXPENSE' ? 'Khoản chi' : 'Khoản thu'}
              </button>
            ))}
          </div>
          <div className="icon-pick" role="radiogroup" aria-label="Biểu tượng">
            {ICONS.map((i) => (
              <button key={i} type="button" role="radio" aria-checked={icon === i}
                className={`icon-pick__item${icon === i ? ' icon-pick__item--on' : ''}`} onClick={() => setIcon(i)}>
                {i}
              </button>
            ))}
          </div>
          <div className="field-row">
            <label className="field field--grow" htmlFor="cat-name">
              <span className="field__label">Tên danh mục</span>
              <input id="cat-name" value={name} maxLength={50} placeholder="vd: Thú cưng"
                onChange={(e) => setName(e.target.value)} />
            </label>
            <button className="btn btn--primary field-row__btn" type="submit" disabled={create.isPending}>Thêm</button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
        </form>
      </section>
    </>
  )
}
