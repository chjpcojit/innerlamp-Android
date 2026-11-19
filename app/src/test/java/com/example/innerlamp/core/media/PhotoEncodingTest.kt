package com.example.innerlamp.core.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The parts of photo normalisation that decide what is sent — format sniffing, the size cap and the MIME /
 * filename pairing. Bitmap decoding itself needs a device, so it is covered by the manual E2E (AGENTS.md). /
 * Phần quyết định ảnh gửi đi — nhận định dạng, giới hạn kích thước, cặp MIME/tên file. Việc giải mã bitmap
 * cần máy thật nên nằm ở E2E thủ công.
 */
class PhotoEncodingTest {

    private fun jpegHeader(): ByteArray = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte())

    private fun pngHeader(): ByteArray = byteArrayOf(
        0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
    )

    private fun webpHeader(): ByteArray {
        val head = "RIFF".toByteArray(Charsets.US_ASCII) + byteArrayOf(0, 0, 0, 0) +
            "WEBP".toByteArray(Charsets.US_ASCII)
        return head
    }

    private fun heicHeader(): ByteArray {
        val size = byteArrayOf(0, 0, 0, 0x18)
        return size + "ftyp".toByteArray(Charsets.US_ASCII) + "heic".toByteArray(Charsets.US_ASCII) +
            byteArrayOf(0, 0, 0, 0)
    }

    @Test
    fun the_real_format_comes_from_the_magic_bytes() {
        assertEquals(PhotoKind.Jpeg, PhotoEncoding.sniff(jpegHeader()))
        assertEquals(PhotoKind.Png, PhotoEncoding.sniff(pngHeader()))
        assertEquals(PhotoKind.Webp, PhotoEncoding.sniff(webpHeader()))
        assertEquals(PhotoKind.Heic, PhotoEncoding.sniff(heicHeader()))
    }

    @Test
    fun garbage_bytes_are_unknown_and_must_be_rejected() {
        val garbage = ByteArray(1024) { (it * 31 % 251).toByte() }
        assertEquals(PhotoKind.Unknown, PhotoEncoding.sniff(garbage))
        assertEquals(PhotoKind.Unknown, PhotoEncoding.sniff(byteArrayOf()))
        assertEquals(PhotoKind.Unknown, PhotoEncoding.sniff("hello".toByteArray()))
    }

    @Test
    fun a_4000px_image_is_capped_at_2048_and_keeps_its_ratio() {
        assertEquals(2048 to 1536, PhotoEncoding.scaledSize(4000, 3000))
        assertEquals(1536 to 2048, PhotoEncoding.scaledSize(3000, 4000))
    }

    @Test
    fun an_image_inside_the_cap_is_not_resized() {
        assertEquals(2048 to 1000, PhotoEncoding.scaledSize(2048, 1000))
        assertEquals(1000 to 750, PhotoEncoding.scaledSize(1000, 750))
    }

    @Test
    fun a_portrait_image_gets_a_whole_pixel_long_side() {
        val (width, height) = PhotoEncoding.scaledSize(1200, 4000)
        assertEquals(2048, height)
        assertTrue(width in 613..615)
    }

    @Test
    fun heic_becomes_jpeg_and_other_formats_keep_their_type_until_they_shrink() {
        assertEquals(PhotoMime.Jpeg, PhotoEncoding.outputMime(PhotoKind.Heic, needsResize = false))
        assertEquals(PhotoMime.Jpeg, PhotoEncoding.outputMime(PhotoKind.Jpeg, needsResize = false))
        assertEquals(PhotoMime.Png, PhotoEncoding.outputMime(PhotoKind.Png, needsResize = false))
        assertEquals(PhotoMime.Webp, PhotoEncoding.outputMime(PhotoKind.Webp, needsResize = false))
        assertEquals(PhotoMime.Jpeg, PhotoEncoding.outputMime(PhotoKind.Png, needsResize = true))
        assertEquals(PhotoMime.Jpeg, PhotoEncoding.outputMime(PhotoKind.Webp, needsResize = true))
    }

    @Test
    fun the_filename_extension_always_matches_the_mime_type() {
        assertEquals("photo.jpg", PhotoEncoding.fileName(PhotoMime.Jpeg))
        assertEquals("photo.png", PhotoEncoding.fileName(PhotoMime.Png))
        assertEquals("photo.webp", PhotoEncoding.fileName(PhotoMime.Webp))
        PhotoMime.entries.forEach { mime ->
            assertTrue(PhotoEncoding.fileName(mime).endsWith(".${mime.extension}"))
            assertNotEquals("", mime.mimeType.substringAfter("/"))
        }
    }

    @Test
    fun the_limits_match_the_contract() {
        assertEquals(2048, PhotoEncoding.MAX_SIDE)
        assertEquals(5 * 1024 * 1024, PhotoEncoding.MAX_BYTES)
        assertTrue(PhotoEncoding.MAX_RAW_BYTES > PhotoEncoding.MAX_BYTES)
    }
}
