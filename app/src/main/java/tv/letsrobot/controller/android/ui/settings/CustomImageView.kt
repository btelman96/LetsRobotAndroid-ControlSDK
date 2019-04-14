package tv.letsrobot.controller.android.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView

/**
 * Image View that follows other UI behavior if disabled
 */
class CustomImageView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        imageAlpha = if(enabled) 0xFF else 0x3F
    }
}