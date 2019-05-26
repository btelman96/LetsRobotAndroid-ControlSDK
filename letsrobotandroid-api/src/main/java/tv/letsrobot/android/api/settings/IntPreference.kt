package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Created by Brendon on 5/25/2019.
 */
class IntPreference(preferences: SharedPreferences, default: Int, key: String)
    : Preference<Int>(preferences, default, key) {
    var value : Int
        get() = getGenericValue() as Int
        set(value) {saveValue(value)}
}