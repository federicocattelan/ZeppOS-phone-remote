package com.phoneremote.companion

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.IOException

class RemoteControlService : Service() {
    private var server: RemoteHttpServer? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        startHttpServer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (server == null) {
            startHttpServer()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        stopHttpServer()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startHttpServer() {
        stopHttpServer()
        val port = Prefs.getServerPort(this)

        try {
            server = RemoteHttpServer(applicationContext, port).also { it.start() }
            Log.i(TAG, "HTTP server listening on 127.0.0.1:$port")
        } catch (error: IOException) {
            Log.e(TAG, "Failed to start HTTP server", error)
            stopSelf()
        }
    }

    private fun stopHttpServer() {
        server?.stop()
        server = null
    }

    private fun buildNotification(): Notification {
        val channelId = "phone_remote_control"
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId,
                "Phone Remote Control",
                NotificationManager.IMPORTANCE_LOW
            )
        )

        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.service_notification_title))
            .setContentText(getString(R.string.service_notification_text, Prefs.getServerPort(this)))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(openAppIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val TAG = "RemoteControlService"
        private const val NOTIFICATION_ID = 1001
    }
}
