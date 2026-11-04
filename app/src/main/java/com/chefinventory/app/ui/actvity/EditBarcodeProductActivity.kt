package com.chefinventory.app.ui.actvity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.ActivityEditBarcodeProductBinding
import com.chefinventory.app.repository.OpenFoodFactsRepository
import com.chefinventory.app.ui.viewmodel.EditBarcodeProductViewModel

/**
 * Écran d'IHM dédié à la création et à la modification autonome d'un Produit Code-Barres Open Food Facts.
 */
class EditBarcodeProductActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_BARCODE = "EXTRA_BARCODE"
    }

    private lateinit var binding: ActivityEditBarcodeProductBinding
    private lateinit var viewModel: EditBarcodeProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBarcodeProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        val offRepository = OpenFoodFactsRepository(database.openFoodFactsDao())
        viewModel = EditBarcodeProductViewModel(offRepository)

        setupToolbar()
        setupListeners()
        observeViewModel()

        val barcode = intent.getStringExtra(EXTRA_BARCODE)
        if (!barcode.isNullOrBlank()) {
            binding.etBarcode.setText(barcode)
            viewModel.loadProduct(barcode)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupListeners() {
        binding.btnSaveProduct.setOnClickListener {
            val barcode = binding.etBarcode.text.toString()
            val productName = binding.etProductName.text?.toString()
            val brand = binding.etBrand.text?.toString()
            val quantityStr = binding.etQuantity.text?.toString()
            val nutriScore = binding.etNutriscore.text?.toString()
            val imageUrl = binding.etImageUrl.text?.toString()
            val ingredientsText = binding.etIngredientsText.text?.toString()

            viewModel.saveProduct(
                barcode = barcode,
                productName = productName,
                brand = brand,
                quantityStr = quantityStr,
                nutriScore = nutriScore,
                imageUrl = imageUrl,
                ingredientsText = ingredientsText
            )
        }
    }

    private fun observeViewModel() {
        viewModel.product.observe(this) { product ->
            if (product != null) {
                binding.etBarcode.setText(product.barcode)
                binding.etProductName.setText(product.productName ?: "")
                binding.etBrand.setText(product.brand ?: "")
                binding.etQuantity.setText(product.quantityStr ?: "")
                binding.etNutriscore.setText(product.nutriScore ?: "")
                binding.etImageUrl.setText(product.imageUrl ?: "")
                binding.etIngredientsText.setText(product.ingredientsText ?: "")
            }
        }

        viewModel.saveStatus.observe(this) { status ->
            if (!status.isNullOrBlank()) {
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
                if (status.contains("succès")) {
                    finish()
                }
                viewModel.clearSaveStatus()
            }
        }
    }
}
