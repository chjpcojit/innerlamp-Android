package com.example.innerlamp.core.storage.secure

/**
 * Kiểu lỗi trung tính cho lớp lưu trữ bảo mật. Kế thừa [Exception] để trả được
 * qua `Result.failure` (tầng `data` map sang domain), và không để lộ
 * `java.security.*`/`android.security.keystore.*` ra ngoài `core/storage`.
 */
sealed class StorageError(message: String? = null, cause: Throwable? = null) :
    Exception(message, cause) {

    /** Thiết bị chưa có khoá màn hình (PIN/pattern) — không thể ghi `USER_AUTH`. */
    data object DeviceLockRequired : StorageError()

    /** Không có Activity/UI để hiện prompt (ví dụ gọi từ service chạy nền). */
    data object AuthenticationUnavailable : StorageError()

    /** Người dùng huỷ prompt xác thực. */
    data object UserCancelled : StorageError()

    /** Xác thực thất bại (sinh trắc học không đăng ký / lỗi phần cứng / lockout…). */
    data object AuthenticationFailed : StorageError()

    /** Khoá bảo vệ `USER_AUTH` đã bị vô hiệu (gỡ PIN/đổi sinh trắc học sau khi lưu). */
    data object KeyInvalidated : StorageError()

    /** Secret không tồn tại. */
    data object NotFound : StorageError()

    /** Lỗi crypto (mã hoá/giải mã thất bại, dữ liệu hỏng…). */
    class CryptoFailure(cause: Throwable? = null) : StorageError(cause = cause)

    /** Lỗi lưu trữ chung. */
    class StorageFailure(cause: Throwable? = null) : StorageError(cause = cause)
}
