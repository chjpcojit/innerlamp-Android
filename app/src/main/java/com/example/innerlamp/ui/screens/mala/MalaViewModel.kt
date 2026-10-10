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
 * Âm thanh: mỗi lần lần hạt phát tiếng hạt gỗ ([MeditationSound.Bead]); khi niệm tròn
 * số biến đã chọn (21/54/108) thì thỉnh chuông ([MeditationSound.Chuong]). Kế thừa [AndroidViewModel] để có `Application` làm
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

    /** Một lần niệm (chạm/kéo hạt): +1, nháy "pop"; đủ [MalaUiState.target] biến thì hiện hộp thoại hồi hướng. */
    fun onBeadAdvance() {
        val state = _uiState.value
        if (state.done) return
        val next = state.beads + 1
        val finished = next % state.target == 0
        // Tròn một vòng → thỉnh chuông; còn lại là tiếng lần hạt.
        soundPlayer.play(if (finished) MeditationSound.Chuong else MeditationSound.Bead)
        _uiState.update { it.copy(beads = next, done = finished, pop = true) }
        popJob?.cancel()
        popJob = viewModelScope.launch {
            delay(120)
            _uiState.update { it.copy(pop = false) }
        }
    }

    /** Chọn số biến một vòng — đặt hạt về 0 và đóng hộp thoại (prototype `mlTargets.pick`). */
    fun onTargetSelect(target: Int) {
        if (target !in Mala.TARGETS) return
        _uiState.update { it.copy(target = target, beads = 0, done = false) }
    }

    /** "Niệm tiếp vòng mới": đóng hộp thoại, hạt về 0, sang vòng kế. */
    fun onContinue() {
        _uiState.update { it.copy(done = false, beads = 0, round = it.round + 1) }
    }

    /** "Bắt đầu lại": đặt hạt về 0, giữ nguyên vòng hiện tại. */
    fun onReset() {
        _uiState.update { it.copy(beads = 0, done = false) }
    }

    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }
}
