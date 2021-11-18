package com.libx.ui.preference

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.core.content.res.TypedArrayUtils
import com.libx.ui.R

@SuppressLint("RestrictedApi")
class RadioButtonPreferenceGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : PreferenceCategory(context, attrs), RadioButtonPreference.OnClickListener {

    private var initialCheckedKey: String?
    private var lastChecked: RadioButtonPreference? = null

    var listener: RadioButtonPreference.OnClickListener? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.RadioButtonPreferenceGroup)
        initialCheckedKey =
            TypedArrayUtils.getString(typedArray, R.styleable.RadioButtonPreferenceGroup_checkedPreferenceKey, 0)
        typedArray.recycle()
    }

    override fun addPreference(preference: Preference?): Boolean {
        val status = super.addPreference(preference)
        if (!status) return false

        if (preference !is RadioButtonPreference) {
            throw IllegalArgumentException("Only RadioButtonPreference objects are allowed in RadioButtonPreferenceGroup")
        }
        preference.isChecked = false
        if (!TextUtils.isEmpty(initialCheckedKey) && preference.key == initialCheckedKey) {
            lastChecked = preference
            lastChecked?.isChecked = true
        }

        return status
    }

    override fun onAttached() {
        super.onAttached()
        for (i in 0 until preferenceCount) {
            val preference = getPreference(i)
            if (preference !is RadioButtonPreference) continue
            preference.setOnClickListener(this)
        }
    }

    override fun onDetached() {
        super.onDetached()
        for (i in 0 until preferenceCount) {
            val preference = getPreference(i)
            if (preference is RadioButtonPreference) {
                preference.setOnClickListener(null)
            }
        }
    }

    override fun onRadioButtonClicked(radioPreference: RadioButtonPreference?) {
        if (lastChecked == radioPreference) return
        lastChecked?.isChecked = false
        lastChecked = radioPreference
        listener?.onRadioButtonClicked(radioPreference)
    }
}