package com.libx.ui.preference

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.TypedArrayUtils
import com.libx.ui.R
import com.libx.ui.colorpicker.ColorPickerDialog
import com.libx.ui.colorpicker.HSBColorPicker.OnColorChangedListener
import com.libx.ui.preference.internal.PreferenceImageView

@SuppressLint("RestrictedApi")
class ColorPickerPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes),
    OnColorChangedListener,
    Preference.OnPreferenceClickListener {

    private var dialog: ColorPickerDialog? = null
    private val defaultFreakyColor = "#00f801"
    private var currentColor = Color.parseColor(defaultFreakyColor)

    private var previewTile: ImageView? = null
    private val previewDrawable: GradientDrawable?

    init {
        widgetLayoutResource = R.layout.preference_widget_color_picker
        onPreferenceClickListener = this

        previewDrawable = ResourcesCompat.getDrawable(
            context.resources,
            R.drawable.color_picker_preference_preview,
            context.theme
        ) as GradientDrawable?

        val typedArray = context.obtainStyledAttributes(
            attrs, R.styleable.ColorPickerPreference, defStyleAttr, defStyleRes
        )

        if (TypedArrayUtils.getBoolean(
                typedArray, R.styleable.ColorPickerPreference_useSimpleSummaryProvider,
                R.styleable.ColorPickerPreference_useSimpleSummaryProvider, false
            )
        ) {
            summaryProvider = ColorPickerSummaryProvider.instance
        }

        typedArray.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        previewTile = holder.findViewById(R.id.imageview_widget) as PreferenceImageView
        previewTile?.background = previewDrawable
    }

    override fun onColorChanged(color: Int) {
        setColor(color)
        if (onPreferenceChangeListener != null) {
            onPreferenceChangeListener.onPreferenceChange(this, color)
        }
    }

    private fun setColor(color: Int) {
        currentColor = color
        previewDrawable?.setColor(color)
        persistInt(color)
        callChangeListener(color)
        notifyChanged()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        var def = defaultValue
        if (defaultValue == null) {
            def = Color.BLACK
        }
        setColor(getPersistedInt(def as Int))
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        showDialog()
        return true
    }

    private fun showDialog(state: Bundle? = null) {
        dialog = ColorPickerDialog(context, currentColor)
        if (state != null) dialog?.onRestoreInstanceState(state)
        dialog?.addOnColorChangedListener(this)
        dialog?.show()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        //TODO: state handling
//        if (dialog == null || dialog?.isShowing == false) {
        return superState
//        }
//        val myState = SavedState(superState)
//        myState.dialogBundle = dialog?.onSaveInstanceState()
//        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        showDialog(state.dialogBundle)
    }

    private class SavedState : BaseSavedState {
        var dialogBundle: Bundle? = null

        constructor(source: Parcel) : super(source) {
            dialogBundle = source.readBundle()
        }

        constructor(superState: Parcelable?) : super(superState) {}

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBundle(dialogBundle)
        }

        companion object {
            @SuppressLint("ParcelCreator")
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    class ColorPickerSummaryProvider private constructor() : SummaryProvider<ColorPickerPreference> {
        override fun provideSummary(preference: ColorPickerPreference): CharSequence {
            return String.format("#%06X", 0xFFFFFF and preference.currentColor)
        }

        companion object {
            private var sSimpleSummaryProvider: ColorPickerSummaryProvider? = null
            val instance: ColorPickerSummaryProvider?
                get() {
                    if (sSimpleSummaryProvider == null) {
                        sSimpleSummaryProvider = ColorPickerSummaryProvider()
                    }
                    return sSimpleSummaryProvider
                }
        }
    }

    companion object {
        private const val TAG = "ColorPickerPreference"
    }
}