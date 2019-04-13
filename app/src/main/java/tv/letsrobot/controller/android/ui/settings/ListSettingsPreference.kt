package tv.letsrobot.controller.android.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.PreferenceViewHolder
import tv.letsrobot.controller.android.R

class ListSettingsPreference : TwoTargetListPreference {
    var imageView: ImageView? = null
        private set

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    override fun getSecondTargetResId(): Int {
        return R.layout.preference_widget_master_settings
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val widgetView = holder.findViewById(android.R.id.widget_frame)
        widgetView?.setOnClickListener{
            click?.invoke()
        }
    }

    var click : (() -> Unit)? = null

    fun setSettingsEnabled(enabled: Boolean) {
        imageView!!.isEnabled = enabled
    }

    fun setOnClickListener(onClick:() -> Unit) {
        click = onClick
    }
}
