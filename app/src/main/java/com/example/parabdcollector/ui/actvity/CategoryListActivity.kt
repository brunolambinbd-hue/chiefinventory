package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import com.example.parabdcollector.R
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityCategoryListBinding
import com.example.parabdcollector.ui.adapter.CategoryAdapterRevised
import com.example.parabdcollector.ui.model.CategoryInfo
import com.example.parabdcollector.ui.viewmodel.MainViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.TextView

/**
 * An activity that displays a list of categories.
 *
 * This activity has a dual purpose, determined by the extras passed in its Intent:
 * - If no `EXTRA_SUPER_CATEGORY` is provided, it displays a list of all top-level super-categories.
 * - If an `EXTRA_SUPER_CATEGORY` is provided, it displays a list of the detailed sub-categories for that super-category.
 */
class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    // private lateinit var adapter: CategoryAdapter
    private lateinit var adapter: ListAdapter<CategoryInfo, *>
    private var isPossessed: Boolean = true
    private var superCategory: String? = null
    private lateinit var rootTitle: String

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listType = intent.getIntExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_POSSESSED)
        isPossessed = listType == ItemListActivity.TYPE_POSSESSED
        superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)

        // Determine the root title, either from the previous screen or by figuring it out itself.
        rootTitle = intent.getStringExtra(EXTRA_ROOT_TITLE)
            ?: if (isPossessed) getString(R.string.menu_products_title) else getString(R.string.menu_searches_title)

        setupRecyclerView()
        observeViewModel()
    }

    /**
     * Sets up the observers on the ViewModel and configures the toolbar title.
     * The title now includes the root context in a smaller font size (e.g., "Image (Mes Produits)").
     */
    private fun observeViewModel() {
        if (superCategory == null) {
            // This is the super-category list (Page 2)
            Log.d("CategoryListActivity", "Page 2 - superCategory: rootTitle: $rootTitle")
            supportActionBar?.title = rootTitle
            viewModel.getSuperCategoryInfo().observe(this) { adapter.submitList(it) }
        } else {
            // This is the category list (Page 3)
            Log.d("CategoryListActivity", "Page 3 - superCategory: $superCategory, rootTitle: $rootTitle")

            val titleText = superCategory!!
            val contextText = " ($rootTitle)"
            val fullTitle = titleText + contextText
            Log.d("CategoryListActivity", "Page 3 - Full title being set: $fullTitle")

            val spannable = SpannableString(fullTitle)

            // Style for the context part (e.g., "(Mes Recherches)")
            spannable.setSpan(
                RelativeSizeSpan(0.8f),
                titleText.length,
                fullTitle.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Clickable span for the context part
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    finish() // Go back to Page 2
                }
            }
            spannable.setSpan(
                clickableSpan,
                titleText.length,
                fullTitle.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            supportActionBar?.title = spannable
            findToolbarTitleView()?.movementMethod = LinkMovementMethod.getInstance()

            viewModel.getCategoryInfoForSuperCategory(superCategory!!).observe(this) { adapter.submitList(it) }
        }
    }

    private fun findToolbarTitleView(): TextView? {
        // This is a robust way to get the toolbar title TextView, as it doesn't have a public ID.
        val toolbar = binding.toolbar
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is TextView) {
                return child
            }
        }
        return null
    }

    /**
     * Initializes the RecyclerView and its adapter, and defines the click handling logic.
     */
    private fun setupRecyclerView() {
        val isSoughtMode = !isPossessed
        // adapter = CategoryAdapter(isSoughtMode) { categoryName ->
        adapter = CategoryAdapterRevised(isSoughtMode) { categoryName ->
            if (superCategory == null) {
                handleSuperCategoryClick(categoryName)
            } else {
                handleCategoryClick(superCategory!!, categoryName)
            }
        }
        binding.rvCategoryList.adapter = adapter
        binding.rvCategoryList.layoutManager = LinearLayoutManager(this)
    }

    /**
     * Handles a click on a super-category.
     * It navigates to the sub-category list, passing the root title along.
     * @param categoryName The name of the clicked super-category.
     */
    private fun handleSuperCategoryClick(categoryName: String) {
        val intent = Intent(this, CategoryListActivity::class.java).apply {
            val listType = if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, listType)
            putExtra(EXTRA_SUPER_CATEGORY, categoryName)
            putExtra(EXTRA_ROOT_TITLE, rootTitle) // Pass the root title to the next screen
        }
        startActivity(intent)
    }

    /**
     * Handles a click on a sub-category, navigating directly to the item list.
     * @param currentSuperCategory The currently displayed super-category.
     * @param categoryName The name of the clicked sub-category.
     */
    private fun handleCategoryClick(currentSuperCategory: String, categoryName: String) {
        Log.d("CategoryListActivity", "Navigating to Page 4 with superCategory: $currentSuperCategory, category: $categoryName")
        navigateToItemList(currentSuperCategory, categoryName)
    }

    /**
     * Navigates to the [ItemListActivity] for a given super-category and category,
     * passing the root title for context.
     * @param superCat The super-category to filter by.
     * @param cat The detailed category to filter by.
     */
    private fun navigateToItemList(superCat: String, cat: String) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT)
            putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, superCat)
            putExtra(ItemListActivity.EXTRA_CATEGORY, cat)
            putExtra(EXTRA_ROOT_TITLE, rootTitle) // Pass the root title to the final screen
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @Suppress("RedundantVisibilityModifier")
    companion object {
        /** Key for the string extra that holds the name of the super-category to display. */
        public const val EXTRA_SUPER_CATEGORY = "super_category"
        /** Key for the string extra that holds the root title for context (e.g., "Mes Produits"). */
        public const val EXTRA_ROOT_TITLE = "root_title"
    }
}
