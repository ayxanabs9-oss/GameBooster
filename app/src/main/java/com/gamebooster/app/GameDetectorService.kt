package com.gamebooster.app

import android.app.*
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class GameDetectorService : Service() {

    private val WATCHED_PACKAGES = setOf(
        "com.tencent.ig",
        "com.dts.freefiremax",
        "com.mobile.legends"
    )

    private lateinit var performanceBooster: PerformanceBooster
    private lateinit var wireGuardManager: WireGuardManager
    private var isBoostActive = false

    private val handler = Handler(Looper.getMainLooper())
    private val checkInterval = 2000L

    private val checkRunnable = object : Runnable {
        override fun run() {
            checkForegroundApp()
            handler.postDelayed(this, checkInterval)
        }
    }

    override fun onCreate() {
        super.onCreate()
        performanceBooster = PerformanceBooster(this)
        wireGuardManager = WireGuardManager(this)
        wireGuardManager.init()
        startForeground(1, buildNotification("İzleniyor..."))
        handler.post(checkRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun checkForegroundApp() {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val begin = end - 10_000
        val events = usm.queryEvents(begin, end)
        var lastPackage: String? = null
        val event = android.app.usage.UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.packageName
            }
        }

        val isGameNow = lastPackage != null && WATCHED_PACKAGES.contains(lastPackage)

        if (isGameNow && !isBoostActive) {
            activateBoost(lastPackage!!)
        } else if (!isGameNow && isBoostActive) {
            deactivateBoost()
        }
    }

    private fun activateBoost(packageName: String) {
        isBoostActive = true
        performanceBooster.requestHighPerformanceMode()
        performanceBooster.trimBackgroundLoad()
        wireGuardManager.connectBestServer { success, serverName ->
            val msg = if (success) "Boost aktif ($serverName)" else "Boost aktif (VPN bağlanamadı)"
            updateNotification(msg)
        }
    }

    private fun deactivateBoost() {
        isBoostActive = false
        wireGuardManager.disconnect()
        updateNotification("İzleniyor...")
    }

    private fun buildNotification(text: String): Notification {
        val channelId = "gamebooster_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "GameBooster", NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("GameBooster")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(1, buildNotification(text))
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkRunnable)
        wireGuardManager.disconnect()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
