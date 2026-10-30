package com.example.chiefinventory.utils

import android.content.res.Resources
import com.example.chiefinventory.R

/**
 * A singleton object that normalizes raw recipe category names to canonical meal categories.
 */
object CategoryMapper {

    const val CATEGORY_ENTREE: String = "Entrée"
    const val CATEGORY_PLAT: String = "Plat principal"
    const val CATEGORY_DESSERT: String = "Dessert"
    const val CATEGORY_BOISSON: String = "Boisson"
    const val CATEGORY_ACCOMPAGNEMENT: String = "Accompagnement"

    /**
     * Normalise n'importe quel synonyme de catégorie de recette vers l'une des 5 structures de repas canoniques.
     *
     * @see REQ-4.7.1.1
     * @param rawCategory La chaîne de catégorie brute (ex: "Cocktail", "Sauce", "Gâteau", "Starter").
     * @param res Ressources Android optionnelles pour charger les mots-clés localisés depuis XML.
     * @return La catégorie canonique ("Entrée", "Plat principal", "Dessert", "Boisson", "Accompagnement") ou null.
     */
    fun normalizeRecipeCategory(rawCategory: String?, res: Resources? = null): String? {
        if (rawCategory.isNullOrBlank()) return null
        val lower = rawCategory.lowercase().trim()

        val boissonKeywords = res?.getStringArray(R.array.keywords_recipe_boisson)
            ?: arrayOf("boisson", "cocktail", "jus", "sirop", "apéritif", "aperitif")

        val accompagnementKeywords = res?.getStringArray(R.array.keywords_recipe_accompagnement)
            ?: arrayOf("accompagnement", "sauce", "garniture", "purée", "puree")

        val entreeKeywords = res?.getStringArray(R.array.keywords_recipe_entree)
            ?: arrayOf("entrée", "entree", "starter", "appetizer", "amuse-bouche", "hors d'œuvre")

        val platKeywords = res?.getStringArray(R.array.keywords_recipe_plat)
            ?: arrayOf("plat", "main", "dinner", "viande", "poisson")

        val dessertKeywords = res?.getStringArray(R.array.keywords_recipe_dessert)
            ?: arrayOf("dessert", "gâteau", "gateau", "patisserie", "pâtisserie", "douceur")

        val labelBoisson = res?.getString(R.string.category_recipe_boisson) ?: CATEGORY_BOISSON
        val labelAccompagnement = res?.getString(R.string.category_recipe_accompagnement) ?: CATEGORY_ACCOMPAGNEMENT
        val labelEntree = res?.getString(R.string.category_recipe_entree) ?: CATEGORY_ENTREE
        val labelPlat = res?.getString(R.string.category_recipe_plat) ?: CATEGORY_PLAT
        val labelDessert = res?.getString(R.string.category_recipe_dessert) ?: CATEGORY_DESSERT

        return when {
            boissonKeywords.any { it in lower } -> labelBoisson
            accompagnementKeywords.any { it in lower } -> labelAccompagnement
            entreeKeywords.any { it in lower } -> labelEntree
            platKeywords.any { it in lower } -> labelPlat
            dessertKeywords.any { it in lower } -> labelDessert
            else -> null
        }
    }
}
