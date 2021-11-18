package com.libx.ui.preference.internal

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.libx.ui.R

class PreferenceImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private var mMaxWidth = Int.MAX_VALUE
    private var mMaxHeight = Int.MAX_VALUE

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceImageView, defStyleAttr, 0)
        maxWidth = a.getDimensionPixelSize(R.styleable.PreferenceImageView_maxWidth, Int.MAX_VALUE)
        maxHeight = a.getDimensionPixelSize(R.styleable.PreferenceImageView_maxHeight, Int.MAX_VALUE)
        a.recycle()
    }

    override fun getMaxWidth(): Int {
        return mMaxWidth
    }

    override fun setMaxWidth(maxWidth: Int) {
        mMaxWidth = maxWidth
        super.setMaxWidth(maxWidth)
    }

    override fun getMaxHeight(): Int {
        return mMaxHeight
    }

    override fun setMaxHeight(maxHeight: Int) {
        mMaxHeight = maxHeight
        super.setMaxHeight(maxHeight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = widthMeasureSpec
        var height = heightMeasureSpec
        val widthMode = MeasureSpec.getMode(width)
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            val widthSize = MeasureSpec.getSize(width)
            val maxWidth = maxWidth
            if (maxWidth != Int.MAX_VALUE && (maxWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED)) {
                width = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST)
            }
        }
        val heightMode = MeasureSpec.getMode(height)
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            val heightSize = MeasureSpec.getSize(height)
            val maxHeight = maxHeight
            if (maxHeight != Int.MAX_VALUE && (maxHeight < heightSize || heightMode == MeasureSpec.UNSPECIFIED)) {
                height = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            }
        }
        super.onMeasure(width, height)
    }
}