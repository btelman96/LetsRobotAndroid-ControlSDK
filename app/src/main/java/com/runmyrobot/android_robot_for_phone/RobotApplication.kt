package com.runmyrobot.android_robot_for_phone

import android.app.Application

import com.github.hiteshsondhi88.libffmpeg.FFmpeg
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException

/**
 * Application class
 */
class RobotApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val ffmpeg = FFmpeg.getInstance(applicationContext)
        try {
            ffmpeg.loadBinary(object : LoadBinaryResponseHandler() {
                //TODO maybe catch some error to display to the user
            })
        } catch (e: FFmpegNotSupportedException) {
            e.printStackTrace()
        }

    }

    companion object {
        val cameraPass: String
            get() = BuildConfig.CAMERA_PASS
    }
}
