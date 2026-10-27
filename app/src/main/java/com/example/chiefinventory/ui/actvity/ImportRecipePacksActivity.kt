package com.example.chiefinventory.ui.actvity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chiefinventory.R
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.databinding.ActivityImportRecipePacksBinding
import com.example.chiefinventory.utils.StarterRecipeSeedLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportRecipePacksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImportRecipePacksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImportRecipePacksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.import_packs_title)

        binding.btnImportBe.setOnClickListener {
            importPack("seeds/recipes_seed_be.json", getString(R.string.pack_name_belgique))
        }

        binding.btnImportIt.setOnClickListener {
            importPack("seeds/recipes_seed_it.json", getString(R.string.pack_name_italie))
        }

        binding.btnImportFr.setOnClickListener {
            importPack("seeds/recipes_seed_fr.json", getString(R.string.pack_name_france))
        }
    }

    private fun importPack(assetPath: String, packName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val result = StarterRecipeSeedLoader.importCountryPackToDatabase(applicationContext, db, assetPath)

            withContext(Dispatchers.Main) {
                val msg = if (result.insertedCount > 0) {
                    getString(R.string.import_pack_toast_new, result.insertedCount, packName)
                } else if (result.updatedCount > 0) {
                    getString(R.string.import_pack_toast_updated, packName, result.updatedCount)
                } else {
                    getString(R.string.import_pack_toast_already_up_to_date, packName)
                }
                Toast.makeText(this@ImportRecipePacksActivity, msg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) { finish(); return true }
        return super.onOptionsItemSelected(item)
    }
}
