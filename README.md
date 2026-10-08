# thong-dong-quan-pos — iPOS iAP200 + RICHTA R200EU

App Android cho máy POS **iPOS.vn iAP200** dùng máy in hóa đơn **RICHTA R200EU** (ESC/POS)
và máy in tem **Gprinter GP-3150TIN** (TSPL).

## Thiết bị (đọc từ nhãn — đã xác minh)
| Thiết bị | Model | Ghi chú |
|---|---|---|
| Máy POS | iPOS.vn **iAP200** | Android, CPU RK3368, RAM 1G, Disk 8G |
| Máy in hóa đơn | **RICHTA R200EU** | 80mm, USB+LAN, **Command Support: ESC/POS**, **Cash Drawer 24V 1A** |
| Máy in tem | Gprinter GP-3150TIN | In tem; gán riêng trong **Bán hàng → Cấu hình → Cấu hình máy in** hoặc **Kiểm tra thiết bị** |
| Két tiền | **JJ405** | RJ11 "FOR STANDARD", DC 9~24V (Wuxi Lai Bei) |

> Két JJ405 cắm vào cổng RJ11/RJ12 của **RICHTA R200EU**. Vì vậy cả "test két" và
> "test in" đều gửi lệnh tới máy in RICHTA. Két dùng pinout **chuẩn** → lệnh mở
> `ESC p` với pin 2 (`m=0`) là đúng chuẩn.

## Kết nối
Máy in RICHTA nối iAP200 qua **cổng USB Type-B** (cổng vuông trên máy in). `[FROM SOURCE: ảnh Sếp chỉ định]`

## Đăng nhập & Phân quyền

App **bắt buộc đăng nhập** bằng nhân viên + mã PIN. Lần chạy đầu vào màn **Thiết lập lần đầu**
để tạo tài khoản Quản lý — **không có PIN mặc định**.

| Chức năng | Thu ngân | Quản lý |
|---|---|---|
| Bán hàng, gửi phiếu bàn, thanh toán | ✅ | ✅ |
| Xem lịch sử đơn, in lại hóa đơn | ✅ | ✅ |
| Mở ca / Đóng ca | ✅ | ✅ |
| Kiểm tra thiết bị | ✅ | ✅ |
| **Hủy đơn** | ❌ | ✅ |
| **Báo cáo doanh thu** | ❌ | ✅ |
| **Quản lý món** | ❌ | ✅ |
| **Quản lý nhân viên** | ❌ | ✅ |
| **Sao lưu / Xuất dữ liệu** | ❌ | ✅ |
| **Đồng bộ server** (`MANAGE_SYNC`) | ❌ | ✅ |

Cơ chế thực thi 2 lớp: ẩn nút trên Home (UX) **và** mỗi màn kế thừa `SecuredActivity`
tự kiểm tra quyền khi `onCreate`/`onResume` (fail closed nếu vào bằng đường khác).

### Bảo mật PIN đã áp dụng
- PIN **không lưu plaintext**. Lưu **PBKDF2-HMAC-SHA256**, salt random 16 byte riêng mỗi người,
  **120.000 iterations**, key 256-bit.
- So sánh bằng `MessageDigest.isEqual` (**constant-time**) → chống timing attack.
- PIN **không bao giờ ghi log**; ô nhập PIN được xóa sau mỗi lần thử. PBKDF2 fallback trên Android cũ tái sử dụng buffer để giảm cấp phát, vẫn giữ 120.000 vòng.
- Đăng nhập sai chỉ báo **một thông báo chung**, không tiết lộ sai user hay sai PIN.
- Session **chỉ trong RAM** → đóng app là phải đăng nhập lại (máy để quên không bị dùng tiếp).
- `allowBackup="false"` → dữ liệu và hash không bị hút vào cloud/adb backup.
- Không thể xóa/hạ cấp **quản lý cuối cùng** đang hoạt động (chống tự khóa mình ra ngoài).

### ⚠️ CHƯA làm — cần trước khi dùng production
- **Chưa có rate limiting / lockout** khi nhập sai PIN nhiều lần. PIN 4 số + không giới hạn
  số lần thử là **brute-force được** bởi người giữ máy. Đây là lỗ hổng cần bịt.
