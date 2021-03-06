package tv.letsrobot.android.api.components.camera.api21

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import tv.letsrobot.android.api.components.camera.SurfaceTextureCameraBaseComponent
import tv.letsrobot.android.api.enums.ComponentStatus
import tv.letsrobot.android.api.models.CameraSettings


/**
 * Created by Brendon on 10/6/2018.
 */
@RequiresApi(21)
class Camera2SurfaceTextureComponent(context: Context, settings: CameraSettings) : SurfaceTextureCameraBaseComponent(context, settings), ImageReader.OnImageAvailableListener {
    val reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 10)

    private var mPreviewBuilder: CaptureRequest.Builder? = null
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var mBackgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var mBackgroundHandler: Handler? = null

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its status.
     */
    private val mStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(@NonNull cameraDevice: CameraDevice) {
            mCameraDevice = cameraDevice
            startPreview()
        }

        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
            cameraDevice.close()
            mCameraDevice = null
        }

        override fun onError(@NonNull cameraDevice: CameraDevice, error: Int) {
            cameraDevice.close()
            mCameraDevice = null
        }

    }

    init {
        Log.v("CameraAPI", "init")
        init()
    }

    @SuppressLint("MissingPermission") //Already handled. No way to call this
    override fun setupCamera() {
        startBackgroundThread()
        reader.setOnImageAvailableListener(this, mBackgroundHandler)
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]
            manager.openCamera(cameraId, mStateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
            status = ComponentStatus.ERROR
        } catch (e: NullPointerException) {
            e.printStackTrace()
            status = ComponentStatus.ERROR
        } catch (e: InterruptedException) {
            e.printStackTrace()
            status = ComponentStatus.ERROR
            throw RuntimeException("Interrupted while trying to lock camera opening.")
        }
    }

    override fun releaseCamera() {
        stopBackgroundThread()
    }

    /**
     * A reference to the opened [android.hardware.camera2.CameraDevice].
     */
    private var mCameraDevice: CameraDevice? = null

    /**
     * A reference to the current [android.hardware.camera2.CameraCaptureSession] for
     * preview.
     */
    private var mPreviewSession: CameraCaptureSession? = null

    override fun onImageAvailable(reader: ImageReader?) {
        var image: Image? = null
        try {
            image = reader?.acquireLatestImage()
            image?.let {
                val buffer = it.planes[0].buffer
                val imageBytes = ByteArray(buffer.remaining())
                buffer.get(imageBytes)
                //push(imageBytes, ImageFormat.JPEG, null)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                push(bitmap, ImageFormat.JPEG, null)
            }
        } finally {
            image?.close()
        }
    }

    /**
     * Start the camera preview.
     */
    private fun startPreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            closePreviewSession()
            val texture = mStManager.surfaceTexture
            texture.setDefaultBufferSize(height, width)
            mPreviewBuilder = mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

            //val previewSurface = Surface(texture)
            //mPreviewBuilder!!.addTarget(previewSurface)
            mPreviewBuilder!!.addTarget(reader.surface)

            mCameraDevice!!.createCaptureSession(listOf(/*previewSurface, */reader.surface),
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(@NonNull session: CameraCaptureSession) {
                            mPreviewSession = session
                            updatePreview()
                        }

                        override fun onConfigureFailed(@NonNull session: CameraCaptureSession) {

                        }
                    }, mBackgroundHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
/*
    class CaptureCallback : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            Log.d(TAG, "onCaptureCompleted")
        }
    }*/

    /**
     * Update the camera preview. [.startPreview] needs to be called in advance.
     */
    private fun updatePreview() {
        if (null == mCameraDevice) {
            return
        }
        try {
            mPreviewBuilder?.let { setUpCaptureRequestBuilder(it) }
            val thread = HandlerThread("CameraPreview")
            thread.start()
            mPreviewSession!!.setRepeatingRequest(mPreviewBuilder!!.build()
                    ,null
                    , mBackgroundHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun setUpCaptureRequestBuilder(builder: CaptureRequest.Builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
    }

    private fun closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession?.close()
            mPreviewSession = null
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread?.start()
        mBackgroundHandler = Handler(mBackgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        mBackgroundThread?.quitSafely()
        try {
            mBackgroundThread?.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}