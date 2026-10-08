package com.example.innerlamp.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.innerlamp.R
import kotlinx.coroutines.delay

// Thời gian giữ ảnh launch trước khi mờ dần để lộ splash động bên dưới.
private const val LaunchImageHoldMs = 900L
private const val LaunchImageFadeMs = 400

/**
 * Ảnh launch tĩnh (Docs/design_handoff_innerlamp/launch/) phủ toàn màn hình lúc khởi động nguội.
 * Sáng/tối theo **hệ thống** (không theo theme app) — đúng launch/README.md; resource
 * `launch_image` có bản riêng ở drawable-nodpi / drawable-night-nodpi.
 * `ContentScale.Crop` = `resizeMode: "cover"` trong README. Chỉ hiện một lần mỗi lần mở app
 * (rememberSaveable nên xoay màn hình không phát lại).
 */
@Composable
fun LaunchImageOverlay(modifier: Modifier = Modifier) {
    var visible by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(LaunchImageHoldMs)
        visible = false
    }
    AnimatedVisibility(
        visible = visible,
        exit = fadeOut(tween(LaunchImageFadeMs)),
        modifier = modifier,
    ) {
        Image(
            painter = painterResource(R.drawable.launch_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
