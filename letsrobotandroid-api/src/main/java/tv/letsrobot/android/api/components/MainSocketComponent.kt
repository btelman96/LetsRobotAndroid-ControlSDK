package tv.letsrobot.android.api.components

import android.content.Context
import android.os.Message
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import tv.letsrobot.android.api.components.tts.TTSBaseComponent
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.enums.ComponentType
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.ComponentEventObject
import tv.letsrobot.android.api.settings.LRPreferences
import tv.letsrobot.android.api.utils.JsonObjectUtils
import tv.letsrobot.android.api.utils.getJsonObject
import tv.letsrobot.android.api.utils.sendJson
import java.util.concurrent.TimeUnit

/**
 * App Socket for LetsRobot. Will send updates on camera status
 * and IP, and publishes other useful information
 */
class MainSocketComponent(context: Context) : Component(context) {
    val robotId = LRPreferences.INSTANCE.robotId.value
    private var cameraStatus: ComponentStatus? = null

    override fun getType(): ComponentType {
        return ComponentType.APP_SOCKET
    }

    override fun enableInternal() {
        setOwner()
        setupAppWebSocket()
        setupUserWebSocket()
        handler.sendEmptyMessage(DO_SOME_WORK)
    }

    override fun disableInternal() {
        appServerSocket?.disconnect()
        userAppSocket?.disconnect()
    }

    private var appServerSocket: Socket? = null
    private var userAppSocket: Socket? = null

    private fun setOwner(){
        owner = JsonObjectUtils.getJsonObjectFromUrl(
                String.format("https://letsrobot.tv/get_robot_owner/%s", robotId)
        )?.let {
            it.getString("owner")
        }
        //maybe this should rest somewhere else for lookup
        eventDispatcher?.handleMessage(getType(), ROBOT_OWNER, owner, this)
    }

    private fun setupAppWebSocket() {
        appServerSocket = IO.socket("http://letsrobot.tv:8022")
        appServerSocket?.on(Socket.EVENT_CONNECT_ERROR){
            status = ComponentStatus.ERROR
        }
        appServerSocket?.on(Socket.EVENT_CONNECT){
            status = ComponentStatus.STABLE
            appServerSocket?.emit("identify_robot_id", robotId)
        }
        appServerSocket?.connect()

    }

    private fun setupUserWebSocket(){
        userAppSocket = IO.socket("https://letsrobot.tv:8000")
        appServerSocket?.on(Socket.EVENT_DISCONNECT){
            status = ComponentStatus.DISABLED
        }
        userAppSocket!!.on("message_removed"){
            onMessageRemoved(it)
        }
        userAppSocket!!.on("user_blocked"){
            onUserRemoved(it, true)
        }
        userAppSocket!!.on("user_timeout"){
            onUserRemoved(it, false)
        }
        userAppSocket?.connect()
    }

    fun say(text : String){
        eventDispatcher?.handleMessage(ComponentType.TTS, EVENT_MAIN, TTSBaseComponent.TTSObject(text
                , TTSBaseComponent.COMMAND_PITCH, shouldFlush = true), this)
    }

    private fun onUserRemoved(params: Array<out Any>, banned : Boolean) {
        params.getJsonObject()?.runCatching {
            if(this["room"] != owner) return
            LocalBroadcastManager.getInstance(context)
                    .sendJson(ChatSocketComponent.LR_CHAT_USER_REMOVED_BROADCAST, this)
            if(LRPreferences.INSTANCE.internalSystemTTSMessagesEnabled.value &&
                    LRPreferences.INSTANCE.timeoutBanTTSNotificationsEnabled.value) {
                val textToSay = if(banned) "user banned" else "user timed out"
                say(textToSay)
            }
        }
    }

    private fun onMessageRemoved(params: Array<out Any>){
        params.getJsonObject()?.runCatching {
            val intent = JsonObjectUtils.createIntentWithJson(
                    ChatSocketComponent.LR_CHAT_MESSAGE_REMOVED_BROADCAST, this)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }

    private fun maybeSendVideoStatus() {
        cameraStatus?.let { //don't bother if we have not received camera status
            val obj = JSONObject()
            obj.put("send_video_process_exists",true)
            obj.put("ffmpeg_process_exists", it == ComponentStatus.STABLE)
            obj.put("camera_id", LRPreferences.INSTANCE.cameraId.value)
            appServerSocket?.emit("send_video_status", obj)
        }
    }

    /**
     * Update server properties every minute
     */
    private fun onUpdateServer() {
        appServerSocket?.emit("identify_robot_id", robotId)
        maybeSendVideoStatus()
        handler.sendEmptyMessageDelayed(DO_SOME_WORK, TimeUnit.MINUTES.toMillis(1))
    }

    override fun handleMessage(message: Message): Boolean {
        return when(message.what){
            DO_SOME_WORK -> {
                onUpdateServer()
                true
            }
            else ->{
                /*return*/super.handleMessage(message)
            }
        }
    }

    override fun handleExternalMessage(message: ComponentEventObject): Boolean {
        if(message.type == ComponentType.CAMERA && message.what == Component.STATUS_EVENT)
            cameraStatus = message.data as ComponentStatus
        return super.handleExternalMessage(message)
    }

    companion object {
        const val ROBOT_OWNER = 0
        var owner : String? = null
    }
}