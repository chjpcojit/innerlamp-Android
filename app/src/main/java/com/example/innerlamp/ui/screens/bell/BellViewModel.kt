package com.example.innerlamp.ui.screens.bell

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.innerlamp.core.media.MeditationSound
import com.example.innerlamp.core.media.SoundPlayer
import com.example.innerlamp.core.media.SoundPoolSoundPlayer
import com.example.innerlamp.domain.model.BellEveryOptions
import com.example.innerlamp.domain.model.BellLimits
import com.example.innerlamp.domain.model.BellMode
import com.example.innerlamp.domain.model.BellPresets
import com.example.innerlamp.domain.model.intervalMillis
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn hình Chuông mõ (bell).
 *
 * Toàn bộ nhịp/thời gian chạy bằng [viewModelScope] + `delay` (Main-backed) — không
 * gọi `Dispatchers.IO`/`Main`. Chuông mõ không cần repository/data layer (thuần cục bộ),
 * nên không có use case nào ở đây; dữ liệu tĩnh (preset/mode/mốc) lấy từ `domain/model`.
 *
 * Âm thanh mõ/chuông phát qua [SoundPlayer] ([SoundPoolSoundPlayer]) — thay cho tổng hợp
 * WebAudio của prototype. Kế thừa [AndroidViewModel] để có `Application` làm context nạp
 * file âm thanh từ `res/raw`.
 *
 * @author innerlamp
 */
class BellViewModel(application: Application) : AndroidViewModel(application) {

    /** Cổng phát âm thanh mõ/chuông; giải phóng khi ViewModel bị huỷ. */
    private val soundPlayer: SoundPlayer = SoundPoolSoundPlayer(application)

    private val _uiState = MutableStateFlow(BellUiState())
    val uiState: StateFlow<BellUiState> = _uiState.asStateFlow()

    /** Job của vòng lặp giữ nhịp tự động. */
    private var tickJob: Job? = null

    /** Job của chuỗi "dồn mõ" (nhịp nhanh dần). */
    private var rollJob: Job? = null

    /** Job tắt hiệu ứng nháy khi gõ mõ. */
    private var flashMoJob: Job? = null

    /** Job tắt hiệu ứng nháy khi thỉnh chuông. */
    private var flashChuongJob: Job? = null

    // ── Chọn cấu hình ────────────────────────────────────────────────────

    /** Chọn preset — dừng giữ nhịp rồi áp mode/nhịp/mốc của preset. */
    fun onPresetSelect(index: Int) {
        if (index !in BellPresets.all.indices) return
        stop()
        val preset = BellPresets.all[index]
        _uiState.update { s ->
            s.copy(
                presetIndex = index,
                mode = preset.mode,
                bpm = preset.bpm ?: s.bpm,
                gapSeconds = preset.gapSeconds ?: s.gapSeconds,
                everyIndex = preset.everyIndex ?: s.everyIndex,
            )
        }
    }

    /** Chọn chế độ — dừng giữ nhịp, đánh dấu đã rời preset. */
    fun onModeSelect(mode: BellMode) {
        stop()
        _uiState.update { it.copy(mode = mode, presetIndex = -1) }
    }

    /** Chọn mốc "thỉnh chuông sau mỗi" — đánh dấu đã rời preset. */
    fun onEverySelect(index: Int) {
        if (index !in BellEveryOptions.all.indices) return
        _uiState.update { it.copy(everyIndex = index, presetIndex = -1) }
    }

    // ── Hành động gõ / thỉnh ─────────────────────────────────────────────

    /** Chạm tấm gõ: chế độ Chuông thì thỉnh chuông, còn lại gõ mõ. */
    fun onPadTap() {
        val state = _uiState.value
        if (state.rolling) return
        if (state.mode == BellMode.Chuong) strikeChuong() else strikeMo()
    }

    /** Nút phải "Thỉnh chuông". */
    fun onStrikeChuong() = strikeChuong()

    /** Nút trái: chế độ Chuông → đặt lại; còn lại → dồn mõ. */
    fun onLeftAction() {
        if (_uiState.value.mode == BellMode.Chuong) reset() else roll()
    }

    /** Nút −: giảm nhịp/khoảng cách. */
    fun onRateDown() = changeRate(-1)

    /** Nút +: tăng nhịp/khoảng cách. */
    fun onRateUp() = changeRate(+1)

