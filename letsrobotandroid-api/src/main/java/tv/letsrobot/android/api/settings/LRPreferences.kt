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

    val robotId = Preference.fromId(preferenceManager, context,
            "", R.string.connectionRobotIdKey) as StringPreference
    val cameraId = Preference.fromId(preferenceManager, context,
            "", R.string.connectionCameraIdKey) as StringPreference
    val cameraPass = Preference.fromId(preferenceManager, context,
            "hello", R.string.connectionCameraPassKey) as StringPreference
    val cameraEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.cameraSettingsEnableKey) as BooleanPreference
    val sleepMode = Preference.fromId(preferenceManager, context,
            true, R.string.displayPersistKey) as BooleanPreference
    val micEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.microphoneSettingsEnableKey) as BooleanPreference
    val micVolumeBoost = Preference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey) as IntPreference
    val micBitrate = Preference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey) as IntPreference
    val ttsEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.audioSettingsEnableKey) as BooleanPreference
    val videoBitrate = Preference.fromId(preferenceManager, context,
            "512", R.string.cameraBitrateKey) as StringPreference
    val videoResolution = Preference.fromId(preferenceManager, context,
            "640x480", R.string.cameraResolutionKey) as StringPreference

    @Suppress("UNCHECKED_CAST")
    val communication = Preference.fromEnumId(preferenceManager, context,
            CommunicationType.values()[0], R.string.robotConnectionTypeKey)
    @Suppress("UNCHECKED_CAST")
    val protocol = Preference.fromEnumId(preferenceManager, context,
            ProtocolType.values()[0], R.string.robotProtocolTypeKey)
    @Suppress("UNCHECKED_CAST")
    val orientation = Preference.fromEnumId(preferenceManager, context,
            CameraDirection.values()[1], R.string.cameraOrientationKey)
    val useCamera2 = Preference.fromId(preferenceManager, context,
            Build.VERSION.SDK_INT >= 21, R.string.useCamera2)  as BooleanPreference

    companion object{
        const val EMPTY_STRING = ""

        lateinit var INSTANCE : LRPreferences
            private set

        fun init(context: Context){
            INSTANCE = LRPreferences(context)
        }
    }
}