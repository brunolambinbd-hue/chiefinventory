package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityInventoryResultBinding
import com.example.chiefinventory.ui.adapter.CollectionAdapter
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.chiefinventory.ui.viewmodel.InventoryViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory

/**
 * Activity to display the results of an inventory scan and allow the user to take action.
 */
class InventoryResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInventoryResultBinding
    private lateinit var adapter: CollectionAdapter
    private var locationId: Long = -1L
    private var scannedImageUri: String? = null

    private val viewModel: InventoryViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Résultats du Scan"

        locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        scannedImageUri = intent.getStringExtra(EXTRA_SCANNED_IMAGE_URI)
        
        val similarItems = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(EXTRA_SIMILAR_ITEMS, SearchResultItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(EXTRA_SIMILAR_ITEMS)
        }

        setupRecyclerView()
        adapter.submitList(similarItems)

        binding.btnCreateNew.setOnClickListener {
            val intent = Intent(this, EditItemActivity::class.java).apply {
                // On lance en mode création (pas d'itemId).
                putExtra(EditItemActivity.EXTRA_PREFILL_LOCATION_ID, locationId)
                putExtra(EditItemActivity.EXTRA_PREFILL_IMAGE_URI, scannedImageUri)
            }
            startActivity(intent)
            finish() // On ferme cet écran après l'action
        }
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            // Mettre à jour l'emplacement et le statut de l'objet
            viewModel.updateItemLocationAndStatus(searchResult.item.id, locationId)
            setResult(RESULT_OK)
            finish()
        }
        binding.rvInventoryResults.adapter = adapter
        binding.rvInventoryResults.layoutManager = LinearLayoutManager(this)
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
        const val EXTRA_SIMILAR_ITEMS: String = "similar_items"
        const val EXTRA_SCANNED_IMAGE_URI: String = "scanned_image_uri"
    }
}
