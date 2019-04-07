package tv.letsrobot.controller.android.activities.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tv.letsrobot.controller.android.activities.SettingsActivity

/**
 * Created by Brendon on 3/23/2019.
 */
abstract class BasePreferenceFragmentCompat: PreferenceFragmentCompat() {
    abstract fun getDesiredPreferencesFromResources() : Int

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(getDesiredPreferencesFromResources(), rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        return super.onPreferenceTreeClick(preference)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as SettingsActivity).getSwitchBar().hide()
    }
}