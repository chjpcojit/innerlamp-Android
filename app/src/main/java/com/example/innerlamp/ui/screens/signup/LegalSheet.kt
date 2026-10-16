package com.example.innerlamp.ui.screens.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.innerlamp.ui.components.IlButton
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.LocalIlColors
import com.example.innerlamp.ui.theme.Lora

/** Tab của sheet: Điều khoản sử dụng / Chính sách quyền riêng tư. */
enum class LegalTab { Terms, Privacy }

/**
 * Sheet "Điều khoản & quyền riêng tư" (prototype màn Đăng ký): nền mờ 40%, sheet cao
 * gần hết màn, thanh nắm, "Đóng" | tiêu đề, segmented 2 tab, nội dung cuộn, nút "Tôi đồng ý".
 * Chạm nền mờ / "Đóng" / Back → [onDismiss]; "Tôi đồng ý" → [onAgree].
 */
@Composable
fun LegalSheet(
    tab: LegalTab,
    onTabChange: (LegalTab) -> Unit,
    onDismiss: () -> Unit,
    onAgree: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    val doc = LegalDocs.getValue(tab)
    BackHandler(onBack = onDismiss)

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 14.dp)
                .fillMaxSize()
                .shadow(18.dp, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(colors.card, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                // Chặn chạm xuyên xuống nền mờ.
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        ) {
            // ── Đầu sheet ─────────────────────────────────────────────────
            Column(Modifier.padding(top = 5.dp, bottom = 10.dp)) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(width = 36.dp, height = 5.dp)
                        .background(colors.ink.copy(alpha = 0.24f), RoundedCornerShape(3.dp)),
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .padding(horizontal = 8.dp),
                ) {
                    Text(
                        text = "Đóng",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable(onClick = onDismiss)
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        style = TextStyle(fontFamily = BeVietnamPro, fontSize = 17.sp, color = colors.link),
                    )
                    Text(
                        text = "Điều khoản & quyền riêng tư",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 72.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = colors.ink,
                        ),
                    )
                }
                SegmentedTabs(
                    selected = tab,
                    onSelect = onTabChange,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp),
                )
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.ink.copy(alpha = 0.12f)),
            )

            // ── Nội dung cuộn ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 8.dp),
            ) {
                Text(
                    text = doc.title,
                    style = TextStyle(fontFamily = Lora, fontSize = 28.sp, lineHeight = 34.sp, color = colors.ink),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Hiệu lực từ 01/09/2026",
                    style = TextStyle(fontFamily = BeVietnamPro, fontSize = 12.sp, color = colors.ink.copy(alpha = 0.45f)),
                )
                doc.sections.forEach { (heading, body) ->
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = heading,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            lineHeight = 19.6.sp,
                            color = colors.ink,
                        ),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = body,
                        style = TextStyle(
                            fontFamily = Lora,
                            fontSize = 14.sp,
                            lineHeight = 23.8.sp,
                            color = colors.ink.copy(alpha = 0.72f),
                        ),
                    )
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    text = "Câu hỏi về ${doc.short}: hotro@innerlamp.vn",
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontSize = 12.sp,
                        lineHeight = 19.2.sp,
                        color = colors.ink.copy(alpha = 0.45f),
                    ),
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Chân sheet ────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(colors.ink.copy(alpha = 0.12f)),
            )
            IlButton(
                label = "Tôi đồng ý",
                onClick = onAgree,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 30.dp),
            )
        }
    }
}

/** Segmented control 2 tab: nền `ink` 7%, tab chọn nền card + bóng nhẹ. */
@Composable
private fun SegmentedTabs(
    selected: LegalTab,
    onSelect: (LegalTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.ink.copy(alpha = 0.07f), RoundedCornerShape(9.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        LegalTab.entries.forEach { tab ->
            val on = tab == selected
            val shape = RoundedCornerShape(7.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .then(if (on) Modifier.shadow(2.dp, shape) else Modifier)
                    .background(if (on) colors.card else Color.Transparent, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = LegalDocs.getValue(tab).tab,
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (on) colors.ink else colors.ink.copy(alpha = 0.5f),
                    ),
                )
            }
        }
    }
}

