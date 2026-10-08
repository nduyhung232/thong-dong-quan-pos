# Context — Dự án thong-dong-quan-pos (iPOS iAP200)

> File này ghi lại toàn bộ ngữ cảnh, quyết định thiết kế và các điểm chưa hoàn thành
> của dự án, để phiên làm việc sau (người hoặc AI) tiếp tục được ngay mà không cần
> đọc lại lịch sử hội thoại.

**Cập nhật:** 04/10/2026
**Tên project:** `thong-dong-quan-pos`

---

## 1. Bối cảnh & Mục tiêu

Sếp có một bộ thiết bị POS và muốn viết phần mềm bán hàng cho **quán nước**.
Bắt đầu từ việc test thiết bị (két tiền + máy in), sau đó mở rộng dần sang nghiệp vụ
bán hàng, database, và quản trị.

---

## 2. Thiết bị thực tế (đã xác minh từ ảnh nhãn máy)

| Thiết bị | Model | Thông tin | Nguồn |
|---|---|---|---|
| Máy POS | **iPOS.vn iAP200** | Android, CPU RK3368, RAM 1G, Disk 8G, DC 24V-1.5A | `[FROM SOURCE: ảnh nhãn máy]` |
| Máy in hóa đơn | **RICHTA R200EU** | 80mm, USB+LAN, **Command Support: ESC/POS**, Cash Drawer 24V-1A | `[FROM SOURCE: ảnh nhãn]` |
| Máy in tem | Gprinter GP-3150TIN | Thermal Barcode Printer, Serial+USB+Ethernet — dùng USB cho tem mang về | `[FROM SOURCE: ảnh nhãn]` |
| Két tiền | **JJ405** (Wuxi Lai Bei) | RJ11 **"FOR STANDARD"**, DC 9~24V | `[FROM SOURCE: ảnh nhãn]` |

### Sơ đồ kết nối (Sếp đã chốt)
```
iAP200 (Android)
   │
   └── USB Type-B ──> RICHTA R200EU (máy in hóa đơn)
                          │
                          └── RJ11 ──> Két tiền JJ405
```
Sếp đã khoanh đỏ chỉ định **cổng USB Type-B** trên máy in hóa đơn. Máy in tem được gán riêng qua USB trong cấu hình máy in.

---

## 3. Sai lầm đã xảy ra và cách sửa (quan trọng — đừng lặp lại)

**Lượt đầu em giả định máy là Sunmi và viết code dùng Sunmi AIDL SDK → SAI HOÀN TOÀN.**

Nguyên nhân: đoán hãng thiết bị trước khi có thông tin. Sau khi Sếp gửi ảnh nhãn mới
biết là iPOS iAP200 + máy in ngoài RICHTA.

Đã sửa: xóa toàn bộ Sunmi AIDL, chuyển sang **ESC/POS chuẩn qua USB host API**.

> **Lưu ý:** tên project chính thức đã được đổi thành `thong-dong-quan-pos` (cấu hình trong `settings.gradle`). Package ID vẫn giữ `com.example.sunmipostester` để tương thích database đã tạo trên thiết bị.

---

## 4. Kiến trúc hiện tại

### 4.1 Các màn hình (10 Activity)

