package tv.letsrobot.controller.android

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

class SettingsLanding2 : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_landing_options, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        Snackbar.make(view!!, preference?.key.toString(), Snackbar.LENGTH_LONG).show()
        return super.onPreferenceTreeClick(preference)
    }
}