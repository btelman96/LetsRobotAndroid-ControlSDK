package tv.letsrobot.controller.android.activities.settings

import android.os.Bundle
import android.view.View
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.activities.SettingsActivity


class SettingsRobot : BasePreferenceFragmentCompat() {
    override fun getDesiredPreferencesFromResources(): Int {
        return R.xml.settings_robot
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as SettingsActivity).getSwitchBar().setSwitchBarText(
                R.string.robot_settings_master_switch_title,
                R.string.robot_settings_master_switch_title)
    }
}
