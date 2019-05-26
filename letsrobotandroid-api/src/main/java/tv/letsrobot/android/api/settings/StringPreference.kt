package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Preference helper that accesses a String preference given a key
 */
class StringPreference(preferences: SharedPreferences, default: String, key: String)
    : Preference<String>(preferences, default, key) {

    var value : String
        get() = getGenericValue() as String
        set(value) {saveValue(value)}
}