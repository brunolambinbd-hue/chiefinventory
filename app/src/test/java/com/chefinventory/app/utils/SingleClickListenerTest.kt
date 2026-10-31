package com.chefinventory.app.utils

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SingleClickListenerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSingleClickListener_FirstClickAndRapidDoubleClickAndDebounceInterval() {
        var clickCount = 0
        val listener = SingleClickListener(debounceIntervalMs = 500L) {
            clickCount++
        }
        val view = View(context)

        // 1. First click
        listener.onClick(view)
        assertThat(clickCount).isEqualTo(1)

        // 2. Rapid second click within 200ms (should be debounced)
        ShadowSystemClock.advanceBy(Duration.ofMillis(200))
        listener.onClick(view)
        assertThat(clickCount).isEqualTo(1)

        // 3. Third click after 600ms (should succeed)
        ShadowSystemClock.advanceBy(Duration.ofMillis(600))
        listener.onClick(view)
        assertThat(clickCount).isEqualTo(2)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSingleClickListener_DefaultConstructorParameters() {
        var clickCount = 0
        val defaultListener = SingleClickListener(onSingleClick = { clickCount++ })
        val view = View(context)

        defaultListener.onClick(view)
        assertThat(clickCount).isEqualTo(1)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testSetOnSingleClickListener_ViewExtensionFunction() {
        var clicked = false
        val view = View(context)
        view.setOnSingleClickListener(debounceIntervalMs = 300L) {
            clicked = true
        }

        view.performClick()
        assertThat(clicked).isTrue()
    }
}
