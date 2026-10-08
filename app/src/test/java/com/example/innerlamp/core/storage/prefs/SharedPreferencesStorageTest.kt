package com.example.innerlamp.core.storage.prefs

import android.content.SharedPreferences
import app.cash.turbine.test
import com.example.innerlamp.core.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test JVM cho [SharedPreferencesStorage] với fake [SharedPreferences]
 * (interface tách khỏi cài đặt Android thật) và `StandardTestDispatcher`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedPreferencesStorageTest {

    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher,
    ) : DispatcherProvider {
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
        override val main: CoroutineDispatcher get() = dispatcher
    }

    /** Fake [SharedPreferences] nằm trong bộ nhớ, đếm số lần gọi `commit`/`apply`. */
    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any>()
        private val listeners =
            mutableListOf<SharedPreferences.OnSharedPreferenceChangeListener>()

        var commitCount = 0
        var applyCount = 0

        override fun getAll(): MutableMap<String, *> = values.toMutableMap()

        override fun getString(key: String, defValue: String?): String? =
            values[key] as? String ?: defValue

        override fun getStringSet(
            key: String,
            defValues: MutableSet<String>?,
        ): MutableSet<String>? {
            val value = values[key] as? Set<*> ?: return defValues
            return value.map { it as String }.toMutableSet()
        }

        override fun getInt(key: String, defValue: Int): Int = values[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = values[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float =
            values[key] as? Float ?: defValue

        override fun getBoolean(key: String, defValue: Boolean): Boolean =
            values[key] as? Boolean ?: defValue

        override fun contains(key: String): Boolean = values.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) {
            listeners.add(listener)
        }

        override fun unregisterOnSharedPreferenceChangeListener(
            listener: SharedPreferences.OnSharedPreferenceChangeListener,
        ) {
            listeners.remove(listener)
        }

        private fun notifyChanged(changedKeys: Set<String>) {
            changedKeys.forEach { key ->
                listeners.forEach { it.onSharedPreferenceChanged(this, key) }
            }
        }

        private val removeMarker = Any()

        inner class Editor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clearAll = false

            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                pending[key] = value; return this
            }

            override fun putStringSet(
                key: String,
                values: MutableSet<String>?,
            ): SharedPreferences.Editor {
                pending[key] = values; return this
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor {
                pending[key] = value; return this
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                pending[key] = value; return this
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                pending[key] = value; return this
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
                pending[key] = value; return this
            }

            override fun remove(key: String): SharedPreferences.Editor {
                pending[key] = removeMarker; return this
            }

            override fun clear(): SharedPreferences.Editor {
                clearAll = true; return this
            }

            override fun commit(): Boolean {
                commitCount++
                applyChanges()
                return true
            }

            override fun apply() {
                applyCount++
                applyChanges()
            }

            private fun applyChanges() {
                val changed = mutableSetOf<String>()
                if (clearAll) {
                    changed.addAll(values.keys)
                    values.clear()
                    clearAll = false
                }
                pending.forEach { (key, value) ->
                    if (value == null || value === removeMarker) {
                        if (values.remove(key) != null) changed.add(key)
                    } else {
                        val previous = values.put(key, value)
                        if (previous != value) changed.add(key)
                    }
                }
                pending.clear()
                notifyChanged(changed)
            }
        }
    }

    private fun storage(dispatcher: CoroutineDispatcher): SharedPreferencesStorage =
        SharedPreferencesStorage(
            preferences = FakeSharedPreferences(),
            dispatchers = TestDispatcherProvider(dispatcher),
        )

    @Test
    fun putThenGet_roundTrip() = runTest {
        val storage = storage(StandardTestDispatcher(testScheduler))

        storage.putString("k", "v")
        assertEquals("v", storage.getString("k"))

        storage.putBoolean("b", true)
        assertTrue(storage.getBoolean("b"))

        storage.putInt("i", 42)
        assertEquals(42, storage.getInt("i"))
    }

    @Test
    fun put_usesCommit_notApply() = runTest {
        val prefs = FakeSharedPreferences()
        val storage = SharedPreferencesStorage(prefs, TestDispatcherProvider(StandardTestDispatcher(testScheduler)))

        storage.putString("k", "v")

        // Yêu cầu: ghi dùng `commit()` trên IO, không dùng `apply()`.
        assertEquals(1, prefs.commitCount)
        assertEquals(0, prefs.applyCount)
    }

    @Test
    fun remove_deletesValue() = runTest {
        val storage = storage(StandardTestDispatcher(testScheduler))

        storage.putString("k", "v")
        storage.remove("k")

        assertNull(storage.getString("k"))
    }

    @Test
    fun observe_emitsCurrentThenUpdates() = runTest {
        val storage = storage(StandardTestDispatcher(testScheduler))

        storage.observeString("k").test {
            assertEquals(null, awaitItem())
            storage.putString("k", "v1")
            assertEquals("v1", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observe_keyRemoved_emitsDefault() = runTest {
        val storage = storage(StandardTestDispatcher(testScheduler))

        storage.observeString("k", default = "default").test {
            assertEquals("default", awaitItem())
            storage.putString("k", "v1")
            assertEquals("v1", awaitItem())
            storage.remove("k")
            assertEquals("default", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeBoolean_emitsCurrentThenUpdates() = runTest {
        val storage = storage(StandardTestDispatcher(testScheduler))

        storage.observeBoolean("flag", default = false).test {
            assertFalse(awaitItem())
            storage.putBoolean("flag", true)
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
