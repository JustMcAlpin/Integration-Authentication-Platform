package com.example.integrationauthenticationplatform.oauth

import fi.iki.elonen.NanoHTTPD
import java.net.ServerSocket

class LoopbackReceiver(private val onCode: (code: String, state: String?) -> Unit) {
    private var server: NanoHTTPD? = null
    var redirectUri: String = ""

    fun start(): Int {
        val port = ServerSocket(0).use { it.localPort }
        redirectUri = "http://127.0.0.1:$port/callback"
        server = object : NanoHTTPD("127.0.0.1", port) {
            override fun serve(session: IHTTPSession): Response {
                val code = session.parms["code"]
                val state = session.parms["state"]
                val html = """
                  <html><body style="font-family:sans-serif">
                    <h3>LinkedIn sign-in complete</h3>
                    You can close this tab and return to the app.
                  </body></html>
                """.trimIndent()
                if (code != null) onCode(code, state)
                return newFixedLengthResponse(Response.Status.OK, "text/html", html)
            }
        }
        server!!.start(SOCKET_READ_TIMEOUT, false)
        return port
    }

    fun stop() { try { server?.stop() } catch (_: Exception) {} }

    companion object { private const val SOCKET_READ_TIMEOUT = 5_000 }
}
