package tv.letsrobot.controller.android.activities.settings


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.models.Licenses

class SettingsLicenseViewer : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_licenses, rootKey)
        Licenses.licenses.forEach { license ->
            val pref = Preference(context).also { preference ->
                preference.title = license.name
                preference.intent = Intent(Intent.ACTION_VIEW, Uri.parse(license.licenseLink))
            }
            preferenceScreen.addPreference(pref)
        }
    }
}
