package com.example.innerlamp.core.storage.prefs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.innerlamp.core.util.DefaultDispatcherProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test cho [SharedPreferencesStorage] trên `SharedPreferences` thật.
 * Cần chạy trên thiết bị/emulator.
 */
@RunWith(AndroidJUnit4::class)
class SharedPreferencesStorageInstrumentedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun storage(): SharedPreferencesStorage = SharedPreferencesStorage(
        preferences = context.getSharedPreferences("test_innerlamp_prefs", Context.MODE_PRIVATE),
        dispatchers = DefaultDispatcherProvider,
    )

    @After
    fun tearDown() = runBlocking {
        storage().clear()
    }

    @Test
    fun putAndGet_roundTrip() = runBlocking {
        val storage = storage()
        storage.putString("k", "v")
        assertEquals("v", storage.getString("k"))
        storage.remove("k")
        assertNull(storage.getString("k"))
    }
}
