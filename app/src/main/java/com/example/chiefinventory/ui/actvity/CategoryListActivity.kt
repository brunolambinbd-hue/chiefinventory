package com.example.chiefinventory.ui.actvity

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
import com.example.chiefinventory.R
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityCategoryListBinding
import com.example.chiefinventory.ui.adapter.CategoryAdapterRevised
import com.example.chiefinventory.ui.model.CategoryInfo
import com.example.chiefinventory.ui.viewmodel.MainViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView

/**
 * An activity that displays a list of categories.
 */
class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var adapter: ListAdapter<CategoryInfo, *>
    private var isPossessed: Boolean = true
    private var superCategory: String? = null
    private lateinit var rootTitle: String

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository)
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

        rootTitle = intent.getStringExtra(EXTRA_ROOT_TITLE)
            ?: if (isPossessed) getString(R.string.menu_products_title) else getString(R.string.menu_searches_title)

        setupRecyclerView()
        observeViewModel()
    }

    private fun observeViewModel() {
        val isSoughtMode = !isPossessed
        if (superCategory == null) {
            supportActionBar?.title = rootTitle
            viewModel.getSuperCategoryInfo(isSoughtMode).observe(this) { adapter.submitList(it) }
        } else {
            val titleText = superCategory!!
            val contextText = " ($rootTitle)"
            val fullTitle = titleText + contextText
            val spannable = SpannableString(fullTitle)

            spannable.setSpan(RelativeSizeSpan(0.8f), titleText.length, fullTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) { finish() }
            }
            spannable.setSpan(clickableSpan, titleText.length, fullTitle.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            supportActionBar?.title = spannable
            findToolbarTitleView()?.movementMethod = LinkMovementMethod.getInstance()

            viewModel.getCategoryInfoForSuperCategory(superCategory!!, isSoughtMode).observe(this) { adapter.submitList(it) }
        }
    }

    private fun findToolbarTitleView(): TextView? {
        val toolbar = binding.toolbar
        for (i in 0 until toolbar.childCount) {
            val child = toolbar.getChildAt(i)
            if (child is TextView) return child
        }
        return null
    }

    private fun setupRecyclerView() {
        val isSoughtMode = !isPossessed
        adapter = CategoryAdapterRevised(isSoughtMode) { categoryName ->
            if (superCategory == null) handleSuperCategoryClick(categoryName) else handleCategoryClick(superCategory!!, categoryName)
        }
        binding.rvCategoryList.adapter = adapter
        binding.rvCategoryList.layoutManager = LinearLayoutManager(this)
    }

    private fun handleSuperCategoryClick(categoryName: String) {
        val intent = Intent(this, CategoryListActivity::class.java).apply {
            val listType = if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, listType)
            putExtra(EXTRA_SUPER_CATEGORY, categoryName)
            putExtra(EXTRA_ROOT_TITLE, rootTitle)
        }
        startActivity(intent)
    }

    private fun handleCategoryClick(currentSuperCategory: String, categoryName: String) {
        navigateToItemList(currentSuperCategory, categoryName)
    }

    private fun navigateToItemList(superCat: String, cat: String) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT)
            putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, superCat)
            putExtra(ItemListActivity.EXTRA_CATEGORY, cat)
            putExtra(EXTRA_ROOT_TITLE, rootTitle)
        }
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_SUPER_CATEGORY: String = "super_category"
        const val EXTRA_ROOT_TITLE: String = "root_title"
    }
}
