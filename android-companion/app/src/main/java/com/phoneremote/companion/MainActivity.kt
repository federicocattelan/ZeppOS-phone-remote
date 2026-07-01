package com.phoneremote.companion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Prefs.getAuthToken(this)
        startService(Intent(this, RemoteControlService::class.java))

        val tokenView = findViewById<TextView>(R.id.tokenValue)
        val portView = findViewById<TextView>(R.id.portValue)
        val adbView = findViewById<TextView>(R.id.adbCommand)
        val statusView = findViewById<TextView>(R.id.statusValue)
        val overlayStatusView = findViewById<TextView>(R.id.overlayStatusValue)
        val overlayButton = findViewById<MaterialButton>(R.id.overlayPermissionButton)

        fun refreshUi() {
            val token = Prefs.getAuthToken(this)
            val port = Prefs.getServerPort(this)
            tokenView.text = token
            portView.text = "127.0.0.1:$port"
            adbView.text = getString(R.string.adb_grant_command)
            statusView.text = getString(
                R.string.status_running,
                if (LocationController.isEnabled(this)) getString(R.string.gps_on) else getString(R.string.gps_off)
            )

            val overlayGranted = Settings.canDrawOverlays(this)
            overlayStatusView.text = getString(
                R.string.overlay_status,
                if (overlayGranted) getString(R.string.overlay_granted) else getString(R.string.overlay_missing)
            )
            overlayButton.isEnabled = !overlayGranted
        }

        refreshUi()

        findViewById<MaterialButton>(R.id.regenerateTokenButton).setOnClickListener {
            Prefs.regenerateAuthToken(this)
            refreshUi()
        }

        findViewById<MaterialButton>(R.id.restartServiceButton).setOnClickListener {
            stopService(Intent(this, RemoteControlService::class.java))
            startService(Intent(this, RemoteControlService::class.java))
            refreshUi()
        }

        overlayButton.setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<TextView>(R.id.overlayStatusValue)?.let {
            val overlayGranted = Settings.canDrawOverlays(this)
            it.text = getString(
                R.string.overlay_status,
                if (overlayGranted) getString(R.string.overlay_granted) else getString(R.string.overlay_missing)
            )
            findViewById<MaterialButton>(R.id.overlayPermissionButton)?.isEnabled = !overlayGranted
        }
    }
}