- **Chưa mã hóa database** (không dùng SQLCipher). Ai có root/ADB truy cập được file `pos.db`.
- **Khuyến nghị review bảo mật độc lập** trước khi vận hành thật.

## Truy vết trách nhiệm (audit trail)
- Mỗi **đơn** lưu `staffId` + `staffName` (snapshot) → biết ai bán.
- Mỗi **ca** lưu `staffId`/`staffName` người mở và `closedByStaffId` người đóng
  → **két lệch truy được trách nhiệm**.
- **Hủy đơn** lưu `cancelledByStaffId` + `cancelledAtMs` → biết quản lý nào duyệt hủy.

## Màn hình
**1. Trang chủ (HomeActivity)** — menu chính, hiện tên + vai trò người đang đăng nhập,
nút ẩn/hiện theo quyền, có nút Đăng xuất. Lần chạy đầu tự seed menu mẫu.

**2. Bán hàng (SaleActivity)**
- App mở thẳng màn order chia đôi: bên trái là danh sách món theo nhóm; bên phải là giỏ hàng, số lượng, thành tiền và duy nhất nút **THANH TOÁN**.
- Các ô nhập tiền tự thêm dấu phẩy phân cách hàng nghìn (ví dụ `100,000`).
- **LỊCH SỬ** mở giao dịch theo ngày, mặc định hôm nay; tải lười từng 10 đơn và cho chọn ngày khác. **CHI** ghi nội dung/số tiền/nhân viên/ca, trừ khỏi tiền két dự kiến và mở két.
- PULL/PUSH nằm trong menu **CẤU HÌNH** trên thanh đầu.
- Các ô nhập tiền tự thêm dấu phẩy phân cách hàng nghìn (ví dụ `100,000`) khi hiển thị.
- Bấm **THANH TOÁN** mở popup để chọn **Ăn tại chỗ / Mang về**, **Tiền mặt / Chuyển khoản**, và mức giảm theo số tiền hoặc phần trăm.
- Áp dụng giảm giá phải xác thực PIN của nhân viên đang đăng nhập/mở ca. Tiền mặt nhập tiền khách đưa, tự tính tiền thừa; xác nhận sẽ in hóa đơn và gửi lệnh mở két. Chuyển khoản yêu cầu xác nhận đã nhận đủ tiền, không mở két.
- Đơn mang về in thêm tem trên máy in tem; sau khi hoàn tất đơn, giỏ hàng được làm trống.
- Đơn ăn tại chỗ in hai bản trên máy in hóa đơn: phiếu bếp chỉ tên món/số lượng và hóa đơn thanh toán cho khách.
- Trong **Bán hàng → Cấu hình → Cấu hình máy in** (hoặc **Kiểm tra thiết bị**), chọn riêng thiết bị USB cho hóa đơn và tem; lựa chọn được lưu lại, chỉ cần cấu hình một lần và không cho gán một máy cho cả hai vai trò.

**4. Quản lý món (ProductManagementActivity)**
- Thêm / sửa món (tên, giá, nhóm).
- **Ngừng bán / Bán lại** (soft-delete) — giữ nguyên lịch sử đơn cũ.

**5. Quản lý đơn (OrderManagementActivity)**
- Chọn ngày (mặc định hôm nay), xem doanh thu/số đơn ngày đó.
- Danh sách nạp lười theo từng trang 10 đơn → bấm vào xem chi tiết.
- **In lại hóa đơn** (có đánh dấu "BAN IN LAI") và **Hủy đơn** (đổi status, không xóa).

**6. Quản lý ca (ShiftActivity)**
- **MỞ CA**: nhập tiền đầu ca (quỹ ban đầu trong két).
- Khi ca đang mở: xem trực tiếp thu tiền mặt, tổng doanh thu ca, số đơn, **tiền két dự kiến**.
- **ĐÓNG CA**: nhập số tờ theo từng mệnh giá; app cộng tổng kiểm đếm, lưu chuỗi kiểm toán mệnh giá và tính **chênh lệch**. Tiền két dự kiến = đầu ca + thu tiền mặt − khoản chi.
- Nút **CHI** trên màn bán hàng ghi nội dung/số tiền, lưu nhân viên + ca, trừ khoản chi khỏi tiền két dự kiến và mở két.
- Lịch sử ca có tô màu chênh lệch: xanh = khớp, đỏ = thiếu, cam = thừa.
- Chỉ cho **1 ca mở tại 1 thời điểm** (chống chồng ca làm sai đối chiếu két).
- Đơn hàng tự gắn `shiftId` của ca đang mở. Không có ca mở thì vẫn bán được (`shiftId = null`).

