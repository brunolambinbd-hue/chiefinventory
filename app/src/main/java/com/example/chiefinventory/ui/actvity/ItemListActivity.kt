package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.RelativeSizeSpan
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ActivityItemListBinding
import com.example.chiefinventory.ui.adapter.CollectionAdapter
import com.example.chiefinventory.ui.model.SearchResultItem
import com.example.chiefinventory.ui.viewmodel.MainViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory

/**
 * An activity to display a list of collection items based on various filter criteria.
 */
class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var adapter: CollectionAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
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
        val locationId = intent.getLongExtra(EXTRA_LOCATION_ID, -1L)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME)
        val rootTitle = intent.getStringExtra(CategoryListActivity.EXTRA_ROOT_TITLE)

        setupRecyclerView()

        when {
            listType == TYPE_RECENT_POSSESSED -> {
                supportActionBar?.title = "Dernières trouvailles"
                viewModel.recentPossessedItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            listType == TYPE_RECENT_LOCATED -> {
                supportActionBar?.title = "Derniers rangements"
                viewModel.recentLocatedItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            listType == TYPE_UNLOCATED -> {
                supportActionBar?.title = "Objets non localisés"
                viewModel.unlocatedItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            listType == TYPE_LOCATED_NOT_POSSESSED -> {
                supportActionBar?.title = "Objets localisés non possédés"
                viewModel.locatedNotPossessedItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            locationId != -1L -> {
                supportActionBar?.title = locationName ?: getString(R.string.location_items_title)
                viewModel.getItemsByLocationId(locationId).observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            superCategory != null && category != null -> {
                val contextText = if (rootTitle != null) " ($rootTitle)" else ""
                val fullTitle = category + contextText
                val spannable = SpannableString(fullTitle)

                if (contextText.isNotEmpty()) {
                    spannable.setSpan(RelativeSizeSpan(0.8f), category.length, fullTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) { finish() }
                    }
                    spannable.setSpan(clickableSpan, category.length, fullTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                supportActionBar?.title = spannable
                findToolbarTitleView()?.movementMethod = LinkMovementMethod.getInstance()

                viewModel.getItemsBySuperCategoryAndCategory(superCategory, category, listType == TYPE_POSSESSED).observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            listType == TYPE_POSSESSED -> {
                supportActionBar?.title = getString(R.string.menu_products_title)
                viewModel.possessedItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
            else -> {
                supportActionBar?.title = getString(R.string.menu_searches_title)
                viewModel.soughtItems.observe(this) { items ->
                    adapter.submitList(items.map(::SearchResultItem))
                }
            }
        }
    }

    private fun findToolbarTitleView(): TextView? {
        for (i in 0 until binding.toolbar.childCount) {
            val child = binding.toolbar.getChildAt(i)
            if (child is TextView) return child
        }
        return null
    }

    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra(EditItemActivity.EXTRA_ITEM_ID, searchResult.item.id)
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
        const val EXTRA_LIST_TYPE: String = "list_type"
        const val EXTRA_SUPER_CATEGORY: String = "super_category"
        const val EXTRA_CATEGORY: String = "category"
        const val EXTRA_LOCATION_ID: String = "location_id"
        const val EXTRA_LOCATION_NAME: String = "location_name"
        const val TYPE_POSSESSED: Int = 1
        const val TYPE_SOUGHT: Int = 2
        const val TYPE_UNLOCATED: Int = 3
        const val TYPE_LOCATED_NOT_POSSESSED: Int = 4
        const val TYPE_RECENT_POSSESSED: Int = 5
        const val TYPE_RECENT_LOCATED: Int = 6
    }
}
