package com.libx.ui.utils

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import kotlin.math.max

class AutoGridLayoutManager(
    context: Context?,
    private val columnWidth: Float
) : GridLayoutManager(context, 3) {

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        val total = width - paddingRight - paddingLeft
        val spanCount = max(1f, total / columnWidth).toInt()
        setSpanCount(spanCount)
        super.onLayoutChildren(recycler, state)
    }
}