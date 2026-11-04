import { Link } from 'react-router'

/** Phien ban phai khop voi PrivacyPolicy.VERSION o backend. */
export const PRIVACY_VERSION = '2026-11-04'
const CONTACT_URL = 'https://github.com/thaihung05/expense-tracker/issues'

/**
 * Chinh sach quyen rieng tu. Noi dung bam theo Luat Bao ve du lieu ca nhan 91/2025/QH15 (Dieu 9):
 * truoc khi dong y, nguoi dung phai biet loai du lieu, muc dich, ben kiem soat va quyen cua minh.
 */
export function PrivacyPage() {
  return (
    <main className="doc">
      <Link to="/" className="link-btn">← Quay lại</Link>
      <h1>Chính sách quyền riêng tư</h1>
      <p className="muted">Phiên bản {PRIVACY_VERSION} · Áp dụng cho ứng dụng “Sổ thu chi”</p>

      <section>
        <h2>1. Ứng dụng này là gì</h2>
        <p>
          “Sổ thu chi” là <strong>dự án cá nhân phục vụ học tập và giới thiệu năng lực</strong> của tác giả (GitHub:{' '}
          <a href="https://github.com/thaihung05" target="_blank" rel="noreferrer">thaihung05</a>), phi thương mại, không
          quảng cáo. Tác giả là bên kiểm soát và xử lý dữ liệu cá nhân của bạn trong ứng dụng.
        </p>
      </section>

      <section>
        <h2>2. Dữ liệu được thu thập</h2>
        <ul>
          <li><strong>Tài khoản:</strong> email, tên hiển thị, mật khẩu (chỉ lưu dạng đã mã hóa một chiều BCrypt, không ai đọc lại được).</li>
          <li><strong>Dữ liệu bạn tự nhập:</strong> các khoản thu chi (số tiền, ngày, danh mục, ghi chú), ngân sách, danh mục riêng.</li>
          <li><strong>Từ khóa app tự học</strong> từ ghi chú của bạn (ví dụ “trà sữa” → Ăn uống), để lần sau tự chọn danh mục.</li>
          <li><strong>Thời điểm và phiên bản chính sách</strong> bạn đã đồng ý.</li>
        </ul>
        <p>
          Ứng dụng <strong>không</strong> thu thập vị trí, danh bạ, số tài khoản hay số thẻ ngân hàng; không dùng cookie
          theo dõi hay công cụ phân tích hành vi. Mã đăng nhập (token) được lưu trong trình duyệt của bạn.
        </p>
      </section>

      <section>
        <h2>3. Mục đích sử dụng</h2>
        <p>
          Duy nhất để cung cấp chức năng quản lý thu chi <strong>cho chính bạn</strong>: lưu giao dịch, đọc câu nhập nhanh,
          lập báo cáo và theo dõi ngân sách. Dữ liệu <strong>không</strong> được bán, chia sẻ cho bên thứ ba, dùng để quảng
          cáo hay huấn luyện AI.
        </p>
      </section>

      <section>
        <h2>4. Nơi lưu trữ (ngoài lãnh thổ Việt Nam)</h2>
        <p>Dữ liệu được lưu và xử lý trên các dịch vụ đám mây đặt ở nước ngoài:</p>
        <ul>
          <li><strong>Neon</strong>: cơ sở dữ liệu PostgreSQL, máy chủ tại Singapore.</li>
          <li><strong>Render</strong>: máy chủ ứng dụng (backend), tại Singapore.</li>
          <li><strong>Vercel</strong>: phân phối giao diện (không lưu dữ liệu thu chi của bạn).</li>
        </ul>
        <p>Mọi kết nối đều được mã hóa bằng HTTPS.</p>
      </section>

      <section>
        <h2>5. Thời gian lưu</h2>
        <p>
          Dữ liệu được lưu cho tới khi bạn xóa tài khoản. Vì đây là dự án demo, ứng dụng có thể ngừng hoạt động; khi đó toàn
          bộ dữ liệu sẽ bị xóa.
        </p>
      </section>

      <section>
        <h2>6. Quyền của bạn</h2>
        <ul>
          <li><strong>Xem và tải về</strong> toàn bộ dữ liệu: mục <em>Tài khoản → Tải toàn bộ dữ liệu</em>.</li>
          <li><strong>Sửa, xóa</strong> từng giao dịch, danh mục, ngân sách bất kỳ lúc nào.</li>
          <li>
            <strong>Rút lại sự đồng ý và xóa dữ liệu</strong>: mục <em>Tài khoản → Xóa tài khoản</em>. Tài khoản cùng mọi dữ
            liệu liên quan bị xóa vĩnh viễn ngay lập tức.
          </li>
          <li>
            <strong>Khiếu nại, yêu cầu khác</strong>: gửi qua{' '}
            <a href={CONTACT_URL} target="_blank" rel="noreferrer">GitHub Issues của dự án</a>.
          </li>
        </ul>
      </section>

      <section>
        <h2>7. Bảo mật và khuyến nghị</h2>
        <p>
          Ứng dụng dùng HTTPS, mã hóa mật khẩu, tách riêng dữ liệu từng người và giới hạn số lần đăng nhập sai. Tuy vậy không
          hệ thống nào an toàn tuyệt đối, nên bạn <strong>không nên ghi số tài khoản, số thẻ, mật khẩu hay thông tin nhạy cảm
          khác vào ghi chú</strong>, và nên dùng mật khẩu riêng cho ứng dụng này. Nếu xảy ra sự cố lộ dữ liệu, tác giả sẽ
          thông báo trên trang này và qua email bạn đã đăng ký.
        </p>
      </section>

      <section>
        <h2>8. Thay đổi chính sách</h2>
        <p>
          Khi nội dung thay đổi, phiên bản ở đầu trang sẽ được cập nhật. Thay đổi làm mở rộng loại dữ liệu hoặc mục đích xử lý
          sẽ cần bạn đồng ý lại.
        </p>
      </section>
    </main>
  )
}
