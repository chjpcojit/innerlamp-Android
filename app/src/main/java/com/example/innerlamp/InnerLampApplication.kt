package com.example.innerlamp

import android.app.Application
import android.util.Log
import com.example.innerlamp.core.database.InnerLampDatabase
import com.example.innerlamp.di.AuthModule
import com.example.innerlamp.di.DatabaseModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class InnerLampApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        AuthModule.init(this)
        Log.i(TAG, "Local DB: Room ${InnerLampDatabase.NAME} v${InnerLampDatabase.VERSION}")
        // Seed quote Pháp Cú ở background; lỗi được log, lần đọc sau sẽ seed lại nếu DB rỗng.
        // Nạp token đã lưu vào cache để request đầu tiên đã có Bearer. / Load stored tokens into the cache.
        appScope.launch { AuthModule.provideSessionStore().load() }
        appScope.launch {
            try {
                DatabaseModule.provideSutraQuoteRepository(this@InnerLampApplication).seedIfNeeded()
            } catch (e: Exception) {
                Log.e(TAG, "Seed sutra quotes failed", e)
            }
        }
    }

    private companion object {
        const val TAG = "InnerLamp"
    }
}
