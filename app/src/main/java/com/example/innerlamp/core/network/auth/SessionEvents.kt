package com.example.innerlamp.core.network.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Session-level events from the network layer. `MainActivity` listens and sends the user to Login. /
 * Sự kiện phiên từ lớp network. `MainActivity` lắng nghe và đưa người dùng về Login.
 */
class SessionEvents {
    private val _ended = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emits when the refresh token was rejected. / Phát khi refresh token bị từ chối. */
    val ended: SharedFlow<Unit> = _ended.asSharedFlow()

    fun emitEnded() {
        _ended.tryEmit(Unit)
    }
}
