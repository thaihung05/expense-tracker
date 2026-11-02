import { formatDayHeading, formatMoney, formatSigned } from '../lib/format'
import type { Transaction } from '../lib/types'
import { useEditor } from './editor'

/** Danh sach giao dich nhom theo ngay, moi ngay co tong chi. Cham vao 1 dong de sua. */
export function TransactionList({ items }: { items: Transaction[] }) {
  const openEditor = useEditor()
  const groups = new Map<string, Transaction[]>()
  for (const t of items) {
    const list = groups.get(t.occurredOn) ?? []
    list.push(t)
    groups.set(t.occurredOn, list)
  }

  return (
    <div className="tx-groups">
      {[...groups.entries()].map(([day, list]) => {
        const spent = list.filter((t) => t.type === 'EXPENSE').reduce((sum, t) => sum + t.amount, 0)
        return (
          <section key={day} className="tx-group">
            <header className="tx-group__head">
              <h3>{formatDayHeading(day)}</h3>
              {spent > 0 && <span className="muted mono">Chi {formatMoney(spent)}</span>}
            </header>
            <ul className="tx-list">
              {list.map((t) => (
                <li key={t.id}>
                  <button className="tx" onClick={() => openEditor({ transaction: t })}>
                    <span className="tx__icon" aria-hidden>{t.category.icon}</span>
                    <span className="tx__text">
                      <span className="tx__title">{t.note || t.category.name}</span>
                      {t.note && <span className="tx__sub">{t.category.name}</span>}
                    </span>
                    <span className={`tx__amount tx__amount--${t.type.toLowerCase()}`}>
                      {formatSigned(t.amount, t.type)}
                    </span>
                  </button>
                </li>
              ))}
            </ul>
          </section>
        )
      })}
    </div>
  )
}
