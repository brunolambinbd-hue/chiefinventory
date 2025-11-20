package com.example.parabdcollector.ui

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
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
        val editor = intent.getStringExtra(EXTRA_EDITOR)
        val year = intent.getIntExtra(EXTRA_YEAR, 0)
        val month = intent.getIntExtra(EXTRA_MONTH, 0)
        val superCategory = intent.getStringExtra(EXTRA_SUPER_CATEGORY)
        val category = intent.getStringExtra(EXTRA_CATEGORY)
        val material = intent.getStringExtra(EXTRA_MATERIAL)
        val run = intent.getStringExtra(EXTRA_RUN)
        val dimensions = intent.getStringExtra(EXTRA_DIMENSIONS)
        val description = intent.getStringExtra(EXTRA_DESCRIPTION)
        val signature = intent.getByteArrayExtra(EXTRA_IMAGE_SIGNATURE)

        // Affichage de l'image avec Coil
        if (imageUriString != null) {
            binding.fullScreenImageView.load(imageUriString) {
                placeholder(R.mipmap.ic_launcher)
                error(R.mipmap.ic_launcher)
            }
        }

        // Affichage des informations textuelles avec libellés
        binding.imageInfoTitle.text = title
        
        binding.imageInfoManufacturer.text = editor?.let { getString(R.string.item_editor_hint) + ": " + it } ?: ""
        binding.imageInfoManufacturer.visibility = if (editor.isNullOrBlank()) View.GONE else View.VISIBLE

        var yearMonthText = ""
        if (year != 0) {
            yearMonthText += getString(R.string.item_year_hint) + ": " + year.toString()
            if (month != 0) {
                yearMonthText += "/$month"
            }
        }
        binding.imageInfoYear.text = yearMonthText
        binding.imageInfoYear.visibility = if (yearMonthText.isBlank()) View.GONE else View.VISIBLE

        binding.imageInfoSupercategory.text = superCategory?.let { getString(R.string.item_super_category_hint) + ": " + it } ?: ""
        binding.imageInfoSupercategory.visibility = if (superCategory.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoCategory.text = category?.let { getString(R.string.item_category_hint) + ": " + it } ?: ""
        binding.imageInfoCategory.visibility = if (category.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoMaterial.text = material?.let { getString(R.string.item_material_hint) + ": " + it } ?: ""
        binding.imageInfoMaterial.visibility = if (material.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoRun.text = run?.let { getString(R.string.item_run_hint) + ": " + it } ?: ""
        binding.imageInfoRun.visibility = if (run.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoDimensions.text = dimensions?.let { getString(R.string.item_dimensions_hint) + ": " + it } ?: ""
        binding.imageInfoDimensions.visibility = if (dimensions.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.imageInfoDescription.text = description
        binding.imageInfoDescription.visibility = if (description.isNullOrBlank()) View.GONE else View.VISIBLE

        // On affiche les infos de debug si on est en mode "debug"
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebuggable) {
            val sigInfo = signature?.size?.let { "$it bytes" } ?: "N/A"
            binding.debugSignatureInfo.text = getString(R.string.debug_signature_info_fullscreen, sigInfo)
            binding.debugSignatureInfo.visibility = View.VISIBLE
        } else {
            binding.debugSignatureInfo.visibility = View.GONE
        }

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
        const val EXTRA_EDITOR = "editor"
        const val EXTRA_YEAR = "year"
        const val EXTRA_MONTH = "month"
        const val EXTRA_SUPER_CATEGORY = "super_category"
        const val EXTRA_CATEGORY = "category"
        const val EXTRA_MATERIAL = "material"
        const val EXTRA_RUN = "run"
        const val EXTRA_DIMENSIONS = "dimensions"
        const val EXTRA_DESCRIPTION = "description"
        const val EXTRA_IMAGE_SIGNATURE = "image_signature"
    }
}