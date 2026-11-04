package com.chefinventory.app.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.chefinventory.app.ChefInventoryApplication
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ActivityMainBinding
import com.chefinventory.app.model.LocationType
import com.chefinventory.app.ui.viewmodel.MainViewModel
import com.chefinventory.app.ui.viewmodel.ViewModelFactory
import com.google.android.material.navigation.NavigationView

import com.chefinventory.app.utils.setOnSingleClickListener

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    private val viewModel: MainViewModel by viewModels {
        val app = application as ChefInventoryApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbarAndDrawer() {
        setSupportActionBar(binding.toolbar)
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnSingleClickListener {
            startActivity(Intent(this, EditIngredientActivity::class.java))
        }

        binding.cardIngredients.setOnSingleClickListener {
            startActivity(Intent(this, IngredientListActivity::class.java))
        }

        binding.cardShoppingList.setOnSingleClickListener {
            startActivity(Intent(this, ShoppingListActivity::class.java))
        }

        binding.cardRecipes.setOnSingleClickListener {
            startActivity(Intent(this, MatchRecipeInventoryActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.allIngredients.observe(this) { ingredients ->
            val count = ingredients?.size ?: 0
            binding.totalIngredientsText.text = "Ingrédients en stock : $count"
        }

        viewModel.outOfStockIngredients.observe(this) { outOfStockList ->
            val count = outOfStockList?.size ?: 0
            if (count > 0) {
                binding.totalShoppingItemsText.text = "🛒 $count article(s) à acheter"
                val preview = outOfStockList.joinToString("\n") { "• ${it.name}" }
                binding.shoppingPreviewText.text = preview
                binding.shoppingPreviewText.isVisible = true
            } else {
                binding.totalShoppingItemsText.text = "🟢 Garde-manger au complet"
                binding.shoppingPreviewText.isVisible = false
            }
        }

        viewModel.allRecipes?.observe(this) { recipes ->
            val count = recipes?.size ?: 0
            binding.totalRecipesText.text = "Toutes les recettes : $count"
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_home -> { /* Already here */ }
            R.id.nav_setup_inventory -> {
                startActivity(Intent(this, SetupInventoryActivity::class.java))
            }
            R.id.nav_update_inventory -> {
                val intent = Intent(this, SetupInventoryActivity::class.java).apply {
                    putExtra("EXTRA_MODE", "UPDATE")
                }
                startActivity(intent)
            }
            R.id.nav_shopping_list -> {
                startActivity(Intent(this, ShoppingListActivity::class.java))
            }
            R.id.nav_match_pantry_recipe -> {
                startActivity(Intent(this, MatchRecipeInventoryActivity::class.java))
            }
            R.id.nav_all_ingredients -> {
                startActivity(Intent(this, IngredientListActivity::class.java))
            }
            R.id.nav_search_recipe -> {
                startActivity(Intent(this, SearchRecipeActivity::class.java))
            }
            R.id.nav_ingredients_locations -> {
                val intent = Intent(this, LocationManagementActivity::class.java).apply {
                    putExtra(LocationManagementActivity.EXTRA_LOCATION_TYPE, LocationType.INGREDIENT.ordinal)
                }
                startActivity(intent)
            }
            R.id.nav_off_catalog -> {
                startActivity(Intent(this, OpenFoodFactsCatalogActivity::class.java))
            }
            R.id.nav_all_recipes -> {
                startActivity(Intent(this, RecipeListActivity::class.java))
            }
            R.id.nav_recipe_categories -> {
                startActivity(Intent(this, RecipeCategoryListActivity::class.java))
            }
            R.id.nav_add_recipe -> {
                startActivity(Intent(this, EditRecipeActivity::class.java))
            }
            R.id.nav_import_web_recipe -> {
                startActivity(Intent(this, ImportWebRecipeActivity::class.java))
            }
            R.id.nav_import_recipe_packs -> {
                startActivity(Intent(this, ImportRecipePacksActivity::class.java))
            }
            R.id.nav_backup -> {
                startActivity(Intent(this, BackupActivity::class.java))
            }
            R.id.nav_recipe_audit -> {
                startActivity(Intent(this, RecipeAuditActivity::class.java))
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        return super.onOptionsItemSelected(item)
    }
}
