package com.example.innerlamp.core.media

/** Image formats the memorial photo route accepts, plus the two families we convert. / Định dạng ảnh chấp nhận. */
enum class PhotoKind { Jpeg, Png, Webp, Heic, Unknown }

/** A supported output format with its MIME type and the extension the multipart filename must use. */
enum class PhotoMime(val mimeType: String, val extension: String) {
    Jpeg("image/jpeg", "jpg"),
    Png("image/png", "png"),
    Webp("image/webp", "webp"),
}

/** The bytes to upload, with the MIME type and filename that actually match them. / Bytes gửi đi + MIME + tên. */
data class NormalizedPhoto(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
) {
    override fun equals(other: Any?): Boolean =
        other is NormalizedPhoto && bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType && fileName == other.fileName

    override fun hashCode(): Int = bytes.contentHashCode() * 31 + mimeType.hashCode() * 31 + fileName.hashCode()
}

/**
 * Format sniffing and sizing rules, kept free of Android types so they run as JVM tests. The server sniffs the
 * first 512 bytes and does not trust the part's Content-Type, so the client must know the real format. /
 * Nhận dạng định dạng và luật kích thước, không dùng kiểu Android nên test được trên JVM. Server tự sniff
 * 512 byte đầu và không tin Content-Type của part, nên client phải biết đúng định dạng.
 */
object PhotoEncoding {

    /** Long side of the image sent; keeps a 2048px JPEG far below the server's 5 MiB limit. / Cạnh dài. */
    const val MAX_SIDE = 2048

    /** JPEG quality after decoding (HEIC always, other formats only when they must shrink). / Chất lượng JPEG. */
    const val QUALITY = 85

    const val MAX_BYTES = 5 * 1024 * 1024

    /** Raw file read from the picker is capped so a huge file cannot exhaust memory. / Trần đọc file gốc. */
    const val MAX_RAW_BYTES = 32 * 1024 * 1024

    private val JPEG = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
    private val PNG = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    )

    /** Real format from the magic bytes; `Unknown` covers everything we cannot decode and send. / Định dạng thật. */
    fun sniff(bytes: ByteArray): PhotoKind = when {
        bytes.startsWith(JPEG) -> PhotoKind.Jpeg
        bytes.startsWith(PNG) -> PhotoKind.Png
        isWebp(bytes) -> PhotoKind.Webp
        isHeif(bytes) -> PhotoKind.Heic
        else -> PhotoKind.Unknown
    }

    private fun isWebp(bytes: ByteArray): Boolean =
        bytes.size >= 12 &&
            bytes.copyOfRange(0, 4).contentEquals("RIFF".toByteArray(Charsets.US_ASCII)) &&
            bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray(Charsets.US_ASCII))

    /** HEIC/HEIF/AVIF: an ISO-BMFF `ftyp` box whose brand says so. / HEIC/HEIF/AVIF qua box `ftyp`. */
    private fun isHeif(bytes: ByteArray): Boolean {
        if (bytes.size < 12) return false
        if (!bytes.copyOfRange(4, 8).contentEquals("ftyp".toByteArray(Charsets.US_ASCII))) return false
        val brand = String(bytes, 8, 4, Charsets.US_ASCII)
        return brand in HEIF_BRANDS
    }

    private val HEIF_BRANDS = setOf("heic", "heix", "hevc", "hevx", "heim", "heis", "mif1", "msf1", "avif", "avis")

    private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
        size >= prefix.size && copyOfRange(0, prefix.size).contentEquals(prefix)

    /**
     * Size to send: the long side is capped at [maxSide] and the ratio is kept. An image inside the cap is
     * returned unchanged. / Cạnh dài tối đa [maxSide], giữ tỉ lệ; ảnh nhỏ hơn giữ nguyên.
     */
    fun scaledSize(width: Int, height: Int, maxSide: Int = MAX_SIDE): Pair<Int, Int> {
        if (width <= 0 || height <= 0) return 1 to 1
        val longSide = maxOf(width, height)
        if (longSide <= maxSide) return width to height
        val ratio = maxSide.toDouble() / longSide
        return maxOf(1, (width * ratio).toInt()) to maxOf(1, (height * ratio).toInt())
    }

    /**
     * Format actually uploaded: HEIC/HEIF becomes JPEG; JPEG/PNG/WebP keep their format when they need no
     * resize, and become JPEG when they must be decoded to shrink. /
     * Định dạng gửi đi: HEIC/HEIF → JPEG; JPEG/PNG/WebP giữ nguyên nếu không phải thu nhỏ, ngược lại → JPEG.
     */
    fun outputMime(kind: PhotoKind, needsResize: Boolean): PhotoMime = when {
        kind == PhotoKind.Heic -> PhotoMime.Jpeg
        needsResize -> PhotoMime.Jpeg
        kind == PhotoKind.Png -> PhotoMime.Png
        kind == PhotoKind.Webp -> PhotoMime.Webp
        else -> PhotoMime.Jpeg
    }

    /** The multipart filename must end with an extension that matches the MIME type. / Tên file khớp MIME. */
    fun fileName(mime: PhotoMime): String = "photo.${mime.extension}"
}
