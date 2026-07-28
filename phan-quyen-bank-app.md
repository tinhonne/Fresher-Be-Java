# Thiết kế Phân quyền — Bank App (User – Role – Permission)

## 1. Mô hình quan hệ

```
User (N) ←→ (N) Role (N) ←→ (N) Permission
```

- Một `User` có nhiều `Role`, một `Role` thuộc nhiều `User`
- Một `Role` có nhiều `Permission`, một `Permission` thuộc nhiều `Role`
- `Role` là dữ liệu **linh động** — Admin tạo/sửa/xoá qua UI, không hardcode bằng enum
- Phân quyền dựa trên **Permission** (`hasAuthority("XXX")`), không hardcode theo tên Role trong code nghiệp vụ

## 2. Ba Role trong hệ thống

| Role | Vai trò |
|---|---|
| **Employee** | Nhân viên giao dịch — nghiệp vụ cơ bản với khách hàng |
| **Manager** | Quản lý chi nhánh — duyệt giao dịch, quản lý nhân viên (Employee) |
| **Admin** | Quản trị hệ thống phân quyền — **không** thao tác nghiệp vụ tài chính |

> **Nguyên tắc tách biệt nhiệm vụ (segregation of duties):** Admin chỉ quản lý User/Role/Permission, hoàn toàn không có quyền trên Customer/Account/Transaction — tránh 1 người vừa quyết định luật chơi vừa tham gia nghiệp vụ tài chính.

## 3. Danh sách Permission

| Code | Mô tả | Employee | Manager | Admin |
|---|---|---|---|---|
| `CUSTOMER_VIEW` | Xem thông tin khách hàng | ✅ | ✅ | ❌ |
| `CUSTOMER_CREATE` | Tạo hồ sơ khách hàng mới | ✅ | ✅ | ❌ |
| `CUSTOMER_UPDATE` | Cập nhật thông tin khách hàng | ✅ | ✅ | ❌ |
| `ACCOUNT_VIEW` | Xem thông tin tài khoản/số dư | ✅ | ✅ | ❌ |
| `ACCOUNT_CREATE` | Mở tài khoản mới cho khách hàng | ✅ | ✅ | ❌ |
| `ACCOUNT_FREEZE` | Phong toả tài khoản | ❌ | ✅ | ❌ |
| `TRANSACTION_VIEW` | Xem lịch sử giao dịch | ✅ | ✅ | ❌ |
| `TRANSACTION_CREATE` | Thực hiện giao dịch cho khách hàng | ✅ | ✅ | ❌ |
| `TRANSACTION_APPROVE` | Duyệt giao dịch vượt hạn mức | ❌ | ✅ | ❌ |
| `TRANSACTION_REVERSE` | Hoàn/huỷ giao dịch | ❌ | ✅ | ❌ |
| `REPORT_VIEW` | Xem báo cáo | ✅ | ✅ | ❌ |
| `USER_VIEW` | Xem thông tin nhân viên | ❌ | ✅ | ✅ |
| `USER_CREATE` | Tạo tài khoản nhân viên mới | ❌ | ✅ | ✅ |
| `USER_LOCK` | Khoá/mở tài khoản nhân viên | ❌ | ✅ | ✅ |
| `USER_ASSIGN_RESTRICTED_ROLE` | Gán role Manager/Admin cho user khác | ❌ | ❌ | ✅ |
| `ROLE_MANAGE` | Tạo/sửa/xoá Role | ❌ | ❌ | ✅ |
| `PERMISSION_MANAGE` | Tạo/sửa/xoá Permission (chỉ sửa description) | ❌ | ❌ | ✅ |

## 4. Nguyên tắc maker-checker (áp dụng cho nghiệp vụ tài chính)

- Người **tạo** giao dịch (`TRANSACTION_CREATE`) không được tự **duyệt** (`TRANSACTION_APPROVE`) giao dịch của chính mình.
- Đảm bảo tối thiểu 2 người tham gia mỗi hành động nhạy cảm về tài chính — đúng chuẩn kiểm soát nội bộ ngân hàng.

## 5. Quy tắc tạo User theo Role

| Ai tạo | Role được phép gán | Trạng thái ban đầu |
|---|---|---|
| Manager | Chỉ **Employee** | `enabled = true` ngay |
| Admin | Employee / Manager / Admin | `enabled = true` ngay |
| Employee | Không được tạo User | — |

- Không truyền `roleIds` khi tạo → mặc định gán **Employee**.
- Manager cố gán role `Manager`/`Admin` → bị chặn bởi thiếu quyền `USER_ASSIGN_RESTRICTED_ROLE`.
- **Không có API xoá User** — chỉ khoá (`enabled = false`) để giữ lịch sử liên kết dữ liệu (giao dịch, audit log).
- **Không có API tự đăng ký** — hệ thống nội bộ, mọi tài khoản do Manager/Admin tạo.

### 5.1. Cơ chế bắt buộc đổi mật khẩu lần đầu (`mustChangePassword`)

- Mọi User mới tạo (kể cả tài khoản Admin seed sẵn lúc khởi động) đều có `mustChangePassword = true` — vì mật khẩu ban đầu do Manager/Admin đặt, người tạo đã biết trước mật khẩu này.
- Khi đăng nhập, response trả kèm `mustChangePassword`; nếu `true`, FE bắt buộc redirect sang màn hình đổi mật khẩu trước khi cho vào các chức năng khác.
- Sau khi đổi mật khẩu thành công qua `PATCH /users/{id}/password`, Service tự động set `mustChangePassword = false` — không nhận field này từ request.

