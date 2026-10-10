package com.example.innerlamp.ui.screens.profile

import com.example.innerlamp.domain.model.BadgeTone
import com.example.innerlamp.domain.model.MeritSummary
import com.example.innerlamp.domain.model.PracticeStat
import com.example.innerlamp.domain.model.ProfileBadge
import com.example.innerlamp.domain.model.ProfileUser

/**
 * Dữ liệu mẫu cho màn Hồ sơ, lấy từ prototype `Docs/design_handoff_innerlamp/`.
 *
 * TODO(il-data-builder): thay bằng repository/API người dùng (hồ sơ, thống kê,
 * dấu ấn, công đức). Giữ ở đây để UI dựng được độc lập, giống `ChuaSampleData`.
 */
internal object ProfileSampleData {

    /** Header hồ sơ (avatar + tên + pháp danh). */
    val user = ProfileUser(
        fullName = "Nguyễn Bình An",
        initials = "A",
        dharmaName = "Tâm Minh",
        role = "Phật tử tại gia",
    )

    /** Ba cột số liệu thực hành. */
    val stats = listOf(
        PracticeStat(value = "168", label = "giờ tu tập"),
        PracticeStat(value = "42", label = "ngày liên tục"),
        PracticeStat(value = "9.7k", label = "câu niệm Phật"),
    )

    /** 84 ô nhiệt, cùng thuật sinh độ đậm như prototype `heat`. */
    val heat = List(84) { i -> 0.12f + ((i * 37) % 9) / 10f }

    /** Ba dấu ấn mở đầu (tổng 18 ở màn `awards`). */
    val badges = listOf(
        ProfileBadge(name = "108 tràng", sub = "Niệm Phật", tone = BadgeTone.Gold),
        ProfileBadge(name = "An cư 30 ngày", sub = "Không gián đoạn", tone = BadgeTone.Green),
        ProfileBadge(name = "Vu Lan", sub = "Hồi hướng cha mẹ", tone = BadgeTone.Rose),
    )

    /** Công đức đã hồi hướng. */
    val merit = MeritSummary(offeringCount = 7, lampCount = 3)

    const val JOURNAL_LOGGED_DAYS = 84
    const val JOURNAL_TOTAL_DAYS = 84
    const val REMINDER_SUMMARY = "05:30 · mỗi ngày"
    const val TEMPLE_NAME = "Từ Quang"
}
