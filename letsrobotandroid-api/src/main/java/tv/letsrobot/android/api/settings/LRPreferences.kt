package tv.letsrobot.android.api.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import tv.letsrobot.android.api.R
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType

/**
 * LR preferences based on settings.
 */
class LRPreferences private constructor(context: Context) {
    private val preferenceManager : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    val configured = LRPreference(preferenceManager,
            false, "configured")
    val robotId = LRPreference.fromId(preferenceManager, context,
            "", R.string.connectionRobotIdKey)
    val cameraId = LRPreference.fromId(preferenceManager, context,
            "", R.string.connectionCameraIdKey)
    val cameraPass = LRPreference.fromId(preferenceManager, context,
            "hello", R.string.connectionCameraPassKey)
    val cameraEnabled = LRPreference.fromId(preferenceManager, context,
            false, R.string.cameraSettingsEnableKey)
    val sleepMode = LRPreference.fromId(preferenceManager, context,
            true, R.string.displayPersistKey)
    val micEnabled = LRPreference.fromId(preferenceManager, context,
            false, R.string.microphoneSettingsEnableKey)
    val micVolumeBoost = LRPreference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey)
    val micBitrate = LRPreference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey)
    val ttsEnabled = LRPreference.fromId(preferenceManager, context,
            false, R.string.audioSettingsEnableKey)
    val videoBitrate = LRPreference.fromId(preferenceManager, context,
            "512", R.string.cameraBitrateKey)
    val videoResolution = LRPreference.fromId(preferenceManager, context,
            "640x480", R.string.cameraResolutionKey)
    val communication = LRPreference.fromId(preferenceManager, context,
            CommunicationType.values()[0], R.string.robotConnectionTypeKey)
    val protocol = LRPreference.fromId(preferenceManager, context,
            ProtocolType.values()[0], R.string.robotProtocolTypeKey)
    val orientation = LRPreference.fromId(preferenceManager, context,
            CameraDirection.values()[1], R.string.cameraOrientationKey) //default to 90 degrees
    val useCamera2 = LRPreference.fromId(preferenceManager, context,
            Build.VERSION.SDK_INT >= 21, R.string.useCamera2) //true if Android 5.0 or greater

    companion object{
        lateinit var INSTANCE : LRPreferences
            private set

        fun init(context: Context){
            INSTANCE = LRPreferences(context)
        }
    }
}