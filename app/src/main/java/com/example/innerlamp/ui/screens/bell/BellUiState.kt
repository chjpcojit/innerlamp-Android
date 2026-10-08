package com.example.innerlamp.ui.screens.bell

import com.example.innerlamp.domain.model.BellEveryOptions
import com.example.innerlamp.domain.model.BellMode
import com.example.innerlamp.domain.model.BellPresets

/**
 * State render cho màn hình Chuông mõ (bell).
 *
 * Gộp toàn bộ cấu hình (mode/preset/nhịp/mốc) + đếm tiếng + cờ hiệu ứng vào một
 * data class; nhãn phụ thuộc (subtitle, rate label, hint...) tính bằng computed
 * property để dễ test.
 *
 * @author innerlamp
 */
data class BellUiState(
    /** Chế độ đang chọn: Mõ / Chuông / Mõ + chuông. */
    val mode: BellMode = BellMode.MoVaChuong,
    /** Chỉ số preset đang chọn; -1 khi người dùng đã chỉnh tay. */
    val presetIndex: Int = 0,
    /** Nhịp mõ (nhịp/phút), áp cho chế độ gõ mõ. */
    val bpm: Int = 72,
    /** Khoảng cách giữa hai tiếng chuông (giây), áp cho chế độ chuông tự động. */
    val gapSeconds: Int = 15,
    /** Chỉ số mốc "thỉnh chuông sau mỗi" trong [BellEveryOptions.all]. */
    val everyIndex: Int = 2,
    /** Đang giữ nhịp tự động. */
    val running: Boolean = false,
    /** Đang dồn mõ (nhịp nhanh dần). */
    val rolling: Boolean = false,
    /** Số tiếng mõ đã gõ. */
    val moCount: Int = 0,
    /** Số tiếng chuông đã thỉnh. */
    val chuongCount: Int = 0,
    /** Đang nháy hiệu ứng gõ mõ. */
    val hitMo: Boolean = false,
    /** Đang nháy hiệu ứng thỉnh chuông. */
    val hitChuong: Boolean = false,
) {
    /** Phụ đề dưới tiêu đề: tên preset/mode + trạng thái giữ nhịp/dồn mõ. */
    val subtitle: String
        get() {
            val base = if (presetIndex in BellPresets.all.indices) BellPresets.all[presetIndex].label else mode.label
            return when {
                running -> "$base · đang giữ nhịp"
                rolling -> "$base · dồn mõ"
                else -> base
            }
        }

    /** Nhãn ô điều chỉnh nhịp/khoảng cách. */
    val rateLabel: String
        get() = if (mode == BellMode.Chuong) "Chuông tự động · khoảng cách" else "Mõ tự động · nhịp"

    /** Giá trị nhịp/khoảng cách đang hiển thị. */
    val rateValue: String
        get() = if (mode == BellMode.Chuong) "$gapSeconds giây một tiếng" else "$bpm nhịp/phút"

    /** Nhãn chữ trên tấm gõ (pad). */
    val padTag: String
        get() = if (mode == BellMode.Chuong) "CHUÔNG" else "MÕ"

    /** Số lớn giữa tấm gõ. */
    val padNumber: Int
        get() = if (mode == BellMode.Chuong) chuongCount else moCount

    /** Gợi ý dưới số lớn. */
    val padHint: String
        get() = when {
            rolling -> "Đang dồn mõ…"
            running -> "Đang giữ nhịp"
            mode == BellMode.Chuong -> "Chạm để thỉnh chuông"
            else -> "Chạm để gõ"
        }

    /** Nhãn nút trái (Đặt lại / Dồn mõ). */
    val leftLabel: String
        get() = if (mode == BellMode.Chuong) "Đặt lại" else "Dồn mõ"

    /** Hiện dải "Thỉnh chuông sau mỗi" (chỉ chế độ Mõ + chuông). */
    val showEvery: Boolean
        get() = mode == BellMode.MoVaChuong

    /** Giá trị mốc "thỉnh chuông" hiện tại (0 = Tắt). */
    val everyValue: Int
        get() = BellEveryOptions.all[everyIndex]
}
