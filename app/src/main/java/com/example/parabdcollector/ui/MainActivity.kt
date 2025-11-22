package com.example.parabdcollector.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private var possessedCounterTextView: TextView? = null
    private var soughtCounterTextView: TextView? = null
    private var missingSignatureCounterTextView: TextView? = null
    private var missingSignatureSeparator: TextView? = null

    private val viewModel: MainViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    private val importViewModel: ImportViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
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

        setSupportActionBar(binding.toolbar)

        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        binding.fabAdd.setOnClickListener {
            // On ajoute un petit délai pour laisser l'animation du ripple se jouer.
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, EditItemActivity::class.java))
            }, 200) // 200 millisecondes de délai
        }

        // On observe les trois informations pour le tableau de bord.
        viewModel.possessedItems.observe(this) { items ->
            possessedCounterTextView?.text = items.size.toString()
            binding.possessedItemsText.text = getString(R.string.possessed_items_label, items.size)
        }

        viewModel.soughtItems.observe(this) { items ->
            soughtCounterTextView?.text = items.size.toString()
            binding.soughtItemsText.text = getString(R.string.sought_items_label, items.size)
        }

        viewModel.totalItemsCount.observe(this) { count ->
            binding.totalItemsText.text = getString(R.string.total_items_label, count)
        }

        // On active le mode débogage si nécessaire
        setupDebugView()
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

                // Mise à jour du compteur dans la barre d'outils
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
        
        val possessedCount = viewModel.possessedItems.value?.size ?: 0
        val soughtCount = viewModel.soughtItems.value?.size ?: 0
        possessedCounterTextView?.text = possessedCount.toString()
        soughtCounterTextView?.text = soughtCount.toString()

        // On met à jour le compteur de signatures manquantes au cas où les données sont déjà là
        val missingCount = viewModel.signatureStats.value?.missingCount ?: 0
        val showMissing = missingCount > 0
        missingSignatureCounterTextView?.text = missingCount.toString()
        missingSignatureCounterTextView?.isVisible = showMissing
        missingSignatureSeparator?.isVisible = showMissing
        
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { 
                // Ne fait rien, car nous sommes déjà sur l'écran d'accueil.
            }
            R.id.nav_products -> {
                val intent = Intent(this, CategoryListActivity::class.java)
                intent.putExtra(CategoryListActivity.EXTRA_IS_POSSESSED, true)
                startActivity(intent)
            }
            R.id.nav_searches -> {
                val intent = Intent(this, CategoryListActivity::class.java)
                intent.putExtra(CategoryListActivity.EXTRA_IS_POSSESSED, false)
                startActivity(intent)
            }
            R.id.nav_locations -> {
                startActivity(Intent(this, LocationManagementActivity::class.java))
            }
            R.id.nav_import -> {
                importCsvLauncher.launch("text/comma-separated-values")
            }
            R.id.nav_signature_report -> {
                startActivity(Intent(this, SignatureReportActivity::class.java))
            }
        }
        binding.drawerLayout.closeDrawers()
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
