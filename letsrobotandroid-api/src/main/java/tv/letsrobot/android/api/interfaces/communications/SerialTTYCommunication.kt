package tv.letsrobot.android.api.interfaces.communications

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import tv.letsrobot.android.api.EventManager
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by Brendon on 1/21/2018.
 */

class SerialTTYCommunication : CommunicationInterface{
    private val handler = Handler()
    var _status = ComponentStatus.DISABLED
    override fun getAutoReboot(): Boolean {
        return false
    }

    override fun getStatus(): ComponentStatus {
        return _status
    }

    override fun initConnection(context: Context) {
        //TODO setup prefs
    }

    override fun needsSetup(activity: Activity): Boolean {
        //TODO setup prefs
        return false
    }

    override fun setupComponent(activity: Activity): Int {
        //TODO setup prefs
        return -1
    }

    override fun receivedComponentSetupDetails(context: Context, intent: Intent?) {
        //TODO setup prefs
    }

    override fun isConnected(): Boolean {
        return true
    }

    private val onSendRobotCommand: (Any?) -> Unit = { uncheckedData ->
        uncheckedData?.takeIf { it is ByteArray }?.let{ data ->
            send(data as ByteArray)
        }
    }

    override fun send(byteArray: ByteArray): Boolean {
        transmissionData = byteArray
        //we are managing our own time here, we don't trust core to do the right thing
        timeSinceLastData = System.currentTimeMillis()
        serOut(byteArray) //TODO hookup to looper
        return true
    }

    private var stream: FileOutputStream? = null
    private var transmissionData: Any? = null
    private var timeSinceLastData : Long = 0

    private fun serOut(`object`: Any) {
        val value: ByteArray? = when (`object`) {
            is ByteArray -> `object`
            is Byte -> {
                val arr = ByteArray(1)
                arr[0] = `object`
                arr
            }
            else -> null
        }
        _status = try {
            value?.let {
                stream?.write(it)
                ComponentStatus.STABLE
            } ?: ComponentStatus.ERROR
        } catch (e: IOException) {
            e.printStackTrace()
            ComponentStatus.ERROR
        }
    }

    fun getMaxLatency(): Int {
        return 200
    }

    override fun enable() {
        _status = try {
            //fstream = new FileWriter(channel.getAddress(), false);
            stream = FileOutputStream("/dev/ttyHS4")
            ComponentStatus.STABLE
        } catch (e: IOException) {
            e.printStackTrace()
            ComponentStatus.ERROR
        }
        EventManager.subscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
    }

    override fun disable() {
        _status = try {
            stream?.close()
            ComponentStatus.DISABLED
        } catch (e: IOException) {
            e.printStackTrace()
            ComponentStatus.ERROR
        }
        EventManager.unsubscribe(EventManager.ROBOT_BYTE_ARRAY, onSendRobotCommand)
    }

    fun stop(){
        serOut(0x00)
    }

    val update = Runnable {
        if(System.currentTimeMillis() - timeSinceLastData < getMaxLatency()){
            transmissionData?.let {
                serOut(it)
            }
        }
        else{
            stop()
        }
    }
}
