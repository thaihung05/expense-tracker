import { createContext, useContext } from 'react'
import type { CategoryType, Transaction } from '../lib/types'

/** Du lieu dien san khi mo form (vd tu o go nhanh). */
export interface EditorDraft {
  amount?: number | null
  type?: CategoryType
  categoryId?: number | null
  occurredOn?: string
  note?: string
  learnKeyword?: boolean
}

export type EditorTarget = { transaction: Transaction } | { draft: EditorDraft }

/** Mo form them/sua giao dich tu bat ky trang nao. */
export const EditorContext = createContext<(target?: EditorTarget) => void>(() => {})

export function useEditor() {
  return useContext(EditorContext)
}
