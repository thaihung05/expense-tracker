# Sổ thu chi: Expenditure App

Ứng dụng quản lý thu chi cá nhân, ưu tiên dùng trên điện thoại. Điểm khác biệt: **gõ một câu như nhắn tin** (`cafe 35k hôm qua`), app tự hiểu số tiền, ngày và danh mục, rồi **tự học** thói quen ghi chép của bạn.

<p>
  <img src="docs/screenshots/1-nhap-nhanh.jpg" width="240" alt="Gõ nhanh 'grab 52k hôm qua', app in ra biên lai với danh mục Đi lại">
  <img src="docs/screenshots/2-bao-cao.jpg" width="240" alt="Báo cáo tháng: tổng thu chi, biểu đồ chi theo ngày, chi theo danh mục">
  <img src="docs/screenshots/3-ngan-sach-toi.jpg" width="240" alt="Ngân sách ở giao diện tối, cảnh báo vượt hạn mức">
</p>

## Tính năng

- **Nhập bằng câu tự nhiên**: hiểu `35k`, `1tr2`, `1.5tr`, `45.000`, `2 triệu`; hiểu `hôm nay`, `hôm qua`, `hôm kia`, `30/10`. Có dấu hay không dấu đều được.
- **Tự đoán danh mục** bằng từ điển hơn 150 từ khóa tiếng Việt, ưu tiên cụm từ cụ thể nhất (`tiền nước` → Hóa đơn, `nước mía` → Ăn uống).
- **Tự học**: bạn chọn danh mục cho "gửi tiền mẹ" một lần, lần sau app tự chọn đúng.
- **Báo cáo tháng**: tổng thu, tổng chi, số dư, chi theo ngày và theo danh mục, xuất file CSV mở được bằng Excel.
- **Ngân sách**: đặt hạn mức chi mỗi tháng cho từng danh mục. Cảnh báo khi dùng tới 80% và khi vượt.
- **Danh mục riêng** cho từng người, ngoài các danh mục mặc định.
- **Đăng ký / đăng nhập** bằng JWT. Dữ liệu mỗi người hoàn toàn tách biệt.
- **PWA**: cài lên màn hình chính điện thoại, giao diện sáng/tối tự đổi theo máy.

## Công nghệ

| Phần | Công nghệ |
|---|---|
| Backend | Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Validation, Security + OAuth2 Resource Server) |
| Database | PostgreSQL 17, Flyway (quản lý phiên bản database) |
| Frontend | React 19, TypeScript, Vite, React Router, TanStack Query, Recharts |
| Test | JUnit 5, AssertJ, MockMvc, Testcontainers (PostgreSQL thật trong Docker) |
| Hạ tầng | Docker (multi-stage build), Docker Compose, Nginx, GitHub Actions |
| Tài liệu API | OpenAPI / Swagger UI (springdoc) |

## Kiến trúc

```mermaid
flowchart LR
    U[Điện thoại / trình duyệt<br/>React PWA] -- "HTTPS + JWT" --> N[Nginx]
    N -- "/api/*" --> C[Controller]
    subgraph Spring Boot
        C --> S[Service<br/>logic nghiệp vụ, transaction]
        S --> R[Repository<br/>Spring Data JPA]
    end
    R --> DB[(PostgreSQL)]
    F[Flyway migration] -. tạo / nâng cấp bảng .-> DB
```

Backend chia theo **tính năng** (package-by-feature). Mỗi tính năng có đủ Controller, Service, Repository riêng:

```
backend/src/main/java/com/kei/backend/
├── auth/         Đăng ký, đăng nhập, /me
├── security/     Cấu hình Spring Security, tạo/đọc JWT, @CurrentUserId
├── user/         Entity người dùng
├── category/     Danh mục mặc định + danh mục riêng
├── transaction/  Giao dịch: CRUD, lọc, phân trang, xuất CSV
├── quickentry/   Đọc câu tự nhiên, từ điển từ khóa, tự học từ khóa
├── report/       Tổng hợp theo tháng (SUM / GROUP BY ngay trong database)
├── budget/       Ngân sách và trạng thái OK / WARNING / EXCEEDED
└── common/       Xử lý lỗi chung, cấu hình, tiện ích
```

### Database

| Bảng | Nội dung |
|---|---|
| `users` | Tài khoản (mật khẩu mã hóa BCrypt) |
| `categories` | Danh mục. `user_id = NULL` là mặc định dùng chung |
| `transactions` | Giao dịch: số tiền (`BIGINT`, đơn vị đồng), ngày, ghi chú, danh mục |
| `budgets` | Hạn mức chi mỗi tháng theo danh mục |
| `keyword_rules` | Từ khóa app đã học: "tra sua" → Ăn uống |

Cấu trúc database được tạo bằng các file trong [`db/migration`](backend/src/main/resources/db/migration) (V1 → V5). Hibernate chỉ kiểm tra cho khớp (`ddl-auto=validate`), không tự sửa bảng.

## API chính

Tài liệu đầy đủ, có thể thử trực tiếp: **http://localhost:8080/swagger-ui.html** (bấm *Authorize* rồi dán token).

