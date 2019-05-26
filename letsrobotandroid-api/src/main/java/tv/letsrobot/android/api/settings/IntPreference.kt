package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Preference helper that accesses an int preference given a key
 */
class IntPreference(preferences: SharedPreferences, default: Int, key: String)
    : Preference<Int>(preferences, default, key) {
    var value : Int
        get() = getGenericValue() as Int
        set(value) {saveValue(value)}
}