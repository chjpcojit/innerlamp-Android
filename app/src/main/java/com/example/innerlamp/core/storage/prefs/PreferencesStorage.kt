package com.example.innerlamp.core.storage.prefs

import kotlinx.coroutines.flow.Flow

/**
 * Kho key-value không nhạy cảm (onboarding, pattern mặc định, cờ cài đặt…).
 * Bất đồng bộ (`suspend`/`Flow`); cài đặt thật là [SharedPreferencesStorage].
 *
 * `get*` đọc trực tiếp (đồng bộ); `put*` ghi trên `DispatcherProvider.io`;
 * `observe*` trả `Flow` phát giá trị hiện tại rồi cập nhật mỗi khi key đổi.
 */
interface PreferencesStorage {

    fun getString(key: String, default: String? = null): String?
    fun getBoolean(key: String, default: Boolean = false): Boolean
    fun getInt(key: String, default: Int = 0): Int
    fun getLong(key: String, default: Long = 0L): Long
    fun getFloat(key: String, default: Float = 0f): Float
    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String>

    suspend fun putString(key: String, value: String)
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun putInt(key: String, value: Int)
    suspend fun putLong(key: String, value: Long)
    suspend fun putFloat(key: String, value: Float)
    suspend fun putStringSet(key: String, value: Set<String>)

    fun observeString(key: String, default: String? = null): Flow<String?>
    fun observeBoolean(key: String, default: Boolean = false): Flow<Boolean>
    fun observeInt(key: String, default: Int = 0): Flow<Int>
    fun observeLong(key: String, default: Long = 0L): Flow<Long>
    fun observeFloat(key: String, default: Float = 0f): Flow<Float>
    fun observeStringSet(key: String, default: Set<String> = emptySet()): Flow<Set<String>>

    suspend fun remove(key: String)
    suspend fun clear()
}
