package com.example.innerlamp.core.media

/**
 * Cổng phát âm thanh thực hành (mõ / chuông / lần hạt).
 *
 * Tách interface để tầng UI không phụ thuộc trực tiếp vào [android.media.SoundPool]
 * và test có thể thay bằng fake. Tuân theo ranh giới layering: `core/media` chỉ là
 * hạ tầng phát âm, không chứa logic nghiệp vụ.
 */
interface SoundPlayer {

    /** Phát [sound] một lần (non-blocking). Gọi từ mọi luồng đều an toàn. */
    fun play(sound: MeditationSound)

    /** Giải phóng tài nguyên âm thanh khi không còn dùng (gọi trong `onCleared`). */
    fun release()
}
