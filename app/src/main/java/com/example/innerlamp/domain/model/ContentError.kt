package com.example.innerlamp.domain.model

/**
 * Failure of a public catalogue read (temples, deities, gallery), so the UI can pick the offline or the
 * generic text without importing `core/network`. /
 * Lỗi khi đọc một danh mục công khai (chùa, chư Phật, ảnh) để UI chọn câu mất mạng hay câu chung mà không
 * phải import `core/network`.
 */
sealed class ContentError : Exception() {
    /** No connection or timeout. / Không có mạng hoặc quá thời gian chờ. */
    data object Offline : ContentError()

    /** The id does not exist (`*_NOT_FOUND`). / Id không tồn tại. */
    data object NotFound : ContentError()

    /** Any other error (5xx, bad payload, …). / Lỗi khác. */
    data object Failed : ContentError()
}
