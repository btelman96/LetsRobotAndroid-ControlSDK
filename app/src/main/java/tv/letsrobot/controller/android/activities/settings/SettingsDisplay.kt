package tv.letsrobot.controller.android.activities.settings

import tv.letsrobot.controller.android.R

class SettingsDisplay : BasePreferenceFragmentCompat() {
    override fun getDesiredPreferencesFromResources(): Int {
        return R.xml.settings_display
    }
}