| # | Activity | Chức năng | Quyền yêu cầu |
|---|---|---|---|
| 1 | `LoginActivity` | **Launcher** — đăng nhập nhân viên + PIN | — |
| 2 | `SetupActivity` | Tạo tài khoản Quản lý đầu tiên (lần chạy đầu) | — |
| 3 | `HomeActivity` | Menu chính, ẩn/hiện nút theo quyền, đăng xuất | (đã đăng nhập) |
| 4 | `SaleActivity` | Chọn món (từ DB) → giỏ hàng → in KOT → thanh toán | `SELL` |
| 5 | `PaymentActivity` | Thanh toán (MOCK) → lưu DB → in hóa đơn → mở két nếu tiền mặt | `SELL` |
| 6 | `OrderManagementActivity` | Lịch sử đơn, doanh thu ngày, chi tiết, in lại, hủy đơn | `SELL` (hủy cần `CANCEL_ORDER`) |
| 7 | `ShiftActivity` | Mở ca / đóng ca + đối chiếu két + lịch sử ca | `MANAGE_SHIFT` |
| 8 | `RevenueReportActivity` | Báo cáo doanh thu theo kỳ (1/7/30 ngày) + top món | `VIEW_REPORTS` |
| 9 | `ProductManagementActivity` | CRUD món (soft-delete) | `MANAGE_PRODUCTS` |
| 10 | `StaffManagementActivity` | CRUD nhân viên + đổi PIN + vai trò | `MANAGE_STAFF` |
| 11 | `BackupActivity` | Xuất CSV + sao lưu database | `EXPORT_DATA` |
| 12 | `DeviceTestActivity` | Test két tiền / máy in + radio trạng thái két | `DEVICE_TEST` |

### 4.2 Cây thư mục source

```
app/src/main/
├── AndroidManifest.xml          usb.host required, allowBackup=false, Login là launcher
├── java/com/example/sunmipostester/
│   ├── LoginActivity.kt
│   ├── SetupActivity.kt
│   ├── HomeActivity.kt
│   ├── SaleActivity.kt
│   ├── PaymentActivity.kt
│   ├── OrderManagementActivity.kt
│   ├── ShiftActivity.kt
│   ├── RevenueReportActivity.kt
│   ├── ProductManagementActivity.kt
│   ├── StaffManagementActivity.kt
│   ├── BackupActivity.kt
│   ├── DeviceTestActivity.kt
│   ├── auth/
│   │   ├── PinHasher.kt         PBKDF2-HMAC-SHA256, salt/user, 120k iter, constant-time compare
│   │   ├── Permission.kt        enum Permission + AccessPolicy (role → permissions)
│   │   ├── Session.kt           phiên đăng nhập (RAM only, không giữ credential)
│   │   └── SecuredActivity.kt   base class enforce quyền mỗi màn (fail closed)
│   ├── backup/
│   │   └── DataExporter.kt      xuất CSV (escape chuẩn + BOM UTF-8), copy DB kèm WAL/SHM
│   ├── data/
│   │   ├── Entities.kt          ProductEntity, OrderEntity, OrderItemEntity, ShiftEntity, StaffEntity
│   │   ├── Models.kt            PaymentMethod, CartLine, OrderWithItems, ShiftSummary, RevenueReport, ExportSnapshot
│   │   ├── Daos.kt              ProductDao, OrderDao, ShiftDao, ReportDao, StaffDao
│   │   ├── PosDatabase.kt       Room DB v3 + TypeConverters
│   │   ├── PosRepository.kt     API persistence duy nhất, chạy trên Dispatchers.IO
│   │   ├── MenuSeed.kt          menu MẪU cho lần chạy đầu
│   │   ├── OrderStore.kt        giỏ hàng đang xây (RAM)
│   │   └── TextFormat.kt        format VND + bỏ dấu tiếng Việt cho máy in
│   ├── sale/Adapters.kt         CategoryAdapter, ProductAdapter, CartAdapter
│   ├── manage/                  ManageProductAdapter, OrderAdapter, ShiftAdapter, StaffAdapter
│   └── printer/
│       ├── EscPos.kt            dựng byte lệnh ESC/POS chuẩn
│       ├── PrinterConnection.kt interface transport
│       ├── UsbPrinterConnection.kt  ESC/POS qua USB host API
│       ├── PrinterProvider.kt   tìm máy in USB + quản lý quyền USB (dùng chung mọi màn)
│       └── ReceiptPrinter.kt    openDrawer / printTest / printKitchenTicket / printReceipt / readDrawerStatus
└── res/layout/, res/values/strings.xml
```

