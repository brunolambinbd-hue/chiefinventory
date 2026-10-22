package com.example.chiefinventory.utils

/**
 * Représente le résultat structuré et nettoyé issu du traitement d'une fiche de recette par le pipeline OCR.
 *
 * @property title Le titre principal de la recette (ex: "Canapés grecs"), ou null si non trouvé.
 * @property ingredients La liste des ingrédients nettoyés et formatés, séparés par des sauts de ligne (`\n`).
 * @property instructions La liste des consignes de préparation nettoyées et ordonnées, séparées par des sauts de ligne (`\n`).
 * @property wine La recommandation de vin ou accord mets-vins extrait (ex: "Touraine Sauvignon blanc, 1995, 75 cl").
 * @property source La provenance ou référence de la recette (ex: "Maxi GB", "Cuisine Actuelle").
 * @property servings Le nombre de personnes ou portions (ex: "4").
 * @property prepTime Le temps de préparation en minutes (ex: "15").
 * @property cookTime Le temps de cuisson en minutes (ex: "45").
 * @property restingTime Le temps de repos ou de marination en minutes (ex: "30").
 * @property kcalPerServing L'apport calorique par personne en KCAL (ex: "250").
 * @property difficulty Le niveau de difficulté de la recette (ex: "Facile", "Moyen").
 * @property nutritionInfo Le détail synthétique des valeurs nutritionnelles (ex: "156 kcal/100g (Lipides: 9.7g, Protéines: 8.8g)").
 */
data class RecipeOcrResult(
    val title: String? = null,
    val ingredients: String? = null,
    val instructions: String? = null,
    val wine: String? = null,
    val source: String? = null,
    val servings: String? = null,
    val prepTime: String? = null,
    val cookTime: String? = null,
    val restingTime: String? = null,
    val kcalPerServing: String? = null,
    val difficulty: String? = null,
    val nutritionInfo: String? = null
)
