package com.example.innerlamp.domain.model

/**
 * Content of the "Trang chùa" screen, read from `GET /v1/temples/{id}` (contract 3.0.0). Pure Kotlin;
 * an empty contract string is `null` here, an empty array is an empty list. /
 * Nội dung màn "Trang chùa", đọc từ `GET /v1/temples/{id}`. Thuần Kotlin; chuỗi rỗng của contract là
 * `null`, mảng rỗng là danh sách rỗng.
 */
data class TempleDetail(
    val id: String,
    val name: String,
    /** Province (`location.province`), e.g. `"Ninh Bình"`. / Tỉnh/thành. */
    val place: String,
    /** Display labels of `region`, `type`, `sect`, `heritage_rank`. / Nhãn hiển thị của các mã. */
    val region: String?,
    val type: String?,
    val sect: String?,
    val heritageRank: String?,
    val establishedYear: Int?,
    val imageUrl: String?,
    val address: String,
    val lat: Double,
    val lng: Double,
    val aliases: List<String>,
    val founder: String?,
    val currentAbbot: String?,
    /** Raw `operating_hours`, e.g. `"07:00 - 18:00"`. / Giờ mở cửa nguyên văn. */
    val operatingHours: String?,
    val amenities: TempleAmenities,
    val introduction: String?,
    val architectureHeritage: String?,
    val visitingNotes: List<String>,
)

/** `amenities` of the temple page. / Tiện ích của chùa. */
data class TempleAmenities(
    val parkingCar: Boolean,
    val parkingMotorcycle: Boolean,
    val vegetarianMeals: Boolean,
    val summerRetreat: Boolean,
)
