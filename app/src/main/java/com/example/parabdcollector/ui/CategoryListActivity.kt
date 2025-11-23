package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityCategoryListBinding
import com.example.parabdcollector.model.CategoryInfo

class CategoryListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryListBinding
    private lateinit var adapter: CategoryAdapter

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository, app.locationRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val listType = intent.getBooleanExtra(EXTRA_IS_POSSESSED, true)
        val superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)

        setupRecyclerView(listType, superCategory)

        if (superCategory == null) {
            // On affiche les super-catégories
            supportActionBar?.title = if (listType) "Mes Produits" else "Mes Recherches"
            viewModel.getSuperCategoryInfo(listType).observe(this) {
                adapter.submitList(it)
            }
        } else {
            // On affiche les catégories pour une super-catégorie donnée
            supportActionBar?.title = superCategory
            viewModel.getCategoryInfoForSuperCategory(superCategory, listType).observe(this) {
                adapter.submitList(it)
            }
        }
    }

    private fun setupRecyclerView(isPossessed: Boolean, superCategory: String?) {
        adapter = CategoryAdapter { categoryName ->
            if (superCategory == null) {
                // Clic sur une super-catégorie. On vérifie si on peut sauter une étape.
                viewModel.getCategoryInfoForSuperCategory(categoryName, isPossessed).observe(this, object : Observer<List<CategoryInfo>> {
                    override fun onChanged(subCategories: List<CategoryInfo>) {
                        viewModel.getCategoryInfoForSuperCategory(categoryName, isPossessed).removeObserver(this)
                        if (subCategories.size == 1) {
                            // Il n'y a qu'une seule sous-catégorie, on va directement à la liste des objets.
                            val intent = Intent(this@CategoryListActivity, ItemListActivity::class.java).apply {
                                putExtra(ItemListActivity.EXTRA_LIST_TYPE, if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT)
                                putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, categoryName)
                                putExtra(ItemListActivity.EXTRA_CATEGORY, subCategories.first().name)
                            }
                            startActivity(intent)
                        } else {
                            // Comportement normal : on ouvre la liste des catégories.
                            val intent = Intent(this@CategoryListActivity, CategoryListActivity::class.java).apply {
                                putExtra(EXTRA_IS_POSSESSED, isPossessed)
                                putExtra(EXTRA_SUPER_CATEGORY, categoryName)
                            }
                            startActivity(intent)
                        }
                    }
                })
            } else {
                // Clic sur une catégorie -> on ouvre la liste des objets
                val intent = Intent(this, ItemListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, if (isPossessed) ItemListActivity.TYPE_POSSESSED else ItemListActivity.TYPE_SOUGHT)
                    putExtra(ItemListActivity.EXTRA_SUPER_CATEGORY, superCategory)
                    putExtra(ItemListActivity.EXTRA_CATEGORY, categoryName)
                }
                startActivity(intent)
            }
        }
        binding.rvCategoryList.adapter = adapter
        binding.rvCategoryList.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_IS_POSSESSED = "is_possessed"
        const val EXTRA_SUPER_CATEGORY = "super_category"
    }
}
