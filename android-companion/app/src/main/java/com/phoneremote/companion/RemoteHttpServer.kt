package com.phoneremote.companion

import android.content.Context
import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject
import java.io.IOException

class RemoteHttpServer(
    private val appContext: Context,
    port: Int,
) : NanoHTTPD("127.0.0.1", port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        val method = session.method

        return try {
            when {
                method == Method.GET && uri == "/health" -> jsonResponse(
                    200,
                    JSONObject()
                        .put("ok", true)
                        .put("version", "1.0.0")
                        .put("gpsEnabled", LocationController.isEnabled(appContext))
                )

                method == Method.GET && uri == "/api/gps" -> jsonResponse(
                    200,
                    JSONObject().put("enabled", LocationController.isEnabled(appContext))
                )

                method == Method.POST && uri == "/api/gps" -> {
                    if (!isAuthorized(session)) return unauthorized()
                    val body = readBody(session)
                    val enabled = body.optBoolean("enabled", true)
                    val result = LocationController.setEnabled(appContext, enabled)
                    jsonResponse(
                        if (result.success) 200 else 400,
                        JSONObject()
                            .put("success", result.success)
                            .put("message", result.message)
                            .put("enabled", LocationController.isEnabled(appContext))
                    )
                }

                method == Method.POST && uri == "/api/launch" -> {
                    if (!isAuthorized(session)) return unauthorized()
                    val body = readBody(session)
                    val packageName = body.optString("package", "")
                    val result = AppLauncher.launch(appContext, packageName)
                    jsonResponse(
                        if (result.success) 200 else 400,
                        JSONObject()
                            .put("success", result.success)
                            .put("message", result.message)
                    )
                }

                else -> jsonResponse(404, JSONObject().put("error", "Not found"))
            }
        } catch (error: Exception) {
            Log.e(TAG, "Request failed: $uri", error)
            jsonResponse(500, JSONObject().put("error", error.message ?: "Server error"))
        }
    }

    private fun isAuthorized(session: IHTTPSession): Boolean {
        val expected = Prefs.getAuthToken(appContext)
        val provided = session.headers["x-auth-token"]
            ?: session.headers["X-Auth-Token"]
            ?: session.parms["token"]
        return provided == expected
    }

    private fun readBody(session: IHTTPSession): JSONObject {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val raw = files["postData"] ?: ""
        return if (raw.isBlank()) JSONObject() else JSONObject(raw)
    }

    private fun unauthorized(): Response {
        return jsonResponse(401, JSONObject().put("error", "Unauthorized"))
    }

    private fun jsonResponse(status: Response.Status, body: JSONObject): Response {
        return newFixedLengthResponse(status, "application/json", body.toString())
    }

    private fun jsonResponse(statusCode: Int, body: JSONObject): Response {
        val status = when (statusCode) {
            200 -> Response.Status.OK
            400 -> Response.Status.BAD_REQUEST
            401 -> Response.Status.UNAUTHORIZED
            404 -> Response.Status.NOT_FOUND
            else -> Response.Status.INTERNAL_ERROR
        }
        return jsonResponse(status, body)
    }

    companion object {
        private const val TAG = "RemoteHttpServer"
    }
}
