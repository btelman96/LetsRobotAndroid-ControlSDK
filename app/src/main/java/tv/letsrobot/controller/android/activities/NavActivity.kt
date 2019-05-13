package tv.letsrobot.controller.android.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_settings.*
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.ui.settings.SwitchBar

class NavActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        NavigationUI.setupActionBarWithNavController(this, NavHostFragment.findNavController(nav_host_fragment))
    }

    fun getSwitchBar(): SwitchBar {
        return switch_bar
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    override fun onSupportNavigateUp()
            = NavHostFragment.findNavController(nav_host_fragment).navigateUp()
}