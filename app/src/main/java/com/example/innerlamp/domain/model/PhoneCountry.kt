package com.example.innerlamp.domain.model

import java.text.Normalizer

/**
 * Quốc gia trong bộ chọn mã vùng điện thoại (khớp `Component.COUNTRIES` của prototype).
 *
 * @property len số chữ số quốc gia (không gồm số 0 đầu) để coi là "đủ số".
 *
 * @author innerlamp
 */
data class PhoneCountry(
    val iso: String,
    val flag: String,
    val name: String,
    val dial: String,
    val len: Int,
)

object PhoneCountries {
    /** Mặc định Việt Nam — phần tử đầu tiên. */
    val all: List<PhoneCountry> = listOf(
        PhoneCountry("VN", "🇻🇳", "Việt Nam", "+84", 9),
        PhoneCountry("US", "🇺🇸", "Hoa Kỳ", "+1", 10),
        PhoneCountry("JP", "🇯🇵", "Nhật Bản", "+81", 10),
        PhoneCountry("KR", "🇰🇷", "Hàn Quốc", "+82", 9),
        PhoneCountry("TH", "🇹🇭", "Thái Lan", "+66", 9),
        PhoneCountry("SG", "🇸🇬", "Singapore", "+65", 8),
        PhoneCountry("MY", "🇲🇾", "Malaysia", "+60", 9),
        PhoneCountry("TW", "🇹🇼", "Đài Loan", "+886", 9),
        PhoneCountry("AU", "🇦🇺", "Úc", "+61", 9),
        PhoneCountry("DE", "🇩🇪", "Đức", "+49", 10),
        PhoneCountry("FR", "🇫🇷", "Pháp", "+33", 9),
        PhoneCountry("CA", "🇨🇦", "Canada", "+1", 10),
    )

    val default: PhoneCountry get() = all.first()

    fun byIso(iso: String): PhoneCountry = all.firstOrNull { it.iso == iso } ?: default

    /** Lọc theo tên (không dấu), mã vùng hoặc mã ISO; rỗng → toàn bộ danh sách. */
    fun search(query: String): List<PhoneCountry> {
        val q = normalize(query).trim()
        if (q.isEmpty()) return all
        return all.filter {
            normalize(it.name).contains(q) || it.dial.contains(q) || normalize(it.iso).contains(q)
        }
    }

    private fun normalize(value: String): String =
        Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace('đ', 'd')
}

/**
 * `0949…` / `949…` / `+84949…` / `0084949…` → phần số quốc gia (bỏ số 0 đầu, bỏ mã vùng),
 * cắt tối đa `len + 1` chữ số như prototype.
 */
fun PhoneCountry.nationalDigits(raw: String): String {
    var d = raw.filter { it.isDigit() }
    val dialDigits = dial.drop(1)
    d = when {
        d.startsWith("00$dialDigits") -> d.drop(2 + dialDigits.length)
        d.startsWith(dialDigits) && d.length > len -> d.drop(dialDigits.length)
        else -> d
    }
    return d.trimStart('0').take(len + 1)
}

/** Đã đủ số theo độ dài quốc gia. */
fun PhoneCountry.isComplete(raw: String): Boolean = nationalDigits(raw).length >= len

/** Dạng hiển thị `+84 949 123 456` (nhóm 3 số) — dùng ở gợi ý và màn OTP. */
fun PhoneCountry.formatE164(raw: String): String {
    val groups = nationalDigits(raw).chunked(3).joinToString(" ")
    return if (groups.isEmpty()) dial else "$dial $groups"
}
