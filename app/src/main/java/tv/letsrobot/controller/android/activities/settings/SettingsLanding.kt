package tv.letsrobot.controller.android.activities.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.Navigation
import androidx.preference.Preference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import tv.letsrobot.controller.android.R


class SettingsLanding : BasePreferenceFragmentCompat(
        R.xml.settings_landing_options
) {

    /**
     * Settings navigation links are placed here. This requires that the key is a StringId
     */
    private val linkedPageSet = HashMap<@IdRes Int, @IdRes Int>().also {
        it[R.string.connectionSettingsKey] = R.id.action_settingsLanding_to_settingsConnection
        it[R.string.robotSettingsEnableKey] = R.id.action_settingsLanding_to_settingsRobot
        it[R.string.cameraSettingsEnableKey] = R.id.action_settingsLanding_to_settingsCamera
        it[R.string.microphoneSettingsEnableKey] = R.id.action_settingsLanding_to_settingsMicrophone
        it[R.string.audioSettingsEnableKey] = R.id.action_settingsLanding_to_settingsAudio
        it[R.string.displaySettingsKey] = R.id.action_settingsLanding_to_settingsDisplay
    }

    /**
     * This converts the linkedPageSet keys to strings for easy access
     */
    private val dict = HashMap<String, @IdRes Int>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateDictFromLinkedPageSet()
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        dict[preference?.key]?.let {
            navigate(it)
        }
        if(preference?.key == getString(R.string.openSourceSettingsKey)){
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun navigate(@IdRes resId : Int){
        Navigation.findNavController(view!!).navigate(resId)
    }

    private fun populateDictFromLinkedPageSet() {
        linkedPageSet.forEach { entry ->
            val idString = getString(entry.key)
            dict[idString] = entry.value
        }
    }
}