    /** Bật/tắt giữ nhịp tự động. */
    fun onToggleRun() {
        if (_uiState.value.running) stop() else start()
    }

    // ── Nội bộ ──────────────────────────────────────────────────────────

    /** Gõ một tiếng mõ; nếu chế độ Mõ + chuông và đủ mốc thì thỉnh chuông sau 180ms. */
    private fun strikeMo() {
        playMo()
        flashMo()
        val newCount = _uiState.updateAndGet { it.copy(moCount = it.moCount + 1) }.moCount
        val state = _uiState.value
        val every = BellEveryOptions.all[state.everyIndex]
        if (state.mode == BellMode.MoVaChuong && every > 0 && newCount % every == 0) {
            viewModelScope.launch {
                delay(180)
                strikeChuong()
            }
        }
    }

    /** Thỉnh một tiếng chuông. */
    private fun strikeChuong() {
        playChuong()
        flashChuong()
        _uiState.update { it.copy(chuongCount = it.chuongCount + 1) }
    }

    /** Bắt đầu vòng lặp giữ nhịp tự động (tính lại khoảng cách ở mỗi nhịp). */
    private fun start() {
        stop()
        _uiState.update { it.copy(running = true) }
        tickJob = viewModelScope.launch {
            while (isActive) {
                val state = _uiState.value
                delay(state.mode.intervalMillis(state.bpm, state.gapSeconds))
                val current = _uiState.value
                if (!current.running) break
                if (current.mode == BellMode.Chuong) strikeChuong() else strikeMo()
            }
        }
    }

    /** Dừng giữ nhịp tự động. */
    private fun stop() {
        tickJob?.cancel()
        tickJob = null
        _uiState.update { it.copy(running = false) }
    }

    /** Dồn mõ: 16 tiếng mõ nhanh dần (440ms → giảm ×0.8, tối thiểu 70ms) rồi kết một tiếng chuông. */
    private fun roll() {
        if (_uiState.value.rolling) return
        stop()
        _uiState.update { it.copy(rolling = true) }
        rollJob?.cancel()
        rollJob = viewModelScope.launch {
            var gap = 440.0
            repeat(16) {
                strikeMo()
                delay(gap.toLong())
                gap = maxOf(70.0, gap * 0.8)
            }
            delay(260)
            strikeChuong()
            _uiState.update { it.copy(rolling = false) }
        }
    }

    /** Đặt lại bộ đếm mõ/chuông. */
    private fun reset() {
        stop()
        rollJob?.cancel()
        _uiState.update { it.copy(moCount = 0, chuongCount = 0, rolling = false) }
    }

    /** Điều chỉnh nhịp mõ (±4) hoặc khoảng cách chuông (±5s), rời preset. */
    private fun changeRate(dir: Int) {
        _uiState.update { s ->
            if (s.mode == BellMode.Chuong) {
                val gap = (s.gapSeconds + dir * BellLimits.GapStepSeconds)
                    .coerceIn(BellLimits.GapMinSeconds, BellLimits.GapMaxSeconds)
                s.copy(gapSeconds = gap, presetIndex = -1)
            } else {
                val bpm = (s.bpm + dir * BellLimits.BpmStep)
                    .coerceIn(BellLimits.BpmMin, BellLimits.BpmMax)
                s.copy(bpm = bpm, presetIndex = -1)
            }
        }
    }

    /** Nháy hiệu ứng gõ mõ trong 110ms. */
    private fun flashMo() {
        _uiState.update { it.copy(hitMo = true) }
        flashMoJob?.cancel()
        flashMoJob = viewModelScope.launch {
            delay(110)
            _uiState.update { it.copy(hitMo = false) }
        }
    }

    /** Nháy hiệu ứng thỉnh chuông trong 420ms. */
    private fun flashChuong() {
        _uiState.update { it.copy(hitChuong = true) }
        flashChuongJob?.cancel()
        flashChuongJob = viewModelScope.launch {
            delay(420)
            _uiState.update { it.copy(hitChuong = false) }
        }
    }

    // ── Âm thanh ─────────────────────────────────────────────────────────

    /** Phát tiếng mõ (mộc ngư). */
    private fun playMo() {
        soundPlayer.play(MeditationSound.Mo)
    }

    /** Phát tiếng chuông (gong ngân dài). */
    private fun playChuong() {
        soundPlayer.play(MeditationSound.Chuong)
    }

    override fun onCleared() {
        soundPlayer.release()
        super.onCleared()
    }
}