Công thức đối chiếu:
```
expectedCash   = openingCash + tổng thu TIỀN MẶT của ca
cashDifference = countedCash − expectedCash
```
Chỉ tính tiền mặt vì chuyển khoản/thẻ không đi vào két.

**7. Báo cáo doanh thu (RevenueReportActivity)**
- Chọn kỳ: **Hôm nay / 7 ngày / 30 ngày**.
- Tổng doanh thu, số đơn, trung bình/đơn.
- Tách doanh thu **theo hình thức thanh toán**.
- **Top 10 món bán chạy** (số lượng + doanh thu).
- Chỉ tính đơn `PAID`; đơn đã hủy bị loại khỏi báo cáo.

**8. Quản lý nhân viên (StaffManagementActivity)** — chỉ Quản lý
- Thêm nhân viên (tên, vai trò, PIN), sửa tên/vai trò.
- **Đổi PIN** — không cần PIN cũ, không đọc được PIN cũ, chỉ ghi đè.
- Ngừng hoạt động / Kích hoạt lại (soft-delete, giữ audit trail).

**9. Sao lưu & Xuất dữ liệu (BackupActivity)** — chỉ Quản lý
- **XUẤT CSV**: 1 file CSV mỗi bảng, gồm cả khoản chi tiền mặt, có BOM UTF-8 để Excel đọc đúng tiếng Việt.
- **SAO LƯU DATABASE**: copy `pos.db` + WAL/SHM để restore đầy đủ.
- File ghi vào `Android/data/<package>/files/export` (app-scoped, không cần xin quyền).
- **File xuất KHÔNG chứa PIN hash/salt** → lộ file cũng không brute-force được PIN.

**10. Kiểm tra thiết bị (DeviceTestActivity)** — 2 nút + radio trạng thái két.
- Có nút **GÁN MÁY IN HÓA ĐƠN / MÁY IN TEM**: cần cắm đồng thời hai máy in USB, chọn vai trò từng thiết bị; một máy không được gán cho cả hai.
- Khi thanh toán **tại chỗ**, máy in hóa đơn in riêng phiếu bếp (chỉ tên món/số lượng) và hóa đơn thanh toán cho khách.

## Đồng bộ với server web (pos-admin)

App POS đồng bộ dữ liệu với server `pos-admin` qua REST. Vào **Trang chủ → ĐỒNG BỘ SERVER**
(chỉ Quản lý) để cấu hình và chạy.

### Cấu hình
- **Địa chỉ server**: VD `http://192.168.1.10:3100` (hoặc URL public).
- **Token**: chuỗi do Quản lý tự đặt trên web (`/devices`, tối thiểu 12 ký tự). Máy POS
  gửi đúng token này qua header `Authorization: Bearer <token>` — **không có deviceId**.
- Lưu bằng SharedPreferences (`pos_sync_prefs`).

### Mô hình ownership (một chiều, không có conflict)
| Dữ liệu | Chiều | Ghi chú |
|---|---|---|
| products, staff (kèm pinHash/pinSalt), discount_codes | **pull** server → POS | upsert theo `syncId` |
| orders, shifts | **push** POS → server | gửi bản có `syncedAtMs = null` |

- **Pull** (`GET /api/sync/pull`): tải món, nhân viên (để đăng nhập offline), và mã giảm
  giá server gán cho máy này. Mã không còn được gán (và chưa tiêu) sẽ bị dọn khỏi máy.
- **Push** (`POST /api/sync/push`): đẩy đơn + ca chưa đồng bộ. Server tính lại tiền và
  **từ chối** nếu lệch; đơn được chấp nhận được đánh dấu `syncedAtMs`.
- **Idempotent**: mỗi bản ghi mang `syncId` (UUID). Đẩy lại không tạo trùng.

