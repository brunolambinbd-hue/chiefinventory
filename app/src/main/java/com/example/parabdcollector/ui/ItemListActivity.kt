package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityItemListBinding
import com.example.parabdcollector.model.SearchResultItem

class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var adapter: CollectionAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listType = intent.getIntExtra(EXTRA_LIST_TYPE, TYPE_POSSESSED)
        val superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)
        val category = intent.getStringExtra(EXTRA_CATEGORY)

        setupRecyclerView()

        when {
            superCategory != null && category != null -> {
                // On affiche les objets pour une catégorie et une super-catégorie données
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

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", searchResult.item.id)
            startActivity(intent)
        }
        binding.rvItemList.adapter = adapter
        binding.rvItemList.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_LIST_TYPE = "list_type"
        const val EXTRA_SUPER_CATEGORY = "super_category"
        const val EXTRA_CATEGORY = "category"

        const val TYPE_POSSESSED = 1
        const val TYPE_SOUGHT = 2
    }
}
