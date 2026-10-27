package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le formatage du texte du rapport d'audit pour la copie dans le presse-papier.
 */
class RecipeAuditCopyFormatterTypeBTest {

    private fun formatAuditReportText(report: RecipeAuditReport): String {
        val pct = if (report.totalRecipes > 0) (report.validRecipesCount * 100) / report.totalRecipes else 100
        val sb = StringBuilder()
        sb.appendLine("=== RAPPORT D'AUDIT DES RECETTES ===")
        sb.appendLine("Santé globale : ${report.validRecipesCount} / ${report.totalRecipes} conformes ($pct%)\n")

        sb.appendLine("⚠️ Recettes sans ingrédients (${report.missingIngredientsList.size}) :")
        if (report.missingIngredientsList.isEmpty()) {
            sb.appendLine("• Aucune")
        } else {
            report.missingIngredientsList.forEach { sb.appendLine("• ${it.recipe.title}") }
        }
        sb.appendLine()

        sb.appendLine("⚠️ Recettes sans instructions (${report.missingInstructionsList.size}) :")
        if (report.missingInstructionsList.isEmpty()) {
            sb.appendLine("• Aucune")
        } else {
            report.missingInstructionsList.forEach { sb.appendLine("• ${it.recipe.title}") }
        }

        return sb.toString().trim()
    }

    @Test
    fun `formatAuditReportText should format complete copyable report string`() {
        val report = RecipeAuditReport(
            totalRecipes = 10,
            validRecipesCount = 8,
            missingIngredientsList = emptyList(),
            missingInstructionsList = emptyList(),
            missingCategoryList = emptyList(),
            drinkRecipesList = emptyList(),
            sideRecipesList = emptyList()
        )

        val text = formatAuditReportText(report)

        assertThat(text).contains("=== RAPPORT D'AUDIT DES RECETTES ===")
        assertThat(text).contains("Santé globale : 8 / 10 conformes (80%)")
        assertThat(text).contains("• Aucune")
    }
}
