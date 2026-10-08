package com.example.innerlamp.core.network

import kotlinx.serialization.json.Json

/**
 * `Json` dùng chung cho cả app — cấu hình một chỗ, không rải rác ở từng feature.
 */
object JsonProvider {
    val json: Json = Json {
        // Bỏ qua field lạ trong JSON để không crash khi backend thêm field mới.
        ignoreUnknownKeys = true
        // Không encode các field null (giữ payload gọn, khớp `omitempty` của backend).
        explicitNulls = false
        // Ép kiểu linh hoạt (vd "1" → 1) khi input lỏng lẻo.
        coerceInputValues = true
    }
}
