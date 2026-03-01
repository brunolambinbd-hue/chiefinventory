package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ActivityInventoryScannerBinding
import com.example.chiefinventory.ui.viewmodel.InventoryViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.example.chiefinventory.utils.BitmapUtils
import com.example.chiefinventory.utils.ImageCaptureUtil

/**
 * Activity for the Inventory Scanner feature.
 */
class InventoryScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryScannerBinding
    private lateinit var imageCaptureUtil: ImageCaptureUtil
    private var locationId: Long = -1L

    private val viewModel: InventoryViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: ""

        if (locationId == -1L) {
            Toast.makeText(this, "Erreur : Emplacement non valide", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        supportActionBar?.title = "Scanner pour : $locationName"
        binding.tvLocationName.text = getString(R.string.inventory_for_location, locationName)

        imageCaptureUtil = ImageCaptureUtil(this) { permanentUri ->
            if (permanentUri != null) {
                val bitmap = BitmapUtils.getBitmapFromUri(this, permanentUri)
                if (bitmap != null) {
                    viewModel.findSimilarItems(bitmap, permanentUri)
                } else {
                    Toast.makeText(this, "Erreur de décodage de l'image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Erreur lors de la capture de l'image", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnScanItem.setOnClickListener {
            imageCaptureUtil.start()
        }

        viewModel.similarItems.observe(this) { (similarItems, scannedImageUri) ->
            val intent = Intent(this, InventoryResultActivity::class.java).apply {
                putExtra(InventoryResultActivity.EXTRA_LOCATION_ID, locationId)
                putParcelableArrayListExtra(InventoryResultActivity.EXTRA_SIMILAR_ITEMS, ArrayList(similarItems))
                putExtra(InventoryResultActivity.EXTRA_SCANNED_IMAGE_URI, scannedImageUri.toString())
            }
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_LOCATION_ID: String = "location_id"
        const val EXTRA_LOCATION_NAME: String = "location_name"
    }
}
