package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.PhotoCategory
import com.example.innerlamp.domain.model.TemplePhoto

/**
 * Trả về danh sách ảnh chùa đẹp của màn `photos`.
 *
 * Hiện dữ liệu là mẫu lấy từ prototype (`Docs/design_handoff_innerlamp/`, màn
 * `photos`).
 *
 * TODO(il-data-builder): chuyển sang delegate cho repository ảnh (danh mục ảnh chùa
 * thuộc "Data cần API" theo README) khi có tầng data. Lúc đó use case nhận
 * repository qua constructor và trả `Result<List<TemplePhoto>>` để UI xử lý được
 * lỗi mạng.
 */
class GetTemplePhotosUseCase {

    operator fun invoke(): List<TemplePhoto> = listOf(
        TemplePhoto(
            id = "pg-1",
            category = PhotoCategory.Temple,
            title = "Chánh điện lúc bình minh",
            place = "Chùa Từ Quang",
            heightDp = 220,
        ),
        TemplePhoto(
            id = "pg-2",
            category = PhotoCategory.BuddhaStatue,
            title = "Tôn tượng Bổn Sư",
            place = "Chùa Từ Quang",
            heightDp = 170,
        ),
        TemplePhoto(
            id = "pg-3",
            category = PhotoCategory.Meditation,
            title = "Lối đi vào tam quan",
            place = "Chùa Từ Quang",
            heightDp = 150,
        ),
        TemplePhoto(
            id = "pg-4",
            category = PhotoCategory.Temple,
            title = "Chùa Một Cột",
            place = "Hà Nội",
            heightDp = 180,
        ),
        TemplePhoto(
            id = "pg-5",
            category = PhotoCategory.BuddhaStatue,
            title = "Tượng Quán Thế Âm",
            place = "Chùa Linh Ứng · Đà Nẵng",
            heightDp = 240,
        ),
        TemplePhoto(
            id = "pg-6",
            category = PhotoCategory.Temple,
            title = "Chùa Thiên Mụ bên sông Hương",
            place = "Huế",
            heightDp = 160,
        ),
        TemplePhoto(
            id = "pg-7",
            category = PhotoCategory.Meditation,
            title = "Thiền đường trong sương",
            place = "Thiền viện Trúc Lâm · Đà Lạt",
            heightDp = 200,
        ),
        TemplePhoto(
            id = "pg-8",
            category = PhotoCategory.Festival,
            title = "Đêm hoa đăng Vu Lan",
            place = "Chùa Từ Quang",
            heightDp = 170,
        ),
        TemplePhoto(
            id = "pg-9",
            category = PhotoCategory.BuddhaStatue,
            title = "Tượng Phật nằm",
            place = "Núi Tà Cú · Bình Thuận",
            heightDp = 150,
        ),
        TemplePhoto(
            id = "pg-10",
            category = PhotoCategory.Temple,
            title = "Chùa Bái Đính",
            place = "Ninh Bình",
            heightDp = 210,
        ),
        TemplePhoto(
            id = "pg-11",
            category = PhotoCategory.Festival,
            title = "Lễ Phật đản",
            place = "Chùa Từ Quang",
            heightDp = 180,
        ),
        TemplePhoto(
            id = "pg-12",
            category = PhotoCategory.Meditation,
            title = "Hồ sen sau chùa",
            place = "Chùa Từ Quang",
            heightDp = 160,
        ),
    )
}
