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
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()

        val listType = intent.getStringExtra(EXTRA_LIST_TYPE)
        val superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)
        val category = intent.getStringExtra(EXTRA_CATEGORY)

        if (superCategory != null && category != null) {
            // On affiche les objets pour une catégorie spécifique
            val isPossessed = listType == TYPE_POSSESSED
            supportActionBar?.title = category
            viewModel.getItemsBySuperCategoryAndCategory(superCategory, category, isPossessed).observe(this) { items ->
                adapter.submitList(items.map { SearchResultItem(it) })
            }
        } else {
            // Comportement par défaut (si on arrive ici sans passer par la nouvelle navigation)
            if (listType == TYPE_POSSESSED) {
                supportActionBar?.title = "Mes Produits"
                viewModel.possessedItems.observe(this) { items ->
                    adapter.submitList(items.map { SearchResultItem(it) })
                }
            } else {
                supportActionBar?.title = "Mes Recherches"
                viewModel.soughtItems.observe(this) { items ->
                    adapter.submitList(items.map { SearchResultItem(it) })
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
        const val TYPE_POSSESSED = "possessed"
        const val TYPE_SOUGHT = "sought"
        const val EXTRA_SUPER_CATEGORY = "super_category"
        const val EXTRA_CATEGORY = "category"
    }
}