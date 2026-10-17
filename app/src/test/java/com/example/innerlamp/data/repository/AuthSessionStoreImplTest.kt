package com.example.innerlamp.data.repository

import com.example.innerlamp.core.network.auth.SessionTokenCache
import com.example.innerlamp.core.storage.prefs.PreferencesStorage
import com.example.innerlamp.core.storage.secure.SecureLevel
import com.example.innerlamp.core.storage.secure.SecureStorage
import com.example.innerlamp.domain.model.AuthSession
import com.example.innerlamp.domain.model.AuthUser
import com.example.innerlamp.domain.repository.AccountDataCleaner
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionStoreImplTest {

    private class FakeSecure(var failPut: Boolean = false) : SecureStorage {
        val map = mutableMapOf<String, String>()
        override suspend fun put(level: SecureLevel, key: String, value: String): Result<Unit> =
            if (failPut) Result.failure(IllegalStateException("boom")) else Result.success(Unit.also { map[key] = value })

        override suspend fun get(level: SecureLevel, key: String): Result<String> =
            map[key]?.let { Result.success(it) } ?: Result.failure(NoSuchElementException(key))

        override suspend fun delete(key: String): Result<Unit> = Result.success(Unit.also { map.remove(key) })
        override suspend fun clear(level: SecureLevel): Result<Unit> = Result.success(Unit.also { map.clear() })
        override suspend fun isAvailable(level: SecureLevel) = true
    }

    private val session = AuthSession("acc", "ref", AuthUser(1, "a@b.vn", "A", null))
    private val prefs = mockk<PreferencesStorage>(relaxed = true)
    private val cache = SessionTokenCache()

    @Test
    fun save_storesTokensSecurely_andFillsCache() = runTest {
        val secure = FakeSecure()
        val store = AuthSessionStoreImpl(secure, prefs, cache)

        assertTrue(store.save(session).isSuccess)

        assertEquals("acc", secure.map["auth.access_token"])
        assertEquals("ref", secure.map["auth.refresh_token"])
        assertEquals("acc", cache.currentToken())
        assertTrue(store.hasSession())
        coVerify { prefs.putString("account.email", "a@b.vn") }
    }

    @Test
    fun save_failure_leavesNoHalfSession() = runTest {
        val secure = FakeSecure(failPut = true)
        val store = AuthSessionStoreImpl(secure, prefs, cache)

        assertTrue(store.save(session).isFailure)

        assertTrue(secure.map.isEmpty())
        assertFalse(store.hasSession())
    }

    @Test
    fun clear_removesTokensAndAccountKeys_runsCleaners_keepsDeviceSettings() = runTest {
        val secure = FakeSecure()
        var cleaned = false
        val store = AuthSessionStoreImpl(secure, prefs, cache, listOf(AccountDataCleaner { cleaned = true }))
        store.save(session)

        store.clear()

        assertTrue(secure.map.isEmpty())
        assertNull(cache.currentToken())
        assertTrue(cleaned)
        coVerify { prefs.remove("account.user_id") }
        coVerify(exactly = 0) { prefs.remove("onboarding_completed") }
        coVerify(exactly = 0) { prefs.clear() }
    }

    @Test
    fun load_fillsCacheFromSecureStorage() = runTest {
        val secure = FakeSecure().apply {
            map["auth.access_token"] = "acc"
            map["auth.refresh_token"] = "ref"
        }
        val store = AuthSessionStoreImpl(secure, prefs, cache)

        store.load()

        assertEquals("acc", cache.currentToken())
        assertTrue(store.hasSession())
    }
}
