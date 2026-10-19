package com.example.chiefinventory.utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LiveDataExtTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var lifecycleOwner: LifecycleOwner

    @Before
    fun setup() {
        val customLifecycleOwner = object : LifecycleOwner {
            val registry = LifecycleRegistry(this).apply { handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
            override val lifecycle: Lifecycle get() = registry
        }
        lifecycleOwner = customLifecycleOwner
    }

    @Test
    fun `observeOnce triggers only once when data changes multiple times`() {
        val liveData = MutableLiveData<String>()
        val observedValues = mutableListOf<String>()

        liveData.observeOnce(lifecycleOwner) {
            observedValues.add(it)
        }

        liveData.value = "First"
        liveData.value = "Second"
        liveData.value = "Third"

        assertThat(observedValues).containsExactly("First")
    }
}
