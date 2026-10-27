package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chiefinventory.databinding.ActivityMatchRecipeInventoryBinding
import com.example.chiefinventory.ui.adapter.MatchRecipeAdapter
import com.example.chiefinventory.ui.viewmodel.MatchRecipeViewModel
import com.example.chiefinventory.utils.RecipeMatchResult
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchRecipeInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchRecipeInventoryBinding
    private val viewModel: MatchRecipeViewModel by viewModels()
    private lateinit var adapter: MatchRecipeAdapter

    private var allMatchResults: List<RecipeMatchResult> = emptyList()
    private var locationNamesMap: Map<Long, String> = emptyMap()
    private var selectedCourseFilter: String = "TOUT"
    private var selectedCuisineFilter: String = "Toutes les cuisines"

    private val cuisineOptions = listOf(
        "Toutes les cuisines",
        "Cuisine Belge 🇧🇪",
        "Cuisine Française 🇫🇷",
        "Cuisine Italienne 🇮🇹"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchRecipeInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = MatchRecipeAdapter { matchResult ->
            val intent = Intent(this, EditRecipeActivity::class.java).apply {
                putExtra(EditRecipeActivity.EXTRA_RECIPE_ID, matchResult.recipe.id)
            }
            startActivity(intent)
        }
        binding.rvMatchRecipes.adapter = adapter

        setupCourseTabs()
        setupCuisineDropdown()
        setupTextSearch()

        viewModel.matchedRecipes.observe(this) { matches ->
            allMatchResults = matches ?: emptyList()
            applyFilters()
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val db = com.example.chiefinventory.data.AppDatabase.getDatabase(applicationContext)
            val locations = db.locationDao().getAllSync()
            locationNamesMap = locations.associate { it.id to it.name }
            withContext(Dispatchers.Main) {
                applyFilters()
            }
        }

        viewModel.calculateMatches()
    }

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    private fun setupTextSearch() {
        binding.etMatchSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun setupCourseTabs() {
        binding.tabCourseFilter.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedCourseFilter = when (tab?.position) {
                    1 -> "Entrée"
                    2 -> "Plat"
                    3 -> "Dessert"
                    4 -> "Boisson"
                    5 -> "Accompagnement"
                    else -> "TOUT"
                }
                applyFilters()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCuisineDropdown() {
        val adapterDropdown = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cuisineOptions)
        binding.actvCuisineFilter.setAdapter(adapterDropdown)

        binding.actvCuisineFilter.setOnItemClickListener { _, _, position, _ ->
            selectedCuisineFilter = cuisineOptions[position]
            applyFilters()
        }
    }

    private fun applyFilters() {
        val rawQuery = binding.etMatchSearch.text?.toString() ?: ""
        val searchTokens = rawQuery.split(Regex("[,+&]|\\bet\\b"))
            .map { stripAccents(it) }
            .filter { it.length >= 2 }

        val filtered = allMatchResults.filter { res ->
            val instStr = res.recipe.instructions ?: ""
            val sourceStr = res.recipe.source ?: ""
            val titleStr = res.recipe.title

            val matchesText = searchTokens.isEmpty() || searchTokens.all { token ->
                stripAccents(titleStr).contains(token) ||
                stripAccents(instStr).contains(token) ||
                res.missingIngredients.any { stripAccents(it).contains(token) }
            }

            val locName = res.recipe.locationId?.let { locationNamesMap[it] }
            val locCat = com.example.chiefinventory.utils.CategoryMapper.normalizeRecipeCategory(locName)
            val sourceCat = com.example.chiefinventory.utils.CategoryMapper.normalizeRecipeCategory(sourceStr)
            val titleCat = com.example.chiefinventory.utils.CategoryMapper.normalizeRecipeCategory(titleStr)
            val effectiveCourse = locCat ?: sourceCat ?: titleCat ?: "Plat principal"

            val matchesCourse = when (selectedCourseFilter) {
                "Entrée" -> effectiveCourse == "Entrée"
                "Plat" -> effectiveCourse == "Plat principal"
                "Dessert" -> effectiveCourse == "Dessert"
                "Boisson" -> effectiveCourse == "Boisson"
                "Accompagnement" -> effectiveCourse == "Accompagnement"
                else -> true
            }

            val matchesCuisine = if (selectedCuisineFilter == "Toutes les cuisines") {
                true
            } else if (selectedCuisineFilter.contains("Française")) {
                val frenchKeywords = listOf("française", "francais", "provençale", "alsacienne", "bretonne", "normande", "bourguignonne", "lyonnaise", "auvergnate", "savoyarde", "corse")
                frenchKeywords.any { kw -> titleStr.lowercase().contains(kw) || instStr.lowercase().contains(kw) || sourceStr.lowercase().contains(kw) }
            } else {
                val kw = selectedCuisineFilter.replace("Cuisine", "").replace("🇧🇪", "").replace("🇮🇹", "").replace("🇫🇷", "").trim().lowercase()
                titleStr.lowercase().contains(kw) || instStr.lowercase().contains(kw) || sourceStr.lowercase().contains(kw)
            }

            matchesText && matchesCourse && matchesCuisine
        }

        val totalCount = filtered.size
        val readyCount = filtered.count { it.matchPercentage == 100.0 }

        if (totalCount == 0) {
            binding.tvMatchCountHeader.text = getString(com.example.chiefinventory.R.string.search_no_results)
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvMatchRecipes.visibility = View.GONE
        } else {
            binding.tvMatchCountHeader.text = getString(com.example.chiefinventory.R.string.match_count_header_format, totalCount, readyCount)
            binding.tvEmptyState.visibility = View.GONE
            binding.rvMatchRecipes.visibility = View.VISIBLE
        }
        adapter.submitList(filtered)
    }
}
