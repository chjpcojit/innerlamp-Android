package com.example.innerlamp.core.media

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * [SoundPlayer] dùng [SoundPool] — phù hợp cho các âm ngắn, độ trễ thấp (gõ mõ giữ nhịp,
 * lần hạt từng tiếng). Tải toàn bộ [MeditationSound] lên ngay khi tạo, phát không chặn.
 *
 * Dùng `USAGE_MEDIA` + `CONTENT_TYPE_MUSIC`: âm thanh theo luồng nhạc/phương tiện của
 * hệ thống, đúng ngữ nghĩa "chuông/mõ là âm chính của màn thực hành".
 */
class SoundPoolSoundPlayer(context: Context) : SoundPlayer {

    private val soundPool: SoundPool
    private val soundIds: Map<MeditationSound, Int>

    init {
        soundPool = SoundPool.Builder()
            .setMaxStreams(MeditationSound.entries.size)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
            )
            .build()

        // `load` trả về id ngay; SoundPool tự nạp dữ liệu ngầm. Khoảng thời gian từ lúc
        // mở màn đến thao tác đầu tiên đủ để âm thanh sẵn sàng.
        soundIds = MeditationSound.entries.associateWith { sound ->
            soundPool.load(context, sound.resId, 1)
        }
    }

    override fun play(sound: MeditationSound) {
        val id = soundIds[sound] ?: return
        soundPool.play(id, VOLUME, VOLUME, PRIORITY, NO_LOOP, RATE)
    }

    override fun release() {
        soundPool.release()
    }

    private companion object {
        const val VOLUME = 1f
        const val PRIORITY = 1
        const val NO_LOOP = 0
        const val RATE = 1f
    }
}
