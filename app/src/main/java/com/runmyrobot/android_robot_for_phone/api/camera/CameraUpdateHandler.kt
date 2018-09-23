package com.runmyrobot.android_robot_for_phone.api.camera

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil
import org.bytedeco.javacpp.avcodec.AV_CODEC_ID_MPEG1VIDEO
import org.bytedeco.javacpp.avutil
import org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame.DEPTH_UBYTE
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Threaded calls to update the camera feed
 */
class CameraUpdateHandler(val context: Context, looper: Looper) : Handler(looper){
    private var process : Process? = null

    private var streaming: Boolean = false

    private var addr: String? = null

    private var processExists = AtomicBoolean(false)

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            NEW_PROCESS -> {
                Log.d(TAG, "NEW_PROCESS")
                processExists.set(true)
            }
            PROCESS_DIED -> {
                Log.d(TAG, "PROCESS_DIED")
                process = null
                processExists.set(false)
                if(streaming){
                    addr?.let { bootFFMPEG(it) }
                }
            }
            PUBLISH_TO_PROCESS -> {
                //Only proceed if our trustworthy booleans are true
                if(streaming && processExists.get()){
                    //And then lets not trust them
                    try{
                        (msg.obj as? ByteArray)?.let {
                            Log.d(TAG, "PUBLISH_TO_PROCESS")
                            renderYuvToService(msg.arg1, msg.arg2, it)
                        }
                    }catch (e : Exception){e.printStackTrace()}
                }
            }
            START_STREAM -> {
                Log.d(TAG, "START_STREAM")
                Thread.currentThread().priority = Thread.MAX_PRIORITY
                addr = msg.obj as? String
                addr?.let {
                    Log.d(TAG, "ADDR_OKAY")
                    streaming = true
                    bootFFMPEG(it)
                } ?: throw Exception("Address is missing!")
            }
            STARVE_STREAM -> {
                Log.d(TAG, "STARVE_STREAM")
                Thread.currentThread().priority = Thread.MIN_PRIORITY
                streaming = false
            }
        }
    }

    private var frameRecorder: FFmpegFrameRecorder? = null

    private fun bootFFMPEG(addr : String) {
        Log.d(TAG, "bootFFMPEG")
        if(!streaming){
            return
        }
        Log.d(TAG, "bootFFMPEG_OKAY")
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val xres = "640"
            val yres = "480"

            val rotationOption = StoreUtil.getOrientation(context).ordinal //leave blank
            val builder = StringBuilder()
            for (i in 0..rotationOption){
                if(i == 0) builder.append("transpose=1")
                else builder.append(",transpose=1")
            }
            print("\"$builder\"")
            //TODO hook up with bitrate and resolution prefs
            val kbps = 2000000
            val streamKey = RobotApplication.Instance.getCameraPass()
            try {
                frameRecorder?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            frameRecorder = null
            print("http://$addr/$streamKey/$xres/$yres/")
            frameRecorder = FFmpegFrameRecorder("http://$addr/$streamKey/$xres/$yres/", xres.toInt(), yres.toInt())
            frameRecorder?.let {
                //it.aspectRatio = 16.0/9.0
                it.format = "mpegts"
                it.videoCodec = AV_CODEC_ID_MPEG1VIDEO
                it.frameRate = 30.0
                it.videoOptions["tune"] = "zerolatency"
                it.videoOptions["preset"] = "ultrafast"
                it.videoOptions["vf"] = builder.toString()
                it.videoBitrate = kbps
                it.pixelFormat = AV_PIX_FMT_YUV420P
            }
            frameRecorder?.start()
            updateProcess() //allow frames to be processed
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    private var videoTS = 0L

    private var startTime = 0L

    private fun renderYuvToService(width : Int, height : Int, b : ByteArray){
        Log.d(TAG, "renderYuvToService")
        try {
            // Let's define our start time...
            // This needs to be initialized as close to when we'll use it as
            // possible,
            // as the delta from assignment to computed time could be too high
            if (startTime == 0L)
                startTime = System.currentTimeMillis()

            // Create timestamp for this frame
            videoTS = 1000 * (System.currentTimeMillis() - startTime)
            // Check for AV drift
            if (videoTS > frameRecorder!!.timestamp)
            {
                System.out.println(
                        "Lip-flap correction: "
                                + videoTS + " : "
                                + frameRecorder!!.timestamp + " -> "
                                + (videoTS - frameRecorder!!.timestamp))

                // We tell the recorder to write this frame at this timestamp
                frameRecorder!!.timestamp = videoTS
            }
            frameRecorder?.recordImage(width, height,DEPTH_UBYTE, 2,0,avutil.AV_PIX_FMT_NV21, ByteBuffer.wrap(b))
        } catch (e: Exception) {
            notifyDeadProcess()
            e.printStackTrace()
        }
    }

    fun enable(serverAddr: String){
        Message.obtain(this, START_STREAM, serverAddr).sendToTarget()
    }

    fun disable(){
        //We want to interrupt everything to disable this in case handler is backed up
        sendMessageAtFrontOfQueue(Message.obtain(this, STARVE_STREAM))
    }

    //Handler queueing functions
    fun postYuv(width: Int, height: Int, b : ByteArray) : Boolean{
        if(!processExists.get() || hasMessages(PUBLISH_TO_PROCESS)) return false//We don't want to flood the handler...
        Message.obtain(this, PUBLISH_TO_PROCESS, width, height, b).sendToTarget()
        return true
    }

    private fun updateProcess(){
        Message.obtain(this, NEW_PROCESS).sendToTarget()
    }

    private fun notifyDeadProcess(){
        this.sendEmptyMessage(PROCESS_DIED)
    }

    companion object {
        const val NEW_PROCESS = 0
        const val PROCESS_DIED = 1
        const val PUBLISH_TO_PROCESS = 2
        const val START_STREAM = 3
        const val STARVE_STREAM = 4
        private val list = HashMap<String, HandlerThread>()
        private const val TAG = "CameraUpdateThread"
        fun create(context: Context, name : String) : CameraUpdateHandler{
            val handlerThread : HandlerThread?
            if(!list.containsKey(name)){
                handlerThread = HandlerThread(name)
                handlerThread.start()
                list[name] = handlerThread
            }
            else{
                handlerThread = list[name]
            }
            return CameraUpdateHandler(context, handlerThread!!.looper)
        }
    }
}
