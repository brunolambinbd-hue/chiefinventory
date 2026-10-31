package com.chefinventory.app.ui.actvity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.chefinventory.app.R
import com.chefinventory.app.databinding.ActivityFullScreenImageBinding

/**
 * An activity to display a single image in full-screen, with overlayed item details.
 *
 * This activity is launched with a URI and various item details passed as Intent extras.
 * It supports a basic immersive mode by toggling the visibility of system bars and info panels on tap.
 */
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

        // Retrieve data from the intent
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

        // Load the image using Coil
        if (imageUriString != null) {
            binding.fullScreenImageView.load(imageUriString) {
                placeholder(R.mipmap.ic_launcher)
                error(R.mipmap.ic_launcher)
            }
        }

        // Populate the info overlay
        binding.imageInfoTitle.text = title

        fun setInfoText(textView: TextView, labelResId: Int, value: String?) {
            if (value.isNullOrBlank()) {
                textView.visibility = View.GONE
            } else {
                textView.text = getString(R.string.generic_field_format, getString(labelResId), value)
                textView.visibility = View.VISIBLE
            }
        }

        setInfoText(binding.imageInfoManufacturer, R.string.item_editor_hint, editor)
        setInfoText(binding.imageInfoSupercategory, R.string.item_super_category_hint, superCategory)
        setInfoText(binding.imageInfoCategory, R.string.item_category_hint, category)
        setInfoText(binding.imageInfoMaterial, R.string.item_material_hint, material)
        setInfoText(binding.imageInfoRun, R.string.item_run_hint, run)
        setInfoText(binding.imageInfoDimensions, R.string.item_dimensions_hint, dimensions)

        var yearMonthText = ""
        if (year > 0) {
            yearMonthText += getString(R.string.item_year_hint) + ": " + year.toString()
            if (month in 1..12) {
                yearMonthText += "/$month"
            }
        }
        binding.imageInfoYear.text = yearMonthText
        binding.imageInfoYear.visibility = if (yearMonthText.isBlank()) View.GONE else View.VISIBLE

        binding.imageInfoDescription.text = description
        binding.imageInfoDescription.visibility = if (description.isNullOrBlank()) View.GONE else View.VISIBLE

        binding.debugSignatureInfo.visibility = View.GONE

        // Set up immersive mode toggle
        binding.fullScreenImageView.setOnClickListener {
            toggleSystemUI()
        }
    }

    /**
     * Toggles the visibility of the system bars (status bar, action bar) and the info overlay.
     */
    private fun toggleSystemUI() {
        if (areSystemBarsVisible) {
            // Hide bars
            binding.toolbar.visibility = View.GONE
            binding.infoContainer.parent.let { if (it is View) it.visibility = View.GONE }
        } else {
            // Show bars
            binding.toolbar.visibility = View.VISIBLE
            binding.infoContainer.parent.let { if (it is View) it.visibility = View.VISIBLE }
        }
        areSystemBarsVisible = !areSystemBarsVisible
    }

    /**
     * Handles the back arrow click in the toolbar.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        /** Key for the String extra that holds the image URI. */
        const val EXTRA_IMAGE_URI: String = "image_uri"
        /** Key for the String extra that holds the item title. */
        const val EXTRA_TITLE: String = "title"
        /** Key for the String extra that holds the item editor. */
        const val EXTRA_EDITOR: String = "editor"
        /** Key for the Int extra that holds the item year. */
        const val EXTRA_YEAR: String = "year"
        /** Key for the String extra that holds the item month. */
        const val EXTRA_MONTH: String = "month"
        /** Key for the String extra that holds the item super-category. */
        const val EXTRA_SUPER_CATEGORY: String = "super_category"
        /** Key for the String extra that holds the item category. */
        const val EXTRA_CATEGORY: String = "category"
        /** Key for the String extra that holds the item material. */
        const val EXTRA_MATERIAL: String = "material"
        /** Key for the String extra that holds the item print run. */
        const val EXTRA_RUN: String = "run"
        /** Key for the String extra that holds the item dimensions. */
        const val EXTRA_DIMENSIONS: String = "dimensions"
        /** Key for the String extra that holds the item description. */
        const val EXTRA_DESCRIPTION: String = "description"
    }
}
