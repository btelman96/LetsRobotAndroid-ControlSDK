package com.runmyrobot.android_robot_for_phone.api.camera

import android.content.Context
import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.renderscript.Element.U8_4
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.renderscript.Type
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.api.UDPOutputStream
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil
import org.bytedeco.javacpp.avcodec.AV_CODEC_ID_MPEG1VIDEO
import org.bytedeco.javacpp.avutil
import org.bytedeco.javacpp.avutil.AV_PIX_FMT_YUV420P
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.Frame.DEPTH_UBYTE
import org.bytedeco.javacv.LeptonicaFrameConverter
import java.nio.ByteBuffer


/**
 * Threaded calls to update the camera feed
 */
class CameraUpdateHandler(val context: Context, looper: Looper) : Handler(looper), FFmpegExecuteResponseHandler {
    private val UUID = java.util.UUID.randomUUID().toString()
    private var process : Process? = null
    private var r = Rect()
    private val LOGTAG = "awda"
    //TODO switch to using java-cpp implementation
    private var ffmpeg= FFmpeg.getInstance(context)
    var rs = RenderScript.create(context)
    var yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, U8_4(rs))
    private var rgbaType: Type.Builder? = null

    private var streaming: Boolean = false

    private var addr: String? = null

    private var processExists = false

    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        when (msg.what) {
            NEW_PROCESS -> {
                Log.d(LOGTAG, "NEW_PROCESS")
                (msg.obj as? Process)?.let {
                    process = it
                    processExists = true
                }
            }
            PROCESS_DIED -> {
                Log.d(LOGTAG, "PROCESS_DIED")
                process = null
                processExists = false
                if(streaming){
                    addr?.let { bootFFMPEG(it) }
                }
            }
            PUBLISH_TO_PROCESS -> {

                //Only proceed if our trustworthy booleans are true
                if(streaming){
                    //And then lets not trust them
                    try{
                        (msg.obj as? ByteArray)?.let {
                            Log.d(LOGTAG, "PUBLISH_TO_PROCESS")
                            renderYuvToService(msg.arg1, msg.arg2, it)
                        }
                    }catch (e : Exception){e.printStackTrace()}
                }
            }
            START_STREAM -> {
                Log.d(LOGTAG, "START_STREAM")
                Thread.currentThread().priority = Thread.MAX_PRIORITY
                addr = msg.obj as? String
                addr?.let {
                    Log.d(LOGTAG, "ADDR_OKAY")
                    streaming = true
                    bootFFMPEG(it)
                } ?: throw Exception("Address is missing!")
            }
            STARVE_STREAM -> {
                Log.d(LOGTAG, "STARVE_STREAM")
                Thread.currentThread().priority = Thread.MIN_PRIORITY
                streaming = false
            }
        }
    }

    private var streamOut: UDPOutputStream? = null

    private var frameRecorder: FFmpegFrameRecorder? = null

    private fun bootFFMPEG(addr : String) {
        Log.d(LOGTAG, "bootFFMPEG")
        if(!streaming){
            return
        }
        Log.d(LOGTAG, "bootFFMPEG_OKAY")
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            val xres = "640"
            val yres = "480"

            val rotationOption = StoreUtil.getOrientation(context).ordinal //leave blank
            val builder = StringBuilder()
            for (i in 0..rotationOption){
                if(i == 0) builder.append("-vf transpose=1")
                else builder.append(",transpose=1")
            }
            print("\"$builder\"")
            val kbps = "20"
            val stream_key = RobotApplication.Instance.getCameraPass()
            //TODO hook up with bitrate and resolution prefs
            val command = "-f mjpeg -i udp://localhost:1234 -f mpegts -framerate 30 -codec:v mpeg1video -b:v 10k -bf 0 -muxdelay 0.001 -tune zerolatency -preset ultrafast -pix_fmt yuv420p $builder http://$addr/$stream_key/$xres/$yres/"
            //ffmpeg.execute(UUID, null, command.split(" ").toTypedArray(), this)
            //streamOut = UDPOutputStream("localhost", 1234)
            try {
                frameRecorder?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            frameRecorder = null
            print("http://$addr/$stream_key/$xres/$yres/")
            frameRecorder = FFmpegFrameRecorder("http://$addr/$stream_key/$xres/$yres/", xres.toInt(), yres.toInt())
            frameRecorder?.let {
                //it.aspectRatio = 16.0/9.0
                it.format = "mpegts"
                it.videoCodec = AV_CODEC_ID_MPEG1VIDEO
                it.frameRate = 30.0
                it.videoOptions["tune"] = "zerolatency"
                it.videoOptions["preset"] = "ultrafast"
                it.videoBitrate = 2000000
                it.pixelFormat = AV_PIX_FMT_YUV420P
            }
            frameRecorder?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    private val convertor = LeptonicaFrameConverter()

    private var videoTS = 0L

    private var startTime = 0L

    private var yuvImage: Frame? = null

    private fun renderYuvToService(width : Int, height : Int, b : ByteArray){
        Log.d(LOGTAG, "bootFFMPEG_OKAY")
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
    fun postYuv(width: Int, height: Int, b : ByteArray){
        if(hasMessages(PUBLISH_TO_PROCESS)) return //We don't want to flood the handler...
        Message.obtain(this, PUBLISH_TO_PROCESS, width, height, b).sendToTarget()
    }

    private fun updateProcess(p : Process?){
        Message.obtain(this, NEW_PROCESS, p).sendToTarget()
    }

    private fun notifyDeadProcess(){
        this.sendEmptyMessage(PROCESS_DIED)
    }

    //FFMpeg callbacks
    override fun onFinish() {
        Log.d(LOGTAG, "onFinish")
        notifyDeadProcess()
    }

    override fun onProcess(p0: Process?) {
        Log.d(LOGTAG, "onProcess")
        updateProcess(p0)
    }

    override fun onStart() {
        Log.d(LOGTAG, "onStart")
    }

    override fun onProgress(message: String?) {
        Log.d(LOGTAG, "onProgress : $message")
    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, "onFailure : $message")
    }

    override fun onSuccess(message: String?) {
        Log.d(LOGTAG, "onSuccess : $message")
    }

    companion object {
        const val NEW_PROCESS = 0
        const val PROCESS_DIED = 1
        const val PUBLISH_TO_PROCESS = 2
        const val START_STREAM = 3
        const val STARVE_STREAM = 4
        val list = HashMap<String, HandlerThread>()

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
