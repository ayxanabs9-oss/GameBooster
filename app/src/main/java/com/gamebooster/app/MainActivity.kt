package com.gamebooster.app

import android.app.AppOpsManager
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val startButton = findViewById<Button>(R.id.startButton)
        val permissionButton = findViewById<Button>(R.id.permissionButton)

        permissionButton.setOnClickListener {
            requestUsageStatsPermission()
        }

        startButton.setOnClickListener {
            if (!hasUsageStatsPermission()) {
                statusText.text = "Önce 'Kullanım Erişimi' iznini ver"
                requestUsageStatsPermission()
                return@setOnClickListener
            }
            requestVpnPermissionAndStart()
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    private fun requestVpnPermissionAndStart() {
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, 100)
        } else {
            startBoostService()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            startBoostService()
        }
    }

    private fun startBoostService() {
        val serviceIntent = Intent(this, GameDetectorService::class.java)
        ActivityCompat.startForegroundService(this, serviceIntent)
        statusText.text = "GameBooster çalışıyor, oyun açmanı bekliyor..."
    }
}
