package com.example.innerlamp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.innerlamp.domain.model.ContentError
import com.example.innerlamp.ui.theme.LocalIlColors

/**
 * Load status of a screen that reads a public catalogue (temples, deities, gallery). /
 * Trạng thái tải của một màn đọc danh mục công khai (chùa, chư Phật, ảnh).
 */
enum class ContentStatus { Loading, Loaded, Offline, Failed, NotFound }

/** Maps a repository failure to the status shown on screen. / Đổi lỗi của repository sang trạng thái màn. */
fun Throwable.toContentStatus(): ContentStatus = when (this) {
    ContentError.Offline -> ContentStatus.Offline
    ContentError.NotFound -> ContentStatus.NotFound
    else -> ContentStatus.Failed
}

/** Shared copy of the catalogue screens. / Câu chữ dùng chung cho các màn danh mục. */
object ContentText {
    const val OFFLINE = "Không có kết nối mạng. Vui lòng kiểm tra và thử lại."
    const val FAILED = "Không tải được dữ liệu. Vui lòng thử lại."
    const val RETRY = "Thử lại"
    const val UPDATING = "Đang cập nhật"
}

/** Centered spinner for [ContentStatus.Loading]. / Vòng quay giữa vùng cho trạng thái đang tải. */
@Composable
fun IlContentLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = LocalIlColors.current.mid, strokeWidth = 2.dp)
    }
}

/**
 * Offline / generic error with a "Thử lại" button; [notFoundTitle] is shown for [ContentStatus.NotFound]
 * (no retry there). /
 * Lỗi mất mạng / lỗi chung kèm nút "Thử lại"; [notFoundTitle] dùng cho `NotFound` (không có nút thử lại).
 */
@Composable
fun IlContentError(
    status: ContentStatus,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    notFoundTitle: String = "Không tìm thấy nội dung này.",
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        when (status) {
            ContentStatus.NotFound -> IlState(title = notFoundTitle, kind = IlStateKind.Empty)
            ContentStatus.Offline -> IlState(title = ContentText.OFFLINE, kind = IlStateKind.Offline)
            else -> IlState(title = ContentText.FAILED, kind = IlStateKind.Error)
        }
        if (status != ContentStatus.NotFound) {
            IlButton(label = ContentText.RETRY, variant = IlButtonVariant.Ghost, full = false, onClick = onRetry)
        }
    }
}
