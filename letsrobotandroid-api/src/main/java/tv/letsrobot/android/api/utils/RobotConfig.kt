package tv.letsrobot.android.api.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.StringRes
import tv.letsrobot.android.api.enums.CameraDirection
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.robot.ProtocolType

/**
 * Easy way to access all defined settings without duplicating a lot of code
 */
enum class RobotConfig(val default: Any, @StringRes val key : Int? = null) {
    Configured(false),
    RobotId(""),
    CameraId(""),
    CameraPass("hello"),
    CameraEnabled(false),
    SleepMode(true),
    MicEnabled(false),
    TTSEnabled(false),
    ErrorReporting(false),
    VideoBitrate("512"),
    VideoResolution("640x480"),
    Communication(CommunicationType.values()[0]),
    Protocol(ProtocolType.values()[0]),
    Orientation(CameraDirection.values()[1]), //default to 90 degrees
    UseLegacyCamera(Build.VERSION.SDK_INT < 21); //true if less than Android 5.0

    @Throws(IllegalArgumentException::class)
    fun saveValue(context: Context, value: Any){
        val prefKey = determineKey(context)
        if(value::class.java.name != default::class.java.name)
            throw IllegalArgumentException("Expected type of ${default::class.java.simpleName}")
        val sharedPrefs = getSharedPrefs(context).edit()
        when(default){
            is Boolean -> sharedPrefs.putBoolean(prefKey, value as Boolean)
            is String -> sharedPrefs.putString(prefKey, value as String)
            is Enum<*> -> sharedPrefs.putInt(prefKey, (value as Enum<*>).ordinal)
        }
        sharedPrefs.apply()
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalArgumentException::class)
    fun <T : Any> getValue(context: Context) : T{
        val prefKey = determineKey(context)
        val sharedPrefs = getSharedPrefs(context)
        val value = when(default){
            is Boolean -> sharedPrefs.getBoolean(prefKey, default)
            is String -> sharedPrefs.getString(prefKey, default)
            is Int -> sharedPrefs.getInt(prefKey, default)
            is Enum<*> -> {
                sharedPrefs.getInt(prefKey, -1).takeIf { it != -1 }?.let{
                    default::class.java.enumConstants[it]
                } ?: default
            }
            else -> default
        }
        return value as T
    }

    fun reset(context: Context) {
        val prefKey = determineKey(context)
        getSharedPrefs(context).edit().remove(prefKey).apply()
    }

    private fun determineKey(context: Context): String? {
        return key?.let { context.getString(key) } ?: name
    }

    companion object {
        fun getSharedPrefs(context: Context) : SharedPreferences {
            return context.getSharedPreferences("robotConfig", 0)
        }
    }
}