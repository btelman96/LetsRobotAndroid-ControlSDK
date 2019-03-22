package tv.letsrobot.controller.android

import android.os.Bundle
import androidx.navigation.Navigation
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar

class SettingsLanding : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_landing_options, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        Snackbar.make(view!!, preference?.key.toString(), Snackbar.LENGTH_LONG).show()
        when(preference?.key){
            "connection"->{
                Navigation.findNavController(view!!).navigate(R.id.action_settingsLanding_to_settingsConnection)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}
