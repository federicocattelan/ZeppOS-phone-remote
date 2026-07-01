package com.phoneremote.companion

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

/**
 * Invisible trampoline activity. Its only job is to turn the screen on and
 * surface the target app above the lock screen, then hand off and disappear.
 * Android does not let a background Service/BroadcastReceiver flip the
 * screen on directly, so we need a real (if invisible) Activity window to do
 * it, the same trick alarm/incoming-call apps use.
 */
class WakeAndLaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wakeUpScreen()
        launchTarget(intent.getStringExtra(EXTRA_PACKAGE_NAME))
        finish()
    }

    private fun wakeUpScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        (getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager)
            ?.requestDismissKeyguard(this, null)

        val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
        try {
            @Suppress("DEPRECATION")
            val wakeLock = powerManager?.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "PhoneRemote:WakeLaunch"
            )
            wakeLock?.acquire(3000)
        } catch (error: Exception) {
            Log.w(TAG, "Wake lock acquire failed", error)
        }
    }

    private fun launchTarget(packageName: String?) {
        if (packageName.isNullOrBlank()) return

        try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(launchIntent)
            } else {
                Log.e(TAG, "No launch intent for $packageName")
            }
        } catch (error: Exception) {
            Log.e(TAG, "Failed to launch $packageName", error)
        }
    }

    companion object {
        private const val TAG = "WakeAndLaunchActivity"
        const val EXTRA_PACKAGE_NAME = "package_name"
    }
}
