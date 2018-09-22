package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.graphics.Rect
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler
import com.google.common.util.concurrent.RateLimiter
import com.runmyrobot.android_robot_for_phone.api.camera.CameraUpdateHandler
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean


/**
 * Class that contains only the camera components for streaming to letsrobot.tv
 *
 * To make this functional, pass in cameraId and a valid SurfaceHolder to a Core.Builder instance
 *
 * This will grab the camera password automatically from config file
 */
class CameraComponent
/**
 * Init camera object.
 * @param context Needed to access the camera
 * @param cameraId camera id for robot
 */
constructor(context: Context, val cameraId: String, val holder: SurfaceHolder) : Component(context), FFmpegExecuteResponseHandler, android.hardware.Camera.PreviewCallback, SurfaceHolder.Callback {
    internal var ffmpegRunning = AtomicBoolean(false)
    val cameraUpdateHandler = CameraUpdateHandler.create(context, "cameraUpdateHandler")

    init {
        holder.addCallback(this)
    }
    var UUID = java.util.UUID.randomUUID().toString()
    var process : Process? = null
    var port: String? = null
    var host: String? = null
    var streaming = AtomicBoolean(false)
    var previewRunning = false
    override fun enable() : Boolean{
        if(!super.enable()) return false
        try {
            val client = OkHttpClient.Builder()
                    .build()
            var call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_video_port/%s", cameraId)).build())
            var response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                port = `object`.getString("mpeg_stream_port")
            }
            call = client.newCall(Request.Builder().url(String.format("https://letsrobot.tv/get_websocket_relay_host/%s", cameraId)).build())
            response = call.execute()
            if (response.body() != null) {
                val `object` = JSONObject(response.body()!!.string())
                Log.d("ROBOT", `object`.toString())
                host = `object`.getString("host")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        if(host == null || port == null){
            status = ComponentStatus.ERROR
        }
        else {
            streaming.set(true)
            cameraUpdateHandler.enable("$host:$port")
        }
        return true
    }

    private var successCounter: Int = 0

    var width = 0
    var height = 0
    var limiter = RateLimiter.create(30.0)

    private lateinit var r: Rect

    override fun onPreviewFrame(b: ByteArray?, camera: android.hardware.Camera?) {
        if(!streaming.get()) return
        if(!limiter.tryAcquire()) return
        b ?: return //return if null
        camera?.parameters?.let {
            val size = it.previewSize
            this.width = size.width
            this.height = size.height
            this.r = Rect(0, 0, width, height)
        }
        cameraUpdateHandler.postYuv(width, height, b)
    }

    private fun setupCam(){
        if (!cameraActive.get() && surface) {
            camera?.let {
                if (previewRunning) {
                    it.stopPreview()
                }

                try {
                    val p = it.parameters
                    val previewSizes = p.supportedPreviewSizes
                    // You need to choose the most appropriate previewSize for your app
                    val previewSize = previewSizes.get(0) // .... select one of previewSizes here
                    //p.setPreviewSize(previewSize.width, previewSize.height);
                    p.setPreviewSize(640, 480)
                    it.parameters = p

                    it.setPreviewDisplay(holder)
                    it.setPreviewCallback(this)
                    Log.v(LOGTAG, "startPreview")
                    it.startPreview()
                    previewRunning = true
                    cameraActive.set(true)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun disable() : Boolean{
        if(!super.disable()) return false
        // Setting this to false will prevent the preview from executing code, which will starve FFmpeg
        // And sever the stream
        streaming.set(false)
        cameraUpdateHandler.disable()
        return true
    }

    override fun onStart() {
        ffmpegRunning.set(true)
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onStart")
    }

    override fun onProgress(message: String?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onProgress : $message")
        successCounter++
        status = when {
            successCounter > 5 -> ComponentStatus.STABLE
            successCounter > 2 -> ComponentStatus.INTERMITTENT
            else -> ComponentStatus.CONNECTING
        }
    }

    override fun onFailure(message: String?) {
        Log.e(LOGTAG, "progress : $message")
        status = ComponentStatus.ERROR
    }

    override fun onSuccess(message: String?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onSuccess : $message")
    }

    override fun onFinish() {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onFinish")
        ffmpegRunning.set(false)
        process?.destroy()
        process = null
        status = ComponentStatus.DISABLED
    }

    override fun onProcess(p0: Process?) {
        @Suppress("ConstantConditionIf")
        if(shouldLog)
            Log.d(LOGTAG, "onProcess")
        this.process = p0
    }

    private var camera : android.hardware.Camera? = null

    private var surface = false

    override fun surfaceCreated(holder: SurfaceHolder) {
        surface = true
        camera = Camera.open()
        camera?.setDisplayOrientation(90)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        setupCam()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.v("CameraAPI", "surfaceDestroyed")
        cameraActive.set(false)
        surface = false
        previewRunning = false
        camera?.stopPreview()
        camera?.setPreviewCallback (null)
        camera?.release()
        camera = null
    }

    companion object {
        private const val LOGTAG = "CameraComponent"
        private const val shouldLog = false
        private val cameraActive = AtomicBoolean(false)
    }
}
