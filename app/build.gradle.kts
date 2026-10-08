import com.android.build.api.variant.BuildConfigField

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.innerlamp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.innerlamp"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    // Môi trường build (dev/staging/prod). Đổi môi trường = chọn flavor khi build,
    // không sửa source Kotlin. Base URL tương ứng được nhúng vào `BuildConfig.BASE_URL`
    // ở khối `androidComponents` bên dưới.
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Nhúng base URL cho từng variant qua Variant API của AGP 9 (không hard-code trong
// Kotlin). Giá trị hiện là URL fake (backend chưa tồn tại) — gọi ra sẽ fail ở tầng
// kết nối (`AppError.NoConnection`), không phải `NotConfigured`. Flavor lạ → chuỗi
// rỗng để rơi vào guard `AppError.NotConfigured`.
androidComponents {
    onVariants { variant ->
        val url = when (variant.flavorName) {
            "dev" -> "https://dev-api.innerlamp.vn/"
            "staging" -> "https://staging-api.innerlamp.vn/"
            "prod" -> "https://api.innerlamp.vn/"
            else -> ""
        }
        variant.buildConfigFields?.put(
            "BASE_URL",
            BuildConfigField("String", "\"$url\"", "Backend REST base URL"),
        )
    }
}

ksp {
    // Export schema để review migration diff qua git.
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Architecture stack
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.biometric)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
