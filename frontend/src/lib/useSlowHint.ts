import { useEffect, useState } from 'react'

/**
 * true neu viec dang cho (active) keo dai qua `afterMs`.
 * Dung de bao "may chu dang khoi dong": goi mien phi cua Render ngu khi khong co ai dung,
 * lan goi dau tien phai cho khoang 1 phut.
 */
export function useSlowHint(active: boolean, afterMs = 4000): boolean {
  const [elapsed, setElapsed] = useState(false)
  useEffect(() => {
    if (!active) return
    const timer = setTimeout(() => setElapsed(true), afterMs)
    return () => {
      clearTimeout(timer)
      setElapsed(false) // het cho -> lan sau dem lai tu dau
    }
  }, [active, afterMs])
  return active && elapsed
}

export const SLOW_SERVER_TEXT = 'Máy chủ đang khởi động (gói miễn phí), lần đầu có thể mất khoảng 1 phút…'
