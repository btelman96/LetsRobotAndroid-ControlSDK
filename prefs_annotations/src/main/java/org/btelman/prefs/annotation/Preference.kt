package org.btelman.prefs.annotation
import kotlin.reflect.KClass

/**
 * Created by Brendon on 5/25/2019.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Preference(val arg1: KClass<*>, val defaultObject: String = "")

