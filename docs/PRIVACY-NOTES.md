# Ghi chú về quyền riêng tư và pháp luật Việt Nam

> Đây là ghi chú kỹ thuật của dự án, **không phải tư vấn pháp lý**. Trước khi vận hành cho nhiều người dùng thật, nên hỏi ý kiến luật sư.

Văn bản áp dụng (từ 01/01/2026):
- **Luật Bảo vệ dữ liệu cá nhân số 91/2025/QH15**
- **Nghị định 356/2025/NĐ-CP** hướng dẫn thi hành (thay Nghị định 13/2023/NĐ-CP)

## App đã làm gì để tuân thủ

| Yêu cầu | Căn cứ | Trong app |
|---|---|---|
| Đồng ý phải tự nguyện, rõ ràng, cho từng mục đích; im lặng không phải là đồng ý | Luật 91, Điều 9 | Ô tick **không chọn sẵn** khi đăng ký; backend từ chối nếu thiếu (`acceptedPrivacy`) |
| Đồng ý phải kiểm chứng được | Luật 91, Điều 9(3) | Lưu `privacy_accepted_at` + `privacy_version` (migration V6) |
| Cho biết loại dữ liệu, mục đích, bên kiểm soát, quyền của chủ thể trước khi đồng ý | Luật 91, Điều 9(2) | Trang `/quyen-rieng-tu`, liên kết ngay cạnh ô tick |
| Quyền xem, yêu cầu cung cấp dữ liệu | Luật 91, Điều 4 | Tài khoản → Tải toàn bộ dữ liệu (JSON) |
| Quyền xóa, rút lại sự đồng ý | Luật 91, Điều 4, 10, 14 | Tài khoản → Xóa tài khoản (xóa vĩnh viễn, dây chuyền mọi bảng) |
| Biện pháp bảo vệ dữ liệu | Luật 91 | HTTPS, BCrypt, tách dữ liệu theo người dùng, giới hạn đăng nhập sai, CSP, không log mật khẩu |
| Hạn chế thu thập | Nguyên tắc tối thiểu | Không thu thập vị trí, danh bạ, số tài khoản/thẻ; không cookie theo dõi |

## Điểm còn rủi ro

1. **Chuyển dữ liệu ra nước ngoài (Luật 91, Điều 20).** Dữ liệu lưu trên Neon và Render đặt tại Singapore. Theo luật, bên kiểm soát phải lập **hồ sơ đánh giá tác động chuyển dữ liệu xuyên biên giới** và gửi cơ quan chuyên trách (Bộ Công an) trong 60 ngày kể từ lần chuyển đầu tiên. Miễn trừ áp dụng khi **chủ thể tự chuyển dữ liệu của chính mình** (Điều 20(6)(c)).
2. **Dữ liệu tài chính.** Nghị định 356/2025 xếp lịch sử giao dịch tài chính tại tổ chức tín dụng vào nhóm nhạy cảm. Dữ liệu chi tiêu do người dùng tự ghi không phải dữ liệu của ngân hàng, nhưng vẫn nên đối xử thận trọng: chính sách khuyên người dùng không ghi số tài khoản, số thẻ vào ghi chú.
3. **Không xác minh email.** Người khác có thể đăng ký bằng email không phải của họ. App không gửi email nào nên không gây spam, nhưng email đó vẫn bị lưu.

## Cách giảm rủi ro cho bản demo

- **Cách an toàn nhất:** đặt `REGISTRATION_ENABLED=false` trên Render. App chỉ có tài khoản của chính bạn, tức là chủ thể tự chuyển dữ liệu của mình (được miễn theo Điều 20(6)(c)). Nhà tuyển dụng xem ảnh chụp, README và Swagger.
- Nếu mở đăng ký: chỉ coi là bản demo, không quảng bá rộng, theo dõi số người dùng, và lập hồ sơ theo Điều 20 trước khi có người dùng thật.
