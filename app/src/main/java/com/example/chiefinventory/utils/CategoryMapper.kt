package com.example.chiefinventory.utils

/**
 * A singleton object that maps detailed, raw categories from the data source to broader, standardized super-categories.
 *
 * This utility is essential for organizing and navigating the collection by providing a clean, hierarchical structure.
 * For example, it maps both "Affiches" and "Sérigraphies" to the [SUPER_CAT_IMAGE] super-category.
 */
object CategoryMapper {

    private const val SUPER_CAT_IMAGE = "Image"
    private const val SUPER_CAT_ALBUM = "Album"
    private const val SUPER_CAT_OBJETS = "Objets"
    private const val SUPER_CAT_CARTE = "Carte"
    private const val SUPER_CAT_PROMOS = "Promos - Publicités"
    private const val SUPER_CAT_DIVERS = "Divers"
    private const val SUPER_CAT_ILLUSTRATION = "Illustration"
    private const val SUPER_CAT_PRESSE = "Presse"

    /**
     * The definitive mapping of specific categories to their standardized super-category.
     * This map is private to ensure that all interactions go through the public functions.
     */
    private val categoryMap = mapOf(
        "Affiches" to SUPER_CAT_IMAGE,
        "Albums" to SUPER_CAT_ALBUM,
        "Albums collectifs" to SUPER_CAT_ALBUM,
        "Albums éditions étrangères" to SUPER_CAT_ALBUM,
        "Autocollant" to SUPER_CAT_OBJETS,
        "Bronze" to SUPER_CAT_OBJETS,
        "Calendrier" to SUPER_CAT_OBJETS,
        "Cartes de vœux" to SUPER_CAT_CARTE,
        "Cartes postales" to SUPER_CAT_CARTE,
        "Cartes-divers" to SUPER_CAT_CARTE,
        "Catalogues de ventes" to SUPER_CAT_PROMOS,
        "Catalogues éditeurs" to SUPER_CAT_PROMOS,
        "Dictionnaires" to SUPER_CAT_DIVERS,
        "Dossiers de presse" to SUPER_CAT_PROMOS,
        "Etiquette" to SUPER_CAT_OBJETS,
        "Etudes" to SUPER_CAT_DIVERS,
        "Ex-libris" to SUPER_CAT_IMAGE,
        "Faire-part" to SUPER_CAT_CARTE,
        "ILLUSTRATIONS ALBUMS" to SUPER_CAT_ILLUSTRATION,
        "Illustrations livres" to SUPER_CAT_ILLUSTRATION,
        "Illustrations presses" to SUPER_CAT_ILLUSTRATION,
        "ILLUSTRATIONS REVUES" to SUPER_CAT_ILLUSTRATION,
        "Interviews" to SUPER_CAT_DIVERS,
        "Invitations" to SUPER_CAT_CARTE,
        "Marque-pages" to SUPER_CAT_CARTE,
        "Objets-divers" to SUPER_CAT_OBJETS,
        "Offsets" to SUPER_CAT_IMAGE,
        "Portfolios" to SUPER_CAT_IMAGE,
        "Programmes festivals" to SUPER_CAT_PROMOS,
        "Promos-divers" to SUPER_CAT_PROMOS,
        "Sérigraphies" to SUPER_CAT_IMAGE,
        "T-shirt" to SUPER_CAT_OBJETS,
        "Travaux pour Spirou" to SUPER_CAT_PRESSE
    )

    /**
     * Returns a distinct, alphabetically sorted list of all available super-categories.
     * @return A sorted list of unique super-category names.
     */
    fun getSuperCategories(): List<String> {
        return categoryMap.values.distinct().sorted()
    }

    /**
     * Returns a sorted list of all detailed categories that belong to a given super-category.
     * @param superCategory The name of the super-category to filter by (e.g., "Image").
     * @return A sorted list of matching category names (e.g., ["Affiches", "Ex-libris", ...]).
     */
    fun getCategoriesFor(superCategory: String): List<String> {
        return categoryMap.filterValues { it == superCategory }.keys.sorted()
    }

    /**
     * Returns the standardized super-category for a given raw category.
     * @param category The raw category name.
     * @return The matching super-category name, or null if no rule exists.
     */
    fun getSuperCategoryFor(category: String): String? {
        return categoryMap[category]
    }
}
