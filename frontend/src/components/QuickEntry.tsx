import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../lib/api'
import { formatDayHeading, formatMoney } from '../lib/format'
import { useCategories, useParse, useSaveTransaction } from '../lib/queries'
import { useToast } from '../lib/toast'
import type { Category, ParseResult } from '../lib/types'
import { CategoryPicker } from './CategoryPicker'
import { useEditor } from './editor'
import { Icon } from './Icon'

const EXAMPLES = ['cafe 35k', 'grab 52k hôm qua', 'lương 15tr', '1tr2 tiền nhà', 'phở 45.000 30/10']

/**
 * O "go 1 cau la xong": go "cafe 35k hom qua", app doc ra so tien, ngay, danh muc
 * va in thanh 1 to bien lai de xac nhan. Enter la luu.
 */
export function QuickEntry() {
  const [text, setText] = useState('')
  const [result, setResult] = useState<ParseResult | null>(null)
  const [category, setCategory] = useState<Category | null>(null)
  const [picking, setPicking] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [printKey, setPrintKey] = useState(0)
  const latest = useRef('')
  const printedAmount = useRef<number | null | undefined>(undefined)

  const parse = useParse()
  const save = useSaveTransaction()
  const { data: categories = [] } = useCategories()
  const openEditor = useEditor()
  const toast = useToast()

  // Doc cau sau khi ngung go 350ms (khong goi API moi lan bam phim).
  useEffect(() => {
    const value = text.trim()
    latest.current = value
    if (!/\d/.test(value)) {
      setResult(null)
      printedAmount.current = undefined
      return
    }
    const timer = setTimeout(async () => {
      try {
        const res = await parse.mutateAsync(value)
        if (latest.current !== value) return // nguoi dung da go tiep
        // So tien moi -> "in" lai bien lai (chay lai hieu ung)
        if (printedAmount.current !== res.amount) {
          printedAmount.current = res.amount
          setPrintKey((k) => k + 1)
        }
        setResult(res)
        setCategory(res.category)
        setPicking(!res.category)
        setError(null)
      } catch (err) {
        if (latest.current === value) setError(err instanceof ApiError ? err.message : 'Không đọc được câu này')
      }
    }, 350)
    return () => clearTimeout(timer)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [text])

  const ready = result?.amount != null && category != null

  async function onSubmit(e: FormEvent) {
    e.preventDefault()
    if (!result || result.amount == null) {
      setError('Thêm số tiền vào câu, ví dụ "cafe 35k"')
      return
    }
    if (!category) {
      setPicking(true)
      setError('Chọn danh mục cho khoản này')
      return
    }
    try {
      const saved = await save.mutateAsync({
        input: {
          amount: result.amount,
          categoryId: category.id,
          occurredOn: result.occurredOn,
          note: result.note || null,
          learnKeyword: true,
        },
      })
      toast(`Đã lưu ${formatMoney(saved.amount)} · ${saved.category.name}`)
      setText('')
      setResult(null)
      setCategory(null)
      setPicking(false)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Lưu không thành công')
    }
  }

  function openDetails() {
    openEditor({
      draft: {
        amount: result?.amount,
        type: category?.type ?? 'EXPENSE',
        categoryId: category?.id,
        occurredOn: result?.occurredOn,
        note: result?.note,
        learnKeyword: true,
      },
    })
    setText('')
  }

  const hint = !result
    ? null
    : result.source === 'LEARNED' && category?.id === result.category?.id
      ? `App nhớ từ lần trước: "${result.matchedKeyword}"`
      : result.source === 'DICTIONARY' && category?.id === result.category?.id
        ? `Nhận ra từ "${result.matchedKeyword}"`
        : 'Chọn danh mục, lần sau app sẽ tự nhớ'

  return (
    <section className="quick" aria-labelledby="quick-title">
      <h2 id="quick-title" className="quick__title">Vừa tiêu gì?</h2>
      <form onSubmit={onSubmit} className="quick__form">
        <input
          className="quick__input"
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="cafe 35k hôm qua"
          aria-describedby="quick-help"
          autoComplete="off"
          enterKeyHint="done"
          maxLength={255}
        />
        <button className="quick__go" type="submit" disabled={!ready || save.isPending} aria-label="Lưu">
          <Icon name="enter" />
        </button>
      </form>

      {!result && !error && (
        <p id="quick-help" className="quick__help">
          Gõ như nhắn tin, ví dụ{' '}
          {EXAMPLES.map((ex, i) => (
            <span key={ex}>
              <span className="nowrap">
                <button type="button" className="link-btn" onClick={() => setText(ex)}>{ex}</button>
                {i < EXAMPLES.length - 1 ? ',' : ''}
              </span>{' '}
            </span>
          ))}
        </p>
      )}

      {error && <p className="form-error" role="alert">{error}</p>}

      {result && (
        <div className="receipt" key={printKey}>
          <div className="receipt__row receipt__meta">
            <span>BIÊN LAI</span>
            <span>{formatDayHeading(result.occurredOn).toUpperCase()}</span>
          </div>
          <div className="receipt__row">
            <span className="receipt__note">{result.note || '(không ghi chú)'}</span>
            <strong className="receipt__amount">
              {result.amount != null ? formatMoney(result.amount) : '— đ'}
            </strong>
          </div>
          <div className="receipt__rule" aria-hidden />
          <div className="receipt__row">
            <button type="button" className="receipt__category" onClick={() => setPicking((p) => !p)}
              aria-expanded={picking}>
              {category ? (
                <><span aria-hidden>{category.icon}</span> {category.name} <span className="muted">· đổi</span></>
              ) : (
                <span className="receipt__missing">Chọn danh mục</span>
              )}
            </button>
            <span className={`receipt__kind receipt__kind--${(category?.type ?? 'EXPENSE').toLowerCase()}`}>
              {category?.type === 'INCOME' ? 'Thu' : 'Chi'}
            </span>
          </div>
          {picking && (
            <div className="receipt__picker">
              <CategoryPicker compact categories={categories} type="EXPENSE" value={category?.id ?? null}
                onChange={(c) => { setCategory(c); setPicking(false); setError(null) }} />
              <CategoryPicker compact categories={categories} type="INCOME" value={category?.id ?? null}
                onChange={(c) => { setCategory(c); setPicking(false); setError(null) }} />
            </div>
          )}
          <div className="receipt__foot">
            <span className="receipt__hint"><Icon name="sparkle" size={14} /> {hint}</span>
            <button type="button" className="link-btn" onClick={openDetails}>Sửa chi tiết</button>
          </div>
        </div>
      )}
    </section>
  )
}
