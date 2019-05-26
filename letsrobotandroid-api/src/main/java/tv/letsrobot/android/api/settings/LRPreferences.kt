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

    /**
     * Robot ID that is used to communicate with some Let's Robot server sockets
     */
    val robotId = Preference.fromId(preferenceManager, context,
            "", R.string.connectionRobotIdKey) as StringPreference
    /**
     * CameraID that is used to communicate with some Let's Robot server sockets
     */
    val cameraId = Preference.fromId(preferenceManager, context,
            "", R.string.connectionCameraIdKey) as StringPreference

    /**
     * Camera Stream Key that is used as authentication with some Let's Robot sockets
     */
    val cameraPass = Preference.fromId(preferenceManager, context,
            "hello", R.string.connectionCameraPassKey) as StringPreference

    /**
     * Should the camera be used?
     */
    val cameraEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.cameraSettingsEnableKey) as BooleanPreference

    /**
     * stream output bitrate for video
     */
    val videoBitrate = Preference.fromId(preferenceManager, context,
            "512", R.string.cameraBitrateKey) as StringPreference

    /**
     * stream output resolution for video
     */
    val videoResolution = Preference.fromId(preferenceManager, context,
            "640x480", R.string.cameraResolutionKey) as StringPreference

    /**
     * Camera orientation of video
     */
    @Suppress("UNCHECKED_CAST")
    val orientation = Preference.fromEnumId(preferenceManager, context,
            CameraDirection.values()[1], R.string.cameraOrientationKey)
    /**
     * If true, we will use newer camera features if they exist
     */
    val useCamera2 = Preference.fromId(preferenceManager, context,
            Build.VERSION.SDK_INT >= 21, R.string.useCamera2) as BooleanPreference

    /**
     * Should the screen be allowed to stay on?
     */
    val sleepMode = Preference.fromId(preferenceManager, context,
            true, R.string.displayPersistKey) as BooleanPreference

    /**
     * Should the mic be used?
     */
    val micEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.microphoneSettingsEnableKey) as BooleanPreference

    /**
     * Audio In volume boost for use with FFmpeg
     */
    val micVolumeBoost = Preference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey) as IntPreference

    /**
     * Audio In bitrate for use with FFmpeg
     */
    val micBitrate = Preference.fromId(preferenceManager, context,
            1, R.string.micVolumeBoostKey) as IntPreference

    /**
     * Should Text to speech features be used?
     */
    val ttsEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.audioSettingsEnableKey) as BooleanPreference

    /**
     * Should we pull Let's Robot messages use Text To Speech for them?
     */
    val ttsLREnabled = Preference.fromId(preferenceManager, context,
            false, R.string.audioSettingsTTSLREnabledKey) as BooleanPreference

    /**
     * Should anonymous users be broadcast to Text to Speech?
     */
    val anonTTSEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.audioSettingsTTSAnonEnabledKey) as BooleanPreference

    /**
     * Should Text to Speech be used when someone is banned or timed out?
     */
    val timeoutBanTTSNotificationsEnabled =  Preference.fromId(preferenceManager, context,
    false, R.string.audioBanVoiceEnabledKey) as BooleanPreference

    /**
     * Should Internal messages be sent to Text to Speech? (network events, etc...)
     */
    val internalSystemTTSMessagesEnabled =  Preference.fromId(preferenceManager, context,
            false, R.string.audioTTSInternalEnabledKey) as BooleanPreference

    /**
     * Should the streamer be able to use .battery level?
     */
    val internalBatteryCommandEnabled = Preference.fromId(preferenceManager, context,
    false, R.string.audioBatteryCommandEnabledKey) as BooleanPreference

    /**
     * Should the chat display be enabled in the main robot activity?
     */
    val chatDisplayEnabled = Preference.fromId(preferenceManager, context,
            false, R.string.displayChatEnabledKey) as BooleanPreference

    /**
     * Should the controls hide themselves when the robot is on?
     */
    val autoHideMainControls = Preference.fromId(preferenceManager, context,
            false, R.string.autoHideControlsEnabledKey) as BooleanPreference

    @Suppress("UNCHECKED_CAST")
    val communication = Preference.fromEnumId(preferenceManager, context,
            CommunicationType.values()[0], R.string.robotConnectionTypeKey)

    @Suppress("UNCHECKED_CAST")
    val protocol = Preference.fromEnumId(preferenceManager, context,
            ProtocolType.values()[0], R.string.robotProtocolTypeKey)

    companion object{

        lateinit var INSTANCE : LRPreferences
            private set

        fun init(context: Context){
            INSTANCE = LRPreferences(context)
        }
    }
}