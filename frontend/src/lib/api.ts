// Ham goi API dung chung: tu gan token, doi loi backend (Problem Details) thanh ApiError de hien thi.

const BASE_URL = import.meta.env.VITE_API_URL ?? ''
const TOKEN_KEY = 'thuchi.token'

export const tokenStore = {
  get(): string | null {
    try {
      return localStorage.getItem(TOKEN_KEY)
    } catch {
      return null
    }
  },
  set(token: string) {
    try {
      localStorage.setItem(TOKEN_KEY, token)
    } catch {
      // trinh duyet chan luu tru: van dung duoc trong phien nay
    }
  },
  clear() {
    try {
      localStorage.removeItem(TOKEN_KEY)
    } catch {
      // bo qua
    }
  },
}

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

let onUnauthorized: (() => void) | null = null

/** Goi khi token het han / khong hop le (de dang xuat va ve trang dang nhap). */
export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler
}

type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'

async function send(path: string, method: Method, body?: unknown): Promise<Response> {
  const headers: Record<string, string> = {}
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  let res: Response
  try {
    res = await fetch(BASE_URL + path, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Không kết nối được máy chủ. Kiểm tra mạng rồi thử lại.')
  }

  if (res.status === 401 && token) onUnauthorized?.()
  if (!res.ok) {
    let problem: { detail?: string; errors?: Record<string, string> } | null = null
    try {
      problem = await res.json()
    } catch {
      // khong co body JSON
    }
    throw new ApiError(res.status, problem?.detail ?? fallbackMessage(res.status), problem?.errors ?? {})
  }
  return res
}

export async function api<T>(path: string, method: Method = 'GET', body?: unknown): Promise<T> {
  const res = await send(path, method, body)
  if (res.status === 204) return undefined as T
  return (await res.json()) as T
}

/** Tai file (vd CSV) kem token, roi kich hoat "luu file" tren trinh duyet. */
export async function download(path: string, fallbackName: string) {
  const res = await send(path, 'GET')
  const disposition = res.headers.get('Content-Disposition') ?? ''
  const name = /filename="?([^";]+)"?/.exec(disposition)?.[1] ?? fallbackName
  const url = URL.createObjectURL(await res.blob())
  const link = document.createElement('a')
  link.href = url
  link.download = name
  link.click()
  URL.revokeObjectURL(url)
}

function fallbackMessage(status: number): string {
  if (status === 401) return 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.'
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này.'
  if (status === 404) return 'Không tìm thấy dữ liệu.'
  if (status >= 500) return 'Máy chủ đang gặp sự cố, thử lại sau ít phút.'
  return 'Có lỗi xảy ra, vui lòng thử lại.'
}
