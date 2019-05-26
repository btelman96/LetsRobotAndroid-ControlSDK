package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Created by Brendon on 5/25/2019.
 */
class EnumPreference<T : Enum<*>>(preferences: SharedPreferences, default: Enum<*>, key: String)
    : Preference<Enum<*>>(preferences, default, key) {
    var value : T
        get() = getGenericValue() as T
        set(value) {saveValue(value)}
}