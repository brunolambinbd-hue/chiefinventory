package com.example.parabdcollector.ui.actvity

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
import androidx.recyclerview.widget.RecyclerView
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityItemListBinding
import com.example.parabdcollector.ui.adapter.CollectionAdapter
import com.example.parabdcollector.ui.model.SearchResultItem
import com.example.parabdcollector.ui.viewmodel.MainViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory

/**
 * An activity to display a list of collection items based on various filter criteria.
 *
 * This activity can display items filtered by:
 * - A specific location ([EXTRA_LOCATION_ID]).
 * - A combination of super-category and category ([EXTRA_SUPER_CATEGORY], [EXTRA_CATEGORY]).
 * - Possession status ([EXTRA_LIST_TYPE] = [TYPE_POSSESSED] or [TYPE_SOUGHT]).
 * - Unlocated status ([EXTRA_LIST_TYPE] = [TYPE_UNLOCATED]).
 *
 * The title of the activity is dynamically updated to reflect the current filter.
 */
class ItemListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemListBinding
    private lateinit var adapter: CollectionAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    /**
     * Initializes the activity, toolbar, and RecyclerView. It then observes the ViewModel
     * for the appropriate item list based on the intent extras.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in [onSaveInstanceState]. Otherwise it is null.
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
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME)
        val rootTitle = intent.getStringExtra(CategoryListActivity.EXTRA_ROOT_TITLE)

        setupRecyclerView()

        when {
            listType == TYPE_UNLOCATED -> {
                supportActionBar?.title = "Objets non localisés"
                viewModel.unlocatedItems.observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            locationId != -1L -> {
                supportActionBar?.title = locationName ?: getString(R.string.location_items_title)
                viewModel.getItemsByLocationId(locationId).observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            superCategory != null && category != null -> {
                val contextText = if (rootTitle != null) " ($rootTitle)" else ""
                val fullTitle = category + contextText
                val spannable = SpannableString(fullTitle)

                if (contextText.isNotEmpty()) {
                    spannable.setSpan(
                        RelativeSizeSpan(0.8f),
                        category.length,
                        fullTitle.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val clickableSpan = object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            finish() // Simply go back to the previous category list
                        }
                    }
                    spannable.setSpan(
                        clickableSpan,
                        category.length,
                        fullTitle.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                supportActionBar?.title = spannable
                findToolbarTitleView()?.movementMethod = LinkMovementMethod.getInstance()

                viewModel.getItemsBySuperCategoryAndCategory(superCategory, category, listType == TYPE_POSSESSED).observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            listType == TYPE_POSSESSED -> {
                supportActionBar?.title = getString(R.string.menu_products_title)
                viewModel.possessedItems.observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
            else -> {
                supportActionBar?.title = getString(R.string.menu_searches_title)
                viewModel.soughtItems.observe(this) { items ->
                    val searchResults = items.map(::SearchResultItem)
                    adapter.submitList(searchResults)
                }
            }
        }
    }

    /**
     * Finds the toolbar's title TextView by iterating through its children.
     * This is necessary to make parts of the title clickable, as the title view has no public ID.
     *
     * @return The [TextView] used for the title, or null if it cannot be found.
     */
    private fun findToolbarTitleView(): TextView? {
        for (i in 0 until binding.toolbar.childCount) {
            val child = binding.toolbar.getChildAt(i)
            if (child is TextView) {
                return child
            }
        }
        return null
    }

    /**
     * Initializes the RecyclerView, its adapter, and scroll listeners.
     * The adapter is configured to open [EditItemActivity] on item click.
     * A scroll listener is added to show/hide a "scroll to top" FAB.
     */
    private fun setupRecyclerView() {
        adapter = CollectionAdapter { searchResult ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", searchResult.item.id)
            startActivity(intent)
        }
        binding.rvItemList.adapter = adapter
        binding.rvItemList.layoutManager = LinearLayoutManager(this)

        binding.rvItemList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    if (!binding.fabScrollToTop.isShown) {
                        binding.fabScrollToTop.show()
                    }
                } else if (dy < 0) {
                    if (binding.fabScrollToTop.isShown) {
                        binding.fabScrollToTop.hide()
                    }
                }
            }
        })

        binding.fabScrollToTop.setOnClickListener {
            binding.rvItemList.smoothScrollToPosition(0)
        }
    }

    /**
     * Handles action bar item selections. In this case, it handles the "Up" button
     * to navigate back to the previous screen.
     *
     * @param item The menu item that was selected.
     * @return True if the item was handled, false otherwise.
     */
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
        const val EXTRA_LOCATION_ID = "location_id"
        const val EXTRA_LOCATION_NAME = "location_name"
        const val EXTRA_ROOT_TITLE = "root_title"
        const val TYPE_POSSESSED = 1
        const val TYPE_SOUGHT = 2
        const val TYPE_UNLOCATED = 3
    }
}
