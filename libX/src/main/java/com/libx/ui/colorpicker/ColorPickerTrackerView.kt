package com.libx.ui.colorpicker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.libx.ui.R

class ColorPickerTrackerView(
    context: Context?,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val trackerWidth = resources.getDimension(R.dimen.color_picker_scale_tracker_width)
    private val trackerHeight = resources.getDimension(R.dimen.color_picker_scale_tracker_height)
    private val trackerStrokeWidth = resources.getDimension(R.dimen.color_picker_scale_tracker_stroke_width)
    private val trackerShadowRadius = resources.getDimension(R.dimen.color_picker_scale_tracker_stroke_shadow_radius)

    private val trackerColor =
        ResourcesCompat.getColor(resources, R.color.color_picker_scale_tracker_color, null)
    private val trackerShadowColor =
        ResourcesCompat.getColor(resources, R.color.color_picker_scale_tracker_shadow_color, null)

    private val paint = Paint()
    private val rect = RectF()

    init {
        paint.apply {
            color = trackerColor
            setShadowLayer(trackerShadowRadius, 0f, 0f, trackerShadowColor)
            style = Paint.Style.STROKE
            strokeWidth = trackerStrokeWidth
            isAntiAlias = true
        }
    }

    override fun onDraw(canvas: Canvas) {
        rect.left = (trackerStrokeWidth + trackerShadowRadius) / 2
        rect.top = (trackerStrokeWidth + trackerShadowRadius) / 2
        rect.right = width.toFloat() - rect.left
        rect.bottom = height.toFloat() - rect.top

        canvas.drawRoundRect(rect, width / 2f, width / 2f, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = 2 * (trackerStrokeWidth + trackerShadowRadius) + trackerWidth
        val height = 2 * paint.strokeWidth + trackerHeight + 2 * trackerShadowRadius
        setMeasuredDimension(width.toInt(), height.toInt())
    }
}