package com.chefinventory.app.ui.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class InventoryViewModelTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testInventoryViewModel_InstantiatesSuccessfully() {
        val viewModel = InventoryViewModel(app)
        assertThat(viewModel.getApplication<Application>()).isEqualTo(app)
    }
}
