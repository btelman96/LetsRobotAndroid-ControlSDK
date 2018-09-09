package com.runmyrobot.android_robot_for_phone.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import com.runmyrobot.android_robot_for_phone.R
import com.runmyrobot.android_robot_for_phone.api.Core
import com.runmyrobot.android_robot_for_phone.utils.StoreUtil
import kotlinx.android.synthetic.main.activity_main_robot.*

/**
 * Main activity for the robot. It has a simple camera UI and a button to connect and disconnect.
 * For camera functionality, this activity needs to have a
 * SurfaceView to pass to the camera component via the Builder
 */
class MainRobotActivity : Activity(){
    private var recording = false
    var core: Core? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_main_robot)

        val builder = Core.Builder(applicationContext) //Initialize the Core Builder
        //Attach the SurfaceView holder to render the camera to
        builder.holder = cameraSurfaceView.holder
        builder.robotId = StoreUtil.getRobotId(this) //Pass in our Robot ID
        if(StoreUtil.getCameraEnabled(this)){
            builder.cameraId = StoreUtil.getCameraId(this) //Pass in our Camera ID
        }
        builder.useTTS = StoreUtil.getTTSEnabled(this)
        builder.useMic = StoreUtil.getMicEnabled(this)
        try {
            core = builder.build() //Retrieve the built Core instance
        } catch (e: Core.InitializationException) {
            e.printStackTrace()
        }
        recordButtonMain.setOnClickListener{
            if (recording) {
                recording = false
                if (core != null)
                    core!!.disable() //Disable core if we hit the button to disable recording
                Log.v(LOGTAG, "Recording Stopped")
            } else {
                recording = true
                if (core != null)
                    core!!.enable() //enable core if we hit the button to enable recording
                Log.v(LOGTAG, "Recording Started")
            }
        }
        settingsButtonMain.setOnClickListener {
            startActivity(Intent(this, ManualSetupActivity::class.java))
        }
    }

    override fun onPause() {
        super.onPause()
        core?.onPause()
    }

    override fun onResume() {
        super.onResume()
        core?.onResume()
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}