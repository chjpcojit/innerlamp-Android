package com.example.innerlamp.ui.screens.temple

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.innerlamp.domain.model.TempleDetail

/**
 * Opens directions to a temple: a `geo:` intent for any maps app, Google Maps in the browser when no app
 * handles it; never crashes when neither exists (Temple spec AC33, AC34). /
 * Mở chỉ đường tới chùa: intent `geo:` cho app bản đồ bất kỳ, không có app thì Google Maps trên trình
 * duyệt; không có cả hai thì bỏ qua, không crash.
 */
internal object TempleIntents {

    fun openDirections(context: Context, temple: TempleDetail) {
        val point = "${temple.lat},${temple.lng}"
        val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$point?q=$point(${Uri.encode(temple.name)})"))
        val web = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$point"))
        if (!start(context, geo)) start(context, web)
    }

    private fun start(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
