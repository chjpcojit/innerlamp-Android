package com.example.innerlamp.ui.screens.templelist

import com.example.innerlamp.domain.model.Temple
import java.text.Normalizer

/**
 * State render cho màn "Tất cả chùa".
 *
 * @property query chuỗi tìm kiếm (khớp tên chùa / địa danh, không phân biệt hoa thường và dấu).
 * @property region miền đang lọc; `null` = "Tất cả".
 */
data class TempleListUiState(
    val temples: List<Temple>,
    val query: String = "",
    val region: String? = null,
) {
    /** Các miền có trong danh bạ, giữ thứ tự xuất hiện — nguồn của dãy chip lọc. */
    val regions: List<String> get() = temples.map { it.region }.distinct()

    val filtered: List<Temple>
        get() {
            val q = query.trim().foldVietnamese()
            return temples.filter { t ->
                (region == null || t.region == region) &&
                    (q.isEmpty() || "${t.name} ${t.place}".foldVietnamese().contains(q))
            }
        }

    /** Phụ đề header (prototype `ftSub`). */
    val subtitle: String get() = "${temples.size} chùa trong danh bạ"

    /** Câu hiển thị khi không có kết quả (prototype `ftEmptyTxt`). */
    val emptyMessage: String
        get() {
            val q = query.trim()
            return if (q.isNotEmpty()) {
                "Không tìm thấy chùa nào khớp “$q”" + (region?.let { " ở $it" } ?: "") + "."
            } else {
                "Chưa có chùa nào ở ${region ?: "đây"}."
            }
        }
}

/** Bỏ dấu + hạ chữ thường để so khớp tìm kiếm (đ → d). */
internal fun String.foldVietnamese(): String =
    Normalizer.normalize(lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
        .replace('đ', 'd')
