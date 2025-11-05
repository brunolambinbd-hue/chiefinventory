package com.example.parabdcollector.ui

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.parabdcollector.R
import com.example.parabdcollector.databinding.ActivityFullScreenImageBinding

class FullScreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenImageBinding
    private var areSystemBarsVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        // Récupération des données de l'intent
        val imageUriString = intent.getStringExtra(EXTRA_IMAGE_URI)
        val title = intent.getStringExtra(EXTRA_TITLE)
        val universe = intent.getStringExtra(EXTRA_UNIVERSE)
        val editeur = intent.getStringExtra(EXTRA_EDITOR)
        val year = intent.getIntExtra(EXTRA_YEAR, 0)
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val material = intent.getStringExtra(EXTRA_MATERIAL)
        val run = intent.getStringExtra(EXTRA_RUN)
        val dimensions = intent.getStringExtra(EXTRA_DIMENSIONS)

        // Affichage de l'image
        if (imageUriString != null) {
            binding.fullScreenImageView.setImageURI(imageUriString.toUri())
        }

        // Affichage des informations textuelles avec libellés
        binding.imageInfoTitle.text = title
        
        binding.imageInfoUniverse.text = universe?.let { getString(R.string.item_universe_hint) + ": " + it } ?: ""
        binding.imageInfoUniverse.visibility = if (universe.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoManufacturer.text = editeur?.let { getString(R.string.item_editor_hint) + ": " + it } ?: ""
        binding.imageInfoManufacturer.visibility = if (editeur.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoYear.text = if (year != 0) getString(R.string.item_year_hint) + ": " + year.toString() else ""
        binding.imageInfoYear.visibility = if (year == 0) View.GONE else View.VISIBLE

        binding.imageInfoCategory.text = category?.let { getString(R.string.item_category_hint) + ": " + it } ?: ""
        binding.imageInfoCategory.visibility = if (category.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoMaterial.text = material?.let { getString(R.string.item_material_hint) + ": " + it } ?: ""
        binding.imageInfoMaterial.visibility = if (material.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoRun.text = run?.let { getString(R.string.item_run_hint) + ": " + it } ?: ""
        binding.imageInfoRun.visibility = if (run.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoDimensions.text = dimensions?.let { getString(R.string.item_dimensions_hint) + ": " + it } ?: ""
        binding.imageInfoDimensions.visibility = if (dimensions.isNullOrBlank()) View.GONE else View.VISIBLE


        // Gestion du clic pour le mode immersif
        binding.fullScreenImageView.setOnClickListener {
            toggleSystemUI()
        }
    }

    private fun toggleSystemUI() {
        if (areSystemBarsVisible) {
            // Masquer les barres
            binding.toolbar.visibility = View.GONE
            binding.infoContainer.parent.let { if(it is View) it.visibility = View.GONE }
        } else {
            // Afficher les barres
            binding.toolbar.visibility = View.VISIBLE
            binding.infoContainer.parent.let { if(it is View) it.visibility = View.VISIBLE }
        }
        areSystemBarsVisible = !areSystemBarsVisible
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val EXTRA_IMAGE_URI = "image_uri"
        const val EXTRA_TITLE = "title"
        const val EXTRA_UNIVERSE = "universe"
        const val EXTRA_EDITOR = "editor"
        const val EXTRA_YEAR = "year"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_MATERIAL = "material"
        const val EXTRA_RUN = "run"
        const val EXTRA_DIMENSIONS = "dimensions"
    }
}