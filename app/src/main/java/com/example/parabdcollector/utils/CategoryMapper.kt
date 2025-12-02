package com.example.parabdcollector.utils

/**
 * A singleton object that maps detailed categories to broader super-categories.
 *
 * This utility is used to standardize and group items from the original data source
 * into a more manageable hierarchy.
 */
object CategoryMapper {

    private const val SUPER_CAT_IMAGE = "Image"
    private const val SUPER_CAT_ALBUM = "Album"
    private const val SUPER_CAT_OBJETS = "Objets"
    private const val SUPER_CAT_CARTE = "Carte"
    private const val SUPER_CAT_PROMOS = "Promos - Publicités"
    private const val SUPER_CAT_DIVERS = "Divers"
    private const val SUPER_CAT_ILLUSTRATION = "Illustration"

    private val categoryMap = mapOf(
        "Affiches" to SUPER_CAT_IMAGE,
        "ALBUMS" to SUPER_CAT_ALBUM,
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
        "PORTFOLIOS" to SUPER_CAT_IMAGE,
        "Programmes festivals" to SUPER_CAT_PROMOS,
        "Promos-divers" to SUPER_CAT_PROMOS,
        "Sérigraphies" to SUPER_CAT_IMAGE,
        "T-shirt" to SUPER_CAT_OBJETS
    )

    /**
     * Returns a distinct, sorted list of all available super-categories.
     * @return A list of super-category names.
     */
    fun getSuperCategories(): List<String> {
        return categoryMap.values.distinct().sorted()
    }

    /**
     * Returns a sorted list of all detailed categories that belong to a given super-category.
     * @param superCategory The name of the super-category to filter by.
     * @return A list of matching category names.
     */
    fun getCategoriesFor(superCategory: String): List<String> {
        return categoryMap.filterValues { it == superCategory }.keys.sorted()
    }
}
