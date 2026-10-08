# Thông Dong Quán POS — Release Notes

## Version 1.0.1 (08/10/2026)
> Phiên bản ổn định chính thức, tối ưu hóa in tem, hỗ trợ chọn Topping linh hoạt, giảm giá nhanh không cần PIN và tự động hóa đồng bộ dữ liệu.

---

### 1. In Tem Nhãn Ly (Label Printer)
- **Tự động xuống dòng thông minh:** Nâng cấp thuật toán ngắt dòng (`wrap`) theo từ và ký tự khi tên món dài, khắc phục triệt để lỗi mất chữ ở mép phải của tem 40mm x 30mm.
- **Bổ sung giá tiền sản phẩm:** In rõ ràng đơn giá từng món trên tem (`Giá: xx.xxx đ`).
- **Hiển thị Topping trên tem:** In chi tiết các topping kèm theo món (ví dụ: `(+Caramen (TP))`), giúp nhân viên pha chế dễ dàng quan sát và thao tác chính xác.

### 2. Tối Ưu Quy Trình Giảm Giá (Discount)
- **Xóa bỏ yêu cầu nhập mã PIN:** Thu ngân có thể áp dụng giảm giá (theo số tiền hoặc theo phần trăm) trực tiếp tại màn hình thanh toán mà không cần nhập mã PIN quản lý/nhân viên.
- **Tính toán trực tiếp theo thời gian thực:** Cập nhật ngay số tiền được giảm và tổng tiền thanh toán ngay khi nhập.

### 3. Hệ Thống Topping Đầy Đủ (Topping Support)
- **Room Database v8 (Migration 7 -> 8):** Bổ sung bảng `toppings` và bảng ánh xạ quan hệ nhiều-nhiều `product_toppings`.
- **Đồng bộ Master Data:** Tự động PULL danh mục Topping và cấu hình Topping theo từng món từ Web Admin về POS.
- **Giao diện chọn Topping trực quan:** Bấm trực tiếp vào món đã chọn trên đơn hàng (khung giỏ hàng bên phải tại `SaleActivity`) để mở popup chọn/bỏ chọn các Topping đi kèm.
- **Tính toán tự động:** Tự động cộng tiền Topping vào đơn giá dòng (`lineTotal`), cập nhật tổng tiền đơn hàng, lưu chi tiết vào cơ sở dữ liệu và in ra hóa đơn cũng như tem nhãn.

### 4. Tự Động PUSH Dữ Liệu Theo Lịch (Scheduled Auto Push)
- **Tích hợp Android WorkManager (`work-runtime-ktx:2.9.0`):** Đảm bảo dịch vụ chạy nền bền bỉ, tiết kiệm pin, hoạt động ổn định kể cả khi khóa màn hình hoặc tắt ứng dụng.
- **4 khung giờ cố định trong ngày:** Tự động gửi đơn hàng và ca làm việc chưa đồng bộ lên máy chủ vào lúc **10:00 sáng, 15:00 chiều, 18:00 và 21:00** (Giờ Việt Nam UTC+07).
- **Ràng buộc kết nối mạng (`NetworkType.CONNECTED`):** Nếu mất mạng WiFi vào khung giờ quy định, hệ thống sẽ tự động gửi bù ngay khi có kết nối mạng trở lại.
- **Tự động khôi phục lịch:** Duy trì lịch chạy tự động kể cả khi thiết bị POS khởi động lại máy.
- **Giao diện theo dõi:** Hiển thị rõ lịch trình và thời gian dự kiến của lần PUSH tiếp theo trong màn hình *Cấu hình Đồng bộ*.

### 5. Cấu Hình & Tối Ưu Hệ Thống
- Thiết lập `.gitignore` chuẩn cho Android project, loại bỏ hoàn toàn các file build trung gian khỏi kho lưu trữ Git.
- Cập nhật thông số ứng dụng: `versionCode 2`, `versionName 1.0.1`.
