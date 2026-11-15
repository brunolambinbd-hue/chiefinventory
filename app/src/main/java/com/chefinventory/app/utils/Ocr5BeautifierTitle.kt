package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 5 (Embellissement) dédié à la beautification et à l'harmonisation de la majusculation des titres.
 */
object Ocr5BeautifierTitle {

    /**
     * Harmonise la majusculation d'un titre de recette en Sentence case français (première lettre en majuscule, le reste en minuscules).
     *
     * @param title Le titre brut à beautifier.
     * @return Le titre harmonisé (ex: "Soupe verte aux légumes d'hiver et au roquefort").
     */
    fun beautifyTitle(title: String?): String? {
        if (title.isNullOrBlank()) return title
        return title.trim().lowercase().replaceFirstChar { it.titlecase() }
    }
}
