import { useState } from 'react'
import type { FormEvent } from 'react'
import { Icon } from '../components/Icon'
import { Link } from 'react-router'
import { api, ApiError, download } from '../lib/api'
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
      <YourDataSection />
    </>
  )
}

/**
 * Quyen cua nguoi dung voi du lieu ca nhan (Luat 91/2025/QH15):
 * tai ve toan bo du lieu, xoa tai khoan (= rut lai su dong y + xoa vinh vien).
 */
function YourDataSection() {
  const { logout } = useAuth()
  const toast = useToast()
  const [confirming, setConfirming] = useState(false)
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function onExport() {
    try {
      await download('/api/account/export', 'du-lieu-cua-toi.json')
    } catch {
      toast('Không tải được dữ liệu, thử lại sau', 'error')
    }
  }

  async function onDelete(e: FormEvent) {
    e.preventDefault()
    if (!window.confirm('Xóa vĩnh viễn tài khoản và TOÀN BỘ dữ liệu? Không thể khôi phục.')) return
    setBusy(true)
    setError(null)
    try {
      await api<void>('/api/account/delete', 'POST', { password })
      logout()
      toast('Đã xóa tài khoản và toàn bộ dữ liệu')
    } catch (err) {
      setError(err instanceof ApiError ? Object.values(err.fieldErrors)[0] ?? err.message : 'Không xóa được')
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="card" aria-labelledby="data-title">
      <header className="card__head"><h2 id="data-title">Dữ liệu của bạn</h2></header>
      <p className="muted small">
        Bạn có quyền xem, tải về và xóa toàn bộ dữ liệu của mình.{' '}
        <Link to="/quyen-rieng-tu">Chính sách quyền riêng tư</Link>
      </p>
      <button className="btn btn--ghost btn--block" onClick={onExport}>
        <Icon name="download" size={18} /> Tải toàn bộ dữ liệu (JSON)
      </button>

      {!confirming ? (
        <button className="btn btn--ghost btn--danger btn--block" onClick={() => setConfirming(true)}>
          <Icon name="trash" size={18} /> Xóa tài khoản
        </button>
      ) : (
        <form className="danger-zone" onSubmit={onDelete}>
          <p className="small">
            Tài khoản, mọi giao dịch, ngân sách, danh mục riêng và từ khóa đã học sẽ bị <strong>xóa vĩnh viễn</strong>.
            Nên tải dữ liệu về trước.
          </p>
          <label className="field" htmlFor="delete-password">
            <span className="field__label">Nhập mật khẩu để xác nhận</span>
            <input id="delete-password" type="password" value={password} autoComplete="current-password"
              onChange={(e) => setPassword(e.target.value)} required />
          </label>
          {error && <p className="form-error" role="alert">{error}</p>}
          <div className="sheet__actions">
            <button type="button" className="btn btn--ghost" onClick={() => { setConfirming(false); setPassword('') }}>
              Hủy
            </button>
            <button type="submit" className="btn btn--primary btn--destructive" disabled={busy || !password}>
              {busy ? 'Đang xóa…' : 'Xóa vĩnh viễn'}
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
