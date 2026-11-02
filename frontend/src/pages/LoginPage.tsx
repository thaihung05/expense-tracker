import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../lib/api'
import { useAuth } from '../lib/auth'
import { SLOW_SERVER_TEXT, useSlowHint } from '../lib/useSlowHint'

export function LoginPage() {
  const { login, register } = useAuth()
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [displayName, setDisplayName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [busy, setBusy] = useState(false)
  const slow = useSlowHint(busy)

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    setFieldErrors({})
    try {
      if (mode === 'login') await login(email, password)
      else await register(email, password, displayName)
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message)
        setFieldErrors(err.fieldErrors)
      } else {
        setError('Có lỗi xảy ra, vui lòng thử lại.')
      }
    } finally {
      setBusy(false)
    }
  }

  const isRegister = mode === 'register'

  return (
    <main className="auth">
      <div className="auth__brand">
        <span className="auth__mark" aria-hidden>₫</span>
        <h1>Sổ thu chi</h1>
        <p>Gõ “cafe 35k” là xong. Cuối tháng biết tiền đi đâu.</p>
      </div>

      <form className="auth__card" onSubmit={onSubmit} noValidate>
        <h2>{isRegister ? 'Tạo tài khoản' : 'Đăng nhập'}</h2>

        {isRegister && (
          <label className="field" htmlFor="name">
            <span className="field__label">Tên của bạn</span>
            <input id="name" value={displayName} onChange={(e) => setDisplayName(e.target.value)}
              autoComplete="name" required maxLength={100} />
            {fieldErrors.displayName && <span className="field__error">{fieldErrors.displayName}</span>}
          </label>
        )}

        <label className="field" htmlFor="email">
          <span className="field__label">Email</span>
          <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)}
            autoComplete="email" required />
          {fieldErrors.email && <span className="field__error">{fieldErrors.email}</span>}
        </label>

        <label className="field" htmlFor="password">
          <span className="field__label">Mật khẩu{isRegister ? ' (ít nhất 8 ký tự)' : ''}</span>
          <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)}
            autoComplete={isRegister ? 'new-password' : 'current-password'} required minLength={isRegister ? 8 : undefined} />
          {fieldErrors.password && <span className="field__error">{fieldErrors.password}</span>}
        </label>

        {error && Object.keys(fieldErrors).length === 0 && <p className="form-error" role="alert">{error}</p>}

        <button className="btn btn--primary btn--block" type="submit" disabled={busy}>
          {busy ? 'Đang xử lý…' : isRegister ? 'Tạo tài khoản' : 'Đăng nhập'}
        </button>
        {slow && <p className="muted small" role="status">{SLOW_SERVER_TEXT}</p>}

        <p className="auth__switch">
          {isRegister ? 'Đã có tài khoản?' : 'Chưa có tài khoản?'}{' '}
          <button type="button" className="link-btn"
            onClick={() => { setMode(isRegister ? 'login' : 'register'); setError(null); setFieldErrors({}) }}>
            {isRegister ? 'Đăng nhập' : 'Tạo tài khoản mới'}
          </button>
        </p>
      </form>
    </main>
  )
}
