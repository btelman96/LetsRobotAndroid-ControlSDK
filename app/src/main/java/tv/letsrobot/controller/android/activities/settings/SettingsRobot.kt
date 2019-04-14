package tv.letsrobot.controller.android.activities.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.utils.RobotConfig
import tv.letsrobot.android.api.utils.getEntries
import tv.letsrobot.android.api.utils.getEntryValues
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.ui.settings.ListSettingsPreference

class SettingsRobot : BasePreferenceFragmentCompat(
        R.xml.settings_robot,
        R.string.robotSettingsEnableKey
){
    private var pendingResultCode: Int? = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connPref = findPreference<ListSettingsPreference>(getString(R.string.robotConnectionType))
        val protoPref = findPreference<ListSettingsPreference>(getString(R.string.robotProtocolType))
        createFromDefaultAndListen(connPref, RobotConfig.Communication)
        createFromDefaultAndListen(protoPref, RobotConfig.Protocol)
        connPref?.setOnClickListener {
            val enum = RobotConfig.Communication.getValue(context!!) as CommunicationType
            val clazz = enum.getInstantiatedClass
            pendingResultCode = clazz?.setupComponent(activity!!)
        }

        protoPref?.setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(pendingResultCode == resultCode)
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
    }

    private fun createFromDefaultAndListen(pref : ListPreference?, key : RobotConfig){
        pref ?: return //skip if null
        val enumValue = (key.getValue(context!!) as Enum<*>)
        pref.entries = enumValue.getEntries()
        pref.entryValues = enumValue.getEntryValues()
        pref.value = enumValue.name
        pref.setOnPreferenceChangeListener { preference, newValue ->
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
