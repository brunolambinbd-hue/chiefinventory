package com.example.parabdcollector.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Observes a LiveData object only once. After the first value is received,
 * the observer is automatically removed.
 */
fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, onChanged: (T) -> Unit) {
    // This cannot be a lambda because we need the 'this' reference to remove the observer.
    @Suppress("ObjectLiteralToLambda")
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            onChanged(value)
        }
    }
    observe(owner, observer)
}
