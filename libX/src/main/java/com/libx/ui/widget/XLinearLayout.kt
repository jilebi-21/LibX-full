package com.libx.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.libx.ui.R
import com.libx.ui.utils.RoundRectDrawable

class XLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val backgroundDrawable: RoundRectDrawable

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.XLinearLayout)
        val backgroundColor = typedArray.getColorStateList(R.styleable.XLinearLayout_backgroundColor)
        val cornerSize = typedArray.getDimension(R.styleable.XLinearLayout_cornerRadius, 0f)
        typedArray.recycle()

        clipToOutline = true
        backgroundDrawable = RoundRectDrawable(backgroundColor, cornerSize)
        ViewCompat.setBackground(this, backgroundDrawable)
    }

    override fun setBackgroundColor(color: Int) {
        backgroundDrawable.color = ColorStateList.valueOf(color)
    }
}