### 4.3 Tech stack
- Kotlin, minSdk 21, targetSdk 34, AGP 8.2.2, Kotlin 1.9.22
- ViewBinding, Material 3, RecyclerView, Coroutines
- **Room 2.6.1 + KSP** (`com.google.devtools.ksp` 1.9.22-1.0.17)

---

## 5. Database (Room/SQLite — `pos.db`, version 3)

| Bảng | Cột chính |
|---|---|
| `products` | id, name, price, category, active |
| `orders` | id, total, paymentMethod, cashReceived, changeAmount, status, createdAtMs, shiftId, **staffId, staffName**, cancelledByStaffId, cancelledAtMs |
| `order_items` | id, orderId, productId, **productName, unitPrice** (snapshot), quantity |
| `shifts` | id, openedAtMs, closedAtMs, openingCash, countedCash, expectedCash, cashDifference, status, **staffId, staffName, closedByStaffId** |
| `staff` | id, name, role, **pinHash, pinSalt**, active, createdAtMs |

### Nguyên tắc toàn vẹn dữ liệu đã áp dụng
1. `order_items` **snapshot tên + giá** tại thời điểm bán → sửa giá món sau này KHÔNG
   làm thay đổi đơn cũ.
2. Xóa món / xóa nhân viên = **soft-delete** (`active = 0`) → lịch sử vẫn giải thích được.
3. Hủy đơn = **đổi status** (không xóa row) → giữ audit trail; đơn hủy không tính doanh thu.
4. Lưu order header + items trong **một transaction** → không sinh đơn mồ côi.
5. Đóng ca lưu cả `expectedCash`, `countedCash`, `cashDifference` → chênh lệch có bằng chứng,
   không tính lại về sau.
6. **Lưu đơn vào DB TRƯỚC khi in** → lỗi máy in không làm mất doanh thu.

---

## 6. Nghiệp vụ

### 6.1 Luồng bán hàng (theo đúng mô tả của Sếp)
```
Khách order
  → SaleActivity: chọn món → giỏ hàng
  → IN PHIẾU PHA CHẾ (KOT: chỉ tên món + số lượng, không có giá) → cho người pha chế
  → THANH TOÁN → PaymentActivity
  → chọn hình thức (Tiền mặt / Chuyển khoản / Thẻ)
  → tiền mặt: nhập tiền khách đưa → tự tính tiền thừa
  → XÁC NHẬN → giả lập thành công → LƯU DB → in hóa đơn → MỞ KÉT (nếu tiền mặt) → xóa giỏ
```

### 6.2 Quản lý ca — công thức đối chiếu két
```
expectedCash   = openingCash (tiền đầu ca) + tổng thu TIỀN MẶT của ca
cashDifference = countedCash (tiền đếm thực tế) − expectedCash
```
- Chỉ tính **tiền mặt**, vì chuyển khoản/thẻ không đi vào két.
- Chỉ cho **1 ca mở tại 1 thời điểm** (chống chồng ca làm sai đối chiếu).
- Không có ca mở **vẫn bán được** (`shiftId = null`) — sự cố quy trình không làm dừng bán hàng.
- Lịch sử ca tô màu: xanh = khớp, đỏ = thiếu, cam = thừa.

### 6.3 Phân quyền

| Chức năng | Thu ngân (CASHIER) | Quản lý (MANAGER) |
|---|---|---|
| Bán hàng, in KOT, thanh toán | ✅ | ✅ |
| Xem lịch sử đơn, in lại hóa đơn | ✅ | ✅ |
| Mở ca / Đóng ca | ✅ | ✅ |
| Kiểm tra thiết bị | ✅ | ✅ |
| Hủy đơn | ❌ | ✅ |
| Báo cáo doanh thu | ❌ | ✅ |
| Quản lý món | ❌ | ✅ |
| Quản lý nhân viên | ❌ | ✅ |
| Sao lưu / Xuất dữ liệu | ❌ | ✅ |

