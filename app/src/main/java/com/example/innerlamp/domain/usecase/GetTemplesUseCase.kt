package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.Temple

/**
 * Trả về danh bạ chùa (khối "Chùa Việt Nam" ở Trang chủ và màn "Tất cả chùa").
 *
 * Hiện là dữ liệu mẫu lấy từ prototype.
 * TODO(il-data-builder): thay bằng repository/API khi backend có; lúc đó trả `Result<List<Temple>>`.
 */
class GetTemplesUseCase {

    operator fun invoke(): List<Temple> = TEMPLES

    private companion object {
        val TEMPLES = listOf(
            Temple("tp-motcot", "Chùa Một Cột", "Ba Đình, Hà Nội", "Miền Bắc", "Dựng năm 1049 · thời Lý"),
            Temple("tp-tranquoc", "Chùa Trấn Quốc", "Hồ Tây, Hà Nội", "Miền Bắc", "Ngôi chùa cổ nhất Thăng Long"),
            Temple("tp-huong", "Chùa Hương", "Mỹ Đức, Hà Nội", "Miền Bắc", "Lễ hội tháng Giêng – tháng Ba"),
            Temple("tp-baidinh", "Chùa Bái Đính", "Gia Viễn, Ninh Bình", "Miền Bắc", "Quần thể chùa lớn nhất cả nước"),
            Temple("tp-thienmu", "Chùa Thiên Mụ", "Bờ sông Hương, Huế", "Miền Trung", "Tháp Phước Duyên 7 tầng"),
            Temple("tp-linhung", "Chùa Linh Ứng", "Sơn Trà, Đà Nẵng", "Miền Trung", "Tượng Quán Âm cao 67m"),
            Temple("tp-truclam", "Thiền viện Trúc Lâm", "Hồ Tuyền Lâm, Đà Lạt", "Tây Nguyên", "Thiền phái Trúc Lâm Yên Tử"),
            Temple("tp-vinhnghiem", "Chùa Vĩnh Nghiêm", "Quận 3, TP. Hồ Chí Minh", "Miền Nam", "Kiến trúc Bắc Bộ giữa Sài Gòn"),
        )
    }
}
