package com.libx.ui.preference

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.content.res.TypedArrayUtils
import com.libx.ui.R

@SuppressLint("RestrictedApi")
class LayoutPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {

    private val TAG = "LayoutPreference"
    private var rootView: View? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Preference)
        val layoutRes = TypedArrayUtils.getResourceId(
            typedArray, R.styleable.Preference_layout,
            R.styleable.Preference_android_layout, 0
        )
        if (layoutRes != 0) {
            setView(layoutRes)
        }
        typedArray.recycle()

        layoutResource = R.layout.layout_preference_frame
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        holder.itemView.isFocusable = isSelectable
        holder.itemView.isClickable = isSelectable

        val layout = holder.itemView as FrameLayout
        layout.removeAllViews()
        (rootView?.parent as ViewGroup?)?.removeView(rootView)
        layout.addView(rootView)
    }

    fun setView(@LayoutRes res: Int) {
        val inflater = LayoutInflater.from(context)
        setView(inflater.inflate(res, null, false))
    }

    fun setView(view: View?) {
        rootView = view
    }

    fun <T : View?> findViewById(id: Int): T? {
        return rootView?.findViewById(id)
    }
}