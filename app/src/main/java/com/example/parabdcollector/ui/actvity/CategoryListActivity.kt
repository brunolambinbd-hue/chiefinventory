package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityCategoryListBinding
import com.example.parabdcollector.ui.adapter.CategoryAdapter
import com.example.parabdcollector.ui.viewmodel.MainViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import com.example.parabdcollector.utils.observeOnce

/**
 * An activity that displays a list of categories.
 *
 * This activity has a dual purpose, determined by the extras passed in its Intent:
 * - If no `EXTRA_SUPER_CATEGORY` is provided, it displays a list of all top-level super-categories.
 * - If an `EXTRA_SUPER_CATEGORY` is provided, it displays a list of the detailed sub-categories for that super-category.
 */
class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var adapter: CategoryAdapter
    private var isPossessed: Boolean = true
    private var superCategory: String? = null

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isPossessed = intent.getBooleanExtra(EXTRA_IS_POSSESSED, true)
        superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)

        setupRecyclerView()
        observeViewModel()
    }

    /**
     * Sets up the observers on the ViewModel based on whether we are displaying
     * super-categories or sub-categories.
     */
    private fun observeViewModel() {
        if (superCategory == null) {
            supportActionBar?.title = if (isPossessed) "Mes Produits" else "Mes Recherches"
            viewModel.getSuperCategoryInfo(isPossessed).observe(this) { adapter.submitList(it) }
        } else {
            supportActionBar?.title = superCategory
            viewModel.getCategoryInfoForSuperCategory(superCategory!!, isPossessed).observe(this) { adapter.submitList(it) }
        }
    }

    /**
     * Initializes the RecyclerView and its adapter, and defines the click handling logic.
     */
    private fun setupRecyclerView() {
        adapter = CategoryAdapter { categoryName ->
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
     * If the super-category contains only one sub-category, it navigates directly to the item list.
     * Otherwise, it re-launches this activity to display the sub-categories.
     * @param categoryName The name of the clicked super-category.
     */
    private fun handleSuperCategoryClick(categoryName: String) {
        viewModel.getCategoryInfoForSuperCategory(categoryName, isPossessed)
            .observeOnce(this) { subCategories ->
                if (subCategories.size == 1) {
                    navigateToItemList(categoryName, subCategories.first().name)
                } else {
                    val intent = Intent(this, CategoryListActivity::class.java).apply {
                        putExtra(EXTRA_IS_POSSESSED, isPossessed)
                        putExtra(EXTRA_SUPER_CATEGORY, categoryName)
                    }
                    startActivity(intent)
                }
            }
    }

    /**
     * Handles a click on a sub-category, navigating directly to the item list.
     * @param currentSuperCategory The currently displayed super-category.
     * @param categoryName The name of the clicked sub-category.
     */
    private fun handleCategoryClick(currentSuperCategory: String, categoryName: String) {
        navigateToItemList(currentSuperCategory, categoryName)
    }

    /**
     * Navigates to the [ItemListActivity] for a given super-category and category.
     * @param superCat The super-category to filter by.
     * @param cat The detailed category to filter by.
     */
    private fun navigateToItemList(superCat: String, cat: String) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT)
            putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, superCat)
            putExtra(ItemListActivity.EXTRA_CATEGORY, cat)
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

    companion object {
        /** Key for the boolean extra indicating if the list should display possessed or sought items. */
        const val EXTRA_IS_POSSESSED = "is_possessed"
        /** Key for the string extra that holds the name of the super-category to display. */
        const val EXTRA_SUPER_CATEGORY = "super_category"
    }
}
