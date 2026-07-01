package com.phoneremote.companion

import android.content.Context
import java.util.UUID

object Prefs {
    private const val PREFS_NAME = "phone_remote_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_SERVER_PORT = "server_port"

    const val DEFAULT_PORT = 8765

    fun getAuthToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_AUTH_TOKEN, null)
        if (existing != null) return existing

        val generated = UUID.randomUUID().toString().replace("-", "")
        prefs.edit().putString(KEY_AUTH_TOKEN, generated).apply()
        return generated
    }

    fun regenerateAuthToken(context: Context): String {
        val generated = UUID.randomUUID().toString().replace("-", "")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_AUTH_TOKEN, generated)
            .apply()
        return generated
    }

    fun getServerPort(context: Context): Int {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_SERVER_PORT, DEFAULT_PORT)
    }
}
