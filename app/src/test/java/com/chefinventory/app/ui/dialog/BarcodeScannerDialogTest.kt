package com.chefinventory.app.ui.dialog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires de Type B vérifiant le dialogue de scan de code-barres [BarcodeScannerDialog].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BarcodeScannerDialogTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(com.chefinventory.app.R.style.Theme_ChefInventory)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBarcodeScannerDialog_ShowDialog() {
        BarcodeScannerDialog.show(context, CoroutineScope(Dispatchers.Main))
    }
}
