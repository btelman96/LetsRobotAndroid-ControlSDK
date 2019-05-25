package tv.letsrobot.controller.android.activities


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.models.ServiceComponentGenerator
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.services.LetsRobotService
import tv.letsrobot.android.api.settings.LRPreferences
import tv.letsrobot.controller.android.R

class SplashScreen : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_splash_screen)

        ServiceComponentGenerator.initDependencies(this){
            runOnUiThread{
                if(LRPreferences.INSTANCE.robotId.getValue<String>() == "" ||
                        (RobotConfig.RobotId.getValue(this) as? String) == ""){
                    startSetup()
                    Toast.makeText(this, "RobotID or CameraId need to be setup!", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }
                next()
            }
        }
    }

    private fun startSetup() {
        startActivity(SettingsActivity.getIntent(this))
    }

    private fun next() {
        //Check permissions. break out if that returns false
        if(!checkPermissions()){
            return
        }
        //Setup device. break out if not setup, or if error occurred
        setupDevice()?.let {
            if(!it){
                //setup not complete
                return
            }
        } ?: run{
            //Something really bad happened here. Not sure how we continue
            setupError()
            return
        }
        //All checks are done. Lets startup the activity!
        ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, LetsRobotService::class.java))
        startActivity(MainRobotActivity.getIntent(this))
        finish()
    }

    /**
     * Show some setup error message. Allow the user to attempt setup again
     */
    private fun setupError() {
        Toast.makeText(this
                , "Something happened while trying to setup. Please try again"
                , Toast.LENGTH_LONG).show()
        RobotConfig.Configured.saveValue(this, false)
        startSetup()
    }

    private var pendingDeviceSetup: CommunicationInterface? = null

    private var pendingRequestCode: Int = -1

    private fun setupDevice(): Boolean? {
        val commType = RobotConfig.Communication.getValue(this) as CommunicationType?
        commType?.let {
            val clazz = it.getInstantiatedClass
            clazz?.let {
                return if(it.needsSetup(this)){
                    val tmpCode = it.setupComponent(this)
                    //Sometimes we still need setup without a UI. Will return -1 if that is the case
                    if(tmpCode == -1){
                        true
                    }
                    else{
                        pendingRequestCode = tmpCode
                        pendingDeviceSetup = it
                        false
                    }
                } else{
                    true
                }
            }
        }
        return null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(checkPermissions()){
            next()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //Check if result was due to a pending interface setup
        pendingDeviceSetup?.takeIf { pendingRequestCode == requestCode}?.let {
            //relay info to interface
            if(resultCode != Activity.RESULT_OK) {
                startSetup() //not ok, exit to setup
            }
            else{
                it.receivedComponentSetupDetails(this, data)
                next()
            }
            pendingDeviceSetup = null
            pendingRequestCode = -1
        }
    }

    private val requestCode = 1002

    private fun checkPermissions() : Boolean{
        val permissionsToAccept = ArrayList<String>()
        for (perm in getCurrentRequiredPermissions()){
            if(ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED){
                permissionsToAccept.add(perm)
            }
        }

        return if(permissionsToAccept.isNotEmpty()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        permissionsToAccept.toArray(Array(0) {""}),
                        requestCode)
                false
            }
            else true
        }
        else{
            true
        }
    }

    private fun getCurrentRequiredPermissions() : ArrayList<String> {
        val list = ArrayList<String>()
        val settings = RobotSettingsObject.load(this)
        if(settings.enableMic){
            list.add(Manifest.permission.RECORD_AUDIO)
        }
        if(settings.cameraEnabled){
            list.add(Manifest.permission.CAMERA)
        }

        //location permission required to scan for bluetooth device
        if(settings.robotCommunication == CommunicationType.BluetoothClassic){
            list.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            list.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        return list
    }
}
