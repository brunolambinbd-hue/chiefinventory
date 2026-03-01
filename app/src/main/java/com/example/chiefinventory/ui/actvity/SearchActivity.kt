package com.example.chiefinventory.ui.actvity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.*
import com.example.chiefinventory.*
import com.example.chiefinventory.databinding.ActivitySearchBinding
import com.example.chiefinventory.model.SearchCriteria
import com.example.chiefinventory.ui.adapter.CollectionAdapter
import com.example.chiefinventory.ui.viewmodel.*
import com.example.chiefinventory.utils.*

class SearchActivity : AppCompatActivity() {
    private lateinit var b: ActivitySearchBinding; private lateinit var ad: CollectionAdapter; private lateinit var img: ImageCaptureUtil
    private var desc = ""; private var bmp: Bitmap? = null; private var simple = true; private var q: String? = null; private var crit: SearchCriteria? = null
    private val vm: SearchViewModel by viewModels { val a = application as CollectionApplication; ViewModelFactory(a, a.repository, a.locationRepository, a.ingredientRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivitySearchBinding.inflate(layoutInflater); setContentView(b.root)
        setSupportActionBar(b.toolbar); supportActionBar?.setDisplayHomeAsUpEnabled(true); supportActionBar?.title = getString(R.string.menu_search_title)
        ViewCompat.setOnApplyWindowInsetsListener(b.root) { v, i -> val s = i.getInsets(WindowInsetsCompat.Type.systemBars()); v.updatePadding(s.left, s.top, s.right, s.bottom); WindowInsetsCompat.CONSUMED }
        img = ImageCaptureUtil(this) { u ->
            if (u == null) return@ImageCaptureUtil
            val bit = BitmapUtils.getBitmapFromUri(this, u)
            if (bit != null) {
                bmp = bit; b.ivSearchImagePreview.setImageBitmap(bit)
                if (b.advancedSearchFields.isGone) toggleAdvancedSearch()
                b.ivSearchImagePreview.isVisible = true; vm.calculateSignatureForPreview(bit)
            } else b.ivSearchImagePreview.isGone = true
            validateSearchButton()
        }
        setupRecyclerView(); setupSpinners(); setupClickListeners(); setupTextWatchers(); observeViewModel(); validateSearchButton()
    }

    private fun setupClickListeners() {
        b.btnSearch.setOnClickListener { performSearch() }
        b.ivSearchImagePreview.setOnClickListener { img.startCamera() }
        b.tvToggleAdvancedSearch.setOnClickListener { toggleAdvancedSearch() }
        b.btnRetry.setOnClickListener { retryLastSearch() }
        b.btnSearchByCamera.setOnClickListener { img.startCamera() }
        b.btnSearchByGallery.setOnClickListener { img.startGallery() }
        b.fabScrollToTop.setOnClickListener { b.searchScrollView.smoothScrollTo(0, 0) }
    }

    private fun setupTextWatchers() {
        val w = { _: Editable? -> validateSearchButton() }
        b.etSearchSimple.doAfterTextChanged(w); b.etSearchTitle.doAfterTextChanged(w); b.etSearchSuperCategory.doAfterTextChanged(w)
        b.etSearchCategory.doAfterTextChanged(w); b.etSearchEditor.doAfterTextChanged(w); b.etSearchYear.doAfterTextChanged(w)
        b.etSearchMonth.doAfterTextChanged(w); b.etSearchDescription.doAfterTextChanged(w); b.etSearchTirage.doAfterTextChanged(w)
        b.etSearchDimensions.doAfterTextChanged(w); b.etSearchStatus.doAfterTextChanged(w)
    }

    private fun validateSearchButton() {
        val isAdv = b.advancedSearchFields.isVisible
        val ok = if (isAdv) {
            val fs = listOf(b.etSearchTitle, b.etSearchSuperCategory, b.etSearchCategory, b.etSearchEditor, b.etSearchYear, b.etSearchMonth, b.etSearchDescription, b.etSearchTirage, b.etSearchDimensions)
            val anyF = fs.any { !it.text.isNullOrBlank() }
            val st = b.etSearchStatus.text.toString(); val stOk = st.isNotBlank() && st != resources.getStringArray(R.array.search_status_options)[0]
            anyF || bmp != null || stOk
        } else {
            !b.etSearchSimple.text.isNullOrBlank()
        }
        b.btnSearch.isEnabled = ok
    }

    private fun observeViewModel() {
        vm.searchResultState.observe(this) { s ->
            b.progressBar.isVisible = s is SearchResultState.Loading; b.resultsListContainer.isVisible = s is SearchResultState.Success
            b.errorContainer.isVisible = s is SearchResultState.Error
            // On cache "tvNoResults" si c'est une recherche image pour utiliser le bandeau tvResultsSummary à la place
            b.tvNoResults.isVisible = s is SearchResultState.Success && s.results.isEmpty() && bmp == null
            b.fabScrollToTop.isVisible = s is SearchResultState.Success && s.results.isNotEmpty()
            if (s is SearchResultState.Success) {
                ad.submitList(s.results); updateResultSummary(s.totalCount, s.results.size)
            } else if (s is SearchResultState.Idle) { ad.submitList(emptyList()); updateResultSummary(0, 0) }
        }
        vm.signaturePreview.observe(this) { p -> b.tvSignaturePreview.text = p; b.tvSignaturePreview.isVisible = p.isNotBlank() }
    }

    private fun updateResultSummary(total: Int, displayed: Int) {
        if (total > 0) {
            val base = resources.getQuantityString(R.plurals.search_results_count_with_criteria, total, total, desc)
            val info = when {
                bmp != null -> getString(R.string.search_visual_matches, displayed)
                total > displayed -> getString(R.string.search_displayed_count, displayed)
                else -> ""
            }
            b.tvResultsSummary.text = getString(R.string.search_results_summary_format, base, info)
            b.tvResultsSummary.isVisible = true
        } else if (bmp != null) {
            b.tvResultsSummary.text = getString(R.string.search_no_visual_matches)
            b.tvResultsSummary.isVisible = true
        } else b.tvResultsSummary.isGone = true
    }

    private fun clearFields() {
        listOf(b.etSearchTitle, b.etSearchEditor, b.etSearchYear, b.etSearchMonth, b.etSearchDescription, b.etSearchTirage, b.etSearchDimensions).forEach { it.setText("") }
        b.etSearchSuperCategory.setText("", false); b.etSearchCategory.setText("", false)
        b.etSearchStatus.setText(resources.getStringArray(R.array.search_status_options)[0], false)
        bmp = null; b.ivSearchImagePreview.isGone = true; b.tvSignaturePreview.isGone = true
    }

    private fun resetSearchState() {
        b.etSearchSimple.setText(""); b.etSearchSimple.isEnabled = true; b.etSearchSimple.alpha = 1.0f
        if (b.advancedSearchFields.isVisible) toggleAdvancedSearch()
        clearFields(); desc = ""; q = null; crit = null; vm.clearSearchResults(); validateSearchButton()
    }

    private fun retryLastSearch() {
        if (simple && q != null) vm.search(q!!) else if (!simple && crit != null) vm.advancedSearch(crit!!, bmp) else resetSearchState()
    }

    private fun toggleAdvancedSearch() {
        val open = b.advancedSearchFields.isGone
        if (open) {
            b.advancedSearchFields.isVisible = true; b.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_hide)
            b.etSearchSimple.setText(""); b.etSearchSimple.isEnabled = false; b.etSearchSimple.alpha = 0.5f
        } else {
            b.advancedSearchFields.isGone = true; b.tvToggleAdvancedSearch.text = getString(R.string.advanced_search_show)
            b.etSearchSimple.isEnabled = true; b.etSearchSimple.alpha = 1.0f; clearFields()
        }
        validateSearchButton()
    }

