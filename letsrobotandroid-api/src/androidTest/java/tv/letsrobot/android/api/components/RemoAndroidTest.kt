package tv.letsrobot.android.api.components

import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import org.junit.Test
import java.io.IOException
import java.util.concurrent.CountDownLatch

/**
 * Testing Remo
 */
class RemoAndroidTest {
    val apiKey = ""
    val channel = ""
    val chat = ""

    @Test
    @Throws(IOException::class)
    fun testSocket() {
        val request = Request.Builder().url("ws://dev.remo.tv:3231/").build()
        val token = apiKey
        val client = OkHttpClient()
        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                Log.d("TAG","onOpen")
                val json = "{\"e\": \"AUTHENTICATE_ROBOT\", \"d\": {\"token\": \"$token\"}}"
                webSocket.send(json)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d("TAG","onFailure")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                Log.d("TAG","onClosing $reason $code")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("TAG",text)
                super.onMessage(webSocket, text)
                val jsonObject = JSONObject(text)
                Log.d("TAG","Validated JSON")
                if(jsonObject["e"] == "ROBOT_VALIDATED"){
                    val host = jsonObject.getJSONObject("d")["host"] as String
                    val str = "{\"e\":\"GET_CHANNELS\",\"d\":{\"server_id\":\"$host\"}}"
                    webSocket.send(str)
                }
                if(jsonObject["e"] == "SEND_ROBOT_SERVER_INFO"){
                    sendMessage(webSocket, "JOIN_CHANNEL", channel)
                    sendMessage(webSocket, "GET_CHAT", chat)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                Log.d("TAG","onClosed $reason $code")
            }
        })
        client.dispatcher().executorService().shutdown()

        val latch = CountDownLatch(1)

        latch.await()
    }

    private fun sendMessage(webSocket: WebSocket, event: String, data: String) {
        webSocket.send("{\"e\":\"$event\",\"d\":\"$data\"}")
    }
}