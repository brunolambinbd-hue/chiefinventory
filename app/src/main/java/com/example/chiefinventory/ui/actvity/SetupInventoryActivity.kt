package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chiefinventory.R
import com.example.chiefinventory.databinding.ActivitySetupInventoryBinding
import com.example.chiefinventory.ui.adapter.SelectedCatalogEntry
import com.example.chiefinventory.ui.adapter.StandardCatalogAdapter
import com.example.chiefinventory.ui.viewmodel.SetupInventoryViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetupInventoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySetupInventoryBinding
    private val viewModel: SetupInventoryViewModel by viewModels()
    private lateinit var adapter: StandardCatalogAdapter

    private var allEntries: List<SelectedCatalogEntry> = emptyList()
    private var currentFilterLocation: String = "Frigo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val mode = intent.getStringExtra("EXTRA_MODE")
        if (mode == "UPDATE") {
            supportActionBar?.title = getString(R.string.setup_inventory_title_update)
        } else {
            supportActionBar?.title = getString(R.string.setup_inventory_title_init)
        }

        adapter = StandardCatalogAdapter(
            onSelectionChanged = { _ ->
                val totalSelected = allEntries.count { it.isSelected }
                binding.fabValidateStock.text = getString(R.string.setup_inventory_btn_validate, totalSelected)
            },
            onEditIngredientClicked = { item ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = com.example.chiefinventory.data.AppDatabase.getDatabase(applicationContext)
                    val existing = db.ingredientDao().getAllSync().find { it.name.equals(item.name, ignoreCase = true) }
                    withContext(Dispatchers.Main) {
                        val intent = Intent(this@SetupInventoryActivity, EditIngredientActivity::class.java).apply {
                            if (existing != null) {
                                putExtra(EditIngredientActivity.EXTRA_INGREDIENT_ID, existing.id)
                            } else {
                                putExtra(EditIngredientActivity.EXTRA_NAME, item.name)
                                putExtra(EditIngredientActivity.EXTRA_QTY, item.defaultQuantity)
                                putExtra(EditIngredientActivity.EXTRA_UNIT, item.defaultUnit)
                                putExtra(EditIngredientActivity.EXTRA_THRESHOLD, item.minThreshold)
                                putExtra(EditIngredientActivity.EXTRA_PACK_SIZE, item.packSize)
                            }
                        }
                        startActivity(intent)
                    }
                }
            }
        )
        binding.rvCatalog.adapter = adapter

        viewModel.catalogEntries.observe(this) { entries ->
            allEntries = entries
            applyFilter()
            val totalSelected = allEntries.count { it.isSelected }
            binding.fabValidateStock.text = getString(R.string.setup_inventory_btn_validate, totalSelected)
        }

        viewModel.saveStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, getString(R.string.setup_inventory_toast_success), Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        val isUpdateMode = mode == "UPDATE"
        viewModel.loadCatalog(resources, forceSelectAll = !isUpdateMode)

        binding.tabLocations.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentFilterLocation = when (tab?.position) {
                    0 -> "Frigo"
                    1 -> "Placard"
                    2 -> "Surgélateur"
                    3 -> "Cave"
                    else -> "Frigo"
                }
                applyFilter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        binding.etSearchCatalog.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabValidateStock.setOnClickListener {
            viewModel.saveSelectedIngredients(allEntries)
        }
    }

    private fun stripAccents(text: String): String {
        val lower = text.lowercase().replace("œ", "oe").replace("æ", "ae")
        return java.text.Normalizer.normalize(lower, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .trim()
    }

    private fun applyFilter() {
        val rawQuery = binding.etSearchCatalog.text.toString()
        val cleanQuery = stripAccents(rawQuery)
        val filtered = allEntries.filter { entry ->
            val matchesLocation = entry.item.defaultLocationName.equals(currentFilterLocation, ignoreCase = true)
            val cleanName = stripAccents(entry.item.name)
            val matchesQuery = cleanQuery.isBlank() || cleanName.contains(cleanQuery)
            matchesLocation && matchesQuery
        }
        adapter.submitList(filtered)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
