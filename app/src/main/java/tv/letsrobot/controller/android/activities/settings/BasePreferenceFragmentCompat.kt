package tv.letsrobot.controller.android.activities.settings

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.activities.SettingsActivity

/**
 * Fragment that will handle preferences logic.
 * Giving it a second parameter will make it automatically handle a master switch
 */
abstract class BasePreferenceFragmentCompat(
        @XmlRes val preferencesXmlId : Int,
        @StringRes val switchBarKeyStringId : Int = -1
) : PreferenceFragmentCompat() {

    open fun getSwitchBarOnText() : Int{
        return R.string.switch_on_text
    }

    open fun getSwitchBarOffText() : Int{
        return R.string.switch_off_text
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferencesXmlId, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        evalSwitchBar()
    }

    private fun evalSwitchBar(){
        val switchBar = (activity as SettingsActivity).getSwitchBar()
        if(switchBarKeyStringId == -1){
            switchBar.hide()
            return
        }

        switchBar.setSwitchBarText(
                getSwitchBarOnText(),
                getSwitchBarOffText())
        switchBar.setPrefsKey(getString(switchBarKeyStringId))
        switchBar.show()
    }
}