package com.libx.ui.views

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import com.libx.ui.R

class ToggleSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.switchStyle
) : SwitchCompat(context, attrs, defStyleAttr), CompoundButton.OnCheckedChangeListener {

    private val listeners = ArrayList<OnCheckedChangeListener?>()

    init {
        setOnCheckedChangeListener(this)
    }

    fun addOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnCheckedChangeListener(listener: OnCheckedChangeListener?) {
        if (listeners.contains(listener)) {
            listeners.remove(listener)
        }
    }

    fun removeAllListeners() {
        listeners.clear()
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        for (listener in listeners) {
            listener?.onCheckedChanged(buttonView, isChecked)
        }
    }
}