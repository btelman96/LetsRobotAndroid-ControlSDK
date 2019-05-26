package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Preference helper that accesses an enum preference given a key
 */
class EnumPreference<T : Enum<*>>(preferences: SharedPreferences, default: Enum<*>, key: String)
    : Preference<Enum<*>>(preferences, default, key) {
    var value : T
        get() = getGenericValue() as T
        set(value) {saveValue(value)}
}