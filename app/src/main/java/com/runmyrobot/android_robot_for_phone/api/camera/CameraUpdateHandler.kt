package com.runmyrobot.android_robot_for_phone.api.camera

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.renderscript.*
import android.renderscript.Element.U8_4
import android.util.Log
import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException
import com.runmyrobot.android_robot_for_phone.RobotApplication
import com.runmyrobot.android_robot_for_phone.api.UDPOutputStream
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil

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
                Log.d(LOGTAG, "PUBLISH_TO_PROCESS")
                //Only proceed if our trustworthy booleans are true
                if(streaming){
                    //And then lets not trust them
                    try{
                        (msg.obj as? ByteArray)?.let {
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
            ffmpeg.execute(UUID, null, command.split(" ").toTypedArray(), this)
            streamOut = UDPOutputStream("localhost", 1234)
        } catch (e: FFmpegCommandAlreadyRunningException) {
            e.printStackTrace()
            // Handle if FFmpeg is already running
        }
    }

    private fun renderYuvToService(width : Int, height : Int, b : ByteArray){
        Log.d(LOGTAG, "bootFFMPEG_OKAY")
        if(r.height() != height || r.width() != width){
            r.set(0, 0, width, height)
            rgbaType = Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height)
        }
        val yuvType = Type.Builder(rs, Element.U8(rs)).setX(b.size)
        val `in` = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT)
        val outA = Allocation.createTyped(rs, rgbaType!!.create(), Allocation.USAGE_SCRIPT)
        `in`.copyFrom(b)

        yuvToRgbIntrinsic.setInput(`in`)
        yuvToRgbIntrinsic.forEach(outA)
        val cameraBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)
        outA.copyTo(cameraBitmap)

        /*val out = ByteArrayOutputStream()
        val yuvImage = YuvImage(b, ImageFormat.NV21, width, height, null)
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        val image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)*/
        val newBitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888)

        val canvas = Canvas(newBitmap)
        canvas.drawBitmap(cameraBitmap, r, r, null)
        cameraBitmap.recycle()
        canvas.drawCircle(40f, 40f, 20f, Paint().also {
            it.color = Color.BLACK
        })
        //val im = YuvImage(b, ImageFormat.NV21, width, height, null)
        try {
            streamOut?.bufferSize = 15000
            streamOut?.setMaxBufferSize(15000)
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, streamOut)
        } catch (e: Exception) {
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
