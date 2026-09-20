package com.gamebooster.app

import android.content.Context
import android.util.Log

class WireGuardManager(private val context: Context) {
    fun init() {
        Log.d("WireGuardManager", "Test modu: WireGuard devre dışı")
    }
    fun connectBestServer(onResult: (success: Boolean, serverName: String) -> Unit) {
        onResult(false, "Test modu (VPN kapalı)")
    }
    fun disconnect() {}
    fun isConnected(): Boolean = false
}
