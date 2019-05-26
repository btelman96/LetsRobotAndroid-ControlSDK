package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Created by Brendon on 5/25/2019.
 */
class BooleanPreference(preferences: SharedPreferences, default: Boolean, key: String)
    : Preference<Boolean>(preferences, default, key) {
    var value : Boolean
        get() = getGenericValue() as Boolean
        set(value) {saveValue(value)}
}