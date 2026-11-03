package com.chefinventory.app.utils

import android.content.res.Resources
import com.chefinventory.app.R

/**
 * A singleton object that normalizes raw recipe category names to canonical meal categories.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
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

        val boissonKeywords = try { res?.getStringArray(R.array.keywords_recipe_boisson) } catch (_: Exception) { null }
            ?: arrayOf("boisson", "cocktail", "jus", "sirop", "apéritif", "aperitif")

        val accompagnementKeywords = try { res?.getStringArray(R.array.keywords_recipe_accompagnement) } catch (_: Exception) { null }
            ?: arrayOf("accompagnement", "sauce", "garniture", "purée", "puree")

        val entreeKeywords = try { res?.getStringArray(R.array.keywords_recipe_entree) } catch (_: Exception) { null }
            ?: arrayOf("entrée", "entree", "starter", "appetizer", "amuse-bouche", "hors d'œuvre")

        val platKeywords = try { res?.getStringArray(R.array.keywords_recipe_plat) } catch (_: Exception) { null }
            ?: arrayOf("plat", "main", "dinner", "viande", "poisson")

        val dessertKeywords = try { res?.getStringArray(R.array.keywords_recipe_dessert) } catch (_: Exception) { null }
            ?: arrayOf("dessert", "gâteau", "gateau", "patisserie", "pâtisserie", "douceur")

        val labelBoisson = try { res?.getString(R.string.category_recipe_boisson) } catch (_: Exception) { null } ?: CATEGORY_BOISSON
        val labelAccompagnement = try { res?.getString(R.string.category_recipe_accompagnement) } catch (_: Exception) { null } ?: CATEGORY_ACCOMPAGNEMENT
        val labelEntree = try { res?.getString(R.string.category_recipe_entree) } catch (_: Exception) { null } ?: CATEGORY_ENTREE
        val labelPlat = try { res?.getString(R.string.category_recipe_plat) } catch (_: Exception) { null } ?: CATEGORY_PLAT
        val labelDessert = try { res?.getString(R.string.category_recipe_dessert) } catch (_: Exception) { null } ?: CATEGORY_DESSERT

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
