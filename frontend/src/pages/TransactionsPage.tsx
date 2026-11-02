import { useState } from 'react'
import { TransactionList } from '../components/TransactionList'
import { formatMonth } from '../lib/format'
import { useMonth } from '../lib/month'
import { useTransactions } from '../lib/queries'
import type { CategoryType } from '../lib/types'

const FILTERS: { value: CategoryType | null; label: string }[] = [
  { value: null, label: 'Tất cả' },
  { value: 'EXPENSE', label: 'Khoản chi' },
  { value: 'INCOME', label: 'Khoản thu' },
]

export function TransactionsPage() {
  const { month } = useMonth()
  const [type, setType] = useState<CategoryType | null>(null)
  const query = useTransactions(month, type)
  const items = query.data?.pages.flatMap((p) => p.items) ?? []
  const total = query.data?.pages[0]?.totalItems ?? 0

  return (
    <>
      <header className="page-head">
        <h1>Giao dịch</h1>
        <p className="muted">{query.isPending ? 'Đang tải…' : `${total} giao dịch · ${formatMonth(month)}`}</p>
      </header>

      <div className="segmented segmented--small" role="tablist" aria-label="Lọc theo loại">
        {FILTERS.map((f) => (
          <button key={f.label} role="tab" aria-selected={type === f.value}
            className="segmented__item" onClick={() => setType(f.value)}>
            {f.label}
          </button>
        ))}
      </div>

      {query.isError && <p className="form-error" role="alert">{query.error.message}</p>}

      {!query.isPending && items.length === 0 ? (
        <div className="empty"><p>Không có giao dịch nào phù hợp.</p></div>
      ) : (
        <TransactionList items={items} />
      )}

      {query.hasNextPage && (
        <button className="btn btn--ghost btn--block" onClick={() => query.fetchNextPage()}
          disabled={query.isFetchingNextPage}>
          {query.isFetchingNextPage ? 'Đang tải…' : 'Tải thêm'}
        </button>
      )}
    </>
  )
}