### Mã giảm giá (theo yêu cầu Sếp)
- POS **pull** mã từ server → lưu bảng `discount_codes`.
- Màn **Thanh toán** có nút **ÁP MÃ GIẢM GIÁ** → chọn mã khả dụng → tính giảm bằng
  `DiscountCalculator` (khớp server qua `money-vectors.json`).
- Khi dùng → đánh dấu `consumed` local (chống dùng lại offline); lần **push** đơn kèm
  `discountCode` → server ghi nhận đã tiêu và ngừng gửi mã đó ở pull sau.

### Bảo mật (mức "vừa phải", Sếp chốt)
- Token hash **SHA-256** ở server; chạy được qua **HTTP** (`usesCleartextTraffic=true`).
- ⚠️ Chạy qua internet công cộng **nên dùng HTTPS**. Token lưu SharedPreferences thường
  (không mã hoá) — cân nhắc EncryptedSharedPreferences nếu máy có nguy cơ bị truy cập.

## Database (Room / SQLite — `pos.db`, version 7)
> **v7** rebuild `cash_expenses` để sửa FK không khai báo có trong một số v6 APK; migration v6→v7 giữ toàn bộ các dòng chi. **v6** thêm `cash_expenses` và index lịch sử theo ngày; **v5** thêm `table_drafts` và `table_draft_items` (legacy; hiện không dùng). Các migration v4→v5→v6→v7 giữ dữ liệu. **v4** thêm `syncId` (UUID, UNIQUE) cho mọi bảng đồng bộ, các cột discount/orderType
> cho `orders`, cột `syncedAtMs` cho orders/shifts, và bảng `discount_codes`. Vì đang
> `fallbackToDestructiveMigration()`, nâng v3→v4 **XOÁ SẠCH DB cũ trên máy** (chấp nhận
> được vì chưa go-live — xem cảnh báo Migration bên dưới).

| Bảng | Nội dung |
|---|---|
| `products` | **+syncId, +updatedAtMs**; name, price, category, active |
| `orders` | **+syncId, +subtotal, +discountAmount, +discountType, +discountInput, +discountCode, +orderType, +shiftSyncId, +staffSyncId, +cancelledByStaffSyncId, +syncedAtMs**; total, paymentMethod, cashReceived, changeAmount, status, createdAtMs, shiftId, staffId, staffName, cancelledByStaffId, cancelledAtMs |
| `order_items` | **+syncId, +productSyncId**; snapshot productName + unitPrice |
| `shifts` | **+syncId, +staffSyncId, +closedByStaffSyncId, +syncedAtMs**; openedAtMs, closedAtMs, openingCash, countedCash, expectedCash, cashDifference, status, staffId, staffName, closedByStaffId |
| `staff` | **+syncId, +updatedAtMs**; name, role, pinHash, pinSalt (PBKDF2), active, createdAtMs |
| `discount_codes` *(mới)* | syncId, code, campaignSyncId, campaignName, valueType (AMOUNT/PERCENT), value, consumed, consumedByOrderSyncId, consumedAtMs |
| `table_drafts`, `table_draft_items` *(legacy)* | Bảng được giữ lại sau thay đổi luồng order; phiên bản hiện tại không dùng màn hình/bản nháp bàn |
| `cash_expenses` *(v6)* | Nội dung/số tiền chi, thời điểm, ca và nhân viên; dùng để đối chiếu số tiền két còn lại |

Nguyên tắc toàn vẹn dữ liệu đã áp dụng:
- `order_items` **snapshot giá** → sửa giá món sau này KHÔNG làm thay đổi đơn cũ.
- Xóa món là **soft-delete** (`active = 0`) → lịch sử đơn vẫn giải thích được.
- Hủy đơn là **đổi status** (không xóa row) → giữ audit trail; đơn hủy không tính doanh thu.
- Lưu order header + items trong **một transaction** → không sinh đơn mồ côi.
- Đóng ca lưu cả `expectedCash`, `countedCash`, `cashDifference` → chênh lệch két có bằng chứng, không tính lại về sau.

