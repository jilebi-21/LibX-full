package com.libx.ui.colorpicker

import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Bundle
import com.libx.ui.colorpicker.HSBColorPicker.OnColorChangedListener
import com.libx.ui.dialog.XAlertDialog

class ColorPickerDialog(
    context: Context,
    initialColor: Int
) : XAlertDialog(context), OnColorChangedListener,
    DialogInterface.OnClickListener {

    private val TAG = "ColorPickerDialog"

    private val colorPicker: HSBColorPicker

    private var listeners = ArrayList<OnColorChangedListener?>()

    var color: Int = Color.BLACK
        get() = colorPicker.color
        set(value) {
            field = value
            colorPicker.setColor(value)
        }

    init {
        window?.setFormat(PixelFormat.RGBA_8888)

        colorPicker = HSBColorPicker(context)
        colorPicker.addOnColorChangedListener(this)
        color = initialColor

        setView(colorPicker)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this)
    }

    fun addOnColorChangedListener(listener: OnColorChangedListener?) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    override fun onColorChanged(color: Int) {}

    override fun onClick(dialog: DialogInterface, which: Int) {
        when (which) {
            BUTTON_POSITIVE -> {
                for (listener in listeners) {
                    listener?.onColorChanged(colorPicker.color)
                }
            }
            BUTTON_NEGATIVE -> listeners.clear()
        }
        dismiss()
    }

    override fun onSaveInstanceState(): Bundle {
        return super.onSaveInstanceState().apply {
            putInt("color", colorPicker.color)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.containsKey("color")) {
            val color = savedInstanceState.getInt("color")
            colorPicker.setColor(color)
        }
    }
}