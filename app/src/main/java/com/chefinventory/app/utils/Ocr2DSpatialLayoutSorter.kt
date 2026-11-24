package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 2.5 : Trieur spatial 2D de boîtes englobantes ([OcrTextLine]).
 *
 * Responsabilités :
 * - Trier une liste de lignes de texte scannées d'après leurs coordonnées géométriques BoundingBox 2D en pixels (`top`, `left`, `bottom`, `right`).
 * - Grouper les éléments situés sur une même bande horizontale (tolérance Y).
 * - Ordonner les bandes de Haut en Bas (axe Y), puis les éléments au sein d'une bande de Gauche à Droite (axe X).
 */
/**
 * Structure décrivant la densité et les colonnes d'un layout 2D.
 */
data class SpatialDensityResult(
    val columnCount: Int,
    val densityCol1: Int,
    val densityCol2: Int,
    val densityCol3: Int,
    val thresholdS1: Int,
    val thresholdS2: Int
)

object Ocr2DSpatialLayoutSorter {

    private const val DEFAULT_Y_BAND_TOLERANCE_PX = 15
    private const val MAX_SAME_LINE_X_GAP_PX = 40

    /**
     * Analyse la densité spatiale horizontale des lignes 2D [lines] sur 3 tranches proportionnelles (33% W et 66% W).
     *
     * @param lines La liste des lignes [OcrTextLine] à analyser.
     * @return L'objet [SpatialDensityResult] décrivant le nombre de colonnes actives (1, 2 ou 3) et la répartition de densité.
     */
    fun analyzeSpatialDensity(lines: List<OcrTextLine>): SpatialDensityResult {
        val validLines = lines.filter { it.text.isNotBlank() }
        if (validLines.isEmpty()) {
            return SpatialDensityResult(1, 0, 0, 0, 630, 1200)
        }

        val maxX = validLines.maxOfOrNull { it.right } ?: 2000
        val width = maxOf(maxX, 1000)

        val s1 = (width * 0.33).toInt()
        val s2 = (width * 0.66).toInt()

        val col1Count = validLines.count { it.left < s1 }
        val col2Count = validLines.count { it.left in s1 until s2 }
        val col3Count = validLines.count { it.left >= s2 }

        val columnCount = when {
            col2Count >= 2 && col3Count >= 1 -> 3
            col3Count >= 1 -> 2
            col2Count >= 3 -> 2
            else -> 1
        }

        return SpatialDensityResult(
            columnCount = columnCount,
            densityCol1 = col1Count,
            densityCol2 = col2Count,
            densityCol3 = col3Count,
            thresholdS1 = s1,
            thresholdS2 = s2
        )
    }