⚠️ **Migration**: đã có migration thật v4 → v5, v5 → v6 và v6 → v7 để giữ dữ liệu hiện có. Các đường
nâng cấp schema cũ chưa được khai báo (ví dụ v1 → v2) vẫn rơi vào `fallbackToDestructiveMigration()`
và **XÓA SẠCH dữ liệu**. Cần bổ sung migration tương ứng trước khi nâng cấp máy đang chạy phiên bản cũ.

## Kiến trúc
```
LoginActivity               màn đăng nhập nhân viên + PIN
SetupActivity               tạo tài khoản Quản lý đầu tiên (lần chạy đầu)
HomeActivity                menu chính + phân quyền hiển thị + đăng xuất
SaleActivity                màn order mặc định + popup chọn loại đơn / thanh toán / giảm giá
PaymentActivity             màn thanh toán cũ, không còn được gọi trong luồng order
ProductManagementActivity   CRUD món
OrderManagementActivity     lịch sử đơn + doanh thu ngày + in lại + hủy
ShiftActivity               mở ca / đóng ca + đối chiếu két + lịch sử ca
RevenueReportActivity       báo cáo doanh thu theo kỳ + top món
StaffManagementActivity     CRUD nhân viên + đổi PIN + vai trò
BackupActivity              xuất CSV + sao lưu database
DeviceTestActivity          test két / máy in
SyncSettingsActivity        cấu hình + chạy đồng bộ server (Quản lý)
 ├─ common/                 GIỮ KHỚP server qua money-vectors.json
 │   ├─ Money.kt            VND HALF_UP (1:1 với money.ts)
 │   └─ DiscountCalculator.kt  1:1 với discount.ts (+ enum DiscountType)
 ├─ sync/
 │   ├─ SyncConfig.kt       SharedPreferences: baseUrl + token
 │   ├─ SyncApi.kt          HttpURLConnection + org.json, Bearer token
 │   └─ SyncManager.kt      pull (upsert) + push (đơn/ca chưa đồng bộ)
 ├─ auth/
 │   ├─ PinHasher.kt        PBKDF2-HMAC-SHA256 + so sánh constant-time
 │   ├─ Permission.kt       enum quyền + AccessPolicy (role → permissions)
 │   ├─ Session.kt          phiên đăng nhập (RAM, không giữ credential)
 │   └─ SecuredActivity.kt  base class enforce quyền từng màn (fail closed)
 ├─ backup/DataExporter.kt  xuất CSV (escape đúng chuẩn) + copy DB kèm WAL/SHM
 ├─ data/
 │   ├─ Entities.kt        ProductEntity, OrderEntity, OrderItemEntity, ShiftEntity
 │   ├─ Models.kt          PaymentMethod, CartLine, OrderWithItems, ShiftSummary, RevenueReport
 │   ├─ Daos.kt            ProductDao, OrderDao, ShiftDao, ReportDao
 │   ├─ PosDatabase.kt     Room database + converters
 │   ├─ PosRepository.kt   API persistence duy nhất (chạy trên Dispatchers.IO)
 │   ├─ MenuSeed.kt        menu MẪU cho lần chạy đầu
 │   ├─ OrderStore.kt      giỏ hàng đang xây (RAM)
 │   └─ TextFormat.kt      format VND + bỏ dấu tiếng Việt cho máy in
 ├─ sale/Adapters.kt       adapters: category / product / cart
 ├─ manage/                adapters: ManageProductAdapter, OrderAdapter, ShiftAdapter, StaffAdapter
 └─ printer/
     ├─ EscPos.kt              lệnh ESC/POS chuẩn
     ├─ PrinterConnection.kt   interface transport
     ├─ UsbPrinterConnection   ESC/POS qua USB host
     ├─ PrinterProvider.kt     tìm máy in USB + quản lý quyền (dùng chung)
     └─ ReceiptPrinter.kt      openDrawer / printTest / printKitchenTicket / printReceipt / readDrawerStatus
```

## ⚠️ Giới hạn hiện tại (financial-grade)
- Nếu app gặp exception và crash, log sẽ được lưu trong vùng dữ liệu riêng của app. Khi mở lại, màn **Báo cáo sự cố** hiện log để sao chép/chia sẻ; log sẽ che các chuỗi số 4–8 chữ số để tránh lộ PIN.
- **Thanh toán là MOCK** — chỉ giả định thành công, KHÔNG kết nối cổng thanh toán/ngân hàng thật.
  Tích hợp thật (thẻ/QR/NAPAS) là hạng mục nhạy cảm, phải tuân thủ **PCI-DSS/SBV** + review bảo mật.
