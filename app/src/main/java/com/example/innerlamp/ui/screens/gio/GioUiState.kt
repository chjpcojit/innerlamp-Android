package com.example.innerlamp.ui.screens.gio

import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialOccurrence

/** Một dòng trong Sổ giỗ: người thân + kỳ giỗ kế tiếp của họ. */
data class GioItem(
    val entry: MemorialEntry,
    val occurrence: MemorialOccurrence,
)

/** Hai dòng xem trước dưới bộ chọn ngày mất ("Kỳ giỗ tới: …" + diễn giải). */
data class GioPreview(
    val headline: String,
    val detail: String,
)

/**
 * Bản nháp đang soạn ở màn Thêm/sửa. [entry] là giá trị đang nhập; [isNew] quyết định tiêu đề,
 * nhãn nút lưu và việc có nút "Xóa" hay không. Lỗi theo trường hiện dưới đúng ô; [formError] là câu
 * chung của lần lưu/xoá (mạng, server).
 */
data class GioDraft(
    val entry: MemorialEntry,
    val isNew: Boolean,
    val preview: GioPreview? = null,
    val nameError: String = "",
    val noteError: String = "",
    val dateError: String = "",
    val formError: String = "",
    val photoError: String = "",
    val saving: Boolean = false,
    /** A write got 401: the form and its content stay until the user chooses to sign in again. */
    val sessionExpired: Boolean = false,
)

/** Trạng thái tải của màn sổ. / Load state of the book screen. */
enum class GioStatus { Loading, Ready, Guest, Failed }

/**
 * State dùng chung cho màn "Sổ giỗ chạp" và "Thêm/sửa ngày giỗ" (cùng một ViewModel).
 *
 * [items] đã sắp theo kỳ giỗ gần nhất; phần tử đầu là thẻ nổi bật. Rỗng → hiện trạng thái "Sổ giỗ còn
 * trống". [flash] là thông báo xanh ngắn trên đầu danh sách (rỗng = ẩn); [banner] là câu báo bản offline
 * hoặc lỗi tải.
 */
data class GioUiState(
    val status: GioStatus = GioStatus.Loading,
    val items: List<GioItem> = emptyList(),
    val flash: String = "",
    val banner: String = "",
    val draft: GioDraft? = null,
    val confirmDelete: Boolean = false,
) {
    /** Phụ đề header: "N người thân" hoặc "Chưa có ai". */
    val subtitle: String
        get() = if (items.isEmpty()) "Chưa có ai" else "${items.size} người thân"

    /** Người có giỗ gần nhất (thẻ nổi bật); `null` khi sổ trống. */
    val nextUp: GioItem? get() = items.firstOrNull()

    /** Empty state only after a successful load with no entries. / Chỉ hiện sổ trống khi đã tải xong. */
    val showEmpty: Boolean get() = status == GioStatus.Ready && items.isEmpty()
}
