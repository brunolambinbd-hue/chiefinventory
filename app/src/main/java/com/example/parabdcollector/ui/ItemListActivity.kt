package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityItemListBinding

class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var adapter: CollectionAdapter

    private val listType by lazy { intent.getStringExtra(EXTRA_LIST_TYPE) }

    private val viewModel: ItemListViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ItemListViewModelFactory(repository, listType)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupRecyclerView()

        viewModel.items.observe(this) { items ->
            adapter.submitList(items)
            // On met à jour le titre dynamiquement
            if (listType == TYPE_POSSESSED) {
                supportActionBar?.title = getString(R.string.possessed_items_title, items.size)
            } else {
                supportActionBar?.title = getString(R.string.sought_items_title, items.size)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", item.id)
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
    }
}

// Factory spécifique pour ce ViewModel qui a besoin du type de liste
class ItemListViewModelFactory(private val repository: com.example.parabdcollector.repo.CollectionRepository, private val listType: String?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemListViewModel(repository, listType) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}