package tv.letsrobot.android.api.components

import android.util.Log
import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import io.socket.client.IO
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
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

    @Test
    @Throws(IOException::class)
    fun testSocket() {
        val mSocket = IO.socket(
                String.format("http://%s:%s", "35.185.203.47", 3231)
        )
        val latch = CountDownLatch(1)
        mSocket.connect()
        mSocket.on(io.socket.client.Socket.EVENT_CONNECT){
            Log.d("TEST", "HIT")
            val json = JSONObject().also {
                val token  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6InVzZXItOTdjMjY0OWItMWU4NS00OTVlLTgzODktYWQ4OTYxNmRjZjI3IiwiaWF0IjoxNTYwNjM5MjI0LCJleHAiOjE1NjMyMzEyMjQsInN1YiI6IiJ9.OGUcUr313rAUL_s0oJNzIEIfb-9aTTJOunjCYKJfg4w"
                it.put("token", token)
            }
            mSocket.emit("AUTHENTICATE", json)
        }.on(io.socket.client.Socket.EVENT_CONNECT_ERROR){
            Log.d("TEST", "HIT")
        }.on(io.socket.client.Socket.EVENT_MESSAGE){
            Log.d("TEST", "HIT")
        }.on("BUTTON_COMMAND"){
            Log.d("TEST", "HIT")
        }.on("VALIDATED"){
            Log.d("TEST", "HIT")
            mSocket.emit("GET_CHAT", "chat-0cedd0b8-cc04-4fef-af04-6e9a049713ab")
        }.on("HEARTBEAT"){
            Log.d("TEST", "HIT")
        }.on("SEND_ROBOT_SERVER_INFO"){
            Log.d("TEST", "HIT")
        }.on("SUBBED TO CHAT EVENTS"){
            Log.d("TEST", "HIT")
        }.on("SEND_ROBOT_SERVER_INFO"){
            Log.d("TEST", "HIT")
        }.on("SEND_ROBOT_SERVER_INFO"){
            Log.d("TEST", "HIT")
        }
        latch.await()
    }
}
