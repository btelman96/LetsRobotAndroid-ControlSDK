package tv.letsrobot.controller.android.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import kotlinx.android.synthetic.main.activity_settings.*
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.ui.settings.SwitchBar

class SettingsActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        NavigationUI.setupActionBarWithNavController(this, NavHostFragment.findNavController(nav_host_fragment))
        NavHostFragment.findNavController(nav_host_fragment).addOnDestinationChangedListener (this)
    }

    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        if(destination.id == R.id.settingsEntryFragment){
            if(!first){
                startActivity(Intent(this, SplashScreen::class.java))
                finish()
            }
            first = false
        }
    }

    override fun onSupportNavigateUp() : Boolean {
        return NavHostFragment.findNavController(nav_host_fragment).navigateUp()
    }

    fun getSwitchBar(): SwitchBar {
        return switch_bar
    }

    companion object{
        fun getIntent(context: Context) : Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}
