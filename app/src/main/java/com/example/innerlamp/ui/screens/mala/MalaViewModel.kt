package com.example.innerlamp.ui.screens.mala

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.core.media.MeditationSound
import com.example.innerlamp.core.media.SoundPlayer
import com.example.innerlamp.core.media.SoundPoolSoundPlayer
import com.example.innerlamp.domain.model.Mala
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn Niệm Phật (`mala`).
 *
 * Tràng hạt chạy thuần cục bộ (không cần repository/data layer), giống màn Chuông mõ —
 * nên không có use case ở đây, logic thuần nằm trong `domain/model/Mala`. Việc lưu
 * chuỗi hạt xuống Room + đồng bộ là của `il-data-builder` (README "local-first").
 *
 * Âm thanh: mỗi lần lần hạt phát tiếng hạt gỗ ([MeditationSound.Bead]); khi niệm xong
 * hạt 108 (trọn một tràng) thì thỉnh chuông ([MeditationSound.Chuong]) — khớp chú thích
 * "chuông ở hạt 108" của prototype. Kế thừa [AndroidViewModel] để có `Application` làm
 * context nạp file âm thanh từ `res/raw`.
 *
 * TODO(il-data-builder): lưu `beads`/`round` vào repository thực hành để giữ tràng
 * hạt giữa các phiên; hiện chỉ là state trong ViewModel nên mất khi thoát màn.
 */
class MalaViewModel(application: Application) : AndroidViewModel(application) {

    /** Cổng phát âm thanh lần hạt/chuông; giải phóng khi ViewModel bị huỷ. */
    private val soundPlayer: SoundPlayer = SoundPoolSoundPlayer(application)

    private val _uiState = MutableStateFlow(MalaUiState())
    val uiState: StateFlow<MalaUiState> = _uiState.asStateFlow()

    /** Job tắt hiệu ứng "pop" sau 120ms (khớp `bumpBead` của prototype). */
    private var popJob: Job? = null

    /** Một lần niệm (chạm/kéo hạt): +1, vòng lại 1 sau 108 và nháy hiệu ứng "pop". */
    fun onBeadAdvance() {
        val state = _uiState.value
        // Hạt 108 khép lại một tràng → thỉnh chuông; còn lại là tiếng lần hạt.
        soundPlayer.play(
            if (state.beads >= Mala.BEADS_PER_ROUND) MeditationSound.Chuong else MeditationSound.Bead,
        )
        _uiState.update { s ->
            val next = Mala.advance(s.beads)
            s.copy(
                beads = next,
                // Sang tràng mới chỉ khi vừa niệm xong hạt 108 (wrap), không phải lúc 0 → 1.
                round = if (s.beads >= Mala.BEADS_PER_ROUND) s.round + 1 else s.round,
                pop = true,
            )
        }
        popJob?.cancel()
        popJob = viewModelScope.launch {
            delay(120)
            _uiState.update { it.copy(pop = false) }
        }
    }

    /** "Bắt đầu lại": đặt hạt về 0, giữ nguyên tràng hiện tại. */
    fun onReset() {
        _uiState.update { it.copy(beads = 0) }
    }

    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }
}