## 6. Danh sách API — theo từng nhóm (User / Role / Permission)

### 6.1. API User

| Method | Endpoint | Quyền yêu cầu | Ai gọi được | Response | Ghi chú |
|---|---|---|---|---|---|
| POST | `/users` | `USER_CREATE` | Manager, Admin | `UserSummaryResponse` | Manager chỉ gán được Employee; Admin gán được mọi role. Người tạo không cần thấy chi tiết permission |
| GET | `/users` | `USER_VIEW` | Manager, Admin | `List<UserSummaryResponse>` | Danh sách rút gọn |
| GET | `/users/{id}` | `USER_VIEW` | Manager, Admin | `UserResponse` | Đầy đủ (roles + permissions flatten) |
| GET | `/users/me` | Chỉ cần đăng nhập | Employee, Manager, Admin | `UserResponse` | Xem thông tin + quyền của chính mình, dùng để FE cache permission |
| PATCH | `/users/{id}` | `USER_UPDATE` | Manager, Admin | `UserSummaryResponse` | Sửa thông tin + đổi role (áp dụng cùng quy tắc gán role) |
| PATCH | `/users/{id}/password` | Chính chủ | Employee, Manager, Admin | — | Cần `oldPassword` đúng; tự động tắt `mustChangePassword` |
| PATCH | `/users/{id}/status` | `USER_LOCK` | Manager, Admin | — | Khoá/mở tài khoản, thay cho xoá |

**Phân biệt 2 DTO response cho User:**

| DTO | Trường dữ liệu | Dùng khi nào |
|---|---|---|
| `UserSummaryResponse` | `id, username, name, enabled, mustChangePassword, roles (id+name)` | Tạo mới, sửa, danh sách — không cần lộ chi tiết permission |
| `UserResponse` | Như trên + `permissions` (flatten, distinct từ mọi role) | Xem chi tiết 1 user, hoặc chính user xem quyền của mình (`/users/me`) |

### 6.2. API Role

| Method | Endpoint | Quyền yêu cầu | Ai gọi được | Ghi chú |
|---|---|---|---|---|
| POST | `/roles` | `ROLE_MANAGE` | Admin | Tạo Role mới kèm danh sách Permission |
| GET | `/roles` | `ROLE_MANAGE` | Admin | Danh sách đầy đủ (kèm permissions) |
| GET | `/roles/options` | Không cần `ROLE_MANAGE` | Manager, Admin | Danh sách rút gọn (id, name) — dùng cho dropdown khi tạo User |
| GET | `/roles/{id}` | `ROLE_MANAGE` | Admin | Chi tiết 1 Role |
| GET | `/roles/{id}/users` | `ROLE_MANAGE` | Admin | Xem Role đang gán cho những User nào |
| PATCH | `/roles/{id}` | `ROLE_MANAGE` | Admin | Sửa tên, mô tả, danh sách Permission |
| DELETE | `/roles/{id}` | `ROLE_MANAGE` | Admin | Chặn xoá nếu Role đang gán cho User nào đó |

### 6.3. API Permission

| Method | Endpoint | Quyền yêu cầu | Ai gọi được | Ghi chú |
|---|---|---|---|---|
| POST | `/permissions` | `PERMISSION_MANAGE` | Admin | Tạo Permission mới (`code` + `description`) |
| GET | `/permissions` | `PERMISSION_MANAGE` | Admin | Danh sách toàn bộ Permission |
| PATCH | `/permissions/{id}` | `PERMISSION_MANAGE` | Admin | **Chỉ sửa `description`** — không cho sửa `code` (tránh vỡ logic hardcode permission trong code) |
| DELETE | `/permissions/{id}` | `PERMISSION_MANAGE` | Admin | Chặn xoá nếu Permission đang gắn với Role nào đó |

## 7. Bảng tổng hợp — Role nào gọi được nhóm API nào

| Nhóm API | Employee | Manager | Admin |
|---|---|---|---|
| User (tạo/sửa/khoá) | ❌ | ✅ | ✅ |
| User (xem thông tin bản thân) | ✅ | ✅ | ✅ |
| Role (quản lý) | ❌ | ❌ | ✅ |
| Role (xem dropdown) | ❌ | ✅ | ✅ |
| Permission (quản lý) | ❌ | ❌ | ✅ |
| Nghiệp vụ ngân hàng (Customer/Account/Transaction) | ✅ | ✅ | ❌ |

## 8. Lưu ý kỹ thuật khi triển khai

- **Entity dùng `Set`, DTO Response dùng `List`** — ổn định thứ tự khi trả JSON, chuyển đổi qua `mapper` (thủ công hoặc MapStruct).
- **Convert entity → DTO phải nằm trong `@Transactional`** — tránh `LazyInitializationException` do quan hệ `@ManyToMany` khai báo `FetchType.LAZY`.
- **Chống N+1 query**: dùng `JOIN FETCH` cho các endpoint biết chắc cần load kèm quan hệ (VD: `GET /roles` cần permissions), kết hợp `hibernate.default_batch_fetch_size` làm lưới an toàn chung.
- **Không bao giờ trả `password`** trong bất kỳ response nào.
- **`code` của Permission bất biến sau khi tạo** — vì có thể được hardcode trong logic `hasAuthority("XXX")` ở nhiều nơi trong code.
- **Không xoá cứng User** — chỉ dùng `enabled` để khoá, giữ nguyên lịch sử liên kết dữ liệu phục vụ audit/compliance ngân hàng.
