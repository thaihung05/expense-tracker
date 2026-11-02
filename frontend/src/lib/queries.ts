// Cac "hook" lay / sua du lieu bang TanStack Query: tu cache, tu tai lai sau khi sua.

import { useInfiniteQuery, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './api'
import type {
  Budget,
  Category,
  CategoryType,
  MonthlySummary,
  Page,
  ParseResult,
  Transaction,
  TransactionInput,
} from './types'

const PAGE_SIZE = 30

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => api<Category[]>('/api/categories'),
    staleTime: 5 * 60_000,
  })
}

export function useSummary(month: string) {
  return useQuery({
    queryKey: ['summary', month],
    queryFn: () => api<MonthlySummary>(`/api/reports/summary?month=${month}`),
  })
}

export function useRecentTransactions(month: string, size = 8) {
  return useQuery({
    queryKey: ['transactions', month, 'recent', size],
    queryFn: () => api<Page<Transaction>>(`/api/transactions?month=${month}&size=${size}`),
  })
}

export function useTransactions(month: string, type: CategoryType | null) {
  return useInfiniteQuery({
    queryKey: ['transactions', month, type ?? 'ALL'],
    initialPageParam: 0,
    queryFn: ({ pageParam }) =>
      api<Page<Transaction>>(
        `/api/transactions?month=${month}&page=${pageParam}&size=${PAGE_SIZE}${type ? `&type=${type}` : ''}`,
      ),
    getNextPageParam: (last) => (last.page + 1 < last.totalPages ? last.page + 1 : undefined),
  })
}

export function useBudgets(month: string) {
  return useQuery({
    queryKey: ['budgets', month],
    queryFn: () => api<Budget[]>(`/api/budgets?month=${month}`),
  })
}

/** Sau khi them/sua/xoa giao dich: cac so lieu lien quan deu phai tinh lai. */
function useInvalidateMoney() {
  const qc = useQueryClient()
  return () =>
    Promise.all([
      qc.invalidateQueries({ queryKey: ['transactions'] }),
      qc.invalidateQueries({ queryKey: ['summary'] }),
      qc.invalidateQueries({ queryKey: ['budgets'] }),
    ])
}

export function useParse() {
  return useMutation({
    mutationFn: (text: string) => api<ParseResult>('/api/transactions/parse', 'POST', { text }),
  })
}

export function useSaveTransaction() {
  const invalidate = useInvalidateMoney()
  return useMutation({
    mutationFn: ({ id, input }: { id?: number; input: TransactionInput }) =>
      id
        ? api<Transaction>(`/api/transactions/${id}`, 'PUT', input)
        : api<Transaction>('/api/transactions', 'POST', input),
    onSuccess: invalidate,
  })
}

export function useDeleteTransaction() {
  const invalidate = useInvalidateMoney()
  return useMutation({
    mutationFn: (id: number) => api<void>(`/api/transactions/${id}`, 'DELETE'),
    onSuccess: invalidate,
  })
}

export function useSaveBudget() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: { categoryId: number; limit: number }) => api<Budget>('/api/budgets', 'PUT', input),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  })
}

export function useDeleteBudget() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api<void>(`/api/budgets/${id}`, 'DELETE'),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['budgets'] }),
  })
}

export function useCreateCategory() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: { name: string; type: CategoryType; icon: string }) =>
      api<Category>('/api/categories', 'POST', input),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['categories'] }),
  })
}

export function useDeleteCategory() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (id: number) => api<void>(`/api/categories/${id}`, 'DELETE'),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['categories'] }),
  })
}
