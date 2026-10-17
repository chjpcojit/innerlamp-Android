package com.example.innerlamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.innerlamp.ui.theme.LocalIlColors

/**
 * Plain scrolling page for the short auth screens (code entry, forgot / reset password): nav bar on
 * top, gradient canvas like Login. / Trang cuộn đơn giản cho các màn auth ngắn: nav bar trên cùng, nền gradient như Login.
 */
@Composable
fun IlAuthScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.card2, colors.sunk)))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 26.dp),
    ) {
        IlNavBar(title = title, showBack = true, onBack = onBack)
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), content = content)
    }
}
