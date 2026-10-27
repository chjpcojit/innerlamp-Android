package com.example.innerlamp.domain.temple

/**
 * Vietnamese display labels for the temple codes of `openapi.yaml` (`region`, `type`, `sect`,
 * `heritage_rank`). Provisional until the product confirms the wording (Temple spec AC4); an unknown
 * code returns `null` so callers can fall back to something else. /
 * Nhãn tiếng Việt cho các mã chùa trong `openapi.yaml`. Tạm thời cho đến khi product chốt câu chữ
 * (AC4 của spec Temple); mã lạ trả `null` để nơi gọi tự chọn giá trị khác.
 */
object TempleLabels {

    fun region(code: String): String? = REGIONS[code]

    fun type(code: String): String? = TYPES[code]

    fun sect(code: String): String? = SECTS[code]

    fun heritageRank(code: String): String? = HERITAGE_RANKS[code]

    private val REGIONS = mapOf(
        "north" to "Miền Bắc",
        "central" to "Miền Trung",
        "south" to "Miền Nam",
    )

    private val TYPES = mapOf(
        "CHUA" to "Chùa",
        "THIEN_VIEN" to "Thiền viện",
        "DAO_TRANG" to "Đạo tràng",
        "TINH_XA" to "Tịnh xá",
        "THIEN_XA" to "Thiền xá",
    )

    private val SECTS = mapOf(
        "BẮC_TÔNG" to "Bắc tông",
        "TRUC_LAM_YEN_TU" to "Trúc Lâm Yên Tử",
        "KHAT_SI" to "Khất sĩ",
        "NAM_TONG_KHMER" to "Nam tông Khmer",
        "HOA_TONG" to "Hoa tông",
        "NAM_TONG" to "Nam tông",
        "TIN_NGUONG_DANG_GIAN" to "Tín ngưỡng dân gian",
        "MINH_SU_DAO" to "Minh Sư đạo",
    )

    private val HERITAGE_RANKS = mapOf(
        "DI_SAN_THE_GIOI" to "Di sản thế giới",
        "DI_TICH_QUOC_GIA_DAC_BIET" to "Di tích quốc gia đặc biệt",
        "KHU_DU_LICH_QUOC_GIA" to "Khu du lịch quốc gia",
        "DI_TICH_QUOC_GIA" to "Di tích quốc gia",
        "DANH_LAM_THANG_CANH" to "Danh lam thắng cảnh",
        "DI_TICH_CAP_TINH" to "Di tích cấp tỉnh",
        "DI_TICH_CAP_THANH_PHO" to "Di tích cấp thành phố",
        "CHUA_XEP_HANG" to "Chưa xếp hạng",
    )
}
