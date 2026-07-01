package com.phoneremote.companion

import android.content.Context
import android.provider.Settings
import android.util.Log

object LocationController {
    private const val TAG = "LocationController"

    private const val LOCATION_MODE_OFF = 0
    private const val LOCATION_MODE_HIGH_ACCURACY = 3

    fun isEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            ) != LOCATION_MODE_OFF
        } catch (error: Exception) {
            Log.e(TAG, "Unable to read location mode", error)
            false
        }
    }

    fun setEnabled(context: Context, enabled: Boolean): CommandResult {
        val targetMode = if (enabled) LOCATION_MODE_HIGH_ACCURACY else LOCATION_MODE_OFF

        return try {
            Settings.Secure.putInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                targetMode
            )
            CommandResult(true, if (enabled) "GPS enabled" else "GPS disabled")
        } catch (error: SecurityException) {
            CommandResult(
                success = false,
                message = "Missing WRITE_SECURE_SETTINGS. Run the ADB grant command shown in the app."
            )
        } catch (error: Exception) {
            Log.e(TAG, "Failed to change location mode", error)
            CommandResult(false, error.message ?: "GPS toggle failed")
        }
    }
}
