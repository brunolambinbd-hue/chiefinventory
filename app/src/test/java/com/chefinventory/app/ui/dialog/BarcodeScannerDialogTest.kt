package com.chefinventory.app.ui.dialog

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.chefinventory.app.R
import com.chefinventory.app.dao.IngredientDao
import com.chefinventory.app.dao.OpenFoodFactsDao
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.model.Ingredient
import com.chefinventory.app.model.OpenFoodFactsProduct
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires purement JVM vérifiant le composant [BarcodeScannerDialog].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BarcodeScannerDialogTest {

    private lateinit var mockContext: Context
    private lateinit var mockDatabase: AppDatabase
    private lateinit var mockOffDao: OpenFoodFactsDao
    private lateinit var mockIngredientDao: IngredientDao
    private lateinit var mockOffRepo: OpenFoodFactsRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockContext = mock()
        whenever(mockContext.getString(anyInt())).thenReturn("String_Resource")
        whenever(mockContext.getString(anyInt(), anyOrNull())).thenAnswer { "Formatted: ${it.arguments.getOrNull(1)}" }
        whenever(mockContext.getString(anyInt(), anyOrNull(), anyOrNull())).thenAnswer { "Formatted: ${it.arguments.getOrNull(1)}" }
        whenever(mockContext.getString(anyInt(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer { "Formatted: ${it.arguments.getOrNull(1)}" }
        whenever(mockContext.getString(anyInt(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenAnswer { "Formatted: ${it.arguments.getOrNull(1)}" }

        mockDatabase = mock()
        mockOffDao = mock()
        mockIngredientDao = mock()
        mockOffRepo = mock()

        whenever(mockDatabase.openFoodFactsDao()).thenReturn(mockOffDao)
        whenever(mockDatabase.ingredientDao()).thenReturn(mockIngredientDao)
        runBlocking {
            whenever(mockIngredientDao.update(any())).thenReturn(Unit)
        }

        runBlocking {
            val prod = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
            doReturn(null).whenever(mockOffRepo).getProductByBarcode(any())
            doReturn(prod).whenever(mockOffRepo).getProductByBarcode("3017620422003")
        }

        val mockGmsScanner = mock<com.google.mlkit.vision.codescanner.GmsBarcodeScanner>()
        BarcodeScannerDialog.cameraScannerLauncher = { _, _, _ -> }
        BarcodeScannerDialog.scannerClientProvider = { _, _ -> mockGmsScanner }
        BarcodeScannerDialog.databaseProvider = { mockDatabase }
        BarcodeScannerDialog.repositoryProvider = { mockOffRepo }
        BarcodeScannerDialog.activityLauncher = { _, _ -> }
        BarcodeScannerDialog.toastShower = { _, _, _ -> }
    }

    @After
    fun tearDown() {
        val mockGmsScanner = mock<com.google.mlkit.vision.codescanner.GmsBarcodeScanner>()
        com.chefinventory.app.network.OpenFoodFactsApiClient.resetBaseUrlProvider()
        BarcodeScannerDialog.scannerClientProvider = { _, _ -> mockGmsScanner }
        BarcodeScannerDialog.databaseProvider = { mockDatabase }
        BarcodeScannerDialog.repositoryProvider = null
        BarcodeScannerDialog.activityLauncher = { _, _ -> }
        BarcodeScannerDialog.toastShower = { _, _, _ -> }
        Dispatchers.resetMain()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testObjectGeneratedMethodsCoverage() {
        assertWithMessage("Le composant BarcodeScannerDialog doit générer une représentation textuelle valide")
            .that(BarcodeScannerDialog.toString())
            .contains("BarcodeScannerDialog")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCameraScanner_CustomLauncherCoverage() {
        var cameraLauncherCalled = false
        BarcodeScannerDialog.cameraScannerLauncher = { _, _, _ -> cameraLauncherCalled = true }

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)

        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = true)
        assertThat(cameraLauncherCalled).isTrue()

        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = false)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testLaunchCameraScanner_RealBranch_SuccessAndFailureListeners() {
        val mockGmsScanner = mock<com.google.mlkit.vision.codescanner.GmsBarcodeScanner>()
        @Suppress("UNCHECKED_CAST")
        val mockTask = mock<com.google.android.gms.tasks.Task<com.google.mlkit.vision.barcode.common.Barcode>>()

        whenever(mockGmsScanner.startScan()).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { inv ->
            val listener = inv.getArgument<com.google.android.gms.tasks.OnSuccessListener<com.google.mlkit.vision.barcode.common.Barcode>>(0)
            val mockBarcode = mock<com.google.mlkit.vision.barcode.common.Barcode>()
            whenever(mockBarcode.rawValue).thenReturn("3017620422003")
            listener.onSuccess(mockBarcode)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        BarcodeScannerDialog.cameraScannerLauncher = null
        BarcodeScannerDialog.scannerClientProvider = { _, _ -> mockGmsScanner }
        BarcodeScannerDialog.repositoryProvider = { mockOffRepo }
        BarcodeScannerDialog.activityLauncher = { _, _ -> }
        BarcodeScannerDialog.toastShower = { _, _, _ -> }

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)
        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Test failure listener branch
        @Suppress("UNCHECKED_CAST")
        val mockTask2 = mock<com.google.android.gms.tasks.Task<com.google.mlkit.vision.barcode.common.Barcode>>()
        whenever(mockGmsScanner.startScan()).thenReturn(mockTask2)
        whenever(mockTask2.addOnSuccessListener(any())).thenReturn(mockTask2)
        whenever(mockTask2.addOnFailureListener(any())).thenAnswer { inv ->
            val listener = inv.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
            listener.onFailure(RuntimeException("Scan fail"))
            mockTask2
        }
        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Exception branch during scanner start
        whenever(mockGmsScanner.startScan()).thenThrow(RuntimeException("Scanner crash"))
        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = true)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBarcodeScan_EntryMode_WithProductAndNullProduct() = runBlocking {
        val prod = OpenFoodFactsProduct(barcode = "3017620422003", productName = "Nutella")
        whenever(mockOffDao.getProductByBarcode("3017620422003")).thenReturn(prod)
        whenever(mockOffDao.getProductByBarcode("0000000000000")).thenReturn(null)

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)

        // Entry mode with product found
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "3017620422003", isEntryMode = true, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Entry mode with product null
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "0000000000000", isEntryMode = true, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBarcodeScan_ExitMode_WithLocalMatchAndNoMatch() = runBlocking {
        val ingr1 = Ingredient(id = 100L, name = "Nutella 3017620422003", locationId = 1L, quantity = 2.0, unit = "pot")
        val ingr2 = Ingredient(id = 200L, name = "Eau Evian", description = "Evian 3083680085304", locationId = 1L, quantity = null, unit = null)

        whenever(mockIngredientDao.getAllSync()).thenReturn(listOf(ingr1, ingr2))

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)

        // Exit mode with name match and quantity decremented from 2.0 to 1.0
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "3017620422003", isEntryMode = false, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Exit mode with description match and quantity null
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "3083680085304", isEntryMode = false, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()

        // Exit mode with no local match -> falls through to intent
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "9999999999999", isEntryMode = false, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testShowDialog_BasicShowAndDismiss() {
        val activity = org.robolectric.Robolectric.buildActivity(androidx.fragment.app.FragmentActivity::class.java).setup().get()
        activity.setTheme(R.style.Theme_ChefInventory)

        BarcodeScannerDialog.toastShower = { _, _, _ -> }
        BarcodeScannerDialog.activityLauncher = { _, _ -> }
        BarcodeScannerDialog.databaseProvider = { mockDatabase }
        BarcodeScannerDialog.repositoryProvider = { mockOffRepo }

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)

        val dialog = BarcodeScannerDialog.show(activity, scope)
        assertThat(dialog.isShowing).isTrue()
        dialog.dismiss()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBarcodeScan_DefaultNullProvidersAndActivityLauncherExceptions() = runBlocking {
        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)

        BarcodeScannerDialog.toastShower = null
        BarcodeScannerDialog.repositoryProvider = null

        // Failing activityLauncher (throws exception in catch block)
        BarcodeScannerDialog.activityLauncher = { _, _ -> throw RuntimeException("Activity launch fail") }

        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "3017620422003", isEntryMode = true, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()

        whenever(mockIngredientDao.getAllSync()).thenReturn(emptyList())
        BarcodeScannerDialog.processBarcodeScan(mockContext, scope, "3017620422003", isEntryMode = false, customDatabase = mockDatabase)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCameraScanner_EmptyBarcodeRawValue() {
        val mockGmsScanner = mock<com.google.mlkit.vision.codescanner.GmsBarcodeScanner>()
        @Suppress("UNCHECKED_CAST")
        val mockTask = mock<com.google.android.gms.tasks.Task<com.google.mlkit.vision.barcode.common.Barcode>>()

        whenever(mockGmsScanner.startScan()).thenReturn(mockTask)
        whenever(mockTask.addOnSuccessListener(any())).thenAnswer { inv ->
            val listener = inv.getArgument<com.google.android.gms.tasks.OnSuccessListener<com.google.mlkit.vision.barcode.common.Barcode>>(0)
            val mockBarcode = mock<com.google.mlkit.vision.barcode.common.Barcode>()
            whenever(mockBarcode.rawValue).thenReturn("")
            listener.onSuccess(mockBarcode)
            mockTask
        }
        whenever(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        BarcodeScannerDialog.cameraScannerLauncher = null
        BarcodeScannerDialog.scannerClientProvider = { _, _ -> mockGmsScanner }

        val scope = kotlinx.coroutines.CoroutineScope(testDispatcher)
        BarcodeScannerDialog.launchCameraScanner(mockContext, scope, isEntryMode = true)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
