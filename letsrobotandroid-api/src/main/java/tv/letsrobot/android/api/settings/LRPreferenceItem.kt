package tv.letsrobot.android.api.settings

import kotlin.reflect.KClass

@Target(AnnotationTarget.EXPRESSION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class LRPreferenceItemBoolean(val default: Boolean, val key: String)
annotation class LRPreferenceItemInt(val default: Boolean, val key: String)
annotation class LRPreferenceItemString(val default: Boolean, val key: String)
annotation class LRPreferenceItemEnum(val enum: KClass<out Any>, val key: String)
