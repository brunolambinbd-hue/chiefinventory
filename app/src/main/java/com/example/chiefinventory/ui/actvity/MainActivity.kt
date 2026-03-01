package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.example.chiefinventory.R
import com.example.chiefinventory.CollectionApplication
import com.example.chiefinventory.databinding.ActivityMainBinding
import com.example.chiefinventory.model.LocationType
import com.example.chiefinventory.ui.viewmodel.ImportViewModel
import com.example.chiefinventory.ui.viewmodel.MainViewModel
import com.example.chiefinventory.ui.viewmodel.ViewModelFactory
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var possessedCounterTextView: TextView? = null
    private var soughtCounterTextView: TextView? = null

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    private val importViewModel: ImportViewModel by viewModels {
        val app = application as CollectionApplication
        @Suppress("VisibleForTests")
        ViewModelFactory(app, app.repository, app.locationRepository, app.ingredientRepository, app.recipeRepository)
    }

    private val importCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            importViewModel.importCsv(uri)
            Toast.makeText(this, "Importation en cours...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setTheme(R.style.Theme_ChefInventory)
        
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
        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditItemActivity::class.java))
        }

        binding.possessedItemsText.setOnClickListener {
            val intent = Intent(this, CategoryListActivity::class.java).apply {
                putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_POSSESSED)
            }
            startActivity(intent)
        }

        binding.soughtItemsText.setOnClickListener {
            val intent = Intent(this, CategoryListActivity::class.java).apply {
                putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_SOUGHT)
            }
            startActivity(intent)
        }

        binding.btnRecentFinds.setOnClickListener {
            navigateToRecent(ItemListActivity.TYPE_RECENT_POSSESSED)
        }

        binding.btnRecentOrganizations.setOnClickListener {
            navigateToRecent(ItemListActivity.TYPE_RECENT_LOCATED)
        }
        
        binding.totalIngredientsText.setOnClickListener {
            startActivity(Intent(this, IngredientListActivity::class.java))
        }
    }

    private fun navigateToRecent(type: Int) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(ItemListActivity.EXTRA_LIST_TYPE, type)
        }
        startActivity(intent)
    }

    private fun observeViewModel() {
        viewModel.totalItemsCount.observe(this) { count ->
            binding.totalItemsText.text = getString(R.string.total_items_label, count ?: 0)
        }

        viewModel.possessedItems.observe(this) { items ->
            val count = items?.size ?: 0
            binding.possessedItemsText.text = getString(R.string.possessed_items_label, count)
            possessedCounterTextView?.text = count.toString()
        }

        viewModel.soughtItems.observe(this) { items ->
            val count = items?.size ?: 0
            binding.soughtItemsText.text = getString(R.string.sought_items_label, count)
            soughtCounterTextView?.text = count.toString()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.counter_menu, menu)
        
        val counterItem = menu?.findItem(R.id.action_counter)
        val actionView = counterItem?.actionView
        possessedCounterTextView = actionView?.findViewById(R.id.possessed_counter)
        soughtCounterTextView = actionView?.findViewById(R.id.sought_counter)
        
        possessedCounterTextView?.text = viewModel.possessedItems.value?.size?.toString() ?: "0"
        soughtCounterTextView?.text = viewModel.soughtItems.value?.size?.toString() ?: "0"
        
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_home -> { /* Already here */ }
            R.id.nav_products -> {
                val intent = Intent(this, CategoryListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_POSSESSED)
                }
                startActivity(intent)
            }
            R.id.nav_recent_finds -> navigateToRecent(ItemListActivity.TYPE_RECENT_POSSESSED)
            R.id.nav_recent_organizations -> navigateToRecent(ItemListActivity.TYPE_RECENT_LOCATED)
            
            R.id.nav_searches -> {
                val intent = Intent(this, CategoryListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_SOUGHT)
                }
                startActivity(intent)
            }
            R.id.nav_all_ingredients -> {
                startActivity(Intent(this, IngredientListActivity::class.java))
            }
            R.id.nav_ingredients_locations -> {
                val intent = Intent(this, LocationManagementActivity::class.java).apply {
                    putExtra(LocationManagementActivity.EXTRA_LOCATION_TYPE, LocationType.INGREDIENT.ordinal)
                }
                startActivity(intent)
            }
            R.id.nav_all_recipes -> {
                // Ouvre la liste globale des recettes (sans filtre de catégorie)
                startActivity(Intent(this, RecipeListActivity::class.java))
            }
            R.id.nav_recipe_categories -> {
                startActivity(Intent(this, RecipeCategoryListActivity::class.java))
            }
            R.id.nav_add_recipe -> {
                startActivity(Intent(this, EditRecipeActivity::class.java))
            }
            R.id.nav_locations -> {
                val intent = Intent(this, LocationManagementActivity::class.java).apply {
                    putExtra(LocationManagementActivity.EXTRA_LOCATION_TYPE, LocationType.COLLECTION.ordinal)
                }
                startActivity(intent)
            }
            R.id.nav_import -> {
                importCsvLauncher.launch("text/comma-separated-values")
            }
            R.id.nav_signatures -> {
                startActivity(Intent(this, SignatureReportActivity::class.java))
            }
            R.id.nav_backup -> {
                startActivity(Intent(this, BackupActivity::class.java))
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) return true
        if (item.itemId == R.id.action_search) {
            startActivity(Intent(this, SearchActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
