package tv.letsrobot.android.api.components

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * Specifically test just the robot control portion. No motors will move.
 *
 * This will connect to the LetsRobot api to ensure proper functionality and making sure that it
 * would send it to a motor
 *
 * To trigger events in the logs, please use the website controls on the robot page
 */
@RunWith(AndroidJUnit4::class)
class ControlSocketComponentAndroidTests {


    @Test
    fun Init() {
        val controllerComponent = ControlSocketComponent(InstrumentationRegistry.getTargetContext(), "") //TODO ROBOTID
        controllerComponent.enable()
        Assert.assertTrue(controllerComponent.running.get())
        var latch = CountDownLatch(1)
        try {
            latch.await(2, TimeUnit.MINUTES) //Wait for 2 Minutes
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        Assert.assertTrue(controllerComponent.connected) //Make sure socket is actually connected
        controllerComponent.disable() //Disable controller
        latch = CountDownLatch(1)
        try {
            //wait a little bit to make sure it had time to disconnect
            latch.await(2, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        Assert.assertFalse(controllerComponent.connected) //Make sure disable works
    }
}