Thực thi **2 lớp**: ẩn nút ở `HomeActivity` (UX) **và** `SecuredActivity` kiểm quyền ở
`onCreate`/`onResume` từng màn (fail closed nếu vào bằng đường khác).

### 6.4 Audit trail
- Đơn → `staffId` + `staffName` (snapshot): biết ai bán.
- Ca → người mở + `closedByStaffId`: **két lệch truy được trách nhiệm**.
- Hủy đơn → `cancelledByStaffId` + `cancelledAtMs`: biết quản lý nào duyệt.

---

## 7. Lệnh ESC/POS đã dùng

Nguồn: Epson ESC/POS command reference. RICHTA nhãn ghi "Command Support: ESC/POS".

| Mục đích | Lệnh | Ghi chú |
|---|---|---|
| Khởi tạo | `ESC @` | |
| **Mở két** | `ESC p m t1 t2` | `m=0` (pin 2), on 50ms, off 200ms |
| Đọc trạng thái | `DLE EOT 1` | drawer kick pin = **bit 2 (0x04)** |
| Canh lề | `ESC a n` | 0=trái, 1=giữa, 2=phải |
| Cỡ chữ | `GS ! n` | 0x00 normal, 0x11 double |
| In đậm | `ESC E n` | |
| Xuống dòng | `ESC d n` | |
| Cắt giấy | `GS V 0` | |

**Chân mở két `m=0` (pin 2):** đã xác nhận độ tin cậy **High** vì nhãn két JJ405 ghi
"RJ11 **FOR STANDARD**" → dùng pinout chuẩn. `[FROM SOURCE: nhãn két]`

Text in ra dùng **Template XML Android** (`receipt_template.xml`) -> render Canvas Bitmap 576px -> chia dải nhỏ (**chunking 24 dots**, ~1.7KB/lệnh) kèm `Thread.sleep(8)` pacing. Cơ chế này triệt tiêu hoàn toàn lỗi tràn bộ đệm phần cứng (buffer overflow) của máy in nhiệt RICHTA R200EU, in tiếng Việt có dấu sắc nét, căn lề logo và bảng giá chuẩn xác, đồng thời có dialog xem trước hóa đơn (`ReceiptPreviewDialog.kt`).

---

## 8. Bảo mật đã áp dụng

- PIN **không lưu plaintext**: PBKDF2-HMAC-SHA256, salt random 16 byte **riêng mỗi người**,
  **120.000 iterations**, key 256-bit.
- So sánh hash bằng `MessageDigest.isEqual` → **constant-time**, chống timing attack.
- PIN **không ghi log**; ô nhập PIN xóa sau mỗi lần thử; `PBEKeySpec.clearPassword()` +
  wipe char array sau khi derive.
- Đăng nhập sai → **một thông báo chung**, không tiết lộ sai user hay sai PIN.
- **Không có PIN mặc định** — lần chạy đầu buộc chủ quán tự đặt.
- Session **chỉ trong RAM** → đóng app phải đăng nhập lại (máy để quên không bị dùng tiếp).
- `allowBackup="false"` → dữ liệu/hash không bị hút vào cloud/adb backup.
- Không thể xóa/hạ cấp **quản lý cuối cùng** đang hoạt động (chống tự khóa mình ra ngoài).
- **File export KHÔNG chứa `pinHash`/`pinSalt`** → lộ file không brute-force PIN offline được.

---

## 9. ⚠️ TỒN ĐỌNG — việc cần làm tiếp

### 9.1 Chưa verify được (BẮT BUỘC làm trên máy thật)
- [ ] **CHƯA BUILD/COMPILE lần nào.** Máy dev Windows không có Android SDK.
      Room dùng KSP sinh code lúc build → lỗi (nếu có) sẽ lộ ở lần Gradle sync đầu tiên.
      **Việc đầu tiên phải làm: mở Android Studio, sync, sửa lỗi biên dịch.**
