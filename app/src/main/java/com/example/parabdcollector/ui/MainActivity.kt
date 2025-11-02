package com.example.parabdcollector.ui

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.parabdcollector.CollectionApplication
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityMainBinding
import com.example.parabdcollector.model.CollectionItem
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    private val viewModel: MainViewModel by viewModels {
        val repository = (application as CollectionApplication).repository
        ViewModelFactory(application, repository)
    }

    private lateinit var adapter: CollectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // On configure le "toggle" qui lie la Toolbar et le DrawerLayout
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // On écoute les clics dans le tiroir de navigation
        binding.navView.setNavigationItemSelectedListener(this)

        adapter = CollectionAdapter { item ->
            val intent = Intent(this, EditItemActivity::class.java)
            intent.putExtra("itemId", item.id)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, EditItemActivity::class.java))
        }

        viewModel.allItems.observe(this) { items ->
            adapter.submitList(items)
            supportActionBar?.title = "ParaBDCollector"
        }
    }

    // On gère les clics sur les éléments du tiroir de navigation
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> Toast.makeText(this, "Accueil cliqué", Toast.LENGTH_SHORT).show()
            R.id.nav_products -> Toast.makeText(this, "Mes Produits cliqué", Toast.LENGTH_SHORT).show()
            R.id.nav_searches -> Toast.makeText(this, "Mes Recherches cliqué", Toast.LENGTH_SHORT).show()
            R.id.nav_locations -> Toast.makeText(this, "Mes Emplacements cliqué", Toast.LENGTH_SHORT).show()
        }
        binding.drawerLayout.closeDrawers()
        return true
    }

    // On s'assure que le "toggle" réagit correctement aux clics
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}