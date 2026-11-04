package com.chefinventory.app.ui.dialog

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.DialogBarcodeScannerBinding
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.ui.actvity.OpenFoodFactsProductDetailActivity
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
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

    fun show(context: Context, coroutineScope: CoroutineScope) {
        val binding = DialogBarcodeScannerBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(true)
            .create()

        var currentModeIsEntry = true

        binding.btnModePantryEntry.setOnClickListener {
            currentModeIsEntry = true
            Toast.makeText(context, "Mode Entrée activé : scannez pour ranger vos courses", Toast.LENGTH_SHORT).show()
        }

        binding.btnModePantryExit.setOnClickListener {
            currentModeIsEntry = false
            Toast.makeText(context, "Mode Sortie activé : scannez pour consommer un produit", Toast.LENGTH_SHORT).show()
        }

        binding.btnStartCamera.setOnClickListener {
            dialog.dismiss()
            launchCameraScanner(context, coroutineScope, currentModeIsEntry)
        }

        binding.btnSearchBarcode.setOnClickListener {
            val barcode = binding.etManualBarcode.text.toString().trim()
            if (barcode.isBlank()) {
                Toast.makeText(context, "Veuillez saisir un code-barres EAN13", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            dialog.dismiss()
            processBarcodeScan(context, coroutineScope, barcode, currentModeIsEntry)
        }

        dialog.show()
    }

    private fun launchCameraScanner(
        context: Context,
        coroutineScope: CoroutineScope,
        isEntryMode: Boolean
    ) {
        try {
            val options = GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_UPC_A)
                .enableAutoZoom()
                .build()

            val scanner = GmsBarcodeScanning.getClient(context, options)

            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    val rawValue = barcode.rawValue
                    if (!rawValue.isNullOrBlank()) {
                        processBarcodeScan(context, coroutineScope, rawValue, isEntryMode)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Scan caméra annulé ou indisponible : ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(context, "Erreur d'ouverture du viseur caméra : ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun processBarcodeScan(
        context: Context,
        coroutineScope: CoroutineScope,
        barcode: String,
        isEntryMode: Boolean
    ) {
        coroutineScope.launch(Dispatchers.IO) {
            val database = AppDatabase.getDatabase(context)
            val offRepo = OpenFoodFactsRepository(database.openFoodFactsDao())

            if (isEntryMode) {
                // MODE ENTRÉE (COURSES & RANGEMENT) -> Recherche OFF et ouverture fiche produit
                val product = offRepo.getProductByBarcode(barcode)
                withContext(Dispatchers.Main) {
                    val intent = Intent(context, OpenFoodFactsProductDetailActivity::class.java).apply {
                        putExtra(OpenFoodFactsProductDetailActivity.EXTRA_BARCODE, barcode)
                        if (product != null) {
                            putExtra(OpenFoodFactsProductDetailActivity.EXTRA_CLEAN_NAME, product.productName)
                        }
                    }
                    context.startActivity(intent)
                }
            } else {
                // MODE SORTIE (CUISINE & CONSOMMATION) -> Recherche dans le stock local et décrémentation
                val ingredientDao = database.ingredientDao()
                val localMatches = ingredientDao.getAllSync()
                val match = localMatches.firstOrNull { 
                    it.name.contains(barcode, ignoreCase = true) || it.description?.contains(barcode, ignoreCase = true) == true 
                }

                withContext(Dispatchers.Main) {
                    if (match != null) {
                        val currentQty = match.quantity ?: 0.0
                        val newQuantity = maxOf(0.0, currentQty - 1.0)
                        val updated = match.copy(quantity = newQuantity)
                        coroutineScope.launch(Dispatchers.IO) {
                            ingredientDao.update(updated)
                        }
                        Toast.makeText(context, "🔴 Consommé : ${match.name} (Reste : $newQuantity ${match.unit ?: "unité"})", Toast.LENGTH_LONG).show()
                    } else {
                        val intent = Intent(context, OpenFoodFactsProductDetailActivity::class.java).apply {
                            putExtra(OpenFoodFactsProductDetailActivity.EXTRA_BARCODE, barcode)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}
