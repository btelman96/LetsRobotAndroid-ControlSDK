package org.btelman.prefs.annotation

import androidx.annotation.StringRes

/**
 * Created by Brendon on 5/25/2019.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class PreferenceString(@StringRes val id: Int = 0, val defaultObject: String = "")

