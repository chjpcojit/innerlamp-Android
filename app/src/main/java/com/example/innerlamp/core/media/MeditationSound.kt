package com.example.innerlamp.core.media

import androidx.annotation.RawRes
import com.example.innerlamp.R

/**
 * Các âm thanh thực hành của app, ánh xạ tới file trong `res/raw/`.
 *
 * Nguồn (đều là CC0 / public domain — không cần ghi công):
 * - [Mo]  — tiếng mõ (mộc ngư): Freesound `607215__jonopodmore__mokugyo.wav`.
 * - [Chuong] — tiếng chuông/gong: Wikimedia Commons `Gong_or_bell_vibrant_(short).ogg`.
 * - [Bead] — tiếng lần hạt (gỗ gõ nhẹ): Freesound `321083__benjaminnelan__wooden-click`.
 *
 * Các file đã được chuẩn hoá về WAV mono 16-bit 44.1 kHz để [android.media.SoundPool]
 * phát ngay không cần giải mã (giữ nhịp chính xác ở màn Chuông mõ).
 */
enum class MeditationSound(@param:RawRes val resId: Int) {
    /** Tiếng mõ (mộc ngư) — gõ một tiếng. */
    Mo(R.raw.mo),

    /** Tiếng chuông (gong) ngân dài — thỉnh chuông / hoàn tất một tràng. */
    Chuong(R.raw.chuong),

    /** Tiếng lần hạt — tiếng "cốc" nhẹ của hạt gỗ khi niệm từng hạt. */
    Bead(R.raw.bead),
}
