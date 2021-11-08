package com.jankku.alphawall.ui.common

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * GridLayoutManager which has quick `smoothScrollToPosition`
 * https://stackoverflow.com/a/36784136
 */
class FastGridLayoutManager(
    context: Context,
    spanCount: Int
) : GridLayoutManager(context, spanCount) {
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int
    ) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics) =
                MILLISECONDS_PER_INCH / displayMetrics.densityDpi
        }
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    companion object {
        private const val MILLISECONDS_PER_INCH = 5f // default is 25f (bigger = slower)
    }
}