    private fun setupSpinners() {
        val opts = resources.getStringArray(R.array.search_status_options); b.etSearchStatus.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, opts))
        b.etSearchStatus.setText(opts[0], false); val sc = CategoryMapper.getSuperCategories()
        b.etSearchSuperCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, sc))
        b.etSearchSuperCategory.setOnItemClickListener { p, _, pos, _ ->
            val sel = p.getItemAtPosition(pos) as String
            b.etSearchCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, CategoryMapper.getCategoriesFor(sel)))
            b.layoutSearchCategory.isEnabled = true; b.etSearchCategory.setText("", false); validateSearchButton()
        }
    }

    private fun performSearch() {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        if (b.advancedSearchFields.isVisible) {
            val o = resources.getStringArray(R.array.search_status_options); val s = b.etSearchStatus.text.toString()
            val isP = when (s) { o[1] -> true; o[2] -> false; else -> null }
            val c = SearchCriteria(b.etSearchTitle.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchSuperCategory.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchCategory.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchEditor.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchYear.text.toString().trim().toIntOrNull(), b.etSearchMonth.text.toString().trim().toIntOrNull(), b.etSearchDescription.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchTirage.text.toString().trim().takeIf { it.isNotBlank() }, b.etSearchDimensions.text.toString().trim().takeIf { it.isNotBlank() }, isP)
            crit = c 
            simple = false
            val prefix = if (bmp != null) "Image + " else ""
            desc = prefix + listOfNotNull(c.titre, s, c.superCategorie, c.categorie).joinToString(", ").ifBlank { "Avancée" }
            vm.advancedSearch(c, bmp)
        } else {
            val query = b.etSearchSimple.text.toString().trim()
            if (query.isNotBlank()) { desc = query; simple = true; q = query; vm.search(query) }
        }
    }

    private fun setupRecyclerView() { ad = CollectionAdapter { r -> val i = Intent(this, EditItemActivity::class.java); i.putExtra("itemId", r.item.id); startActivity(i) }; b.rvSearchResults.adapter = ad; b.rvSearchResults.layoutManager = LinearLayoutManager(this) }
    override fun onCreateOptionsMenu(m: Menu?): Boolean { menuInflater.inflate(R.menu.search_menu, m); return true }
    override fun onOptionsItemSelected(i: MenuItem): Boolean {
        when (i.itemId) {
            android.R.id.home -> { finish(); return true }
            R.id.action_refine_search -> { b.resultsListContainer.isGone = true; b.errorContainer.isGone = true; b.tvNoResults.isGone = true; b.progressBar.isGone = true; return true }
            R.id.action_new_search -> { resetSearchState(); return true }
        }
        return super.onOptionsItemSelected(i)
    }
}
