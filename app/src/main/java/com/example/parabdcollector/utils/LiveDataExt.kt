package com.example.parabdcollector.utils

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
    // This cannot be a lambda because we need the 'this' reference to the Observer
    // in order to remove it after the first emission.
    @Suppress("ObjectLiteralToLambda")
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            // As soon as we get a value, we remove the observer.
            removeObserver(this)
            // And then we pass the value to the callback.
            onChanged(value)
        }
    }
    observe(owner, observer)
}
