package com.example.parabdcollector.utils

object CategoryMapper {

    private val categoryMap = mapOf(
        "Affiches" to "Image",
        "ALBUMS" to "Album",
        "Albums collectifs" to "Album",
        "Albums éditions étrangères" to "Album",
        "Autocollant" to "Objets",
        "Bronze" to "Objets",
        "Calendrier" to "Objets",
        "Cartes de vœux" to "Carte",
        "Cartes postales" to "Carte",
        "Cartes-divers" to "Carte",
        "Catalogues de ventes" to "Promos - Publicités",
        "Catalogues éditeurs" to "Promos - Publicités",
        "Dictionnaires" to "Divers",
        "Dossiers de presse" to "Promos - Publicités",
        "Etiquette" to "Objets",
        "Etudes" to "Divers",
        "Ex-libris" to "Image",
        "Faire-part" to "Carte",
        "ILLUSTRATIONS ALBUMS" to "Illustration",
        "Illustrations livres" to "Illustration",
        "Illustrations presses" to "Illustration",
        "ILLUSTRATIONS REVUES" to "Illustration",
        "Interviews" to "Divers",
        "Invitations" to "Carte",
        "Marque-pages" to "Carte",
        "Objets-divers" to "Objets",
        "Offsets" to "Image",
        "PORTFOLIOS" to "Image",
        "Programmes festivals" to "Promos - Publicités",
        "Promos-divers" to "Promos - Publicités",
        "Sérigraphies" to "Image",
        "T-shirt" to "Objets"
    )

    fun getSuperCategory(category: String): String? {
        return categoryMap[category]
    }

    fun getSuperCategories(): List<String> {
        return categoryMap.values.distinct().sorted()
    }

    fun getCategoriesFor(superCategory: String): List<String> {
        return categoryMap.filterValues { it == superCategory }.keys.sorted()
    }
}