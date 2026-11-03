package com.chefinventory.app.utils

/**
 * Conteneur de données regroupant les sections brutes catégorisées et méta-données extraites lors de l'Étape 3 du pipeline OCR.
 *
 * @property rawIngredientsList Liste mutable des lignes catégorisées comme ingrédients bruts.
 * @property rawInstructionsList Liste mutable des lignes catégorisées comme consignes d'instructions brutes.
 * @property detectedWineList Liste mutable des recommandations de vins extraites.
 * @property detectedSourceList Liste mutable des provenances/sources extraites.
 * @property detectedTitle Le titre de la recette s'il est détecté à cette étape, ou null.
 * @property detectedServings Le nombre de personnes/portions extrait (ex: "4").
 * @property detectedPrepTime Le temps de préparation extrait en minutes (ex: "15").
 * @property detectedCookTime Le temps de cuisson extrait en minutes (ex: "45").
 * @property detectedRestingTime Le temps de repos extrait en minutes (ex: "30").
 * @property detectedKcal Les kilocalories par portion extraites (ex: "250").
 * @property detectedDifficulty La difficulté extraite (ex: "Facile").
 * @property ingredientKeywords L'ensemble des mots-clés d'ingrédients inventoriés pour le suivi des réapparitions.
 */
data class RawSections(
    val rawIngredientsList: MutableList<String> = mutableListOf(),
    val rawInstructionsList: MutableList<String> = mutableListOf(),
    val detectedWineList: MutableList<String> = mutableListOf(),
    val detectedSourceList: MutableList<String> = mutableListOf(),
    var detectedTitle: String? = null,
    var detectedServings: String? = null,
    var detectedPrepTime: String? = null,
    var detectedCookTime: String? = null,
    var detectedRestingTime: String? = null,
    var detectedKcal: String? = null,
    var detectedDifficulty: String? = null,
    var detectedPriceRating: String? = null,
    var detectedNutritionInfo: String? = null,
    var ingredientKeywords: Set<String> = emptySet()
)