- **Menu seed là dữ liệu MẪU** (`MenuSeed.kt`) — sửa lại trong "Quản lý món" cho đúng quán.
- **Chưa có rate limiting khi nhập sai PIN** — xem mục Bảo mật PIN. Cần bịt trước production.
- **Database chưa mã hóa** — cân nhắc SQLCipher nếu máy có nguy cơ bị truy cập vật lý.
- **Backup phải làm thủ công** — chưa tự động/định kỳ, chưa đẩy lên cloud.
  Sếp cần copy file export ra USB/máy khác định kỳ, nếu không máy hỏng là mất lịch sử.
- **Migration đang destructive** — xem cảnh báo ở mục Database.
- **Chưa có restore trong app** — sao lưu DB xong phải restore thủ công (thay file).
- **Đồng bộ server**: token lưu SharedPreferences thường (không mã hoá); chạy HTTP cleartext
  (nên dùng HTTPS qua internet công cộng). Đồng bộ chạy **thủ công** (bấm "Đồng bộ ngay") —
  chưa tự động định kỳ/nền. Nâng schema v4 **xoá DB cũ** trên máy (chưa go-live nên chấp nhận).

## Build
1. Mở thư mục dự án `thong-dong-quan-pos` bằng **Android Studio**.
2. Gradle sync (AGP 8.2.2, Kotlin 1.9.22, minSdk 21).
3. Cài lên iAP200 (USB debugging) → Run.

## Lệnh ESC/POS đã dùng (nguồn: Epson ESC/POS reference)
- Mở két: `ESC p m t1 t2` `[FROM SOURCE: Epson ESC/POS "Generate pulse"]`
- In: `ESC @`, `ESC a`, `GS !`, `ESC E`, `ESC d`, `GS V` `[FROM SOURCE: Epson ESC/POS]`
- Đọc trạng thái: `DLE EOT 1`, drawer kick pin = bit 2 (0x04) `[FROM SOURCE: Epson ESC/POS "Transmit real-time status"]`

## ⚠️ Cần verify trên máy thật (financial-grade)
1. **Chân mở két (`m`)**: két JJ405 ghi "RJ11 FOR STANDARD" → dùng pin 2 (`m=0`),
   khớp code hiện tại. `[FROM SOURCE: nhãn két] [Confidence: High]`
   (Dự phòng: nếu vì lý do nào đó không bật, thử `EscPos.openDrawer(pin = 1)`.)
2. **Đọc trạng thái két**: JJ405 "standard" thường có microswitch báo trạng thái,
   nhưng đọc được hay không phụ thuộc RICHTA có route tín hiệu vào status byte và
   trả qua USB hay không. Mapping high/low → open/closed tùy công tắc; nếu radio
   hiển thị ngược, đảo lại trong `EscPos.drawerOpenFromStatus`.
   `[INFERRED — verify] [Confidence: Low]`
3. **Đọc status qua USB**: nếu máy in RICHTA không expose bulk-IN endpoint qua USB,
   app không đọc được trạng thái → radio để trống cả hai (đã xử lý case này an toàn).
   `[INFERRED — verify] [Confidence: Medium]`
4. Build compile được; vẫn cần xác minh trên thiết bị POS và máy in thật.
5. **Đồng bộ server**: phần server (`../pos-admin`) đã test LIVE (pull/push bằng token thật).
  Phía Android cần test luồng: cấu hình URL+token → Kiểm tra kết nối → Đồng bộ.
   `[INFERRED — verify] [Confidence: Medium]`
6. **Máy in**: hóa đơn gửi ESC/POS Windows-1258 (code table 33) để in tiếng Việt; cần kiểm tra firmware RICHTA có hỗ trợ code table này. Máy in tem gửi TSPL; cần thử khổ/khe hở/font trên GP-3150TIN.
7. **Đồng bộ khoản chi**: khoản chi hiện lưu tại POS; `pos-admin` cần nhận và đối soát khoản chi để chấp nhận expectedCash đã trừ chi khi đồng bộ ca.
