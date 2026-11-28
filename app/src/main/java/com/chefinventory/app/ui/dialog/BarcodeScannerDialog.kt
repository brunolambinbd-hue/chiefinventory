package com.chefinventory.app.ui.dialog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chefinventory.app.R
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.DialogBarcodeScannerBinding
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.ui.actvity.OpenFoodFactsProductDetailActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.parabd.lib.traceability.utils.LogManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Dialogue de scan de code-barres EAN13 depuis la barre supérieure avec le viseur graphique caméra et le sélecteur 2 Modes :
 * - Mode 🟢 Entrée / Rangement (Courses)
 * - Mode 🔴 Sortie / Cuisine (Consommation)
 */
object BarcodeScannerDialog {

    private const val TAG = "BarcodeScannerDialog"

    fun show(context: Context, coroutineScope: CoroutineScope): AlertDialog {
        val binding = DialogBarcodeScannerBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        var currentModeIsEntry = true

        binding.btnModePantryEntry.setOnClickListener {
            currentModeIsEntry = true
            showToastSafely(context, context.getString(R.string.toast_mode_pantry_entry), Toast.LENGTH_SHORT)
        }

        binding.btnModePantryExit.setOnClickListener {
            currentModeIsEntry = false
            showToastSafely(context, context.getString(R.string.toast_mode_pantry_exit), Toast.LENGTH_SHORT)
        }

        binding.btnStartCamera.setOnClickListener {
            dialog.dismiss()
            launchCameraScanner(context, coroutineScope, currentModeIsEntry)
        }

        binding.btnSearchBarcode.setOnClickListener {
            val barcode = binding.etManualBarcode.text.toString().trim()
            if (barcode.isBlank()) {
                showToastSafely(context, context.getString(R.string.toast_please_enter_barcode), Toast.LENGTH_SHORT)
                return@setOnClickListener
            }

            dialog.dismiss()
            processBarcodeScan(context, coroutineScope, barcode, currentModeIsEntry)
        }

        dialog.show()
        return dialog
    }

    internal var cameraScannerLauncher: ((Context, GmsBarcodeScanner?, Boolean) -> Unit)? = null
    internal var scannerClientProvider: ((Context, GmsBarcodeScannerOptions) -> GmsBarcodeScanner)? = null

    internal fun launchCameraScanner(
        context: Context,
        coroutineScope: CoroutineScope,
        isEntryMode: Boolean
    ) {
        val customLauncher = cameraScannerLauncher
        if (customLauncher != null) {
            customLauncher(context, null, isEntryMode)
            return
        }

        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A)
                .enableAutoZoom()
                .build()

            val provider = scannerClientProvider
            val scanner = if (provider != null) provider(context, options) else GmsBarcodeScanning.getClient(context, options)

            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        processBarcodeScan(context, coroutineScope, rawValue, isEntryMode)
                    }
                }
                .addOnFailureListener { e ->
                    showToastSafely(context, context.getString(R.string.toast_camera_scan_cancelled, e.message ?: ""), Toast.LENGTH_SHORT)
                }
        } catch (e: Throwable) {
            showToastSafely(context, context.getString(R.string.toast_camera_viewfinder_error, e.message ?: ""), Toast.LENGTH_SHORT)
        }
    }

    internal var databaseProvider: ((Context) -> AppDatabase)? = null
    internal var repositoryProvider: ((AppDatabase) -> OpenFoodFactsRepository)? = null
    internal var activityLauncher: ((Context, Intent) -> Unit)? = null
    internal var toastShower: ((Context, String, Int) -> Unit)? = null

    private fun showToastSafely(context: Context, text: String, duration: Int) {
        val shower = toastShower ?: { ctx, txt, dur -> Toast.makeText(ctx, txt, dur).show() }
        shower(context, text, duration)
    }

    internal fun processBarcodeScan(
        context: Context,
        coroutineScope: CoroutineScope,
        barcode: String,
        isEntryMode: Boolean,
        customDatabase: AppDatabase? = null
    ) {
        coroutineScope.launch {
            val database = customDatabase ?: databaseProvider?.invoke(context) ?: AppDatabase.getDatabase(context)
            val offRepo = repositoryProvider?.invoke(database) ?: OpenFoodFactsRepository(database.openFoodFactsDao())

            if (isEntryMode) {
                // MODE ENTRÉE (COURSES & RANGEMENT) -> Recherche OFF et ouverture fiche produit
                val product = offRepo.getProductByBarcode(barcode)
                withContext(Dispatchers.Main) {
                    try {
                        val intent = Intent(context, OpenFoodFactsProductDetailActivity::class.java).apply {
                            putExtra(OpenFoodFactsProductDetailActivity.EXTRA_BARCODE, barcode)
                            if (product != null) {
                                putExtra(OpenFoodFactsProductDetailActivity.EXTRA_CLEAN_NAME, product.productName)
                            }
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        val launcher = activityLauncher ?: { ctx, itnt -> ctx.startActivity(itnt) }
                        launcher(context, intent)
                    } catch (e: Exception) {
                        LogManager.e(TAG, "Erreur lors du lancement de l'activité produit", e)
                    }
                }
            } else {
                // MODE SORTIE (CUISINE & CONSOMMATION) -> Recherche dans le stock local et décrémentation
                val ingredientDao = database.ingredientDao()
                val localMatches = ingredientDao.getAllSync()
                val match = localMatches.firstOrNull { 
                    it.name.contains(barcode, ignoreCase = true) || it.description?.contains(barcode, ignoreCase = true) == true 
                }

                if (match != null) {
                    val currentQty = match.quantity ?: 0.0
                    val newQuantity = maxOf(0.0, currentQty - 1.0)
                    val updated = match.copy(quantity = newQuantity)
                    ingredientDao.update(updated)
                    withContext(Dispatchers.Main) {
                        val unitStr = match.unit ?: context.getString(R.string.unit_default_single)
                        val msg = context.getString(R.string.toast_product_consumed_format, match.name, newQuantity.toString(), unitStr)
                        showToastSafely(context, msg, Toast.LENGTH_LONG)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        try {
                            val intent = Intent(context, OpenFoodFactsProductDetailActivity::class.java).apply {
                                putExtra(OpenFoodFactsProductDetailActivity.EXTRA_BARCODE, barcode)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val launcher = activityLauncher ?: { ctx, itnt -> ctx.startActivity(itnt) }
                            launcher(context, intent)
                        } catch (e: Exception) {
                            LogManager.e(TAG, "Erreur lors du lancement de l'activité produit", e)
                        }
                    }
                }
            }
        }
    }
}
