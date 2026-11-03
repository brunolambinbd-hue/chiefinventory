package com.chefinventory.app.utils

/**
 * Conteneur du résultat d'extraction du titre d'une recette (Étape 2.5).
 *
 * @property title Le titre extrait et nettoyé de la recette, ou null si aucun titre valide n'est trouvé.
 * @property cleanedLines Les lignes de texte restantes après extraction du titre.
 */
data class TitleExtractionResult(
    val title: String?,
    val cleanedLines: List<String>
)

/**
 * Conteneur interne représentant un candidat titre évalué avec son score et le détail du calcul (Étape 2.5).
 *
 * @property candidateText Le texte nettoyé du candidat titre.
 * @property originalLineIndex L'index de ligne d'origine.
 * @property originalLinesToSkip Le nombre de lignes adjacentes fusionnées à ignorer (ex: 1 pour un titre sur 2 lignes).
 * @property score Le score numérique attribué au candidat.
 * @property scoreDetails Le détail explicatif du calcul du score.
 */
internal data class ScoredTitleCandidate(
    val candidateText: String,
    val originalLineIndex: Int,
    val originalLinesToSkip: Int,
    val score: Int,
    val scoreDetails: String
)
