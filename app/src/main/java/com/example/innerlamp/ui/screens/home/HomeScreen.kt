package com.example.innerlamp.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.innerlamp.di.DatabaseModule
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.innerlamp.domain.calendar.LunarCalendar
import com.example.innerlamp.InnerLampApplication
import com.example.innerlamp.di.CalendarModule
import com.example.innerlamp.ui.components.IlBlurHeaderScaffold
import com.example.innerlamp.ui.components.IlBottomNav
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.screens.festivals.CalendarPreviewData
import com.example.innerlamp.ui.theme.BeVietnamPro
import com.example.innerlamp.ui.theme.IlGoldBright
import com.example.innerlamp.ui.theme.InnerLampTheme
import com.example.innerlamp.ui.theme.Lora
import com.example.innerlamp.ui.theme.LocalIlColors
import java.time.LocalDate

/**
 * Các điểm điều hướng đi ra khỏi trang chủ. Trang chủ không giữ `NavController`:
 * mọi điều hướng đi qua callback (giống các màn hình khác), route chưa có thì để mặc định no-op.
 */
@Immutable
class HomeCallbacks(
    val onNotifications: () -> Unit = {},
    val onProfile: () -> Unit = {},
    val onCalendar: () -> Unit = {},
    val onGio: () -> Unit = {},
    val onLunarDetail: (LocalDate) -> Unit = {},
    /** "Tất cả" / "+N sự kiện khác" → the festival calendar. */
    val onEvents: () -> Unit = {},
    /** A row of the event block → the festival detail of `(eventCode, solarDate)`. */
    val onEventDetail: (eventCode: String, solarDate: LocalDate) -> Unit = { _, _ -> },
    val onPhotos: () -> Unit = {},
    val onTemple: (templeId: String) -> Unit = {},
    val onAllTemples: () -> Unit = {},
    val onBuddha: (buddhaId: String) -> Unit = {},
    val onBuddhaLibrary: () -> Unit = {},
    val onJournal: () -> Unit = {},
    val onStats: () -> Unit = {},
    val onMala: () -> Unit = {},
    val onBell: () -> Unit = {},
    val onSetup: () -> Unit = {},
    val onReader: () -> Unit = {},
    val onOffer: () -> Unit = {},
    val onArticle: (articleId: String) -> Unit = {},
    val onAllArticles: () -> Unit = {},
    val onLibrary: () -> Unit = {},
    val onTab: (IlBottomTab) -> Unit = {},
)

/**
 * Màn hình Trang chủ — dựng lại từ prototype `Docs/innerlamp-project/` (màn `home`).
 *
 * Bố cục (trên → dưới): header dán (lời chào, thông báo, avatar) → dải tuần cuộn ngang
 * (kèm chú thích hoàng đạo/hắc đạo/Rằm-Mùng Một) → thẻ âm lịch + câu kinh → Sự kiện Phật
 * giáo (carousel theo tháng) → Ảnh chùa đẹp → Chùa Việt Nam (carousel) → Thư viện Phật &
 * Bồ Tát (carousel) → thời khóa hôm nay + chuỗi ngày → tràng hạt (thẻ lối tắt vào Niệm Phật) → chuông mõ →
 * thời khóa gợi ý → bài viết Phật pháp → bottom nav. Ba khối chùa/Phật/bài viết hiện skeleton
 * khi đang tải, rồi nội dung; tải xong mà không có dữ liệu thì hiện thẻ rỗng (nét đứt + "Thử lại").
 *
 * @author innerlamp
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    callbacks: HomeCallbacks = HomeCallbacks(),
    viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    getHomeEventBlock = CalendarModule.provideGetHomeEventBlockUseCase(
                        (this[APPLICATION_KEY] as InnerLampApplication).calendarEventRepository,
                    ),
                    quotes = DatabaseModule.provideSutraQuoteUseCase(this[APPLICATION_KEY]!!),
                    contentLoader = HomeContentLoader()::load,
                )
            }
        },
    ),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // A new day / month rebuilds the event block when the app returns to the foreground (AC20).
    // Sang ngày / tháng mới thì dựng lại khối sự kiện khi app quay lại foreground.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    HomeContent(
        state = state,
        callbacks = callbacks,
        onDateSelected = viewModel::onDateSelected,
        onReload = viewModel::onReload,
        onReloadEvents = viewModel::onReloadEvents,
        modifier = modifier,
    )
}

/** Phần stateless của [HomeScreen] — dùng cho preview và test. */
@Composable
internal fun HomeContent(
    state: HomeUiState,
    callbacks: HomeCallbacks,
    onDateSelected: (LocalDate) -> Unit,
    onReload: () -> Unit,
    onReloadEvents: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.canvas),
    ) {
        val scroll = rememberScrollState()
        IlBlurHeaderScaffold(
            scrollOffset = { scroll.value },
            header = { HomeHeader(state = state, callbacks = callbacks) },
            modifier = Modifier.weight(1f),
        ) { headerHeight ->
        Column(
            modifier = Modifier.verticalScroll(scroll),
        ) {
            Spacer(Modifier.height(headerHeight + 20.dp))
            WeekStrip(
                pages = state.weekPages,
                today = state.today,
                selected = state.selectedDate,
                onSelect = onDateSelected,
                onCalendarClick = callbacks.onCalendar,
                onGioClick = callbacks.onGio,
            )

            Spacer(Modifier.height(18.dp))
            LunarHeroCard(
                date = state.selectedDate,
                lunar = state.selectedLunar,
                verse = state.verse,
                onClick = { callbacks.onLunarDetail(state.selectedDate) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(24.dp))
            EventsSection(
                cards = state.eventCards,
                isLoading = state.eventsLoading,
                mayBeOutOfDate = state.eventsMayBeOutOfDate,
                onAllClick = callbacks.onEvents,
                onEventClick = callbacks.onEventDetail,
                onRetry = onReloadEvents,
            )

            Spacer(Modifier.height(24.dp))
            PhotosSection(
                photos = state.photos,
                onClick = callbacks.onPhotos,
            )

            Spacer(Modifier.height(24.dp))
            TemplesSection(
                temples = state.temples,
                isLoading = state.isContentLoading,
                onTempleClick = callbacks.onTemple,
                onAllClick = callbacks.onAllTemples,
            )

            Spacer(Modifier.height(24.dp))
            BuddhasSection(
                buddhas = state.buddhas,
                isLoading = state.isContentLoading,
                onBuddhaClick = callbacks.onBuddha,
                onAllClick = callbacks.onBuddhaLibrary,
                onReload = onReload,
            )

            Spacer(Modifier.height(20.dp))
            PracticeStatsRow(
                state = state,
                onJournalClick = callbacks.onJournal,
                onStatsClick = callbacks.onStats,
            )

            Spacer(Modifier.height(22.dp))
            MalaSection(
                beads = state.beads,
                progress = state.beadProgress,
                onKeepClick = callbacks.onMala,
                onFullScreenClick = callbacks.onMala,
            )

            Spacer(Modifier.height(12.dp))
            BellCard(
                onClick = callbacks.onBell,
                modifier = Modifier.padding(horizontal = 20.dp),
            )

            Spacer(Modifier.height(24.dp))
            SuggestedRoutineSection(
                onSetupClick = callbacks.onSetup,
                onReaderClick = callbacks.onReader,
                onOfferClick = callbacks.onOffer,
            )

            Spacer(Modifier.height(24.dp))
            ArticlesSection(
                articles = state.articles,
                isLoading = state.isContentLoading,
                onArticleClick = callbacks.onArticle,
                onAllClick = callbacks.onAllArticles,
                onReload = onReload,
            )
            Spacer(Modifier.height(20.dp))
        }
        }

        IlBottomNav(active = IlBottomTab.Home, onNav = callbacks.onTab)
    }
}

