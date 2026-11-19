package com.example.innerlamp.core.database.memorial

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * P7: two accounts can hold an entry with the same server id only while the copy is account-scoped; the
 * worker's lookup must never cross accounts. Instrumented because Room needs an Android runtime (QA/owner
 * runs it; agents must not start an emulator). /
 * P7: hai tài khoản có thể có mục cùng id; tra cứu của worker không được vượt tài khoản. Test instrumented
 * vì Room cần runtime Android (QA/Đức chạy; agent không bật emulator).
 */
@RunWith(AndroidJUnit4::class)
class MemorialCacheDaoTest {

    private lateinit var database: MemorialCacheDatabase
    private lateinit var dao: MemorialCacheDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            MemorialCacheDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.memorialCacheDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun entity(ownerId: Long, id: Long, name: String) = MemorialCacheEntity(
        ownerId = ownerId,
        id = id,
        name = name,
        relationCode = "FATHER",
        calendarCode = "lunar",
        day = 15,
        month = 10,
        deathYear = 2020,
        leapMonth = false,
        note = "",
        remind = false,
        leadDays = 1,
        remindTime = "07:00",
    )

    @Test
    fun the_same_server_id_in_two_accounts_is_not_confused() = runBlocking {
        dao.upsert(entity(ownerId = 1L, id = 7L, name = "Tài khoản A"))
        dao.upsert(entity(ownerId = 2L, id = 7L, name = "Tài khoản B"))

        assertEquals("Tài khoản A", dao.getById(1L, 7L)?.name)
        assertEquals("Tài khoản B", dao.getById(2L, 7L)?.name)
        assertNull(dao.getById(3L, 7L))
    }
}
