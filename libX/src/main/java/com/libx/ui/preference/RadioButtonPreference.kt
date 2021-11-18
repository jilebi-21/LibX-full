package com.libx.ui.preference

import android.content.Context
import android.util.AttributeSet
import com.libx.ui.R

class RadioButtonPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CheckBoxPreference(context, attrs, defStyleAttr) {

    fun interface OnClickListener {
        fun onRadioButtonClicked(radioPreference: RadioButtonPreference?)
    }

    private var listener: OnClickListener? = null

    init {
        layoutResource = R.layout.preference_radio
        isIconSpaceReserved = false
    }

    override fun onClick() {
        isChecked = true
        listener!!.onRadioButtonClicked(this)
    }

    fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }
}