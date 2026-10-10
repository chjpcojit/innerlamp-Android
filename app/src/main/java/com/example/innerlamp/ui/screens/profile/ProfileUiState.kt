package com.example.innerlamp.ui.screens.profile

import com.example.innerlamp.domain.model.MeritSummary
import com.example.innerlamp.domain.model.PracticeStat
import com.example.innerlamp.domain.model.ProfileBadge
import com.example.innerlamp.domain.model.ProfileUser

/**
 * State render cho màn Hồ sơ (`profile` / tab "Tôi").
 *
 * Màn này gần như tĩnh: mọi tương tác đều là điều hướng (qua [ProfileCallbacks]),
 * nên state chỉ gom dữ liệu hiển thị. Dữ liệu hiện lấy từ [ProfileSampleData] —
 * TODO(il-data-builder): nối vào use case lấy hồ sơ người dùng khi data layer có.
 */
data class ProfileUiState(
    val user: ProfileUser,
    /** Ba cột số liệu: giờ tu tập / ngày liên tục / câu niệm Phật. */
    val stats: List<PracticeStat>,
    /** Số ngày đã ghi / tổng số ngày của khối "Nhật ký 12 tuần qua". */
    val journalLoggedDays: Int,
    val journalTotalDays: Int,
    /** Độ đậm 0–1 của 84 ô nhiệt (xếp 21 cột × 4 hàng). */
    val heat: List<Float>,
    val badges: List<ProfileBadge>,
    val merit: MeritSummary,
    /** Tóm tắt chuông tỉnh thức, vd "05:30 · mỗi ngày". */
    val reminderSummary: String,
    /** Tên chùa tu tập. */
    val templeName: String,
) {
    /** Nhãn "X / Y ngày ghi" cho khối nhật ký. */
    val journalLabel: String get() = "$journalLoggedDays / $journalTotalDays ngày ghi"

    /** Phụ đề thẻ "Công đức đã hồi hướng", vd "7 lần cúng dường · 3 ngọn đèn Vu Lan". */
    val meritLine: String
        get() = "${merit.offeringCount} lần cúng dường · ${merit.lampCount} ngọn đèn Vu Lan"
}