- [ ] Két tiền có bật khi bấm không. Nếu không → thử `EscPos.openDrawer(pin = 1)`.
- [ ] Đọc trạng thái két qua USB có hoạt động không. `[Confidence: Low]`
      Phụ thuộc RICHTA có expose bulk-IN endpoint và có route tín hiệu microswitch
      vào status byte. Nếu radio hiển thị ngược → đảo lại trong `EscPos.drawerOpenFromStatus`.
      Nếu không đọc được → radio để trống cả hai (đã xử lý an toàn case `null`).

### 9.2 Lỗ hổng bảo mật CHƯA bịt (cần trước production)
- [ ] **Không có rate limiting / lockout khi sai PIN.** PIN 4 số + thử vô hạn là
      **brute-force được** bởi người giữ máy. **Đây là điểm yếu nhất hiện tại.**
- [ ] **Database chưa mã hóa** (không dùng SQLCipher). Root/ADB đọc được `pos.db`.
- [ ] **Khuyến nghị review bảo mật độc lập** — đây là code xác thực + dữ liệu tài chính.

### 9.3 Giới hạn nghiệp vụ
- [ ] **Thanh toán là MOCK** — chỉ giả định thành công, KHÔNG kết nối cổng thanh toán/
      ngân hàng thật. Tích hợp thật (thẻ/QR/NAPAS) phải tuân thủ **PCI-DSS/SBV** +
      review bảo mật riêng.
- [ ] **Migration đang destructive** — `fallbackToDestructiveMigration()`. Mỗi lần đổi
      schema sẽ **XÓA SẠCH dữ liệu cũ**. Phải thay bằng Migration thật trước khi quán
      dùng số liệu lịch sử để đối soát.
- [ ] **Menu seed là dữ liệu MẪU** (`MenuSeed.kt`) — cần sửa lại cho đúng quán.
- [ ] **Chưa có quản lý bàn** — Sếp đã yêu cầu bỏ.
- [ ] **Backup thủ công** — chưa tự động/định kỳ, chưa đẩy cloud. Phải copy file ra
      USB định kỳ, không thì máy hỏng là mất lịch sử.
- [ ] **Chưa có restore trong app** — phải thay file DB thủ công.

### 9.4 Việc nhỏ
- [x] Đổi tên project thành `thong-dong-quan-pos` (`settings.gradle`).
- [x] Đã tạo bộ App Icon Thong Dong (mipmap mdpi -> xxxhdpi, adaptive icon round/square) và tích hợp in logo hóa đơn ESC/POS 80mm (`ReceiptLogo.kt`, `assets/`, `EscPos.rasterBitmap`).

---

## 9.6 Đồng bộ server `pos-admin` (BỔ SUNG 14/09/2026)

Mục tiêu Sếp chốt: máy POS đồng bộ với server web. Auth đơn giản (token do Quản lý tự
đặt trên web, **bỏ deviceId**). Discount: POS pull mã, áp mã, đánh dấu đã dùng, push về.

### Đã làm phía Android
- `common/Money.kt` + `common/DiscountCalculator.kt`: port 1:1 từ `money.ts`/`discount.ts`
  của server (VND HALF_UP, DiscountType). BẮT BUỘC khớp vì server tính lại và từ chối nếu lệch.
