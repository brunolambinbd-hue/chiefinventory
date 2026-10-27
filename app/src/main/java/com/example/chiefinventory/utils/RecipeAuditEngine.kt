package com.example.chiefinventory.utils

import com.example.chiefinventory.dao.RecipeWithIngredients

/**
 * Rapport d'audit de santé et de conformité des recettes.
 *
 * @property totalRecipes Nombre total de recettes analysées.
 * @property validRecipesCount Nombre de recettes 100% complètes et conformes.
 * @property missingIngredientsList Liste des recettes n'ayant aucun ingrédient.
 * @property missingInstructionsList Liste des recettes n'ayant aucune instruction.
 * @property missingCategoryList Liste des recettes sans catégorie/emplacement attribué.
 */
data class RecipeAuditReport(
    val totalRecipes: Int,
    val validRecipesCount: Int,
    val missingIngredientsList: List<RecipeWithIngredients>,
    val missingInstructionsList: List<RecipeWithIngredients>,
    val missingCategoryList: List<RecipeWithIngredients>,
    val drinkRecipesList: List<RecipeWithIngredients>,
    val sideRecipesList: List<RecipeWithIngredients>
)

/**
 * Moteur d'audit de qualité et de conformité des recettes du carnet.
 */
object RecipeAuditEngine {

    /**
     * Analyse l'ensemble des recettes et produit un rapport de santé.
     *
     * @param recipesWithIngr Liste des recettes avec leurs ingrédients.
     * @param locationNamesMap Map associant l'ID de la catégorie/emplacement à son nom (ex: 10L -> "Plat principal").
     */
    fun auditRecipes(
        recipesWithIngr: List<RecipeWithIngredients>,
        locationNamesMap: Map<Long, String> = emptyMap()
    ): RecipeAuditReport {
        val total = recipesWithIngr.size
        val noIngr = mutableListOf<RecipeWithIngredients>()
        val noInst = mutableListOf<RecipeWithIngredients>()
        val noCat = mutableListOf<RecipeWithIngredients>()
        val drinks = mutableListOf<RecipeWithIngredients>()
        val sides = mutableListOf<RecipeWithIngredients>()

        var validCount = 0

        for (item in recipesWithIngr) {
            var isValid = true

            if (item.ingredients.isEmpty()) {
                noIngr.add(item)
                isValid = false
            }

            if (item.recipe.instructions.isNullOrBlank()) {
                noInst.add(item)
                isValid = false
            }

            val locName = item.recipe.locationId?.let { locationNamesMap[it] }
            val sourceText = item.recipe.source ?: ""
            val titleText = item.recipe.title

            val catFromLoc = CategoryMapper.normalizeRecipeCategory(locName)
            val catFromSource = CategoryMapper.normalizeRecipeCategory(sourceText)
            val catFromTitle = CategoryMapper.normalizeRecipeCategory(titleText)

            val effectiveCategory = catFromLoc ?: catFromSource ?: catFromTitle

            if (effectiveCategory == null) {
                noCat.add(item)
            } else if (effectiveCategory == "Boisson") {
                drinks.add(item)
            } else if (effectiveCategory == "Accompagnement") {
                sides.add(item)
            }

            if (isValid) {
                validCount++
            }
        }

        return RecipeAuditReport(
            totalRecipes = total,
            validRecipesCount = validCount,
            missingIngredientsList = noIngr,
            missingInstructionsList = noInst,
            missingCategoryList = noCat,
            drinkRecipesList = drinks,
            sideRecipesList = sides
        )
    }
}