/** Header dán: lời chào + dòng ngày dương/âm, chuông thông báo và avatar. Hairline `ink-06` ở đáy. */
@Composable
private fun HomeHeader(
    state: HomeUiState,
    callbacks: HomeCallbacks,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = state.greetingLine,
                    style = TextStyle(
                        fontFamily = Lora,
                        fontWeight = FontWeight.Normal,
                        fontSize = 26.sp,
                        lineHeight = 31.sp,
                        color = colors.ink,
                    ),
                )
                Text(
                    text = todayLine(state.today),
                    modifier = Modifier.padding(top = 5.dp),
                    style = TextStyle(
                        fontFamily = BeVietnamPro,
                        fontSize = 12.sp,
                        color = colors.ink.copy(alpha = 0.50f),
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationButton(
                    hasUnread = state.hasUnreadNotifications,
                    onClick = callbacks.onNotifications,
                )
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colors.sunk2, CircleShape)
                        .border(1.dp, colors.ink.copy(alpha = 0.08f), CircleShape)
                        .tappable(onClickLabel = "Hồ sơ", onClick = callbacks.onProfile),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.avatarInitial,
                        style = TextStyle(
                            fontFamily = BeVietnamPro,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.link,
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationButton(
    hasUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalIlColors.current
    Box(
        modifier = modifier
            .size(38.dp)
            .background(colors.card, CircleShape)
            .border(1.dp, colors.ink.copy(alpha = 0.10f), CircleShape)
            .tappable(onClickLabel = "Thông báo", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Hình chuông bằng khối bo góc, giống prototype (chưa có icon set).
        Box(
            Modifier
                .size(width = 13.dp, height = 15.dp)
                .border(
                    1.5.dp,
                    colors.ink.copy(alpha = 0.55f),
                    RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp),
                ),
        )
        if (hasUnread) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 9.dp)
                    .size(10.dp)
                    .background(IlGoldBright, CircleShape)
                    .border(1.5.dp, colors.card, CircleShape),
            )
        }
    }
}

/** "Thứ Sáu, 25 · 9 — 14 tháng Tám âm". */
internal fun todayLine(today: LocalDate): String {
    val lunar = LunarCalendar.toLunar(today)
    return "${today.weekdayFull()}, ${today.dayOfMonth} · ${today.monthValue} — " +
        "${lunar.day} tháng ${lunar.monthName} âm"
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun HomePreview() {
    InnerLampTheme(themeMode = com.example.innerlamp.ui.theme.ThemeMode.Light) {
        HomeContent(
            state = previewState(),
            callbacks = HomeCallbacks(),
            onDateSelected = {},
            onReload = {},
            onReloadEvents = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 402, heightDp = 1400)
@Composable
private fun HomePreviewDark() {
    InnerLampTheme(themeMode = com.example.innerlamp.ui.theme.ThemeMode.Dark) {
        HomeContent(
            state = previewState(),
            callbacks = HomeCallbacks(),
            onDateSelected = {},
            onReload = {},
            onReloadEvents = {},
        )
    }
}

private fun previewState(): HomeUiState {
    val today = LocalDate.of(2026, 9, 25)
    return HomeUiState.create(
        today = today,
        hour = 9,
        eventCards = CalendarPreviewData.homeCards(),
        displayName = "Diệu Tâm",
    )
}
