package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 4 (Merger) dédié à la détection et qualification des tournures d'assaisonnement
 * "sel [de | du | d'] <item>" et "poivre [de | du | d'] <item>".
 */
object Ocr4MergerSeasoningHelper {

    /**
     * Détermine si une ligne de texte correspond à une tournure valide "sel [de | du | d'] <item>"
     * dont le nom de variété/origine figurant après la préposition fait partie du dictionnaire [saltOrigins].
     *
     * @param line La ligne de texte à analyser (ex: "sel de Guérande", "sel de mer").
     * @param saltOrigins La liste des origines/appellations de sel autorisées (ex: de [OcrVocabulary.saltVarietyOrigins]).
     * @return `true` s'il s'agit d'un sel d'origine valide, `false` sinon.
     */
    fun isSaltWithPrepositionOrigin(line: String, saltOrigins: List<String>): Boolean {
        if (line.isBlank() || saltOrigins.isEmpty()) return false
        val lowerLine = line.trim().lowercase()

        // Détection de la préposition de liaison "de", "du", "d'" avec ou sans mot "sel"
        val isPreposition = Regex("(?i)^(?:sel\\s+)?(?:de|du|d['’])\\s+").containsMatchIn(lowerLine)
        if (!isPreposition) return false

        // Vérification de la présence d'un nom d'origine/appellation de sel autorisé
        return saltOrigins.any { origin ->
            val lowerOrigin = origin.trim().lowercase()
            lowerLine.contains(lowerOrigin)
        }
    }

    /**
     * Détermine si une ligne de texte correspond à une tournure valide "poivre [de | du | d'] <item>"
     * dont le nom de variété/origine figurant après la préposition fait partie du dictionnaire [pepperOrigins].
     *
     * @param line La ligne de texte à analyser (ex: "poivre de Cayenne", "poivre du moulin").
     * @param pepperOrigins La liste des origines/appellations de poivre autorisées (ex: de [OcrVocabulary.pepperDeOriginItems]).
     * @return `true` s'il s'agit d'un poivre d'origine valide, `false` sinon.
     */
    fun isPepperWithPrepositionOrigin(line: String, pepperOrigins: List<String>): Boolean {
        if (line.isBlank() || pepperOrigins.isEmpty()) return false
        val lowerLine = line.trim().lowercase()

        // Détection de la préposition de liaison "de", "du", "d'" avec ou sans mot "poivre"
        val isPreposition = Regex("(?i)^(?:poivre\\s+)?(?:de|du|d['’])\\s+").containsMatchIn(lowerLine)
        if (!isPreposition) return false

        // Vérification de la présence d'un nom d'origine/appellation de poivre autorisé
        return pepperOrigins.any { origin ->
            val lowerOrigin = origin.trim().lowercase()
            lowerLine.contains(lowerOrigin)
        }
    }
}
