package com.example.innerlamp.domain.model

/**
 * Một lựa chọn chùa trong danh sách chọn (vd "Nơi xuất gia" khi đăng ký/hồ sơ).
 *
 * @param id mã chùa; dùng làm `ordination_temple_id` khi đăng ký/cập nhật hồ sơ.
 * @param provinceName tên tỉnh/thành hiển thị.
 * @param provinceCode mã tỉnh (hệ mới); `null` khi chùa chưa được gán địa chỉ có cấu trúc.
 */
data class TempleOption(
    val id: String,
    val name: String,
    val provinceName: String,
    val provinceCode: String?,
)

/** Một trang kết quả danh sách [TempleOption] (backend cố định 100 mục/trang). */
data class TempleOptionPage(
    val items: List<TempleOption>,
    val page: Int,
    val pageSize: Int,
    val total: Int,
    val totalPages: Int,
) {
    /** Còn trang kế tiếp để tải. */
    val hasMore: Boolean get() = page < totalPages
}
