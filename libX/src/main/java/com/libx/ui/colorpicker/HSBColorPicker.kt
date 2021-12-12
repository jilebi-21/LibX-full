package com.libx.ui.colorpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.libx.ui.R
import com.libx.ui.preference.Preference
import com.libx.ui.utils.roundTill
import kotlin.math.roundToInt

class HSBColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes),
    ColorPickerScale.OnScaleValueChangeListener {

    fun interface OnColorChangedListener {
        fun onColorChanged(color: Int)
    }

    enum class ScaleType {
        HUE, SATURATION, BRIGHTNESS, NONE
    }

    private val hueScale: ColorPickerScale
    private val saturationScale: ColorPickerScale
    private val brightnessScale: ColorPickerScale

    private val hsb = HSBColor()

    private val currentColorView: View
    private val newColorView: View

    private val hexView: TextView
    private val hueView: TextView
    private val satView: TextView
    private val briView: TextView

    private var listeners = ArrayList<OnColorChangedListener?>()

    init {
        inflate(context, R.layout.color_picker_layout, this)
        hueScale = findViewById(R.id.hue_scale)
        saturationScale = findViewById(R.id.sat_scale)
        brightnessScale = findViewById(R.id.val_scale)

        hueScale.scaleType = ScaleType.HUE
        hueScale.maxValue = HSBColor.HUE_MAX
        hueScale.gradientColors = hsb.hueArray
        hueScale.listener = this

        saturationScale.scaleType = ScaleType.SATURATION
        saturationScale.maxValue = HSBColor.SATURATION_MAX
        saturationScale.gradientColors = hsb.saturationArray
        saturationScale.listener = this

        brightnessScale.scaleType = ScaleType.BRIGHTNESS
        brightnessScale.maxValue = HSBColor.BRIGHTNESS_MAX
        brightnessScale.gradientColors = hsb.brightnessArray
        brightnessScale.listener = this

        currentColorView = findViewById(R.id.display_current_view)
        newColorView = findViewById(R.id.display_new_view)
        currentColorView.setOnClickListener {
            setColor(currentColorView.backgroundTintList?.defaultColor ?: Color.BLACK)
        }

        hexView = findViewById(R.id.hex_code)
        hueView = findViewById(R.id.hue_code)
        satView = findViewById(R.id.sat_code)
        briView = findViewById(R.id.val_code)
    }

    @SuppressLint("SetTextI18n")
    private fun updateColorDetailsView() {
        newColorView.backgroundTintList = ColorStateList.valueOf(color)

        val saturationPercent = hsb.saturation * 100 / HSBColor.SATURATION_MAX
        val brightnessPercent = hsb.brightness * 100 / HSBColor.BRIGHTNESS_MAX

        hexView.text = hex
        hueView.text = "${hsb.hue.roundTill(1)}\u00B0"
        satView.text = "${saturationPercent.roundToInt()}%"
        briView.text = "${brightnessPercent.roundToInt()}%"
    }

    override fun onScaleValueChanged(scale: ColorPickerScale, value: Float) {
        Log.e(TAG, "${scale.scaleType}: $value")
        when (scale.scaleType) {
            ScaleType.HUE -> {
                hsb.hue = value
            }
            ScaleType.SATURATION -> {
                hsb.saturation = value
            }
            ScaleType.BRIGHTNESS -> {
                hsb.brightness = value
            }
            else -> {}
        }
        updateScalesColors()
        updateColorDetailsView()

        for (listener in listeners) {
            listener?.onColorChanged(color)
        }
    }

    private fun updateScalesColors() {
        hueScale.value = hsb.hue
        saturationScale.value = hsb.saturation
        brightnessScale.value = hsb.brightness
        saturationScale.gradientColors = hsb.saturationArray
        brightnessScale.gradientColors = hsb.brightnessArray
    }

    fun setColor(color: Int) {
        hsb.color = color
        currentColorView.backgroundTintList = ColorStateList.valueOf(color)
        newColorView.backgroundTintList = ColorStateList.valueOf(color)
        updateScalesColors()

        for (listener in listeners) {
            listener?.onColorChanged(color)
        }
    }

    val color: Int
        get() = hsb.color

    val hex: String
        get() = String.format("#%06X", 0xFFFFFF and color)

    fun addOnColorChangedListener(listener: OnColorChangedListener?) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    companion object {
        private const val TAG = "ColorPicker"
    }
}