    /**
     * Extrait les bannières de titres trans-colonnes (width >= 500 px sans puce, quantité ou verbe d'action)
     * et sépare les lignes du corps de texte.
     *
     * @param lines La liste des lignes [OcrTextLine] à évaluer.
     * @param vocabulary Le vocabulaire métier OCR optionnel.
     * @return La paire (bannièresDeTitres, lignesDuCorpsDeTexte).
     */
    fun extractTitleBanners(
        lines: List<OcrTextLine>,
        vocabulary: OcrVocabulary? = null
    ): Pair<List<OcrTextLine>, List<OcrTextLine>> {
        val validLines = lines.filter { it.text.isNotBlank() }
        if (validLines.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        val vocab = vocabulary ?: OcrVocabularyLoader.loadFromProjectResources()
        val actionKeywords = vocab.stepActionKeywords

        val titleBanners = mutableListOf<OcrTextLine>()
        val bodyLines = mutableListOf<OcrTextLine>()

        for (line in validLines) {
            val text = line.text.trim()
            val lower = text.lowercase()
            val width = line.right - line.left

            val startsWithBullet = text.startsWith("•") || text.startsWith("-") || text.startsWith("*")
            val startsWithDigit = text.firstOrNull()?.isDigit() == true
            val hasActionVerb = OcrInstructionUtils.hasActionVerb(text, actionKeywords)
            val isNotBodyHeader = !lower.startsWith("ingrédient") && !lower.startsWith("pour") && !lower.startsWith("vin")
            val isIngredientContext = lower.contains(" g de ") || lower.contains(" c. à ") || lower.contains("forme de") || lower.contains("assiette")

            val isTitleBanner = width >= 500 && !startsWithBullet && !startsWithDigit && !hasActionVerb && isNotBodyHeader && !isIngredientContext

            if (isTitleBanner) {
                titleBanners.add(line)
            } else {
                bodyLines.add(line)
            }
        }

        return Pair(titleBanners, bodyLines)
    }

    /**
     * Trie une liste de lignes [OcrTextLine] d'après leurs coordonnées spatiales 2D en pixels.
     *
     * @param lines La liste des lignes [OcrTextLine] à réordonner géométriquement.
     * @param yTolerancePx La tolérance en pixels pour regrouper les lignes d'une même bande horizontale (défaut: 15 px).
     * @return La liste filtrée et ordonnée dans le vrai sens de lecture humain (Haut en Bas, Gauche à Droite).
     */
    fun sortSpatially(
        lines: List<OcrTextLine>,
        yTolerancePx: Int = DEFAULT_Y_BAND_TOLERANCE_PX
    ): List<OcrTextLine> {
        if (lines.size <= 1) return lines

        // 1. Filtrage des lignes vides
        val validLines = lines.filter { it.text.isNotBlank() }
        if (validLines.size <= 1) return validLines

        // 1.5. Isolation des bannières de titres candidates au sommet (Source A)
        val (titleBanners, nonTitleBodyLines) = extractTitleBanners(validLines)
        if (nonTitleBodyLines.isEmpty()) {
            return sortSingleColumn(titleBanners, yTolerancePx)
        }

        // 2. Détection dynamique du nombre de colonnes et des seuils sur le corps de texte
        val density = analyzeSpatialDensity(nonTitleBodyLines)

        val sortedBody = when (density.columnCount) {
            3 -> {
                val col1 = nonTitleBodyLines.filter { it.left < density.thresholdS1 }
                val col2 = nonTitleBodyLines.filter { it.left in density.thresholdS1 until density.thresholdS2 }
                val col3 = nonTitleBodyLines.filter { it.left >= density.thresholdS2 }
                sortSingleColumn(col1, yTolerancePx) + sortSingleColumn(col2, yTolerancePx) + sortSingleColumn(col3, yTolerancePx)
            }
            2 -> {
                val col1 = nonTitleBodyLines.filter { it.left < density.thresholdS2 }
                val col3 = nonTitleBodyLines.filter { it.left >= density.thresholdS2 }
                sortSingleColumn(col1, yTolerancePx) + sortSingleColumn(col3, yTolerancePx)
            }
            else -> {
                sortSingleColumn(nonTitleBodyLines, yTolerancePx)
            }
        }

        return titleBanners.sortedBy { it.top } + sortedBody
    }

    /**
     * Trie une colonne unique de lignes [OcrTextLine] de Haut en Bas (axe Y) avec fusion des fragments horizontaux adjacents.
     */
    private fun sortSingleColumn(
        lines: List<OcrTextLine>,
        yTolerancePx: Int = DEFAULT_Y_BAND_TOLERANCE_PX
    ): List<OcrTextLine> {
        if (lines.size <= 1) return lines
        val sortedByY = lines.sortedBy { it.centerY }

        val horizontalBands = mutableListOf<MutableList<OcrTextLine>>()
        for (line in sortedByY) {
            val matchingBand = horizontalBands.firstOrNull { band ->
                val bandAverageY = band.map { it.centerY }.average()
                Math.abs(line.centerY - bandAverageY) <= yTolerancePx
            }

            if (matchingBand != null) {
                matchingBand.add(line)
            } else {
                horizontalBands.add(mutableListOf(line))
            }
        }

        val result = mutableListOf<OcrTextLine>()
        for (band in horizontalBands) {
            band.sortBy { it.left }
            var currentMerged: OcrTextLine? = null
            for (line in band) {
                if (currentMerged == null) {
                    currentMerged = line
                } else {
                    val gapX = line.left - currentMerged.right
                    val lineStartsWithDigit = line.text.trim().firstOrNull()?.isDigit() == true
                    val currentIsUnitPrefix = currentMerged.text.trim().let { it == "1" || it == "2" || it.endsWith("c.") || it.endsWith("c") || it.endsWith("a") || it.endsWith("à") }
                    val currentIsStandaloneTimeNumber = currentMerged.text.trim().let { t ->
                        val clean = t.replace(Regex("[^\\d]"), "")
                        clean.isNotEmpty() && clean.toIntOrNull() in 5..180 && t.length <= 4
                    }
                    val isContinuationFragment = (!lineStartsWithDigit || currentIsUnitPrefix) && !currentIsStandaloneTimeNumber

                    if (gapX in -20..MAX_SAME_LINE_X_GAP_PX && isContinuationFragment) {
                        currentMerged = currentMerged.copy(
                            text = "${currentMerged.text.trim()} ${line.text.trim()}",
                            right = maxOf(currentMerged.right, line.right),
                            bottom = maxOf(currentMerged.bottom, line.bottom)
                        )
                    } else {
                        result.add(currentMerged)
                        currentMerged = line
                    }
                }
            }
            if (currentMerged != null) {
                result.add(currentMerged)
            }
        }

        return result
    }
}
