package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Preference helper that accesses a boolean preference given a key
 */
class BooleanPreference(preferences: SharedPreferences, default: Boolean, key: String)
    : Preference<Boolean>(preferences, default, key) {
    var value : Boolean
        get() = getGenericValue() as Boolean
        set(value) {saveValue(value)}
}