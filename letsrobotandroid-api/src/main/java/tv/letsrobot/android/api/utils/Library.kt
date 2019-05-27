package tv.letsrobot.android.api.utils

/**
 * Helper functions for various things
 */

/**
 * Get all of the names of all enums specified under one type as an array
 */
fun Enum<*>.getEntries() : Array<CharSequence>{
    val originList = this::class.java.enumConstants
    return Array(originList.size){
        originList[it].name
    }
}

