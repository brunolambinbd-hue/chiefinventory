package com.example.chiefinventory.java_integration.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chiefinventory.utils.observeOnce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for the LiveDataExt.kt extension functions.
 *
 * These tests verify the behavior of LiveData extensions that interact with the Android Lifecycle.
 */
@RunWith(AndroidJUnit4::class)
class LiveDataExtInstrumentedTest {

    /**
     * This rule makes sure that LiveData updates happen synchronously in tests.
     */
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * Verifies that the [observeOnce] extension function correctly observes a value
     * only one time and then automatically removes the observer.
     */
    @Test
    fun observeOnce_shouldTriggerOnlyOnce(): Unit = runBlocking {
        // GIVEN: A MutableLiveData, a counter, and a TestLifecycleOwner.
        val liveData = MutableLiveData<String>()
        var callCount = 0
        val testLifecycleOwner = TestLifecycleOwner()

        // WHEN: We use observeOnce on the LiveData inside the main thread.
        withContext(Dispatchers.Main) {
            // We must start the lifecycle for the observer to be active.
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)

            liveData.observeOnce(testLifecycleOwner) {
                callCount++
            }

            // Post a first value, which should trigger the observer.
            liveData.value = "first_value"

            // Post a second value. This should NOT trigger the observer.
            liveData.value = "second_value"
        }

        // THEN: The observer should have been called exactly once.
        assertEquals("The observer should only be called once.", 1, callCount)

        // Clean up the lifecycle owner.
        withContext(Dispatchers.Main) {
            testLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }
}
