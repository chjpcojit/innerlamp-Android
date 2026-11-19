package com.example.innerlamp.core.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import com.example.innerlamp.core.util.DispatcherProvider
import com.example.innerlamp.domain.model.MemorialError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.withContext

/**
 * Turns whatever the photo picker returns into bytes the memorial route accepts (contract §3.6/§4): the real
 * format is sniffed from the magic bytes (never the filename), HEIC/HEIF is decoded on API 28+, the EXIF
 * orientation is applied, the long side is capped at 2048 px, JPEG output is quality 85, and the 5 MiB limit is
 * checked on the bytes actually sent. Everything runs on [DispatcherProvider.io]. /
 * Đổi ảnh bất kỳ từ bộ chọn thành bytes route ảnh chấp nhận: nhận định dạng thật từ magic bytes (không tin tên
 * file), giải mã HEIC/HEIF (API 28+), áp hướng EXIF, cạnh dài ≤ 2048 px, JPEG chất lượng 85, kiểm 5 MiB trên
 * bytes thực gửi. Toàn bộ chạy trên [DispatcherProvider.io].
 */
class MemorialPhotoNormalizer(
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider,
    private val maxSide: Int = PhotoEncoding.MAX_SIDE,
    private val quality: Int = PhotoEncoding.QUALITY,
) {

    suspend fun normalize(raw: ByteArray): Result<NormalizedPhoto> = withContext(dispatchers.io) {
        when {
            raw.isEmpty() -> return@withContext invalid()
            raw.size > PhotoEncoding.MAX_RAW_BYTES -> return@withContext Result.failure(MemorialError.TooLarge)
        }
        val kind = PhotoEncoding.sniff(raw)
        if (kind == PhotoKind.Unknown) return@withContext invalid()
        val bounds = decodeBounds(raw) ?: return@withContext invalid()
        val needsResize = maxOf(bounds.width, bounds.height) > maxSide

        // JPEG/PNG/WebP inside the cap are uploaded untouched so PNG/WebP keep their format (and alpha).
        // JPEG/PNG/WebP trong giới hạn được gửi nguyên để giữ định dạng (và alpha).
        if (kind != PhotoKind.Heic && !needsResize) {
            if (raw.size > PhotoEncoding.MAX_BYTES) {
                return@withContext Result.failure(MemorialError.TooLarge)
            }
            val mime = PhotoEncoding.outputMime(kind, needsResize = false)
            return@withContext Result.success(
                NormalizedPhoto(raw, mime.mimeType, PhotoEncoding.fileName(mime)),
            )
        }

        val decoded = decodeScaled(raw, bounds) ?: return@withContext invalid()
        val upright = rotate(decoded, readOrientation(raw))
        val target = PhotoEncoding.scaledSize(upright.width, upright.height, maxSide)
        val scaled = if (upright.width == target.first && upright.height == target.second) {
            upright
        } else {
            upright.scale(target.first, target.second, filter = true)
        }
        var out = compressJpeg(scaled, quality) ?: return@withContext invalid()
        if (out.size > PhotoEncoding.MAX_BYTES) {
            // One lower-quality pass is enough at 2048 px; beyond that the image is not sendable.
            // Một lần hạ chất lượng là đủ ở 2048 px; quá nữa thì không gửi được.
            out = compressJpeg(scaled, FALLBACK_QUALITY) ?: return@withContext invalid()
        }
        if (upright !== decoded) decoded.recycle()
        if (scaled !== upright) upright.recycle()
        scaled.recycle()
        if (out.size > PhotoEncoding.MAX_BYTES) {
            return@withContext Result.failure(MemorialError.TooLarge)
        }
        val mime = PhotoMime.Jpeg
        Result.success(NormalizedPhoto(out, mime.mimeType, PhotoEncoding.fileName(mime)))
    }

    private fun invalid(): Result<NormalizedPhoto> =
        Result.failure(MemorialError.Validation("photo"))

    private fun decodeBounds(bytes: ByteArray): Size? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        if (options.outWidth <= 0 || options.outHeight <= 0) return null
        return Size(options.outWidth, options.outHeight)
    }

    /** Decodes with a power-of-two sample so the bitmap is close to [maxSide] before the exact scale. */
    private fun decodeScaled(bytes: ByteArray, bounds: Size): Bitmap? {
        var sample = 1
        while (maxOf(bounds.width, bounds.height) / (sample * 2) >= maxSide) sample *= 2
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
    }

    private fun readOrientation(bytes: ByteArray): Int = runCatching {
        ExifInterface(ByteArrayInputStream(bytes))
            .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)

    private fun rotate(source: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return source
        }
        return runCatching {
            Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }.getOrDefault(source)
    }

    private fun compressJpeg(bitmap: Bitmap, quality: Int): ByteArray? = runCatching {
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            stream.toByteArray()
        }
    }.getOrNull()

    private data class Size(val width: Int, val height: Int)

    private companion object {
        /** Second pass when the 85-quality JPEG is still over the limit. / Lần hai khi JPEG q85 vẫn quá lớn. */
        const val FALLBACK_QUALITY = 60
    }
}
