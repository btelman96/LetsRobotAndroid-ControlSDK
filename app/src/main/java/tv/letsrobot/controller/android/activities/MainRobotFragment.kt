package tv.letsrobot.controller.android.activities

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_main_robot.*
import tv.letsrobot.android.api.components.*
import tv.letsrobot.android.api.components.camera.CameraBaseComponent
import tv.letsrobot.android.api.enums.Operation
import tv.letsrobot.android.api.interfaces.Component
import tv.letsrobot.android.api.interfaces.IComponent
import tv.letsrobot.android.api.models.ServiceComponentGenerator
import tv.letsrobot.android.api.utils.PhoneBatteryMeter
import tv.letsrobot.android.api.viewModels.LetsRobotViewModel
import tv.letsrobot.controller.android.R
import tv.letsrobot.controller.android.RobotApplication
import tv.letsrobot.controller.android.robot.RobotSettingsObject

class MainRobotFragment : Fragment(), Runnable{
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_robot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        PhoneBatteryMeter.getReceiver(context!!.applicationContext) //Setup phone battery monitor TODO integrate with component
        handler = Handler(Looper.getMainLooper())
        settings = RobotSettingsObject.load(context!!)
        setupExternalComponents()
        setupApiInterface()
        setupButtons()
        initIndicators()
    }

    var components = ArrayList<IComponent>() //arraylist of core components

    override fun run() {
        if (recording){
            lrMainGroup.visibility = View.GONE
            hideSystemUI()
        }
    }

    private var recording = false
    lateinit var handler : Handler
    lateinit var settings : RobotSettingsObject
    private var letsRobotViewModel: LetsRobotViewModel? = null

    private val extComponents = ArrayList<Component>()

    private fun setupExternalComponents() {
        //add custom components here
        //Setup a custom component
        /*val audioList = CommandToMediaList().also {
            it.bindRawResourcesAudioFileToCommand("h", applicationContext, "horn") //R.raw.horn.mp3
        }
        val component = PlayAudioComponentExample(applicationContext, audioList)
        extComponents.add(component) //add to custom components list*/
    }

    private fun setupApiInterface() {
        letsRobotViewModel = LetsRobotViewModel.getObject(activity!!)
        letsRobotViewModel?.setServiceBoundListener(activity!!){ connected ->
            mainPowerButton.isEnabled = connected == Operation.OK
        }
        letsRobotViewModel?.setStatusObserver(activity!!){ serviceStatus ->
            mainPowerButton?.let {
                mainPowerButton.setTextColor(parseColorForOperation(serviceStatus))
                val isLoading = serviceStatus == Operation.LOADING
                mainPowerButton.isEnabled = !isLoading
                if(isLoading) return@setStatusObserver //processing command. Disable button
                recording = serviceStatus == Operation.OK
                if(recording && settings.screenTimeout)
                    startSleepDelayed()
            }
        }
    }

    fun parseColorForOperation(state : Operation) : Int{
        val color : Int = when(state){
            Operation.OK -> Color.GREEN
            Operation.NOT_OK -> Color.RED
            Operation.LOADING -> Color.YELLOW
            else -> Color.BLACK
        }
        return color
    }

    private fun setupButtons() {
        mainPowerButton.setOnClickListener{ //Hook up power button to start the connection
            toggleServiceConnection()
        }
        settingsButtonMain.setOnClickListener {
            launchSetupActivity()
        }

        lrChatView.setOnTouchListener { v, event ->
            handleSleepLayoutTouch()
            return@setOnTouchListener false
        }
    }

    private fun handleSleepLayoutTouch(): Boolean {
        if(settings.screenTimeout) {
            startSleepDelayed()
            showSystemUI()
        }
        return false
    }

    private fun launchSetupActivity() {
        letsRobotViewModel?.api?.disable()
        Navigation.findNavController(view!!).navigate(R.id.action_mainRobotFragment_to_settingsLanding)
    }

    private fun toggleServiceConnection() {
        if (recording) {
            components.forEach { component ->
                letsRobotViewModel?.api?.detachFromLifecycle(component)
            }
            letsRobotViewModel?.api?.disable()
        } else {
            letsRobotViewModel?.api?.reset()
            if(components.isEmpty()) {
                addDefaultComponents()
                components.addAll(extComponents)
            }
            components.forEach { component ->
                letsRobotViewModel?.api?.attachToLifecycle(component)
            }
            letsRobotViewModel?.api?.enable()
        }
    }

    private fun startSleepDelayed() {
        lrMainGroup.visibility = View.VISIBLE
        handler.removeCallbacks(this)
        handler.postDelayed(this, 10000) //10 second delay
    }

    private fun initIndicators() { //Indicators for Core Services
        cloudStatusIcon.setComponentInterface(MainSocketComponent::class.java.simpleName)
        cameraStatusIcon.setComponentInterface(CameraBaseComponent.EVENTNAME)
        robotStatusIcon.setComponentInterface(ControlSocketComponent::class.java.simpleName)
        micStatusIcon.setComponentInterface(AudioComponent::class.java.simpleName)
        ttsStatusIcon.setComponentInterface(ChatSocketComponent::class.java.simpleName)
        robotMotorStatusIcon.setComponentInterface(CommunicationComponent::class.java.simpleName)
        //To add more, add another icon to the layout file somewhere and pass in your component
    }

    private fun destroyIndicators() {
        cloudStatusIcon?.onDestroy()
        cameraStatusIcon?.onDestroy()
        robotStatusIcon?.onDestroy()
        micStatusIcon?.onDestroy()
        ttsStatusIcon?.onDestroy()
        robotMotorStatusIcon?.onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyIndicators()
        PhoneBatteryMeter.destroyReceiver(activity!!.applicationContext)
    }

    /**
     * Create the robot Core object. This will handle enabling all components on its own thread.
     * Core.Builder is the only way to create the Core to make sure settings do not change wile the robot is running
     */
    private fun addDefaultComponents() {
        val builder = ServiceComponentGenerator(activity!!.applicationContext) //Initialize the Core Builder
        //Attach the SurfaceView textureView to render the camera to
        builder.robotId = settings.robotId //Pass in our Robot ID

        (settings.cameraId).takeIf {
            settings.cameraEnabled
        }?.let{ cameraId ->
            builder.cameraSettings = RobotSettingsObject.buildCameraSettings(settings)
        }
        builder.useTTS = settings.enableTTS
        builder.useMic = settings.enableMic
        builder.protocol = settings.robotProtocol
        builder.communication = settings.robotCommunication
        try {
            components = builder.build()
        } catch (e: ServiceComponentGenerator.InitializationException) {
            RobotApplication.Instance.reportError(e) // Reports an initialization error to application
            e.printStackTrace()
        }
    }

//    TODO? override fun onWindowFocusChanged(hasFocus: Boolean) {
//        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) hideSystemUI()
//    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        activity!!.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        activity!!.window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
    }

    companion object {
        const val LOGTAG = "MainRobot"
    }
}