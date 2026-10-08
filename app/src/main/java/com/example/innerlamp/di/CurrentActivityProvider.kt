package com.example.innerlamp.di

import androidx.fragment.app.FragmentActivity
import com.example.innerlamp.core.storage.secure.auth.ActivityProvider
import java.lang.ref.WeakReference

/**
 * Theo dõi Activity đang hoạt động để `BiometricPrompt` hiển thị đúng host.
 * `MainActivity` đăng ký trong `onResume` và huỷ trong `onPause`.
 */
object CurrentActivityProvider : ActivityProvider {

    private var reference: WeakReference<FragmentActivity>? = null

    override fun current(): FragmentActivity? = reference?.get()

    fun attach(activity: FragmentActivity) {
        reference = WeakReference(activity)
    }

    fun detach(activity: FragmentActivity) {
        if (reference?.get() === activity) reference = null
    }
}
