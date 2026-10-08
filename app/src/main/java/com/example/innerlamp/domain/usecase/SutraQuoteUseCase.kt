package com.example.innerlamp.domain.usecase

import com.example.innerlamp.domain.model.SutraQuote
import com.example.innerlamp.domain.repository.SutraQuoteRepository
import java.time.LocalDate
import kotlin.random.Random

/**
 * Lấy quote Pháp Cú (thuật toán y chang iOS `ILSutraQuoteUseCase`).
 *
 * Nếu DB rỗng lúc đọc (seed chưa xong hoặc lần trước lỗi) thì seed rồi mới đọc lại.
 * Trả `null`/rỗng khi vẫn không có dữ liệu để UI hiện câu fallback.
 */
class SutraQuoteUseCase(
    private val repository: SutraQuoteRepository,
    private val random: Random = Random.Default,
) {

    /** Quote ngẫu nhiên đều, không cache: mỗi lần gọi bốc lại. `null` khi catalog rỗng. */
    suspend fun randomQuote(): SutraQuote? {
        val total = totalOrSeed()
        if (total <= 0) return null
        return repository.quoteAt(random.nextInt(0, total))
    }

    /** Tối đa [count] quote khác nhau (catalog nhỏ hơn thì trả ít hơn); [count] <= 0 → rỗng. */
    suspend fun randomQuotes(count: Int): List<SutraQuote> {
        if (count <= 0) return emptyList()
        if (totalOrSeed() <= 0) return emptyList()
        return repository.getAll().shuffled(random).take(count)
    }

    /**
     * Quote cố định theo ngày: `dayOfYear % total` (dayOfYear 1-based, 1..366). Cùng một
     * ngày luôn ra cùng một quote. `null` khi catalog rỗng.
     */
    suspend fun quote(date: LocalDate): SutraQuote? {
        val total = totalOrSeed()
        if (total <= 0) return null
        return repository.quoteAt(date.dayOfYear % total)
    }

    private suspend fun totalOrSeed(): Int {
        val total = repository.count()
        if (total > 0) return total
        repository.seedIfNeeded()
        return repository.count()
    }
}
