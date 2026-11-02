// Dinh dang tien, ngay, thang theo kieu Viet Nam.

const vnd = new Intl.NumberFormat('vi-VN')

/** 35000 -> "35.000 đ" */
export function formatMoney(amount: number): string {
  return `${vnd.format(amount)} đ`
}

/** 35000 -> "+35.000 đ" / "−35.000 đ" theo loai giao dich. */
export function formatSigned(amount: number, type: 'INCOME' | 'EXPENSE'): string {
  return `${type === 'INCOME' ? '+' : '−'}${formatMoney(amount)}`
}

/** Rut gon cho truc bieu do: 35000 -> "35k", 1200000 -> "1,2tr" */
export function formatShort(amount: number): string {
  if (amount >= 1_000_000) return `${trim(amount / 1_000_000)}tr`
  if (amount >= 1_000) return `${trim(amount / 1_000)}k`
  return String(amount)
}

function trim(n: number): string {
  return n.toFixed(1).replace(/\.0$/, '').replace('.', ',')
}

/** Ngay hom nay theo gio may, dang yyyy-MM-dd */
export function todayISO(): string {
  return toISODate(new Date())
}

export function toISODate(d: Date): string {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day}`
}

function parseISODate(iso: string): Date {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y, m - 1, d)
}

/** "2026-11-14" -> "Hôm nay" / "Hôm qua" / "Thứ Bảy, 14/11" */
export function formatDayHeading(iso: string): string {
  const today = todayISO()
  if (iso === today) return 'Hôm nay'
  const y = new Date()
  y.setDate(y.getDate() - 1)
  if (iso === toISODate(y)) return 'Hôm qua'
  const d = parseISODate(iso)
  const weekday = d.toLocaleDateString('vi-VN', { weekday: 'long' })
  return `${weekday.charAt(0).toUpperCase()}${weekday.slice(1)}, ${d.getDate()}/${d.getMonth() + 1}`
}

/** "2026-11-14" -> "14/11" */
export function formatShortDate(iso: string): string {
  const d = parseISODate(iso)
  return `${d.getDate()}/${d.getMonth() + 1}`
}

// ----- Thang dang "2026-11" -----

export function currentMonth(): string {
  return todayISO().slice(0, 7)
}

export function shiftMonth(month: string, delta: number): string {
  const [y, m] = month.split('-').map(Number)
  const d = new Date(y, m - 1 + delta, 1)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
}

export function formatMonth(month: string): string {
  const [y, m] = month.split('-').map(Number)
  return `Tháng ${m}, ${y}`
}
