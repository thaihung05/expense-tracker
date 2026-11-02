import type { Category, CategoryType } from '../lib/types'

interface Props {
  categories: Category[]
  type: CategoryType
  value: number | null
  onChange: (category: Category) => void
  compact?: boolean
}

/** Luoi cac nut danh muc (chi hien danh muc dung loai thu/chi). */
export function CategoryPicker({ categories, type, value, onChange, compact }: Props) {
  const options = categories.filter((c) => c.type === type)
  return (
    <div className={`chips${compact ? ' chips--compact' : ''}`} role="radiogroup" aria-label="Danh mục">
      {options.map((c) => (
        <button
          key={c.id}
          type="button"
          role="radio"
          aria-checked={c.id === value}
          className={`chip${c.id === value ? ' chip--on' : ''}`}
          onClick={() => onChange(c)}
        >
          <span aria-hidden>{c.icon}</span> {c.name}
        </button>
      ))}
    </div>
  )
}
