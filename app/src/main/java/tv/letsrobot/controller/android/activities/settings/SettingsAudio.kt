package tv.letsrobot.controller.android.activities.settings

import android.os.Bundle
import android.view.View
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.activities.SettingsActivity

class SettingsAudio : BasePreferenceFragmentCompat() {
    override fun getDesiredPreferencesFromResources(): Int {
        return R.xml.settings_audio
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switchBar = (activity as SettingsActivity).getSwitchBar()
        switchBar.setSwitchBarText(
                R.string.switch_on_text,
                R.string.switch_off_text)
        switchBar.setPrefsKey("audioSettingsEnable")
        switchBar.show()
    }
}