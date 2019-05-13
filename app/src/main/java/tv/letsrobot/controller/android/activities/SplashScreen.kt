package tv.letsrobot.controller.android.activities


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import tv.letsrobot.android.api.interfaces.CommunicationInterface
import tv.letsrobot.android.api.models.ServiceComponentGenerator
import tv.letsrobot.android.api.robot.CommunicationType
import tv.letsrobot.android.api.services.LetsRobotService
import tv.letsrobot.android.api.utils.RobotConfig
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.robot.RobotSettingsObject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SplashScreen : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup App before initializing anything, then go back to do permissions flow
        // and to do device setup
        if(RobotConfig.RobotId.getValue(context!!) as? String == "" ||
                (RobotConfig.RobotId.getValue(context!!) as? String) == ""){
            startSetup()
            Toast.makeText(context!!, "RobotID or CameraId need to be setup!", Toast.LENGTH_SHORT).show()
            return
        }

        ServiceComponentGenerator.initDependencies(context!!){
            activity!!.runOnUiThread{
                next()
            }
        }
    }

    private fun startSetup() {
        Navigation.findNavController(view!!).navigate(R.id.action_splashScreen_to_settingsLanding)
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
        ContextCompat.startForegroundService(context!!.applicationContext, Intent(context!!.applicationContext, LetsRobotService::class.java))
        Navigation.findNavController(view!!).navigate(R.id.action_splashScreen_to_mainRobotFragment, null, NavOptions.Builder()
                .setPopUpTo(R.id.splashScreen,
                        true).build())
    }

    /**
     * Show some setup error message. Allow the user to attempt setup again
     */
    private fun setupError() {
        Toast.makeText(context
                , "Something happened while trying to setup. Please try again"
                , Toast.LENGTH_LONG).show()
        RobotConfig.Configured.saveValue(context!!, false)
        startSetup()
    }

    private var pendingDeviceSetup: CommunicationInterface? = null

    private var pendingRequestCode: Int = -1

    private fun setupDevice(): Boolean? {
        val commType = RobotConfig.Communication.getValue(context!!) as CommunicationType?
        commType?.let {
            val clazz = it.getInstantiatedClass
            clazz?.let {
                return if(it.needsSetup(activity!!)){
                    val tmpCode = it.setupComponent(activity!!)
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
                it.receivedComponentSetupDetails(context!!, data)
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
            if(ContextCompat.checkSelfPermission(context!!, perm) != PackageManager.PERMISSION_GRANTED){
                permissionsToAccept.add(perm)
            }
        }

        return if(permissionsToAccept.isNotEmpty()){
            requestPermissions(
                    permissionsToAccept.toArray(Array(0) {""}),
                    requestCode)
            false
        }
        else{
            true
        }
    }

    private fun getCurrentRequiredPermissions() : ArrayList<String> {
        val list = ArrayList<String>()
        val settings = RobotSettingsObject.load(context!!)
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
