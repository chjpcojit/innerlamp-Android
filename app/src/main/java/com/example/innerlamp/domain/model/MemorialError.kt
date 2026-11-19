package com.example.innerlamp.domain.model

/**
 * Failures of the memorial feature, branched by `error.code` (never by `message`). /
 * Lỗi của tính năng sổ giỗ, phân nhánh theo `error.code` (không theo `message`).
 */
sealed class MemorialError(message: String? = null) : Exception(message) {

    /** 401 after the session refresh failed → the screen behaves as a guest. / 401 sau refresh → như khách. */
    data object Unauthorized : MemorialError()

    /** 404 `MEMORIAL_NOT_FOUND` — the entry is gone (also for someone else's id). / Mục không còn. */
    data object NotFound : MemorialError()

    /** 409 `MEMORIAL_LIMIT_REACHED` — the book already has 100 entries. / Sổ đã đủ 100 mục. */
    data object LimitReached : MemorialError()

    /** 413 `PHOTO_TOO_LARGE` or a JSON body over the limit. / Ảnh quá lớn (hoặc body JSON quá lớn). */
    data object TooLarge : MemorialError()

    /** 415 `PHOTO_UNSUPPORTED_TYPE`. / Ảnh sai định dạng. */
    data object UnsupportedPhoto : MemorialError()

    /** 400 `VALIDATION_ERROR`, with the field the server named when it sent one. / Lỗi dữ liệu theo trường. */
    data class Validation(val field: String? = null) : MemorialError()

    /** No connection / timeout — writes are refused, reads fall back to the device copy. / Mất mạng. */
    data object Offline : MemorialError()

    /** 5xx, unknown or undecodable response. / Lỗi còn lại. */
    data object Failed : MemorialError()
}

/** Result of loading the book: the server copy, the device copy or a failure. / Kết quả tải sổ. */
sealed interface MemorialLoad {

    /** No session on the device — no request, no cached rows. / Chưa đăng nhập. */
    data object Guest : MemorialLoad

    /** The server answered; the device copy is now this list. / Server trả về; cache đã thay bằng danh sách này. */
    data class Loaded(val entries: List<MemorialEntry>) : MemorialLoad

    /** The server failed but the signed-in account has a device copy. / Lỗi server, đọc bản trên máy. */
    data class Offline(val entries: List<MemorialEntry>, val error: MemorialError) : MemorialLoad

    /** The server failed and there is no device copy. / Lỗi server, không có bản trên máy. */
    data class Failed(val error: MemorialError) : MemorialLoad
}
