package com.libx.ui.colorpicker

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

class HSBColor() {
    @FloatRange(from = 0.0, to = 360.0)
    var hue: Float = 0f

    @FloatRange(from = 0.0, to = 255.0)
    var saturation: Float = 0f

    @FloatRange(from = 0.0, to = 255.0)
    var brightness: Float = 0f

    constructor(hue: Float, saturation: Float, brightness: Float) : this() {
        this.hue = hue
        this.saturation = saturation
        this.brightness = brightness
    }

    var color: Int
        @ColorInt get() {
            return Color.HSVToColor(
                floatArrayOf(
                    hue,
                    saturation / 255f,
                    brightness / 255f
                )
            )
        }
        set(value) {
            val hsv = FloatArray(3)
            Color.colorToHSV(value, hsv)

            hue = hsv[0]
            saturation = hsv[1] * 255
            brightness = hsv[2] * 255

        }

    val hueArray = intArrayOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED)

    val saturationArray: IntArray
        get() {
            return intArrayOf(Color.WHITE, HSBColor(hue, 255f, 255f).color)
        }

    val brightnessArray: IntArray
        get() {
            return intArrayOf(Color.BLACK, HSBColor(hue, saturation, 255f).color)
        }

    companion object {
        const val HUE_MAX = 360
        const val SATURATION_MAX = 255
        const val BRIGHTNESS_MAX = 255
    }
}