| Method | Đường dẫn | Mô tả |
|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | Đăng ký / đăng nhập, trả về token JWT |
| GET | `/api/auth/me` | Thông tin người đang đăng nhập |
| GET | `/api/categories?type=EXPENSE` | Danh mục (mặc định + của mình) |
| POST / DELETE | `/api/categories`, `/api/categories/{id}` | Thêm / xóa danh mục riêng |
| GET | `/api/transactions?month=2026-11&type=&categoryId=&page=&size=` | Danh sách giao dịch có lọc, phân trang |
| POST / PUT / DELETE | `/api/transactions`, `/api/transactions/{id}` | Thêm / sửa / xóa giao dịch |
| POST | `/api/transactions/parse` | Đọc câu `{"text": "cafe 35k hôm qua"}` |
| GET | `/api/transactions/export?month=2026-11` | Xuất CSV |
| GET | `/api/reports/summary?month=2026-11` | Tổng hợp tháng |
| GET / PUT / DELETE | `/api/budgets`, `/api/budgets/{id}` | Xem / đặt / bỏ ngân sách |

Mọi lỗi trả về cùng một định dạng JSON (RFC 9457 Problem Details):

```json
{ "status": 400, "title": "Bad Request", "detail": "Dữ liệu không hợp lệ",
  "errors": { "amount": "Số tiền phải lớn hơn 0" } }
```

## Chạy trên máy

Cần có: **JDK 25+**, **Node.js 22+**, **Docker Desktop**.

```bash
# 1. Bật PostgreSQL (cổng 5434)
docker compose up -d

# 2. Backend, cổng 8080 (terminal riêng)
cd backend
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# 3. Frontend, cổng 5173 (terminal riêng)
cd frontend
npm install
npm run dev
```

Mở http://localhost:5173 và tạo tài khoản.

### Chạy cả hệ thống bằng Docker

```bash
docker compose --profile full up -d --build
```

Mở http://localhost:8088. Nginx phục vụ giao diện và chuyển `/api` sang backend.

### Deploy miễn phí

Neon (database) + Render (backend) + Vercel (frontend), cấu hình sẵn trong [`render.yaml`](render.yaml) và [`frontend/vercel.json`](frontend/vercel.json). Hướng dẫn từng bước: **[docs/DEPLOY.md](docs/DEPLOY.md)**.

### Biến môi trường (khi deploy)

| Biến | Ý nghĩa |
|---|---|
| `DATABASE_URL` | Chuỗi kết nối kiểu `postgresql://user:pass@host/db` (Neon, Render...). Backend tự đổi sang JDBC |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Hoặc khai báo tách riêng (ưu tiên hơn `DATABASE_URL`) |
| `JWT_SECRET` | Khóa ký token, **bắt buộc đổi**, tối thiểu 32 ký tự |
| `CORS_ORIGINS` | Domain frontend được phép gọi API (nếu frontend deploy ở domain khác) |
| `VITE_API_URL` | (frontend, lúc build) địa chỉ backend nếu khác domain |

## Test

```bash
cd backend && ./mvnw test
```

56 test, cần Docker đang chạy:

- **Unit test** cho bộ đọc câu ([`QuickEntryParserTest`](backend/src/test/java/com/kei/backend/quickentry/QuickEntryParserTest.java)) và từ điển: hơn 30 trường hợp như `1tr2`, `45.000`, `hôm kia`, `30/10/2025`...
- **Integration test** ([`ApiIntegrationTest`](backend/src/test/java/com/kei/backend/ApiIntegrationTest.java)) gọi API qua HTTP với PostgreSQL thật (Testcontainers). Kiểm tra đăng nhập, phân quyền, tách dữ liệu giữa người dùng, tự học từ khóa, báo cáo, ngân sách và xuất CSV. "Hôm nay" được cố định bằng `Clock` để test không phụ thuộc ngày chạy.

GitHub Actions ([`ci.yml`](.github/workflows/ci.yml)) tự chạy toàn bộ test backend, lint và build frontend mỗi lần push.

## Các quyết định kỹ thuật

- **JWT stateless** (Spring Security OAuth2 Resource Server, HS256): server không lưu phiên, dễ chạy nhiều instance.
- **Không trả Entity ra ngoài**: mọi API trả DTO (`record`), nên đổi database không làm vỡ API.
- **Truy cập dữ liệu người khác trả 404, không phải 403**: không để lộ id nào tồn tại.
- **Tiền lưu bằng `BIGINT` (đồng)**, không dùng `double`, để tránh sai số làm tròn.
- **Thời gian lưu UTC**; "hôm nay" tính theo múi giờ người dùng qua `Clock` (`app.zone`).
- **Tổng hợp báo cáo bằng SQL** (`SUM ... GROUP BY`) thay vì kéo hết dữ liệu về Java; `join fetch` để tránh lỗi N+1 query.
- **Flyway**: mỗi thay đổi database là một file migration có phiên bản, không sửa file cũ.
- **Giao diện**: font Be Vietnam Pro (thiết kế cho tiếng Việt). Màu thu (xanh dương) và chi (hồng) được kiểm tra an toàn cho người mù màu; số tiền luôn có dấu +/− và vạch màu, không chỉ dựa vào màu. Trang báo cáo tải riêng (code splitting) để trang chủ mở nhanh.

## Hướng phát triển

- Refresh token, đổi mật khẩu
- Giao dịch định kỳ (tiền nhà, Netflix tự ghi mỗi tháng)
- Email / thông báo đẩy khi vượt ngân sách
- Deploy: frontend lên Vercel, backend lên Render/Railway, database lên Neon
- Đọc câu khó bằng AI khi bộ quy tắc không hiểu
