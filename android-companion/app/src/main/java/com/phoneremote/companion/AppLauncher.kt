package com.phoneremote.companion

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.util.Log

object AppLauncher {
    private const val TAG = "AppLauncher"

    fun launch(context: Context, packageName: String): CommandResult {
        val trimmed = packageName.trim()
        if (trimmed.isEmpty()) {
            return CommandResult(false, "Package name is empty")
        }

        return try {
            context.packageManager.getLaunchIntentForPackage(trimmed)
                ?: return CommandResult(false, "No launch intent for $trimmed")

            // Route through a trampoline Activity so the target app can be
            // brought up even if the phone screen is currently off/locked.
            val wakeIntent = Intent(context, WakeAndLaunchActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NO_HISTORY
                )
                putExtra(WakeAndLaunchActivity.EXTRA_PACKAGE_NAME, trimmed)
            }
            context.startActivity(wakeIntent)
            CommandResult(true, "Launched $trimmed")
        } catch (error: Exception) {
            Log.e(TAG, "Failed to launch $trimmed", error)
            CommandResult(false, error.message ?: "Launch failed")
        }
    }

    fun listLaunchableApps(context: Context): List<LaunchableApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return context.packageManager
            .queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                val label = resolveInfo.loadLabel(context.packageManager).toString()
                LaunchableApp(packageName, label)
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}

data class LaunchableApp(
    val packageName: String,
    val label: String,
)

data class CommandResult(
    val success: Boolean,
    val message: String,
)
