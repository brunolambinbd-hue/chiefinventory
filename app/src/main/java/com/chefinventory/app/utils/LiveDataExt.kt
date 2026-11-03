package com.chefinventory.app.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Observes a [LiveData] object only once. After the first non-null value is received,
 * the observer is automatically removed.
 *
 * This is useful for one-shot operations that depend on a LiveData source, preventing
 * the observer from being triggered again on configuration changes or subsequent updates.
 *
 * @param T The type of the data held by the LiveData.
 * @param owner The [LifecycleOwner] which controls the observer.
 * @param onChanged The lambda function to be executed when the data is received.
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            onChanged(value)
        }
    })
}
