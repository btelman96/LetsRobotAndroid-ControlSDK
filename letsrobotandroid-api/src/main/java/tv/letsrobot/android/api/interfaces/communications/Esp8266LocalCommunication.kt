package tv.letsrobot.android.api.interfaces.communications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import okhttp3.*
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Control an Esp8266 via web calls. url can be replaced with anything
 *
 * ex call:
 *
 * if url equals http://example.com/ , call that will be made with control
 * would be http://example.com/F , http://example.com/stop
 */
class Esp8266LocalCommunication : CommunicationInterface {
    val client = OkHttpClient()
    val TAG = "ESP"
    val url = "http://192.168.136.198/"
    private var _status = ComponentStatus.DISABLED
    override fun getAutoReboot(): Boolean {
        return false
    }

    override fun getStatus(): ComponentStatus {
        return _status
    }

    override fun initConnection(context: Context) {

    }

    override fun enable() {
        try {
            val response = client.newCall(Request.Builder().url(url).build()).execute()
            if(response.code() == 200){
                _status = ComponentStatus.STABLE
                EventManager.subscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
                return
            }
        }catch (_ : Exception){

        }
        _status = ComponentStatus.ERROR
    }

    private val onSendRobotCommand: (Any?) -> Unit = {
        Log.d(TAG, "onSendRobotCommand")
        it?.takeIf { it is ByteArray }?.let{ data ->
            send(data as ByteArray)
        }
    }

    override fun disable() {
        EventManager.unsubscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
    }

    override fun needsSetup(activity: Activity): Boolean {
        return false //TODO URL?
    }

    override fun setupComponent(activity: Activity): Int {
        return -1 //TODO URL?
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {
        //Not needed //TODO URL?
    }

    override fun isConnected(): Boolean {
        return _status == ComponentStatus.STABLE
    }

    val running = AtomicBoolean(false)

    override fun send(byteArray: ByteArray): Boolean {
        if(!running.getAndSet(true)) {
            client.newCall(Request.Builder().url(url + String(byteArray)).build()).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    _status = ComponentStatus.STABLE
                    running.set(false)
                }

                override fun onFailure(call: Call, e: IOException) {
                    _status = ComponentStatus.ERROR
                    running.set(false)
                }
            })
        }
        return true
    }
}