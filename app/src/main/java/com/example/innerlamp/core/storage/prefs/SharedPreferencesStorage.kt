package com.example.innerlamp.core.storage.prefs

import android.content.SharedPreferences
import com.example.innerlamp.core.util.DispatcherProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

/**
 * Cài đặt [PreferencesStorage] trên `SharedPreferences` (file `innerlamp_prefs`).
 *
 * - `put*` dùng `commit()` trên `DispatcherProvider.io` (không dùng `apply()` rồi
 *   trả về ngay, và không gọi trực tiếp `Dispatchers.IO`).
 * - `observe*` dùng `callbackFlow` + `OnSharedPreferenceChangeListener`, giữ tham
 *   chiếu mạnh tới listener trong lúc flow còn sống (SharedPreferences chỉ giữ
 *   tham chiếu yếu).
 */
class SharedPreferencesStorage(
    private val preferences: SharedPreferences,
    private val dispatchers: DispatcherProvider,
) : PreferencesStorage {

    override fun getString(key: String, default: String?): String? =
        preferences.getString(key, default)

    override fun getBoolean(key: String, default: Boolean): Boolean =
        preferences.getBoolean(key, default)

    override fun getInt(key: String, default: Int): Int =
        preferences.getInt(key, default)

    override fun getLong(key: String, default: Long): Long =
        preferences.getLong(key, default)

    override fun getFloat(key: String, default: Float): Float =
        preferences.getFloat(key, default)

    override fun getStringSet(key: String, default: Set<String>): Set<String> =
        preferences.getStringSet(key, null)?.toSet() ?: default

    override suspend fun putString(key: String, value: String) = commit { putString(key, value) }
    override suspend fun putBoolean(key: String, value: Boolean) = commit { putBoolean(key, value) }
    override suspend fun putInt(key: String, value: Int) = commit { putInt(key, value) }
    override suspend fun putLong(key: String, value: Long) = commit { putLong(key, value) }
    override suspend fun putFloat(key: String, value: Float) = commit { putFloat(key, value) }
    override suspend fun putStringSet(key: String, value: Set<String>) =
        commit { putStringSet(key, value) }

    override fun observeString(key: String, default: String?): Flow<String?> =
        observe(key, default) { getString(key, default) }

    override fun observeBoolean(key: String, default: Boolean): Flow<Boolean> =
        observe(key, default) { getBoolean(key, default) }

    override fun observeInt(key: String, default: Int): Flow<Int> =
        observe(key, default) { getInt(key, default) }

    override fun observeLong(key: String, default: Long): Flow<Long> =
        observe(key, default) { getLong(key, default) }

    override fun observeFloat(key: String, default: Float): Flow<Float> =
        observe(key, default) { getFloat(key, default) }

    override fun observeStringSet(key: String, default: Set<String>): Flow<Set<String>> =
        observe(key, default) { getStringSet(key, default) }

    override suspend fun remove(key: String) = commit { remove(key) }

    override suspend fun clear() = commit { clear() }

    private suspend fun commit(block: SharedPreferences.Editor.() -> SharedPreferences.Editor) {
        withContext(dispatchers.io) {
            preferences.edit().block().commit()
        }
    }

    private fun <T> observe(key: String, default: T, reader: () -> T): Flow<T> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) trySend(reader())
        }
        // `listener` bị `awaitClose` giữ tham chiếu mạnh trong lúc flow còn sống,
        // tránh bị GC do SharedPreferences chỉ giữ tham chiếu yếu.
        preferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(reader())
        awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
}
