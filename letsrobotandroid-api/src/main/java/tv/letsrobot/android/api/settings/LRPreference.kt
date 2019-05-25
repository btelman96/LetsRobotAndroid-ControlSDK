package tv.letsrobot.android.api.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes

/**
 * Preference Item
 */
class LRPreference<T : Any>(private val preferences: SharedPreferences, val default: T, val key : String) {
    @Throws(IllegalArgumentException::class)
    fun saveValue(value: Any){
        if(value::class.java.name != default::class.java.name)
            throw IllegalArgumentException("Expected type of ${default::class.java.simpleName}")
        val sharedPrefs = preferences.edit()
        when(default){
            is Boolean -> sharedPrefs.putBoolean(key, value as Boolean)
            is String -> sharedPrefs.putString(key, value as String)
            is Enum<*> -> sharedPrefs.putString(key, (value as Enum<*>).name)
        }
        sharedPrefs.apply()
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IllegalArgumentException::class)
    fun <T : Any> getValue() : T{
        val value = when(default){
            is Boolean -> preferences.getBoolean(key, default)
            is String -> preferences.getString(key, default)
            is Int -> preferences.getInt(key, default)
            is Enum<*> -> {
                preferences.getString(key, null)?.let{enumName ->
                    var enum : Enum<*>? = null
                    default::class.java.enumConstants.forEach {
                        if((it as Enum<*>).name == enumName)
                            enum = it
                    }
                    enum
                } ?: default
            }
            else -> default
        }
        return value as T
    }

    fun reset() {
        preferences.edit().remove(key).apply()
    }

    companion object{
        fun extractKeyFromId(context: Context, @StringRes key : Int): String {
            return context.getString(key)
        }

        fun <T:Any> fromId(preferences: SharedPreferences, context: Context, default: T, @StringRes key : Int) : LRPreference<T>{
            val strKey = extractKeyFromId(context, key)
            return LRPreference(preferences, default, strKey)
        }
    }
}