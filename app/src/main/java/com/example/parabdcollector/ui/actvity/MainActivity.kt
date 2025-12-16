package com.example.parabdcollector.ui.actvity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import com.example.parabdcollector.R
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.databinding.ActivityMainBinding
import com.example.parabdcollector.ui.viewmodel.ImportViewModel
import com.example.parabdcollector.ui.viewmodel.MainViewModel
import com.example.parabdcollector.ui.viewmodel.ViewModelFactory
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var possessedCounterTextView: TextView? = null
    private var soughtCounterTextView: TextView? = null
    private var missingSignatureCounterTextView: TextView? = null
    private var missingSignatureSeparator: TextView? = null

    private val viewModel: MainViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    private val importViewModel: ImportViewModel by viewModels {
        val app = application as CollectionApplication
        ViewModelFactory(app, app.repository!!, app.locationRepository!!)
    }

    private val importCsvLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            importViewModel.importCsv(uri)
            Toast.makeText(this, "Importation en cours...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbarAndDrawer()
        setupClickListeners()
        observeViewModel()
        setupDebugView()

        // Make the debug section visible and set up the crash button for testing
        binding.debugSection.visibility = View.VISIBLE
        binding.btnTestCrash.setOnClickListener {
            throw RuntimeException("Test Crash")
        }
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

        binding.totalItemsText.setOnClickListener {
            Toast.makeText(this, "Affichage de tous les objets (à implémenter)", Toast.LENGTH_SHORT).show()
        }
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
    
    private fun setupDebugView() {
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        binding.debugSection.isVisible = isDebuggable

        if (isDebuggable) {
            viewModel.signatureStats.observe(this) { stats ->
                binding.tvSignaturesOk.text = getString(R.string.report_signatures_ok, stats.validCount)

                binding.tvSignaturesEmpty.text = getString(R.string.report_signatures_empty, stats.emptyCount)
                binding.tvSignaturesEmpty.setTextColor(ContextCompat.getColor(this, R.color.status_warning))

                binding.tvSignaturesMissing.text = getString(R.string.report_signatures_missing, stats.missingCount)
                binding.tvSignaturesMissing.setTextColor(ContextCompat.getColor(this, R.color.status_error))

                val showMissing = stats.missingCount > 0
                missingSignatureCounterTextView?.text = stats.missingCount.toString()
                missingSignatureCounterTextView?.isVisible = showMissing
                missingSignatureSeparator?.isVisible = showMissing
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.counter_menu, menu)
        
        val counterItem = menu?.findItem(R.id.action_counter)
        val actionView = counterItem?.actionView
        possessedCounterTextView = actionView?.findViewById(R.id.possessed_counter)
        soughtCounterTextView = actionView?.findViewById(R.id.sought_counter)
        missingSignatureCounterTextView = actionView?.findViewById(R.id.missing_signature_counter)
        missingSignatureSeparator = actionView?.findViewById(R.id.missing_signature_separator)
        
        // Set initial counts
        possessedCounterTextView?.text = viewModel.possessedItems.value?.size?.toString() ?: "0"
        soughtCounterTextView?.text = viewModel.soughtItems.value?.size?.toString() ?: "0"

        val missingCount = viewModel.signatureStats.value?.missingCount ?: 0
        val showMissing = missingCount > 0
        missingSignatureCounterTextView?.text = missingCount.toString()
        missingSignatureCounterTextView?.isVisible = showMissing
        missingSignatureSeparator?.isVisible = showMissing
        
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when (item.itemId) {
            R.id.nav_home -> { /* Do nothing, we are already here */ }
            R.id.nav_products -> {
                val intent = Intent(this, CategoryListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_POSSESSED)
                }
                startActivity(intent)
            }
            R.id.nav_searches -> {
                val intent = Intent(this, CategoryListActivity::class.java).apply {
                    putExtra(ItemListActivity.EXTRA_LIST_TYPE, ItemListActivity.TYPE_SOUGHT)
                }
                startActivity(intent)
            }
            R.id.nav_locations -> {
                val intent = Intent(this, LocationManagementActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_import -> {
                importCsvLauncher.launch("text/comma-separated-values")
            }
            R.id.nav_signatures -> {
                val intent = Intent(this, SignatureReportActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_backup -> {
                val intent = Intent(this, BackupActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        when (item.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
