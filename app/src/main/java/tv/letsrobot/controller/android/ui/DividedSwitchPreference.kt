package tv.letsrobot.controller.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Checkable
import android.widget.CompoundButton
import android.widget.Switch
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import tv.letsrobot.controller.android.R

/**
 * Created by Brendon on 3/21/2019.
 */
class DividedSwitchPreference : SwitchPreference {
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?) : super(context)
    init {
        layoutResource = R.layout.custom_switch
    }



    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        val switchView = holder?.findViewById(android.R.id.switch_widget)
        syncSwitchView(switchView!!)
        syncSummaryView(holder)
    }

    private val mListener = Listener()

    private inner class Listener internal constructor() : CompoundButton.OnCheckedChangeListener {

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            if (!callChangeListener(isChecked)) {
                // Listener didn't like it, change it back.
                // CompoundButton will make sure we don't recurse.
                buttonView.isChecked = !isChecked
                return
            }

            this@DividedSwitchPreference.isChecked = isChecked
        }
    }

    private fun syncSwitchView(view: View) {
        if (view is Switch) {
            view.setOnCheckedChangeListener(null)
        }
        if (view is Checkable) {
            (view as Checkable).isChecked = mChecked
        }
        if (view is Switch) {
            view.textOn = switchTextOn
            view.textOff = switchTextOff
            view.setOnCheckedChangeListener(mListener)
        }
    }
}