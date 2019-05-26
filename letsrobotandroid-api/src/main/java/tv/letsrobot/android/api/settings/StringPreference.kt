package tv.letsrobot.android.api.settings

import android.content.SharedPreferences

/**
 * Created by Brendon on 5/25/2019.
 */
class StringPreference(preferences: SharedPreferences, default: String, key: String)
    : Preference<String>(preferences, default, key) {

    var value : String
        get() = getGenericValue() as String
        set(value) {saveValue(value)}
}