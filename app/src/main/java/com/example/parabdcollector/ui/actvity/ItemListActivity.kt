package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityItemListBinding
import com.example.parabdcollector.ui.adapter.CollectionAdapter
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.ui.viewmodel.MainViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

/**
 * An activity that displays a filtered list of collection items.
 *
 * This activity's behavior is controlled by extras passed in its Intent. It can display:
 * - A list of items filtered by a specific super-category and category.
 * - A list of all possessed items.
 * - A list of all sought items.
 * - A list of all items in a specific location.
 */
class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var adapter: CollectionAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    /**
     * Initializes the activity, toolbar, and RecyclerView.
     * It determines which list of items to display based on the intent extras.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listType = intent.getIntExtra(EXTRA_LIST_TYPE, TYPE_POSSESSED)
        val superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)

        setupRecyclerView()

        when {
            locationId != -1L -> {
                // Display items for a specific location
                // Note: The title could be improved by fetching the location name.
                supportActionBar?.title = "Objets dans l'emplacement"
                viewModel.getItemsByLocationId(locationId).observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            superCategory != null && category != null -> {
                // Display items for a specific category and super-category
                supportActionBar?.title = category
                viewModel.getItemsBySuperCategoryAndCategory(superCategory, category, listType == TYPE_POSSESSED).observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            listType == TYPE_POSSESSED -> {
                supportActionBar?.title = "Mes Produits"
                viewModel.possessedItems.observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            else -> {
                supportActionBar?.title = "Mes Recherches"
                viewModel.soughtItems.observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
        }
    }

    /**
     * Initializes the RecyclerView and its adapter, and defines the item click behavior.
     */
    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", searchResult.item.id)
            startActivity(intent)
        }
        binding.rvItemList.adapter = adapter
        binding.rvItemList.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Handles the back arrow click in the toolbar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /** Key for the Int extra that determines the general list type (possessed or sought). */
        const val EXTRA_LIST_TYPE = "list_type"
        /** Key for the String extra that holds the super-category to filter by. */
        const val EXTRA_SUPER_CATEGORY = "super_category"
        /** Key for the String extra that holds the detailed category to filter by. */
        const val EXTRA_CATEGORY = "category"
        /** Key for the Long extra that holds the location ID to filter by. */
        const val EXTRA_LOCATION_ID = "location_id"

        /** Value for EXTRA_LIST_TYPE to show possessed items. */
        const val TYPE_POSSESSED = 1
        /** Value for EXTRA_LIST_TYPE to show sought items. */
        const val TYPE_SOUGHT = 2
    }
}
