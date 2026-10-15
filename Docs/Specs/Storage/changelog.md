# Changelog — Storage Spec

Tracks version history for `requirements.md`, `design.md`, and `tasks.md` in this folder. All three
files share one changelog since they change together as one spec unit. Newest version first.

## v1.2 (2026-10-15)

- `tasks.md` Deferred: `AuthTokenStorage` trỏ về `Docs/Specs/Login/` v1.0 (token ở `DEVICE_UNLOCKED`, qua
  `AuthSessionStore`). `requirements.md`/`design.md` không đổi ngoài version.

## v1.1 (2026-09-22)

- `requirements.md`: đổi tầng không nhạy cảm sang **`SharedPreferences`** (bỏ DataStore) và bỏ chữ
  "backend"; thêm tiêu chí ghi bằng `commit()` trên IO + `observe` qua `OnSharedPreferenceChangeListener`;
  gộp lại tiêu chí khi **gỡ PIN/bio sau khi đã lưu** (phát hiện lúc khởi động/lần đọc kế tiếp → báo
  `KeyInvalidated`, xoá blob `USER_AUTH` **và** alias khoá trong AndroidKeyStore); cập nhật Out of
  scope (secret không bao giờ lưu ở prefs thường) và Pending (gỡ dependency `androidx.datastore`);
  sửa câu trả lời đầu ở "Open questions (resolved)" theo quyết định cuối.
- `design.md`: thay `DataStorePreferencesStorage` bằng `SharedPreferencesStorage` (file `innerlamp_prefs`);
  bỏ DataStore khỏi sơ đồ, bảng Layers, DI; thêm mục **"Dọn dẹp fail-closed khi khoá bảo vệ bị gỡ
  (`reconcileUserAuth`)"**; đổi tên level thành `DEVICE_UNLOCKED`/`USER_AUTH`; thêm ghi chú cập nhật
  AGENTS.md; bổ sung edge case gỡ PIN/đổi sinh trắc học sau khi lưu.
- `tasks.md`: Phase 1 viết lại theo SharedPreferences (commit trên IO, giữ tham chiếu mạnh tới
  listener, chuyển `UserPreferences.kt`, gỡ `androidx.datastore`); thêm **Phase 4 — Dọn dẹp
  fail-closed** (tách khỏi Phase cũ); dồn DI/backup sang Phase 5, test sang Phase 6, docs sang Phase 7;
  bỏ DataStore khỏi DI.
- Reason: người dùng chỉ ra "backend" không phải từ ngữ của dự án Android và chốt lại — dùng
  `SharedPreferences` cho dữ liệu đơn giản (không DataStore), và nếu đã lưu `USER_AUTH` rồi mà gỡ
  PIN/bio thì phải báo lỗi và xoá dữ liệu/khoá. Đây là thay đổi quyết định ở cùng phạm vi (không thêm
  phase mang tính scope mới, không đổi luồng nghiệp vụ) nên bump **minor**; cascade
  requirements → design → tasks thực hiện đầy đủ.

## v1.0 (2026-09-22)

- `requirements.md`: khởi tạo — user story (lớp lưu trữ dùng chung 2 tầng), 26 tiêu chí EARS
  (tầng không nhạy cảm, phân cấp Level 1/Level 2, xác thực lại mỗi lần đọc, giới hạn face Class 2,
  fail-closed khi key vô hiệu, backup, an toàn tầng), out-of-scope (Room/network/app-lock mềm/cloud),
  Pending; **không còn `⚠️ Not confirmed`** — toàn bộ câu hỏi đã chốt ở "Open questions (resolved)".
- `design.md`: khởi tạo — kiến trúc `Storage` facade + `PreferencesStorage` (DataStore) +
  `SecureStorage` (KeyStore AES-256-GCM, blob store trong prefs riêng); bảng Layers & components
  (14 file, gồm 5 file sửa: `UserPreferences.kt`, `MainActivity.kt`, `build.gradle.kts`,
  2 file backup rules, manifest); Step-by-step Flow 3 bước cho ghi/đọc secret; mục "Giới hạn nền
  tảng & quyết định" (không có `AUTH_BIOMETRIC_WEAK`, CryptoObject chỉ Class 3, per-use vs
  time-based, khác biệt API 29/30) và bảng edge case bảo mật/khôi phục.
- `tasks.md`: khởi tạo — 7 Phase (PreferencesStorage → hạ tầng bảo mật → biometric gate → API/an toàn
  tầng → DI+backup+manifest → test → docs), toàn bộ `[ ]`; mục Deferred cho app-lock mềm,
  `AuthTokenStorage`, đồng bộ cloud.
- Reason: viết spec prospective cho lớp lưu trữ dùng chung. Người dùng chốt: tầng không nhạy cảm
  dùng DataStore sau facade `Storage` (có thể hoán đổi SharedPreferences); Level 2 chỉ **một** mô
  hình hardware-bound (sinh trắc học Class 3 **hoặc** khoá màn hình, xác thực lại mỗi lần đọc,
  fail-closed khi key vô hiệu); thiết bị không có khoá màn hình thì **từ chối lưu** Level 2 và hướng
  dẫn thiết lập; **không** có thành phần nền nào đọc Level 2; ciphertext nằm ở SharedPreferences file
  riêng. Các xung đột kỹ thuật (face Class 2, CryptoObject, per-use + fallback PIN) đã được nêu rõ và
  chốt theo hướng đơn giản, không để lại câu hỏi mở.
