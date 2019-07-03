package tv.letsrobot.android.api.models

import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.settings.LRPreferences

/**
 * Holder to hold settings for camera
 */
data class CameraSettings(val cameraId : String,
                          val pass : String = "hello",
                          val width : Int = 640,
                          val height : Int = 480,
                          val bitrate : Int = 512,
                          /**
                           * Camera orientation
                           */
                          val orientation: CameraDirection = CameraDirection.DIR_90,
                          /**
                           * Use the legacy camera1 api. Automatically uses if less than API 21
                           */
                          val useLegacyApi : Boolean = false,
                          val frameRate : Int = 25){

    companion object{
        fun buildCameraSettings(settings: LRPreferences) : CameraSettings{
            val arrRes = settings.videoResolution.value.split('x')
            return CameraSettings(
                    cameraId = settings.cameraId.value,
                    pass = settings.cameraPass.value,
                    width = arrRes[0].toInt(),
                    height = arrRes[1].toInt(),
                    bitrate = settings.videoBitrate.value.toInt(),
                    useLegacyApi = !settings.useCamera2.value,
                    orientation = settings.orientation.value
            )
        }
    }
}