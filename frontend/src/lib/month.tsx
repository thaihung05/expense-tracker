import { createContext, useContext, useState } from 'react'
import type { ReactNode } from 'react'
import { currentMonth } from './format'

interface MonthState {
  month: string
  setMonth: (month: string) => void
}

const MonthContext = createContext<MonthState | null>(null)

/** Thang dang xem ("2026-11"), dung chung cho moi trang. */
export function MonthProvider({ children }: { children: ReactNode }) {
  const [month, setMonth] = useState(currentMonth)
  return <MonthContext.Provider value={{ month, setMonth }}>{children}</MonthContext.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useMonth(): MonthState {
  const ctx = useContext(MonthContext)
  if (!ctx) throw new Error('useMonth phai dung ben trong <MonthProvider>')
  return ctx
}
