package com.libx.ui.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.textview.MaterialTextView
import com.libx.ui.R
import kotlin.math.abs

class ToolbarLayout(
    context: Context,
    attrs: AttributeSet?
) : LinearLayout(context, attrs) {

    private var expandable: Boolean
    private var isExpanded: Boolean

    //Expanded views
    val appBarLayout: AppBarLayout?
    val collapsingToolbarLayout: CollapsingToolbarLayout?
    private val expandedLayout: LinearLayout?
    private val expandedTitleView: TextView?
    private val expandedSubTitleView: TextView?

    val toolbar: Toolbar
    private val collapsedTitleContainer: LinearLayout
    private val collapsedTitleView: MaterialTextView
    private val collapsedSubTitleView: MaterialTextView
    val switchBar: SwitchBar
    private val mainContainer: LinearLayout?

    private val footerContainer: FrameLayout?

    var title: CharSequence? = null
        set(value) {
            field = value
            expandedTitleView?.text = value
            collapsedTitleView.text = value
        }

    var subtitle: CharSequence? = null
        set(value) {
            field = value
            if (expandable) {
                expandedSubTitleView?.text = value
                expandedSubTitleView?.visibility = if (value != null) VISIBLE else GONE
                collapsedSubTitleView.visibility = GONE
            } else {
                collapsedSubTitleView.text = value
                collapsedSubTitleView.visibility = if (value != null) VISIBLE else GONE
            }
        }

    var navigationIcon: Drawable? = null
        set(value) {
            field = value
            collapsedTitleContainer.setPadding(
                if (field == null) context.resources.getDimension(R.dimen.toolbar_padding_start).toInt() else 0,
                0, 0, 0
            )
            toolbar.navigationIcon = value
        }

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.ToolBarLayout, 0, 0)
        expandable = typedArray.getBoolean(R.styleable.ToolBarLayout_expandable, true)
        isExpanded = typedArray.getBoolean(R.styleable.ToolBarLayout_expanded, false)

        val layoutRes = if (expandable) R.layout.toolbar_layout_expandable else R.layout.toolbar_layout_standard
        inflate(context, layoutRes, this)

        toolbar = findViewById(R.id.tl_toolbar)
        collapsedTitleView = findViewById(R.id.tl_collapsed_title)
        collapsedTitleContainer = findViewById(R.id.tl_collapsed_title_container)
        collapsedSubTitleView = findViewById(R.id.tl_collapsed_subtitle)
        switchBar = findViewById(R.id.tl_switchbar)
        mainContainer = findViewById(R.id.tl_main_container)
        footerContainer = findViewById(R.id.tl_footer)

        //These will be null if expandable is false
        appBarLayout = findViewById(R.id.tl_app_bar)
        collapsingToolbarLayout = findViewById(R.id.tl_collapsing_toolbar_layout)
        expandedLayout = findViewById(R.id.tl_expanded_layout)
        expandedTitleView = findViewById(R.id.tl_expanded_title)
        expandedSubTitleView = findViewById(R.id.tl_expanded_subtitle)

        navigationIcon = typedArray.getDrawable(R.styleable.ToolBarLayout_navigationIcon)
        title = typedArray.getString(R.styleable.ToolBarLayout_title)
        subtitle = typedArray.getString(R.styleable.ToolBarLayout_subtitle)
        typedArray.recycle()

        appBarLayout?.setExpanded(isExpanded)
        appBarLayout?.addOnOffsetChangedListener(CollapsedLayoutOffsetListener())
        appBarLayout?.addOnOffsetChangedListener(ExpandedLayoutOffsetListener())
        collapsedTitleContainer.alpha = 1.0f

        orientation = VERTICAL
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (mainContainer == null || footerContainer == null) {
            super.addView(child, index, params)
        } else {
            val lp = params as LayoutParams
            when (lp.layoutLocation) {
                1 -> footerContainer.addView(child, index, params)
                else -> mainContainer.addView(child, index, params)
            }
        }
    }

    fun setExpanded(expanded: Boolean, animate: Boolean) {
        isExpanded = expanded
        appBarLayout?.setExpanded(expanded, animate)
    }

    override fun generateDefaultLayoutParams(): LayoutParams {
        return LayoutParams(context, null)
    }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    inner class LayoutParams(
        context: Context?,
        attrs: AttributeSet?
    ) : LinearLayout.LayoutParams(context, attrs) {
        val layoutLocation: Int

        init {
            val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.ToolBarLayout_Layout)
            layoutLocation = typedArray?.getInteger(R.styleable.ToolBarLayout_Layout_layout_location, 0) ?: 0
            typedArray?.recycle()
        }
    }

    private inner class CollapsedLayoutOffsetListener : OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout, verticalOffset: Int) {
            val layoutPosition = abs(verticalOffset.toFloat())
            val alphaRange = collapsingToolbarLayout!!.height * 0.17999999f
            val toolbarTitleAlphaStart = collapsingToolbarLayout.height * 0.35f

            val totalHeight = resources.getDimension(R.dimen.toolbar_margin_top) +
                    context.theme.obtainStyledAttributes(intArrayOf(R.attr.actionBarSize)).getDimension(0, 0f) +
                    resources.getDimension(R.dimen.toolbar_margin_bottom)

            if (layout.height <= totalHeight) {
                collapsedTitleContainer.alpha = 1.0f
            } else {
                var collapsedTitleAlpha = 150.0f / alphaRange * (layoutPosition - toolbarTitleAlphaStart)
                when {
                    collapsedTitleAlpha in 0.0f..255.0f -> {
                        collapsedTitleAlpha /= 255.0f
                        collapsedTitleContainer.alpha = collapsedTitleAlpha
                    }
                    collapsedTitleAlpha < 0.0f -> collapsedTitleContainer.alpha = 0.0f
                    else -> collapsedTitleContainer.alpha = 1.0f
                }
            }
        }
    }

    private inner class ExpandedLayoutOffsetListener : OnOffsetChangedListener {
        override fun onOffsetChanged(layout: AppBarLayout, verticalOffset: Int) {
            val pos = abs(verticalOffset.toFloat()) / layout.totalScrollRange
            when {
                pos in 0.1f..0.70f -> {
                    val percentage = pos / (0.70f - 0.1f)
                    expandedLayout?.alpha = 1 - percentage
                }
                pos < 0.1f -> expandedLayout?.alpha = 1f
                else -> expandedLayout?.alpha = 0f
            }
        }
    }

    companion object {
        private const val TAG = "ToolbarLayout"

        private const val NO_FOOTER_ID = -2
    }
}