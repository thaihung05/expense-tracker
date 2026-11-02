# Deploy miễn phí: Neon + Render + Vercel

Kết quả: một đường dẫn `https://...vercel.app` dùng được 24/7 trên điện thoại, cài được như app, gắn được vào CV.

```
Điện thoại ──https──> Vercel (giao diện React)
                          │  gọi API (VITE_API_URL)
                          ▼
                      Render (backend Spring Boot, Docker) ──> Neon (PostgreSQL)
```

Cả 3 dịch vụ đều **đăng ký bằng tài khoản GitHub** và có gói miễn phí. Nên chọn khu vực **Singapore** cho gần Việt Nam.

Làm theo đúng thứ tự: **Neon → Render → Vercel → quay lại Render sửa CORS**. Lý do: Render cần địa chỉ database, Vercel cần địa chỉ Render, rồi Render lại cần địa chỉ Vercel.

---

## Bước 1: Database trên Neon

1. Vào https://neon.tech → **Sign up** → chọn **Continue with GitHub**.
2. Tạo project:
   - Project name: `expense-tracker`
   - Postgres version: **17**
   - Region: **AWS Asia Pacific (Singapore)**
3. Ở trang project, bấm **Connect**:
   - **Tắt** công tắc *Connection pooling*. Flyway cần kết nối trực tiếp.
   - Copy chuỗi kết nối, có dạng:
     ```
     postgresql://USER:PASSWORD@HOST/neondb?sslmode=require&channel_binding=require
     ```
4. Tách chuỗi đó thành **3 giá trị** (dán tạm vào Notepad, **không** gửi cho ai):

   | Biến | Giá trị | Ví dụ |
   |---|---|---|
   | `DB_URL` | `jdbc:postgresql://` + HOST + `/neondb?sslmode=require` | `jdbc:postgresql://ep-cool-river-123456.ap-southeast-1.aws.neon.tech/neondb?sslmode=require` |
   | `DB_USERNAME` | phần USER | `neondb_owner` |
   | `DB_PASSWORD` | phần PASSWORD | (chuỗi ký tự ngẫu nhiên) |

   Lưu ý: Java dùng dạng `jdbc:postgresql://...`, và user, mật khẩu **không** nằm trong URL.

## Bước 2: Backend trên Render

1. Vào https://render.com → **Get Started** → **GitHub**.
2. Bấm **New +** → **Blueprint** → chọn repo `expense-tracker` (nếu không thấy, bấm *Configure account* để cho Render đọc repo).
3. Render đọc file [`render.yaml`](../render.yaml) và hiện service `expense-tracker-api`. Điền các ô còn trống:
   - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`: 3 giá trị ở bước 1
   - `CORS_ORIGINS`: tạm điền `http://localhost:5173` (sửa ở bước 4)
   - `JWT_SECRET`: không cần điền, Render tự tạo khóa ngẫu nhiên
4. Bấm **Apply**. Lần build đầu mất **5–10 phút**. Bấm vào service để xem tab **Logs**.
5. Khi trạng thái là **Live**, copy địa chỉ service (dạng `https://expense-tracker-api-xxxx.onrender.com`) và mở thêm `/actuator/health` phía sau. Thấy `{"status":"UP"}` là backend đã chạy và đã kết nối được Neon.

> Nếu Render đòi thẻ thanh toán ở mục Blueprint, dùng cách thủ công: **New + → Web Service** → chọn repo → Language: **Docker**, Root Directory: `backend`, Instance type: **Free**, Region: **Singapore** → thêm 5 biến môi trường như trên (tự đặt `JWT_SECRET` là chuỗi ngẫu nhiên dài từ 32 ký tự) → Health Check Path: `/actuator/health`.

## Bước 3: Frontend trên Vercel

1. Vào https://vercel.com → **Sign Up** → **Continue with GitHub**.
2. **Add New… → Project** → **Import** repo `expense-tracker`.
3. Cấu hình:
   - **Root Directory**: bấm *Edit* → chọn `frontend` ⚠️ (quan trọng)
   - Framework Preset: **Vite** (tự nhận)
   - **Environment Variables**: thêm `VITE_API_URL` = địa chỉ Render ở bước 2, **không** có dấu `/` ở cuối
4. Bấm **Deploy**. Khoảng 1 phút sau sẽ có địa chỉ dạng `https://expense-tracker-xxxx.vercel.app`.

## Bước 4: Cho phép frontend gọi backend (CORS)

1. Quay lại Render → service `expense-tracker-api` → **Environment**.
2. Sửa `CORS_ORIGINS` = địa chỉ Vercel ở bước 3, ví dụ `https://expense-tracker-xxxx.vercel.app` (không có `/` cuối).
3. **Save Changes**. Render tự deploy lại trong vài phút.

## Bước 5: Dùng trên điện thoại

1. Mở địa chỉ Vercel trên điện thoại → tạo tài khoản.
   Lần đầu có thể chờ khoảng 1 phút: gói miễn phí của Render "ngủ" sau 15 phút không ai dùng. App sẽ hiện thông báo trong lúc chờ.
2. Cài như app:
   - **iPhone (Safari):** nút Chia sẻ → *Thêm vào MH chính*
   - **Android (Chrome):** menu ⋮ → *Cài đặt ứng dụng*
3. Dán địa chỉ Vercel vào README (mục Demo) và vào CV.

---

## Từ giờ, cập nhật app thế nào?

Chỉ cần `git push` lên `main`: Render tự build lại backend (khi thư mục `backend/` đổi), Vercel tự build lại frontend.

## Gặp lỗi?

| Hiện tượng | Nguyên nhân thường gặp |
|---|---|
| App báo "Không kết nối được máy chủ" | `VITE_API_URL` sai, hoặc `CORS_ORIGINS` không khớp **chính xác** địa chỉ Vercel (thừa `/`, thiếu `https`). Sửa `VITE_API_URL` xong phải **Redeploy** trên Vercel. |
| Render log: `Unable to obtain connection` / `password authentication failed` | Sai `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`, hoặc thiếu `?sslmode=require` |
| Render log: `OutOfMemoryError` | Gói free chỉ 512MB, kiểm tra `JAVA_OPTS` đúng như `render.yaml` |
| Render log: `Flyway ... checksum mismatch` | Đã sửa một file migration cũ. Không bao giờ sửa file V1–V5, tạo file mới V6... |
| Mở `/giao-dich` bị 404 trên Vercel | Thiếu file `frontend/vercel.json` hoặc Root Directory không phải `frontend` |
