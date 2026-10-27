package com.example.innerlamp.data.mapper

import com.example.innerlamp.data.remote.dto.DeityDetailDto
import com.example.innerlamp.data.remote.dto.DeitySummaryDto
import com.example.innerlamp.domain.model.BuddhaMantra
import com.example.innerlamp.domain.model.BuddhaProfile
import com.example.innerlamp.domain.model.BuddhaViaDay
import com.example.innerlamp.domain.model.DeitySummary
import com.example.innerlamp.domain.model.DeityType
import com.example.innerlamp.domain.model.kindLabel

/** Open enum: an unknown code is [DeityType.Unknown], the item is kept. / Enum mở: mã lạ vẫn giữ mục. */
internal fun String.toDeityType(): DeityType = when (this) {
    "BUDDHA" -> DeityType.Buddha
    "BODHISATTVA" -> DeityType.Bodhisattva
    "DEVA" -> DeityType.Deva
    else -> DeityType.Unknown
}

fun DeitySummaryDto.toDomain(): DeitySummary = DeitySummary(
    id = id,
    type = type.toDeityType(),
    nameVi = nameVi.ifBlank { titleHonorific },
    nameSanskrit = nameSanskrit.nullIfBlank(),
    titleHonorific = titleHonorific.nullIfBlank(),
    thumbnailUrl = thumbnailUrl.nullIfBlank(),
    imageUrl = imageUrl.nullIfBlank(),
    coreVirtue = coreVirtue.nullIfBlank(),
    pureLand = pureLand.nullIfBlank(),
)

/**
 * Detail → the screen profile. Days outside 1..30 / months outside 1..12 are dropped; the short mantra
 * and the dharani become two mantra cards (a card without text is left out). Related sutras stay empty:
 * the sutra screens still run on sample ids. /
 * Chi tiết → hồ sơ của màn. Ngày/tháng ngoài khoảng bị bỏ; tâm chú ngắn và đà la ni thành hai thẻ (thẻ
 * không có chữ bị bỏ). Kinh liên quan để rỗng vì màn kinh vẫn chạy trên id mẫu.
 */
fun DeityDetailDto.toDomain(): BuddhaProfile {
    val type = type.toDeityType()
    return BuddhaProfile(
        id = id,
        name = titleHonorific.nullIfBlank() ?: nameVi,
        shortName = nameVi.ifBlank { titleHonorific },
        sanskrit = nameSanskrit.nullIfBlank(),
        type = type,
        kindLabel = type.kindLabel,
        realm = pureLand.nullIfBlank(),
        role = coreVirtue.nullIfBlank(),
        imageUrl = imageUrl.nullIfBlank() ?: thumbnailUrl.nullIfBlank(),
        viaDays = lunarMemorialDays
            .filter { it.lunarDay in 1..30 && it.lunarMonth in 1..12 }
            .map { BuddhaViaDay(day = it.lunarDay, month = it.lunarMonth, name = it.eventName.ifBlank { "Ngày vía" }) },
        bio = biographySummary.nullIfBlank(),
        canonicalSource = canonicalSource.nullIfBlank(),
        vows = greatVows.filter { it.isNotBlank() },
        mantras = listOfNotNull(
            mantras.shortMantraSinoVietnamese.nullIfBlank()?.let {
                BuddhaMantra(
                    label = "TÂM CHÚ",
                    text = it,
                    sanskrit = mantras.shortMantraSanskrit.nullIfBlank(),
                    note = "Trì niệm hằng ngày, nhất tâm xưng niệm.",
                )
            },
            mantras.longDharaniName.nullIfBlank()?.let {
                BuddhaMantra(
                    label = "ĐÀ LA NI",
                    text = it,
                    note = mantras.dharaniPhoneticSnippet.nullIfBlank().orEmpty(),
                )
            },
        ),
        sutras = emptyList(),
    )
}
