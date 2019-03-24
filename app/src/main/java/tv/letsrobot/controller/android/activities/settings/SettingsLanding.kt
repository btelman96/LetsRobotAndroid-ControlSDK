package tv.letsrobot.controller.android.activities.settings

import androidx.navigation.Navigation
import androidx.preference.Preference
import tv.letsrobot.controller.android.R


class SettingsLanding : BasePreferenceFragmentCompat() {
    override fun getDesiredPreferencesFromResources(): Int {
        return R.xml.settings_landing_options
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when(preference?.key){
            "connectionSettings"->{
                Navigation.findNavController(view!!).navigate(R.id.action_settingsLanding_to_settingsConnection)
            }
            "robotSettingsEnable"->{
                Navigation.findNavController(view!!).navigate(R.id.action_settingsLanding_to_settingsRobot)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }
}
