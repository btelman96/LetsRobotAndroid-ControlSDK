package tv.letsrobot.controller.android.activities.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import tv.letsrobot.android.api.utils.RobotConfig
import tv.letsrobot.android.api.utils.getEntries
import tv.letsrobot.android.api.utils.getEntryValues
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.ui.settings.ListSettingsPreference

class SettingsRobot : BasePreferenceFragmentCompat(
        R.xml.settings_robot,
        R.string.robotSettingsEnableKey
){
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connPref = findPreference<ListSettingsPreference>(getString(R.string.robotConnectionType))
        val protoPref = findPreference<ListSettingsPreference>(getString(R.string.robotProtocolType))
        createFromDefaultAndListen(connPref, RobotConfig.Communication)
        createFromDefaultAndListen(protoPref, RobotConfig.Protocol)
        connPref?.setOnClickListener {

        }

        protoPref?.setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
    }

    private fun createFromDefaultAndListen(pref : ListPreference?, key : RobotConfig){
        pref ?: return //skip if null
        val enumValue = (key.getValue(context!!) as Enum<*>)
        pref.entries = enumValue.getEntries()
        pref.entryValues = enumValue.getEntryValues()
        pref.value = enumValue.name
        pref.setOnPreferenceChangeListener { _, newValue ->
            val any = searchForEnum(newValue, enumValue)
            any?.let { key.saveValue(context!!, it) }
            true
        }
    }

    private fun searchForEnum(newValue: Any?, enumValue: Enum<*>): Enum<*>? {
        for(value in enumValue::class.java.enumConstants){
            if(value.name == newValue)
                return value
        }
        return null
    }
}