/** Một văn bản pháp lý: nhãn tab, tên ngắn (dòng liên hệ), tiêu đề, các mục (tiêu đề, nội dung). */
private class LegalDoc(
    val tab: String,
    val short: String,
    val title: String,
    val sections: List<Pair<String, String>>,
)

// Nội dung mẫu lấy từ `LEGAL` của prototype.
// TODO(il-data-builder): lấy văn bản chính thức từ backend/CMS khi có.
private val LegalDocs = mapOf(
    LegalTab.Terms to LegalDoc(
        tab = "Điều khoản",
        short = "điều khoản",
        title = "Điều khoản sử dụng",
        sections = listOf(
            "1. Phạm vi" to "InnerLamp là ứng dụng hỗ trợ tu tập: tụng kinh, nghe pháp thoại, thiền, thắp đèn và cúng dường đến các chùa. Khi tạo tài khoản, bạn đồng ý sử dụng ứng dụng theo các điều khoản dưới đây.",
            "2. Tài khoản" to "Bạn chịu trách nhiệm về thông tin đăng ký và giữ bảo mật mã xác thực. Mỗi số điện thoại chỉ gắn với một tài khoản.",
            "3. Nội dung" to "Kinh văn, pháp thoại và bài viết thuộc quyền của chư tôn đức và đơn vị phát hành. Bạn được dùng cho mục đích tu tập cá nhân, không sao chép để kinh doanh.",
            "4. Cúng dường" to "Khoản cúng dường chuyển thẳng vào tài khoản của chùa qua VietQR. InnerLamp không giữ tiền và không thu phí trên khoản cúng dường.",
            "5. Thông tin chùa do người dùng gửi" to "Chùa bạn đề xuất sẽ được InnerLamp xác minh trước khi hiển thị trong danh bạ. Chúng tôi có thể chỉnh sửa hoặc từ chối thông tin chưa chính xác.",
            "6. Chấm dứt" to "Bạn có thể xóa tài khoản bất cứ lúc nào trong Cài đặt. InnerLamp có thể tạm khóa tài khoản vi phạm điều khoản.",
        ),
    ),
    LegalTab.Privacy to LegalDoc(
        tab = "Quyền riêng tư",
        short = "quyền riêng tư",
        title = "Chính sách quyền riêng tư",
        sections = listOf(
            "Thông tin chúng tôi thu thập" to "Họ tên, pháp danh, số điện thoại, nơi xuất gia (nếu có) và lịch sử tu tập trong ứng dụng như số phiên thiền, đèn đã thắp.",
            "Mục đích sử dụng" to "Để đăng nhập, gửi nhắc nhở chuông tỉnh thức, ghi sổ công đức và gợi ý nội dung phù hợp. Chúng tôi không bán dữ liệu cá nhân.",
            "Nhật ký và lời nguyện" to "Nhật ký tu tập và lời nguyện khi thắp đèn ở chế độ riêng tư theo mặc định. Chỉ hiển thị trong vườn đèn khi bạn chọn công khai.",
            "Chia sẻ với chùa" to "Khi cúng dường, chùa nhận được tên hiển thị và số tiền. Bạn có thể chọn cúng dường ẩn danh.",
            "Lưu trữ và xóa" to "Dữ liệu được lưu trên máy chủ đặt tại Việt Nam. Khi bạn xóa tài khoản, dữ liệu cá nhân bị xóa trong vòng 30 ngày.",
        ),
    ),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LegalSheetPreview() {
    InnerLampTheme {
        LegalSheet(tab = LegalTab.Terms, onTabChange = {}, onDismiss = {}, onAgree = {})
    }
}
