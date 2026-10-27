package com.example.chiefinventory.ui.actvity

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chiefinventory.data.AppDatabase
import com.example.chiefinventory.databinding.ActivityRecipeAuditBinding
import com.example.chiefinventory.utils.RecipeAuditEngine
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
                val clip = ClipData.newPlainText(getString(com.example.chiefinventory.R.string.recipe_audit_clip_label), lastReportText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, getString(com.example.chiefinventory.R.string.recipe_audit_toast_copied), Toast.LENGTH_SHORT).show()
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
                binding.tvValidRecipesCount.text = getString(com.example.chiefinventory.R.string.recipe_audit_health_status, report.validRecipesCount, report.totalRecipes, pct)

                binding.tvMissingIngredientsList.text = if (report.missingIngredientsList.isNotEmpty()) {
                    report.missingIngredientsList.joinToString("\n") { "• ${it.recipe.title}" }
                } else {
                    getString(com.example.chiefinventory.R.string.recipe_audit_no_missing_ingredients)
                }

                binding.tvMissingInstructionsList.text = if (report.missingInstructionsList.isNotEmpty()) {
                    report.missingInstructionsList.joinToString("\n") { "• ${it.recipe.title}" }
                } else {
                    getString(com.example.chiefinventory.R.string.recipe_audit_no_missing_instructions)
                }

                binding.tvMissingCategoryList.text = if (report.missingCategoryList.isNotEmpty()) {
                    report.missingCategoryList.joinToString("\n") { "• ${it.recipe.title}" }
                } else {
                    getString(com.example.chiefinventory.R.string.recipe_audit_no_missing_categories)
                }

                binding.tvDrinkRecipesList.text = if (report.drinkRecipesList.isNotEmpty()) {
                    report.drinkRecipesList.joinToString("\n") { "• ${it.recipe.title}" }
                } else {
                    getString(com.example.chiefinventory.R.string.recipe_audit_no_drinks)
                }

                val sb = StringBuilder()
                sb.appendLine("=== RAPPORT D'AUDIT DES RECETTES CHEFINVENTORY ===")
                sb.appendLine("Santé globale : ${report.validRecipesCount} / ${report.totalRecipes} recettes conformes ($pct%)\n")
                sb.appendLine("⚠️ Recettes sans ingrédients (${report.missingIngredientsList.size}) :")
                if (report.missingIngredientsList.isEmpty()) sb.appendLine("• Aucune") else report.missingIngredientsList.forEach { sb.appendLine("• ${it.recipe.title}") }
                sb.appendLine("\n⚠️ Recettes sans instructions (${report.missingInstructionsList.size}) :")
                if (report.missingInstructionsList.isEmpty()) sb.appendLine("• Aucune") else report.missingInstructionsList.forEach { sb.appendLine("• ${it.recipe.title}") }
                sb.appendLine("\n⚠️ Recettes sans catégorie (${report.missingCategoryList.size}) :")
                if (report.missingCategoryList.isEmpty()) sb.appendLine("• Aucune") else report.missingCategoryList.forEach { sb.appendLine("• ${it.recipe.title}") }
                sb.appendLine("\n🍹 Recettes classées en Boissons (${report.drinkRecipesList.size}) :")
                if (report.drinkRecipesList.isEmpty()) sb.appendLine("• Aucune") else report.drinkRecipesList.forEach { sb.appendLine("• ${it.recipe.title}") }
                lastReportText = sb.toString()
            }
        }
    }
}
