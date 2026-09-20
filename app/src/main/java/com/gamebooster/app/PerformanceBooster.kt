package com.gamebooster.app

import android.app.ActivityManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log

class PerformanceBooster(private val context: Context) {

    fun requestHighPerformanceMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            try {
                pm.isPowerSaveMode
            } catch (e: Exception) {
                Log.w("PerformanceBooster", "Performans modu okunamadı", e)
            }
        }
    }

    fun trimBackgroundLoad() {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        try {
            am.killBackgroundProcesses(context.packageName)
        } catch (e: SecurityException) {
            Log.w("PerformanceBooster", "Arka plan temizliği kısıtlı", e)
        }
    }

    fun openPrivateDnsSettings() {
        val intent = android.content.Intent(Settings.ACTION_WIRELESS_SETTINGS)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
