package com.example.innerlamp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.di.CurrentActivityProvider
import com.example.innerlamp.domain.model.AuthError
import com.example.innerlamp.domain.model.CodeTiming
import com.example.innerlamp.ui.navigation.ForgotPasswordRoute
import com.example.innerlamp.ui.navigation.OtpRoute
import com.example.innerlamp.ui.navigation.ResetPasswordRoute
import com.example.innerlamp.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.innerlamp.ui.screens.forgotpassword.ResetPasswordScreen
import com.example.innerlamp.ui.screens.login.LoginNote
import com.example.innerlamp.ui.screens.otp.OtpScreen
import com.example.innerlamp.ui.screens.signup.SignupViewModel
import com.example.innerlamp.ui.screens.signup.signupViewModelFactory
import com.example.innerlamp.ui.components.IlBottomTab
import com.example.innerlamp.ui.navigation.AppNavHost
import com.example.innerlamp.ui.navigation.ArticleListRoute
import com.example.innerlamp.ui.navigation.ArticleRoute
import com.example.innerlamp.ui.navigation.BellRoute
import com.example.innerlamp.domain.model.LibraryHub
import com.example.innerlamp.ui.navigation.BuddhaListRoute
import com.example.innerlamp.ui.navigation.BuddhaRoute
import com.example.innerlamp.ui.navigation.CalRoute
import com.example.innerlamp.ui.navigation.ChuaRoute
import com.example.innerlamp.ui.navigation.EventsRoute
import com.example.innerlamp.ui.navigation.FestivalDetailRoute
import com.example.innerlamp.ui.navigation.FestivalsRoute
import com.example.innerlamp.ui.navigation.GioEditRoute
import com.example.innerlamp.ui.navigation.GioRoute
import com.example.innerlamp.ui.navigation.HomeRoute
import com.example.innerlamp.ui.navigation.LibraryRoute
import com.example.innerlamp.ui.navigation.LoginRoute
import com.example.innerlamp.ui.navigation.LunarDetailRoute
import com.example.innerlamp.ui.navigation.MalaRoute
import com.example.innerlamp.ui.navigation.OnboardingRoute
import com.example.innerlamp.ui.navigation.PhotosRoute
import com.example.innerlamp.ui.navigation.ProfileRoute
import com.example.innerlamp.ui.navigation.ReaderRoute
import com.example.innerlamp.ui.navigation.SettingsRoute
import com.example.innerlamp.ui.navigation.SetupRoute
import com.example.innerlamp.ui.navigation.SignupRoute
import com.example.innerlamp.ui.navigation.SplashRoute
import com.example.innerlamp.ui.navigation.SutraListRoute
import com.example.innerlamp.ui.navigation.SutraRoute
import com.example.innerlamp.ui.navigation.TalkRoute
import com.example.innerlamp.ui.navigation.TempleListRoute
import com.example.innerlamp.ui.navigation.TempleRoute
import com.example.innerlamp.ui.navigation.ilComposable
import com.example.innerlamp.ui.screens.article.ArticleCallbacks
import com.example.innerlamp.ui.screens.article.ArticleScreen
import com.example.innerlamp.ui.screens.articlelist.ArticleListCallbacks
import com.example.innerlamp.ui.screens.articlelist.ArticleListScreen
import com.example.innerlamp.ui.screens.bell.BellScreen
import com.example.innerlamp.ui.screens.buddha.BuddhaDetailCallbacks
import com.example.innerlamp.ui.screens.buddha.BuddhaDetailScreen
import com.example.innerlamp.ui.screens.buddha.BuddhaListCallbacks
import com.example.innerlamp.ui.screens.buddha.BuddhaListScreen
import com.example.innerlamp.ui.screens.calendar.CalScreen
import com.example.innerlamp.ui.screens.chua.ChuaCallbacks
import com.example.innerlamp.ui.screens.chua.ChuaScreen
import com.example.innerlamp.ui.screens.events.EventsCallbacks
import com.example.innerlamp.ui.screens.events.EventsScreen
import com.example.innerlamp.ui.screens.festivals.FestivalDetailScreen
import com.example.innerlamp.ui.screens.festivals.FestivalsCallbacks
import com.example.innerlamp.ui.screens.festivals.FestivalsScreen
import com.example.innerlamp.ui.screens.gio.GioCallbacks
import com.example.innerlamp.ui.screens.gio.GioEditScreen
import com.example.innerlamp.ui.screens.gio.GioScreen
import com.example.innerlamp.ui.screens.home.HomeCallbacks
import com.example.innerlamp.ui.screens.home.HomeScreen
import com.example.innerlamp.ui.screens.lunar.LunarDetailCallbacks
import com.example.innerlamp.ui.screens.lunar.LunarDetailScreen
import com.example.innerlamp.ui.screens.library.LibraryCallbacks
import com.example.innerlamp.ui.screens.library.LibraryScreen
import com.example.innerlamp.ui.screens.login.LoginScreen
import com.example.innerlamp.ui.screens.mala.MalaScreen
import com.example.innerlamp.ui.screens.onboarding.OnboardingScreen
import com.example.innerlamp.ui.screens.photos.PhotosCallbacks
import com.example.innerlamp.ui.screens.photos.PhotosScreen
import com.example.innerlamp.ui.screens.reader.ReaderCallbacks
import com.example.innerlamp.ui.screens.reader.ReaderScreen
import com.example.innerlamp.ui.screens.profile.ProfileCallbacks
import com.example.innerlamp.ui.screens.profile.ProfileScreen
import com.example.innerlamp.ui.screens.settings.SettingsCallbacks
import com.example.innerlamp.ui.screens.settings.SettingsScreen
import com.example.innerlamp.ui.screens.setup.SetupScreen
import com.example.innerlamp.ui.screens.signup.SignupScreen
import com.example.innerlamp.ui.screens.splash.LaunchImageOverlay
import com.example.innerlamp.ui.screens.sutra.SutraCallbacks
import com.example.innerlamp.ui.screens.sutra.SutraScreen
import com.example.innerlamp.ui.screens.sutralist.SutraListCallbacks
import com.example.innerlamp.ui.screens.sutralist.SutraListScreen
import com.example.innerlamp.ui.screens.talk.TalkCallbacks
import com.example.innerlamp.ui.screens.talk.TalkScreen
import com.example.innerlamp.ui.screens.temple.TempleCallbacks
import com.example.innerlamp.ui.screens.templelist.TempleListCallbacks
import com.example.innerlamp.ui.screens.templelist.TempleListScreen
import com.example.innerlamp.ui.screens.temple.TempleScreen
import com.example.innerlamp.ui.screens.splash.SplashScreen
import com.example.innerlamp.ui.theme.InnerLampTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InnerLampTheme {
                val navController = rememberNavController()
                // Vào Home và xoá cả luồng onboarding/đăng nhập khỏi back stack (Back thoát app).
                val enterHome: () -> Unit = {
                    navController.navigate(HomeRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
                // Mở Login làm gốc và xoá toàn bộ back stack (sau đăng xuất / hết phiên / đổi mật khẩu).
                val openLoginAsRoot: (LoginRoute) -> Unit = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
                // Refresh token bị từ chối → xoá dữ liệu tài khoản trên máy, về Login kèm thông báo.
                LaunchedEffect(Unit) {
                    AuthModule.sessionEvents.ended.collect {
                        AuthModule.provideSessionStore().clear()
                        openLoginAsRoot(LoginRoute(note = LoginNote.SessionExpired.name))
                    }
                }
                Box {
                    AppNavHost(
                        navController = navController,
                        startDestination = SplashRoute,
                    ) {
                        ilComposable(SplashRoute) {
                            SplashScreen(
                                onFinished = {
                                    // Có phiên đã lưu → vào thẳng Home; không thì onboarding.
                                    // Xoá splash khỏi back stack: ấn Back không quay lại splash.
                                    val destination: Any =
                                        if (AuthModule.provideSessionStore().hasSession()) HomeRoute else OnboardingRoute
                                    navController.navigate(destination) {
                                        popUpTo<SplashRoute> { inclusive = true }
                                    }
                                },
                            )
                        }
                        ilComposable(OnboardingRoute) {
                            OnboardingScreen(
                                onComplete = { navController.navigate(SignupRoute) },
                                onSkip = { navController.navigate(SignupRoute) },
                                onLogin = { navController.navigate(LoginRoute()) },
                            )
                        }
                        ilComposable(SignupRoute) {
                            SignupScreen(
                                onBack = { navController.popBackStack() },
                                onOtpSent = { email, timing ->
                                    navController.navigate(OtpRoute(email, timing.expiresInSeconds, timing.resendAfterSeconds))
                                },
                                // Thay signup bằng login để Back từ login không quay lại signup rồi lặp vòng.
                                onLoginClick = {
                                    navController.navigate(LoginRoute()) {
                                        popUpTo<SignupRoute> { inclusive = true }
                                    }
                                },
                            )
                        }
                        ilComposable(OtpRoute(email = "", expiresInSeconds = 0, resendAfterSeconds = 0)) { entry ->
                            val route = entry.toRoute<OtpRoute>()
                            // Cùng ViewModel với Signup để "Đổi email" / hết hạn quay về mà giữ nguyên form.
                            val signupEntry = remember(entry) { navController.getBackStackEntry(SignupRoute) }
                            val signupViewModel: SignupViewModel = viewModel(signupEntry, factory = signupViewModelFactory())
                            OtpScreen(
                                email = route.email,
                                timing = CodeTiming(route.expiresInSeconds, route.resendAfterSeconds),
                                onVerified = enterHome,
                                onBackToSignup = { error: AuthError? ->
                                    signupViewModel.onReturnedFromOtp(error)
                                    navController.popBackStack()
                                },
                            )
                        }
                        ilComposable(ForgotPasswordRoute()) { entry ->
                            ForgotPasswordScreen(
                                initialEmail = entry.toRoute<ForgotPasswordRoute>().email,
                                onCodeSent = { email, timing ->
                                    navController.navigate(
                                        ResetPasswordRoute(email, timing.expiresInSeconds, timing.resendAfterSeconds),
                                    )
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }
                        ilComposable(ResetPasswordRoute(email = "", expiresInSeconds = 0, resendAfterSeconds = 0)) { entry ->
                            val route = entry.toRoute<ResetPasswordRoute>()
                            ResetPasswordScreen(
                                email = route.email,
                                timing = CodeTiming(route.expiresInSeconds, route.resendAfterSeconds),
                                onPasswordChanged = { email ->
                                    openLoginAsRoot(LoginRoute(note = LoginNote.PasswordChanged.name, email = email))
                                },
                                onBack = { navController.popBackStack() },
                            )
                        }
                        ilComposable(LoginRoute()) { entry ->
                            val route = entry.toRoute<LoginRoute>()
                            LoginScreen(
                                note = route.note?.let { name -> LoginNote.entries.firstOrNull { it.name == name } },
                                prefillEmail = route.email,
                                onBack = { navController.popBackStack() },
                                // Thay login bằng signup (ngược với luồng ở trên) để không tạo vòng lặp.
                                onSignupClick = {
                                    navController.navigate(SignupRoute) {
                                        popUpTo<LoginRoute> { inclusive = true }
                                    }
                                },
                                onLoggedIn = enterHome,
                                onForgotPassword = { navController.navigate(ForgotPasswordRoute()) },
                                // TODO: chế độ xem trước (chưa đăng nhập) — hiện vào thẳng Home như người dùng thường.
                                onPreviewClick = enterHome,
                            )
                        }
                        ilComposable(HomeRoute) {
                            HomeScreen(
                                callbacks = HomeCallbacks(
                                    onLibrary = { navController.navigate(LibraryRoute) },
                                    onBell = { navController.navigate(BellRoute) },
                                    onSetup = { navController.navigate(SetupRoute) },
                                    onEvents = { navController.navigate(FestivalsRoute) },
                                    onEventDetail = { code, date ->
                                        navController.navigate(FestivalDetailRoute(code, date.toString()))
                                    },
                                    onCalendar = { navController.navigate(CalRoute) },
                                    onGio = { navController.navigate(GioRoute) },
                                    onLunarDetail = { date -> navController.navigate(LunarDetailRoute(date.toEpochDay())) },
                                    onMala = { navController.navigate(MalaRoute) },
                                    onPhotos = { navController.navigate(PhotosRoute) },
                                    onArticle = { id -> navController.navigate(ArticleRoute(id)) },
                                    onAllArticles = { navController.navigate(ArticleListRoute) },
                                    onTemple = { id -> navController.navigate(TempleRoute(id)) },
                                    onAllTemples = { navController.navigate(TempleListRoute) },
                                    onBuddha = { id -> navController.navigate(BuddhaRoute(id)) },
                                    onBuddhaLibrary = { navController.navigate(BuddhaListRoute) },
                                    onTab = { tab ->
                                        when (tab) {
                                            IlBottomTab.Lib -> navController.navigate(LibraryRoute) { launchSingleTop = true }
                                            IlBottomTab.Lamp -> navController.navigate(SetupRoute) { launchSingleTop = true }
                                            IlBottomTab.Chua -> navController.navigate(ChuaRoute) { launchSingleTop = true }
                                            IlBottomTab.Me -> navController.navigate(ProfileRoute) { launchSingleTop = true }
                                            else -> Unit
                                        }
                                    },
                                ),
                            )
                        }
                        ilComposable(LibraryRoute) {
                            LibraryScreen(
                                callbacks = LibraryCallbacks(
                                    onFeaturedTalk = { id -> navController.navigate(TalkRoute(id)) },
                                    onSutra = { id -> navController.navigate(SutraRoute(id)) },
                                    onBuddha = { id -> navController.navigate(BuddhaRoute(id)) },
                                    onBuddhaList = { navController.navigate(BuddhaListRoute) },
                                    onSutraList = { navController.navigate(SutraListRoute) },
                                    onHub = { hub ->
                                        // Thiền định / Pháp thoại: chưa có màn danh sách tương ứng.
                                        when (hub) {
                                            LibraryHub.Sutras -> navController.navigate(SutraListRoute)
                                            LibraryHub.Buddhas -> navController.navigate(BuddhaListRoute)
                                            else -> Unit
                                        }
                                    },
                                    onTab = { tab ->
                                        when (tab) {
                                            IlBottomTab.Home -> navController.navigate(HomeRoute) { launchSingleTop = true }
                                            IlBottomTab.Lamp -> navController.navigate(SetupRoute) { launchSingleTop = true }
                                            IlBottomTab.Chua -> navController.navigate(ChuaRoute) { launchSingleTop = true }
                                            IlBottomTab.Me -> navController.navigate(ProfileRoute) { launchSingleTop = true }
                                            else -> Unit
                                        }
                                    },
                                ),
                            )
                        }
                        ilComposable(ChuaRoute) {
                            ChuaScreen(
                                callbacks = ChuaCallbacks(
                                    onEvents = { navController.navigate(EventsRoute) },
                                    onTemple = { id -> navController.navigate(TempleRoute(id)) },
                                    onTalk = { navController.navigate(TalkRoute("tl-hieu-la-dao")) },
                                    onFestivals = { navController.navigate(FestivalsRoute) },
                                    onMeditate = { navController.navigate(SetupRoute) },
                                    // TODO(il-data-builder): trỏ tới bài kinh livestream thật (Kinh Phổ Môn) khi có dữ liệu.
                                    onSutra = { navController.navigate(SutraRoute("k-kinh-phap-cu")) },
                                    onTab = { tab ->
                                        when (tab) {
                                            IlBottomTab.Home -> navController.navigate(HomeRoute) { launchSingleTop = true }
                                            IlBottomTab.Lib -> navController.navigate(LibraryRoute) { launchSingleTop = true }
                                            IlBottomTab.Lamp -> navController.navigate(SetupRoute) { launchSingleTop = true }
                                            IlBottomTab.Me -> navController.navigate(ProfileRoute) { launchSingleTop = true }
                                            else -> Unit
                                        }
                                    },
                                ),
                            )
                        }
                        ilComposable(ProfileRoute) {
                            ProfileScreen(
                                callbacks = ProfileCallbacks(
                                    onTab = { tab ->
                                        when (tab) {
                                            IlBottomTab.Home -> navController.navigate(HomeRoute) { launchSingleTop = true }
                                            IlBottomTab.Lib -> navController.navigate(LibraryRoute) { launchSingleTop = true }
                                            IlBottomTab.Lamp -> navController.navigate(SetupRoute) { launchSingleTop = true }
                                            IlBottomTab.Chua -> navController.navigate(ChuaRoute) { launchSingleTop = true }
                                            else -> Unit
                                        }
                                    },
                                    onSettings = { navController.navigate(SettingsRoute) },
                                    onTemple = { navController.navigate(TempleRoute("tu-quang")) },
                                    onSignedOut = { openLoginAsRoot(LoginRoute()) },
                                    // TODO(navigation): nối route tương ứng cho các khối còn lại
                                    // (Sửa hồ sơ, Thống kê, Dấu ấn, Cúng dường, Sen Vàng, Chuông
                                    // tỉnh thức, Thông báo) khi các màn đó có.
                                ),
                            )
                        }
                        ilComposable(SettingsRoute) {
                            SettingsScreen(
                                callbacks = SettingsCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // TODO(navigation): nối route tương ứng cho từng hàng (Hồ sơ,
                                    // Ngôn ngữ & hiển thị, Tải về, Trợ giúp, Tải dữ liệu, Ngoại
                                    // tuyến) khi các màn đó có.
                                ),
                            )
                        }
                        ilComposable(BellRoute) {
                            BellScreen(onBack = { navController.popBackStack() })
                        }
                        ilComposable(EventsRoute) {
                            EventsScreen(
                                callbacks = EventsCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // TODO(navigation): nối sang màn Ghi danh (`RegisterRoute`) khi màn đó có.
                                    onRegister = {},
                                ),
                            )
                        }
                        ilComposable(FestivalsRoute) {
                            FestivalsScreen(
                                callbacks = FestivalsCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onEventDetail = { code, date ->
                                        navController.navigate(FestivalDetailRoute(code, date.toString()))
                                    },
                                ),
                            )
                        }
                        ilComposable(FestivalDetailRoute(eventCode = "", solarDate = "")) { entry ->
                            val route = entry.toRoute<FestivalDetailRoute>()
                            FestivalDetailScreen(
                                eventCode = route.eventCode,
                                solarDate = route.solarDate,
                                onBack = { navController.popBackStack() },
                            )
                        }
                        ilComposable(CalRoute) {
                            CalScreen(
                                onClose = { navController.popBackStack() },
                                onTemple = { navController.navigate(TempleRoute("tu-quang")) },
                            )
                        }
                        ilComposable(MalaRoute) {
                            MalaScreen(
                                onBack = { navController.popBackStack() },
                                // "Xong" quay về Trang chủ (màn vào từ Home nên pop là đủ).
                                onDone = { navController.popBackStack() },
                            )
                        }
                        ilComposable(SetupRoute) {
                            SetupScreen(
                                onClose = { navController.popBackStack() },
                                // TODO(navigation): nối sang màn Phiên thiền (`SessionRoute`) khi màn đó có.
                                onStart = { },
                                onStartSilent = { },
                            )
                        }
                        ilComposable(GioRoute) {
                            GioScreen(
                                viewModel = viewModel(),
                                callbacks = GioCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onEdit = { navController.navigate(GioEditRoute) },
                                    // TODO(navigation): nối sang Gia phả (`GiaPhaRoute`) khi màn đó có.
                                    onGiaPha = {},
                                ),
                            )
                        }
                        ilComposable(GioEditRoute) {
                            // Dùng chung ViewModel với màn Sổ giỗ (cùng back stack entry của GioRoute).
                            val gioEntry = remember(it) { navController.getBackStackEntry(GioRoute) }
                            GioEditScreen(
                                viewModel = viewModel(gioEntry),
                                onClose = { navController.popBackStack() },
                            )
                        }
                        ilComposable(PhotosRoute) {
                            PhotosScreen(
                                callbacks = PhotosCallbacks(
                                    onBack = { navController.popBackStack() },
                                ),
                            )
                        }
                        composable<ArticleRoute> {
                            ArticleScreen(
                                callbacks = ArticleCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // TODO(navigation): nối sang màn Chia sẻ (`ShareRoute`) khi màn đó có.
                                    onShare = {},
                                    onArticleClick = { id -> navController.navigate(ArticleRoute(id)) },
                                ),
                            )
                        }
                        ilComposable(SutraListRoute) {
                            SutraListScreen(
                                callbacks = SutraListCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onSutra = { id -> navController.navigate(SutraRoute(id)) },
                                ),
                            )
                        }
                        ilComposable(ArticleListRoute) {
                            ArticleListScreen(
                                callbacks = ArticleListCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onArticle = { id -> navController.navigate(ArticleRoute(id)) },
                                ),
                            )
                        }
                        ilComposable(BuddhaListRoute) {
                            BuddhaListScreen(
                                callbacks = BuddhaListCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onBuddha = { id -> navController.navigate(BuddhaRoute(id)) },
                                ),
                            )
                        }
                        composable<BuddhaRoute> {
                            BuddhaDetailScreen(
                                callbacks = BuddhaDetailCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // "Lịch lễ" → lịch lễ Phật giáo.
                                    onFestivals = { navController.navigate(FestivalsRoute) },
                                    // TODO(navigation): truyền câu niệm sang `MalaRoute` khi màn Niệm Phật
                                    // nhận được nội dung (prototype set `mlText` rồi `go('mala')`).
                                    onChant = { navController.navigate(MalaRoute) },
                                    // TODO(navigation): nối sang màn Chia sẻ (`ShareRoute`) khi màn đó có.
                                    onShare = {},
                                    onSutra = { sutra -> navController.navigate(SutraRoute(sutra.sutraId)) },
                                ),
                            )
                        }
                        ilComposable(TempleListRoute) {
                            TempleListScreen(
                                callbacks = TempleListCallbacks(
                                    onBack = { navController.popBackStack() },
                                    onTemple = { id -> navController.navigate(TempleRoute(id)) },
                                ),
                            )
                        }
                        composable<TempleRoute> {
                            TempleScreen(
                                callbacks = TempleCallbacks(
                                    onBack = { navController.popBackStack() },
                                ),
                            )
                        }
                        composable<LunarDetailRoute> { entry ->
                            LunarDetailScreen(
                                date = java.time.LocalDate.ofEpochDay(entry.toRoute<LunarDetailRoute>().epochDay),
                                callbacks = LunarDetailCallbacks(onBack = { navController.popBackStack() }),
                            )
                        }
                        composable<TalkRoute> {
                            TalkScreen(
                                callbacks = TalkCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // "Nghe xong, ngồi thiền 10 phút" → màn Chuẩn bị thiền.
                                    onMeditate = { navController.navigate(SetupRoute) },
                                ),
                            )
                        }
                        composable<SutraRoute> { entry ->
                            val sutraRoute = entry.toRoute<SutraRoute>()
                            SutraScreen(
                                callbacks = SutraCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // TODO(navigation): nối sang màn Tải về / ngoại tuyến (`OfflineRoute`)
                                    // khi màn đó có.
                                    onDownload = {},
                                    onReader = { navController.navigate(ReaderRoute(sutraRoute.sutraId)) },
                                    // TODO(navigation): nối sang màn Trình phát (`PlayerRoute`) khi màn đó có.
                                    onPlayer = {},
                                ),
                            )
                        }
                        composable<ReaderRoute> {
                            ReaderScreen(
                                callbacks = ReaderCallbacks(
                                    onBack = { navController.popBackStack() },
                                    // TODO(navigation): nối sang màn Chia sẻ (`ShareRoute`) khi màn đó có.
                                    onShare = {},
                                    // TODO(navigation): nối sang màn Tải về / ngoại tuyến (`OfflineRoute`)
                                    // khi màn đó có.
                                    onDownload = {},
                                    // TODO(navigation): nối sang màn Trình phát (`PlayerRoute`) khi màn đó có.
                                    onPlayer = {},
                                    // "Aa" mở cài đặt hiển thị — prototype đi `goSettings`.
                                    onTextSettings = { navController.navigate(SettingsRoute) },
                                ),
                            )
                        }
                    }
                    // Ảnh launch tĩnh phủ lên trên, mờ dần để lộ splash động.
                    LaunchImageOverlay()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CurrentActivityProvider.attach(this)
    }

    override fun onPause() {
        super.onPause()
        CurrentActivityProvider.detach(this)
    }
}
