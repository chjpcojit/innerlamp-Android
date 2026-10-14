package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.District
import com.example.innerlamp.domain.model.Province
import com.example.innerlamp.domain.model.Ward

/**
 * Metadata đơn vị hành chính (tỉnh/huyện/xã). Trả về trọn bộ, không phân trang.
 *
 * Hệ mới (từ 01/07/2025) có 2 cấp: tỉnh → xã/phường; hệ cũ có 3 cấp: tỉnh → huyện → xã.
 * Đề xuất chùa dùng hệ mới.
 */
interface AdministrativeUnitRepository {

    /** Danh sách tỉnh/thành hệ mới (34), sắp ABC tiếng Việt. */
    suspend fun getProvinces(): Result<List<Province>>

    /**
     * Xã/phường/đặc khu thuộc tỉnh [provinceCode] (hệ mới).
     *
     * @param query lọc theo tên đầy đủ; `null`/rỗng nghĩa là không lọc.
     */
    suspend fun getWards(provinceCode: String, query: String? = null): Result<List<Ward>>

    /** Danh sách tỉnh/thành hệ cũ. */
    suspend fun getLegacyProvinces(): Result<List<Province>>

    /** Quận/huyện thuộc tỉnh [provinceCode] (hệ cũ). */
    suspend fun getLegacyDistricts(provinceCode: String): Result<List<District>>

    /** Xã/phường thuộc huyện [districtCode] (hệ cũ); rỗng nếu huyện không có xã. */
    suspend fun getLegacyWards(districtCode: String): Result<List<Ward>>
}
