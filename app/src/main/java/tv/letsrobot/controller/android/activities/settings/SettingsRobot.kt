package tv.letsrobot.controller.android.activities.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.settings.EnumPreference
import tv.letsrobot.android.api.settings.LRPreferences
import tv.letsrobot.android.api.utils.getEntries
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.ui.settings.ListSettingsPreference

class SettingsRobot : BasePreferenceFragmentCompat(
        R.xml.settings_robot,
        R.string.robotSettingsEnableKey
){
    private var pendingResultCode: Int? = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connPref = findPreference<ListSettingsPreference>(getString(R.string.robotConnectionTypeKey))
        val protoPref = findPreference<ListSettingsPreference>(getString(R.string.robotProtocolTypeKey))
        createFromDefaultAndListen(connPref, LRPreferences.INSTANCE.communication)
        createFromDefaultAndListen(protoPref, LRPreferences.INSTANCE.protocol)
        connPref?.setOnClickListener {
            val enum = LRPreferences.INSTANCE.communication.value
            val clazz = enum.getInstantiatedClass
            pendingResultCode = clazz?.setupComponent(activity!!)
        }

//        protoPref?.setOnClickListener {
//              TODO()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(pendingResultCode == resultCode)
            Toast.makeText(context, "Success", Toast.LENGTH_LONG).show()
    }

    private fun <T : Enum<*>> createFromDefaultAndListen(pref : ListPreference?, lrPreference: EnumPreference<T>){
        pref ?: return //skip if null
        val enumValue = lrPreference.value
        pref.entries = enumValue.getEntries()
        pref.entryValues = enumValue.getEntries()
        pref.value = enumValue.name
        maybeDisplayExpandedSetup(pref, enumValue)
        pref.setOnPreferenceChangeListener { preference, newValue ->
            val any = searchForEnum(newValue, enumValue)
            any?.let { lrPreference.saveValue(it) }
            maybeDisplayExpandedSetup(preference, enumValue)
            true
        }
    }

    private fun maybeDisplayExpandedSetup(preference: Preference?, enumValue : Enum<*>) {
        val settingsPref = preference as? ListSettingsPreference
        settingsPref?.let {
            it.hideSecondTarget = !hasSetup(enumValue)
        }
    }

    private fun hasSetup(enumValue : Enum<*>) : Boolean{
        return when(enumValue){
            is CommunicationType -> {
                val clazz = enumValue.getInstantiatedClass
                return clazz?.needsSetup(activity!!) == true //because result could be null
            }
//            RobotConfig.protocol -> TODO()
            else -> false
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
