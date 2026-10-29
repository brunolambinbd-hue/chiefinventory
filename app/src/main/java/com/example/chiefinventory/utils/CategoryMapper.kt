package com.example.chiefinventory.utils

/**
 * A singleton object that normalizes raw recipe category names to canonical meal categories.
 */
object CategoryMapper {

    /**
     * Normalise n'importe quel synonyme de catégorie de recette vers l'une des 5 structures de repas canoniques.
     *
     * @see REQ-4.7.1.1
     * @param rawCategory La chaîne de catégorie brute (ex: "Cocktail", "Sauce", "Gâteau", "Starter").
     * @return La catégorie canonique ("Entrée", "Plat principal", "Dessert", "Boisson", "Accompagnement") ou null.
     */
    fun normalizeRecipeCategory(rawCategory: String?): String? {
        if (rawCategory.isNullOrBlank()) return null
        val lower = rawCategory.lowercase().trim()
        return when {
            lower.contains("boisson") || lower.contains("cocktail") || lower.contains("jus") || lower.contains("sirop") || lower.contains("apéritif") || lower.contains("aperitif") -> "Boisson"
            lower.contains("accompagnement") || lower.contains("sauce") || lower.contains("garniture") || lower.contains("purée") -> "Accompagnement"
            lower.contains("entrée") || lower.contains("entree") || lower.contains("starter") || lower.contains("appetizer") || lower.contains("amuse-bouche") || lower.contains("hors d'œuvre") -> "Entrée"
            lower.contains("plat") || lower.contains("main") || lower.contains("dinner") || lower.contains("viande") || lower.contains("poisson") -> "Plat principal"
            lower.contains("dessert") || lower.contains("gâteau") || lower.contains("gateau") || lower.contains("patisserie") || lower.contains("pâtisserie") || lower.contains("douceur") -> "Dessert"
            else -> null
        }
    }
}
