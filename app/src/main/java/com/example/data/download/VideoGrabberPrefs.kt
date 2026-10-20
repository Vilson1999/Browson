package com.example.data.download

import android.content.Context
import android.content.SharedPreferences

class VideoGrabberPrefs(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("operawave_video_grabber", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean("video_grabber_enabled", true)
    fun setEnabled(enabled: Boolean) = prefs.edit().putBoolean("video_grabber_enabled", enabled).apply()
}
