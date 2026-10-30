package com.chefinventory.app.ui.actvity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chefinventory.app.dao.RecipeWithIngredients
import com.chefinventory.app.data.AppDatabase
import com.chefinventory.app.databinding.ActivityRecipeAuditBinding
import com.chefinventory.app.utils.RecipeAuditEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecipeAuditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecipeAuditBinding
    private var lastReportText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecipeAuditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCopyAuditReport.setOnClickListener {
            if (lastReportText.isNotBlank()) {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(getString(com.chefinventory.app.R.string.recipe_audit_clip_label), lastReportText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(com.chefinventory.app.R.string.recipe_audit_toast_copied), Toast.LENGTH_SHORT).show()
            }
        }

        runRecipeAudit()
    }

    private fun runRecipeAudit() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            val locations = db.locationDao().getAllSync()
            val locationsMap = locations.associate { it.id to it.name }
            val recipesWithIngr = db.recipeDao().getAllRecipesWithIngredients()
            val report = RecipeAuditEngine.auditRecipes(recipesWithIngr, locationsMap)

            withContext(Dispatchers.Main) {
                val pct = if (report.totalRecipes > 0) (report.validRecipesCount * 100) / report.totalRecipes else 100
                binding.tvValidRecipesCount.text = getString(com.chefinventory.app.R.string.recipe_audit_health_status, report.validRecipesCount, report.totalRecipes, pct)

                buildClickableRecipeList(
                    report.missingIngredientsList,
                    getString(com.chefinventory.app.R.string.recipe_audit_no_missing_ingredients),
                    binding.tvMissingIngredientsList
                )

                buildClickableRecipeList(
                    report.missingInstructionsList,
                    getString(com.chefinventory.app.R.string.recipe_audit_no_missing_instructions),
                    binding.tvMissingInstructionsList
                )

                buildClickableRecipeList(
                    report.missingCategoryList,
                    getString(com.chefinventory.app.R.string.recipe_audit_no_missing_categories),
                    binding.tvMissingCategoryList
                )

                val sb = StringBuilder()
                sb.appendLine("=== RAPPORT D'AUDIT DES RECETTES CHEFINVENTORY ===")
                sb.appendLine("Santé globale : ${report.validRecipesCount} / ${report.totalRecipes} recettes conformes ($pct%)\n")
                sb.appendLine("⚠️ Recettes sans ingrédients (${report.missingIngredientsList.size}) :")
                if (report.missingIngredientsList.isEmpty()) sb.appendLine("• Aucune") else report.missingIngredientsList.forEach { sb.appendLine("• ${it.recipe.title}") }
                sb.appendLine("\n⚠️ Recettes sans instructions (${report.missingInstructionsList.size}) :")
                if (report.missingInstructionsList.isEmpty()) sb.appendLine("• Aucune") else report.missingInstructionsList.forEach { sb.appendLine("• ${it.recipe.title}") }
                sb.appendLine("\n⚠️ Recettes sans catégorie (${report.missingCategoryList.size}) :")
                if (report.missingCategoryList.isEmpty()) sb.appendLine("• Aucune") else report.missingCategoryList.forEach { sb.appendLine("• ${it.recipe.title}") }
                lastReportText = sb.toString()
            }
        }
    }

    private fun buildClickableRecipeList(
        items: List<RecipeWithIngredients>,
        emptyMessage: String,
        textView: TextView
    ) {
        if (items.isEmpty()) {
            textView.text = emptyMessage
            textView.movementMethod = null
            return
        }

        val ssb = SpannableStringBuilder()
        for ((index, item) in items.withIndex()) {
            val recipe = item.recipe
            val lineText = "• ${recipe.title}"
            val start = ssb.length
            ssb.append(lineText)
            val end = ssb.length

            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    val intent = Intent(this@RecipeAuditActivity, EditRecipeActivity::class.java).apply {
                        putExtra(EditRecipeActivity.EXTRA_RECIPE_ID, recipe.id)
                    }
                    startActivity(intent)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true
                }
            }

            ssb.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            if (index < items.size - 1) {
                ssb.append("\n")
            }
        }

        textView.text = ssb
        textView.movementMethod = LinkMovementMethod.getInstance()
    }
}
