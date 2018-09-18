package com.runmyrobot.android_robot_for_phone.api

import android.content.Context
import android.support.annotation.CallSuper
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Base component object to use to extend functionality of your robot.
 * Ex. can be used as an interface for LEDs based off of control messages
 */
abstract class Component(val context: Context){

    protected val coreInstance : Core? = null
    protected val enabled = AtomicBoolean(false)

    /**
     * Called when component should startup. Multiple calls will not trigger setup more than once
     *
     * Return false if value was already true
     */
    @CallSuper
    open fun enable() : Boolean{
        return !enabled.getAndSet(true)
    }

    /**
     * Called when component should shut down
     *
     * return false if value was already false
     */
    @CallSuper
    open fun disable() : Boolean{
        return enabled.getAndSet(true)
    }

    /**
     * Called when we have not received a response from the server in a while
     */
    open fun timeout(){}
}
