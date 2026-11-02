import { currentMonth, formatMonth, shiftMonth } from '../lib/format'
import { useMonth } from '../lib/month'
import { Icon } from './Icon'

export function MonthSwitcher() {
  const { month, setMonth } = useMonth()
  const isCurrent = month === currentMonth()

  return (
    <div className="month-switcher">
      <button className="icon-btn" onClick={() => setMonth(shiftMonth(month, -1))} aria-label="Tháng trước">
        <Icon name="chevronLeft" />
      </button>
      <button
        className="month-switcher__label"
        onClick={() => setMonth(currentMonth())}
        disabled={isCurrent}
        title={isCurrent ? undefined : 'Về tháng này'}
      >
        {formatMonth(month)}
      </button>
      <button
        className="icon-btn"
        onClick={() => setMonth(shiftMonth(month, 1))}
        disabled={isCurrent}
        aria-label="Tháng sau"
      >
        <Icon name="chevronRight" />
      </button>
    </div>
  )
}
