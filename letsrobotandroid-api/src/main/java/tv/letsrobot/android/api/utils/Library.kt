package tv.letsrobot.android.api.utils

/**
 * Helper functions for various things
 */
fun Enum<*>.getEntryValues() : Array<CharSequence>{
    val originList = this::class.java.enumConstants
    return Array(originList.size){
        originList[it].name
    }
}

fun Enum<*>.getEntries() : Array<CharSequence>{
    val originList = this::class.java.enumConstants
    return Array(originList.size){
        originList[it].name
    }
}

