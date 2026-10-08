package com.example.innerlamp.ui.screens.settings

import com.example.innerlamp.domain.model.ProfileUser

/** Các cỡ chữ kinh văn (màn `lang` dùng 4 chip, màn Cài đặt dùng thanh trượt). */
internal val SettingsFontSizeLabels = listOf("Nhỏ", "Vừa", "Lớn", "Rất lớn")

/** Số bậc cỡ chữ (0..3). Thanh trượt "Cỡ chữ kinh văn" snap vào từng bậc này. */
internal val SettingsFontSizeSteps = SettingsFontSizeLabels.size

/**
 * State render cho màn Cài đặt (`settings`).
 *
 * Gộp các nhóm cài đặt trong prototype: THỜI KHÓA, HIỂN THỊ & NGÔN NGỮ, NỘI DUNG
 * & TRỢ GIÚP, RIÊNG TƯ. Các switch giữ trạng thái bật/tắt riêng; dữ liệu hiện lấy
 * từ [SettingsSampleData] — TODO(il-data-builder): nối vào use case đọc cài đặt
 * người dùng khi data layer có.
 */
data class SettingsUiState(
    /** Tài khoản ở thẻ đầu màn (tái dùng model của Hồ sơ). */
    val user: ProfileUser,
    /** Số điện thoại định danh, vd "+84 912 345 678". */
    val phone: String,
    // ── Thời khóa ─────────────────────────────────────────────────────────────
    /** Chuông tỉnh thức bật/tắt. */
    val reminderEnabled: Boolean,
    /** Các mốc chuông đang đặt, vd "05:30 · 12:00 · 21:30". */
    val reminderSummary: String,
    /** Nhắc ngày trai & rằm bật/tắt. */
    val fastingReminderEnabled: Boolean,
    /** Âm thanh chuông đang chọn. */
    val bellSound: String,
    // ── Hiển thị & ngôn ngữ ───────────────────────────────────────────────────
    /** Cỡ chữ kinh văn (chỉ số vào [SettingsFontSizeLabels]). */
    val fontSizeIndex: Int,
    /** Song ngữ Việt – Anh bật/tắt. */
    val bilingualEnabled: Boolean,
    /** Nhãn chế độ giao diện hiện tại, vd "Sáng". */
    val themeLabel: String,
    // ── Nội dung & trợ giúp ───────────────────────────────────────────────────
    /** Tóm tắt mục đã tải, vd "8 mục đã tải · 412 MB". */
    val downloadsSummary: String,
    // ── Riêng tư ──────────────────────────────────────────────────────────────
    /** Ẩn nhật ký khỏi đạo tràng bật/tắt. */
    val hideJournalEnabled: Boolean,
    // ── Chân màn ──────────────────────────────────────────────────────────────
    val appVersion: String,
) {
    /** Phụ đề thẻ tài khoản, vd "Pháp danh Tâm Minh · +84 912 345 678". */
    val accountSub: String
        get() = "Pháp danh ${user.dharmaName} · $phone"

    /** Vị trí thanh trượt cỡ chữ (0..1), để vẽ track vàng + núm. */
    val fontSizeFraction: Float
        get() = fontSizeIndex.coerceIn(0, SettingsFontSizeSteps - 1) / (SettingsFontSizeSteps - 1f)

    /** Phụ đề hàng "Ngôn ngữ & hiển thị", vd "Tiếng Việt · cỡ chữ vừa". */
    val languageSummary: String
        get() = "Tiếng Việt · cỡ chữ ${SettingsFontSizeLabels[fontSizeIndex].lowercase()}"
}
