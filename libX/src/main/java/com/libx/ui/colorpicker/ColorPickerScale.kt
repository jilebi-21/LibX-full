package com.libx.ui.colorpicker

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.libx.ui.R
import kotlin.math.roundToInt

class ColorPickerScale @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs), SeekBar.OnSeekBarChangeListener {

    private val seekBar: SeekBar
    private val indicator: ColorPickerTrackerView

    var maxValue: Int = 100
        set(value) {
            field = value * SUB_DIVISIONS
            seekBar.max = field
        }

    var value = 0f
        set(n) {
            field = n * SUB_DIVISIONS
            seekBar.post { seekBar.progress = field.roundToInt() }
        }

    var scaleType = HSBColorPicker.ScaleType.NONE

    fun interface OnScaleValueChangeListener {
        fun onScaleValueChanged(scale: ColorPickerScale, value: Float)
    }

    var gradientColors: IntArray = intArrayOf(Color.WHITE, Color.BLACK)
        set(value) {
            field = value
            bgDrawable.mutate()
            seekBar.progressDrawable = bgDrawable
        }

    private val bgDrawable: GradientDrawable
        get() {
            val height = resources.getDimensionPixelSize(R.dimen.color_picker_scale_height)
            val radius = resources.getDimension(R.dimen.radius_small)
            val trackerShadowRadius = resources.getDimensionPixelSize(R.dimen.color_picker_scale_stroke_size)
            val strokeColor = ResourcesCompat.getColor(resources, R.color.color_picker_scale_stroke_color, null)

            return GradientDrawable().apply {
                setSize(0, height)
                orientation = GradientDrawable.Orientation.LEFT_RIGHT
                cornerRadius = radius
                setStroke(trackerShadowRadius, strokeColor)
                colors = gradientColors
            }
        }

    var listener: OnScaleValueChangeListener? = null

    init {
        inflate(context, R.layout.color_picker_scale_layout, this)

        layoutParams = LayoutParams(LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        seekBar = findViewById(R.id.seekbar)
        indicator = findViewById(R.id.indicator)

        seekBar.progressDrawable = bgDrawable
        seekBar.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        val range = seekBar.max - seekBar.min
        val percent = progress.toFloat() / range

        val seekBarTrueWidth = seekBar.width - seekBar.paddingStart - seekBar.paddingEnd
        indicator.x = seekBar.paddingStart + seekBar.x + (seekBarTrueWidth * percent) - (indicator.width / 2)

        listener?.onScaleValueChanged(this, (maxValue / SUB_DIVISIONS) * percent)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    companion object {
        const val TAG = "ColorPickerScale"
        private const val SUB_DIVISIONS = 100
    }
}