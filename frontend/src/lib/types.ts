// Kieu du lieu khop voi JSON backend tra ve.

export type CategoryType = 'INCOME' | 'EXPENSE'

export interface Category {
  id: number
  name: string
  type: CategoryType
  icon: string | null
  isDefault: boolean
}

export interface Transaction {
  id: number
  amount: number
  type: CategoryType
  note: string | null
  occurredOn: string // yyyy-MM-dd
  category: Category
  createdAt: string
}

export interface TransactionInput {
  amount: number
  categoryId: number
  occurredOn: string
  note?: string | null
  learnKeyword?: boolean
}

export interface Page<T> {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
}

export interface ParseResult {
  amount: number | null
  occurredOn: string
  note: string
  category: Category | null
  matchedKeyword: string | null
  source: 'LEARNED' | 'DICTIONARY' | null
}

export interface CategoryTotal {
  categoryId: number
  name: string
  icon: string | null
  type: CategoryType
  total: number
}

export interface DailyPoint {
  date: string
  income: number
  expense: number
}

export interface MonthlySummary {
  month: string
  totalIncome: number
  totalExpense: number
  balance: number
  expenseByCategory: CategoryTotal[]
  incomeByCategory: CategoryTotal[]
  daily: DailyPoint[]
}

export type BudgetStatus = 'OK' | 'WARNING' | 'EXCEEDED'

export interface Budget {
  id: number
  category: Category
  limit: number
  spent: number
  remaining: number
  percent: number
  status: BudgetStatus
}

export interface User {
  id: number
  email: string
  displayName: string
}

export interface AuthResponse {
  token: string
  user: User
}

export interface PublicConfig {
  registrationEnabled: boolean
  privacyVersion: string
}
