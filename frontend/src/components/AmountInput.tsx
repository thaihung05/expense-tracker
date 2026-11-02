const vnd = new Intl.NumberFormat('vi-VN')

interface Props {
  id?: string
  value: number | null
  onChange: (value: number | null) => void
  autoFocus?: boolean
}

/** O nhap so tien: chi nhan chu so, tu them dau cham hang nghin (35000 -> 35.000). */
export function AmountInput({ id, value, onChange, autoFocus }: Props) {
  return (
    <div className="amount-input">
      <input
        id={id}
        inputMode="numeric"
        autoComplete="off"
        placeholder="0"
        autoFocus={autoFocus}
        value={value ? vnd.format(value) : ''}
        onChange={(e) => {
          const digits = e.target.value.replace(/\D/g, '').slice(0, 12)
          onChange(digits ? Number(digits) : null)
        }}
      />
      <span aria-hidden>đ</span>
    </div>
  )
}
