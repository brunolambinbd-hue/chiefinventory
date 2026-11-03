package com.chefinventory.app.utils

import android.os.SystemClock
import android.view.View

/**
 * An OnClickListener that prevents rapid double-clicks within [debounceIntervalMs] milliseconds.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
class SingleClickListener(
    private val debounceIntervalMs: Long = 600L,
    private val onSingleClick: (View) -> Unit
) : View.OnClickListener {

    private var lastClickTime: Long = -10000L

    override fun onClick(v: View) {
        val currentTime = SystemClock.elapsedRealtime()
        val isFirstClick = lastClickTime < 0L
        val isDebounced = currentTime - lastClickTime >= debounceIntervalMs
        if (isFirstClick || isDebounced) {
            lastClickTime = currentTime
            onSingleClick(v)
        }
    }
}

/**
 * Extension function to safely handle click events without rapid double-tap bugs.
 */
fun View.setOnSingleClickListener(debounceIntervalMs: Long = 600L, onSingleClick: (View) -> Unit) {
    setOnClickListener(SingleClickListener(debounceIntervalMs, onSingleClick))
}
