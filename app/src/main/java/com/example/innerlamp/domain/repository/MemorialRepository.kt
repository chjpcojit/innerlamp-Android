package com.example.innerlamp.domain.repository

import com.example.innerlamp.domain.model.MemorialEntry
import com.example.innerlamp.domain.model.MemorialLoad

/**
 * The book of the signed-in account. Server-first: reads may fall back to the device copy, writes always
 * need the server (decision D5). /
 * Sổ của tài khoản đang đăng nhập. Server là nguồn đúng: đọc có thể dùng bản trên máy, ghi luôn cần server.
 */
interface MemorialRepository {

    /** Loads the server copy, replaces the device copy, or falls back to it. / Tải sổ, thay bản trên máy. */
    suspend fun load(): MemorialLoad

    /** `POST`; the device copy gets the created entry. / Tạo mục. */
    suspend fun create(entry: MemorialEntry): Result<MemorialEntry>

    /** `PUT` with the whole body; the device copy is replaced. / Sửa toàn bộ mục. */
    suspend fun update(entry: MemorialEntry): Result<MemorialEntry>

    /** `DELETE`; a 404 counts as done. / Xoá; 404 coi như xong. */
    suspend fun delete(id: Long): Result<Unit>

    /**
     * `PUT .../photo` (multipart) with the already normalised bytes and the MIME type and filename that match
     * them. Returns the entry with its new signed URL. /
     * `PUT .../photo` với bytes đã chuẩn hoá và MIME/tên file khớp đúng bytes đó.
     */
    suspend fun uploadPhoto(
        id: Long,
        bytes: ByteArray,
        mimeType: String,
        fileName: String,
    ): Result<MemorialEntry>

    /** `DELETE .../photo`; a photo-less entry still returns 200. / Xoá ảnh. */
    suspend fun deletePhoto(id: Long): Result<MemorialEntry>

    /** Drops every device copy of every account (sign-out, account switch). / Xoá bản trên máy. */
    suspend fun clearCache()
}
