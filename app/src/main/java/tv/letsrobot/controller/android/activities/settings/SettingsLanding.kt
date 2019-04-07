package tv.letsrobot.controller.android.activities.settings

import androidx.annotation.IdRes
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
                navigate(R.id.action_settingsLanding_to_settingsConnection)
            }
            "robotSettingsEnable"->{
                navigate(R.id.action_settingsLanding_to_settingsRobot)
            }
            "cameraSettingsEnable"->{
                navigate(R.id.action_settingsLanding_to_settingsCamera)
            }
            "microphoneSettingsEnable"->{
                navigate(R.id.action_settingsLanding_to_settingsMicrophone)
            }
            "speakerSettingsEnable"->{
                navigate(R.id.action_settingsLanding_to_settingsAudio)
            }
            "displaySettings"->{
                navigate(R.id.action_settingsLanding_to_settingsDisplay)
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    fun navigate(@IdRes resId : Int){
        Navigation.findNavController(view!!).navigate(resId)
    }
}