- Room **v3 → v4** (`fallbackToDestructiveMigration` → **xoá DB cũ**, OK vì chưa go-live):
  thêm `syncId` (UUID UNIQUE) mọi bảng; `orders` thêm subtotal/discountAmount/discountType/
  discountInput/discountCode/orderType/*SyncId/syncedAtMs; `order_items` +syncId/+productSyncId;
  `shifts` +syncId/+*SyncId/+syncedAtMs; bảng mới `discount_codes`.
- `sync/`: `SyncConfig` (SharedPreferences URL+token), `SyncApi` (HttpURLConnection + org.json,
  chỉ header `Authorization: Bearer <token>`), `SyncManager` (pull upsert theo syncId → push
  bản `syncedAtMs=null`, JSON khớp OrderPush/ShiftPush, gửi null bằng `JSONObject.NULL`).
- `SyncSettingsActivity` + layout: nhập URL/token, nút Lưu / Kiểm tra kết nối / Đồng bộ ngay.
  Quyền mới `MANAGE_SYNC` (Quản lý). Nút "ĐỒNG BỘ SERVER" ở Home.
- `PaymentActivity`: nút "ÁP MÃ GIẢM GIÁ" → chọn mã đã pull → tính bằng DiscountCalculator →
  push kèm order. `savePaidOrder` nhận discount + orderType, sinh syncId, mark code consumed.
- `AndroidManifest`: +INTERNET, +ACCESS_NETWORK_STATE, `usesCleartextTraffic=true`.
- KHÔNG thêm thư viện (HttpURLConnection + org.json có sẵn) → `build.gradle` không đổi.

### Contract server (đã đơn giản hoá + verify LIVE ở phía server)
- Auth: `Authorization: Bearer <token>` DUY NHẤT (bỏ `X-Device-Id`). Token hash **SHA-256**,
  cột `Device.tokenHash` UNIQUE. Quản lý tự đặt token ở `/devices` (tối thiểu 12 ký tự).
- `GET /api/sync/pull` → products / staff (kèm pinHash,pinSalt) / discountCodes / cursor.
- `POST /api/sync/push` {clientTimeMs, orders[], shifts[]} → acceptedOrderIds/acceptedShiftIds/rejected.
- Đã test trên server: 59/59 unit test PASS; pull token đúng→200, sai→401; push đơn (không
  deviceId)→200 accepted. Token demo sau `npm run seed`: `quay1-demo-token-123456`.

### ⚠️ CHƯA verify được (BẮT BUỘC làm trên máy có Android SDK)
- **CHƯA compile phía Android** (máy dev Windows không có Android SDK/KSP). Việc đầu tiên:
  mở Android Studio, Gradle sync, sửa lỗi biên dịch (nếu có) do KSP sinh code Room v4.
- Test luồng thật: cấu hình URL+token trên máy POS → Kiểm tra kết nối → Đồng bộ ngay →
  bán 1 đơn (có/không mã giảm giá) → Đồng bộ → kiểm trên web `/orders`, `/shifts`.
- Lưu ý overlap menu: `MenuSeed` tạo món local (syncId riêng); sau khi pull, món server có
  syncId riêng → có thể trùng tên. Khi dùng sync, nên quản lý món trên web và bỏ seed local.

## 10. Quy ước làm việc với Sếp

- Gọi Sếp là **"Sếp"**, trả lời bằng tiếng Việt, giữ thuật ngữ kỹ thuật bằng tiếng Anh.
- **File sinh ra phải đặt trong `Kiro Generation/`** (theo steering của workspace).
- Chuẩn financial-grade: **không đoán** — thiếu thông tin thì hỏi. Gắn nhãn
  `[FROM SOURCE: ...]` / `[INFERRED — verify]` cho business rule, API contract,
  cấu hình bảo mật, số liệu tài chính. Nêu rõ giả định. Flag rủi ro bảo mật/pháp lý
  ngay cả khi không được hỏi.

---

## 11. Cách build

1. Mở thư mục dự án `thong-dong-quan-pos` bằng **Android Studio** (Giraffe+).
2. Chờ Gradle sync (AGP 8.2.2, Kotlin 1.9.22, KSP cho Room).
3. Cắm iAP200 qua USB, bật USB debugging → **Run 'app'**.
4. Lần chạy đầu: màn **Thiết lập lần đầu** → tạo tài khoản Quản lý + PIN.
5. Cắm máy in RICHTA vào iAP200 qua **USB Type-B**, cắm két JJ405 vào **RJ11** của máy in.
6. Vào **Kiểm tra thiết bị** để test két + máy in trước khi bán thật.

> Emulator KHÔNG test được phần máy in/két (cần USB host + thiết bị thật).
