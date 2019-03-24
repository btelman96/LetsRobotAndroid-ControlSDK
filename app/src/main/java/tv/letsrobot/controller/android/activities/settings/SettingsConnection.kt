package tv.letsrobot.controller.android.activities.settings

import tv.letsrobot.controller.android.R

/**
 * All of our desired settings pages
 */

class SettingsConnection : BasePreferenceFragmentCompat() {
    override fun getDesiredPreferencesFromResources(): Int {
        return R.xml.settings_connection
    }
}