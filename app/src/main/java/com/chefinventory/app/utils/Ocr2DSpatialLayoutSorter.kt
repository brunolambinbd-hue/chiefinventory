package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 2.5 : Trieur spatial 2D de boîtes englobantes ([OcrTextLine]).
 *
 * Responsabilités :
 * - Trier une liste de lignes de texte scannées d'après leurs coordonnées géométriques BoundingBox 2D en pixels (`top`, `left`, `bottom`, `right`).
 * - Grouper les éléments situés sur une même bande horizontale (tolérance Y).
 * - Ordonner les bandes de Haut en Bas (axe Y), puis les éléments au sein d'une bande de Gauche à Droite (axe X).
 */
object Ocr2DSpatialLayoutSorter {

    private const val DEFAULT_Y_BAND_TOLERANCE_PX = 15

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

        // 2. Tri primaire vertical Y d'après la coordonnée du centre vertical centerY
        val sortedByY = validLines.sortedBy { it.centerY }

        // 3. Regroupement en bandes horizontales de lecture
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

        // 4. Tri de chaque bande horizontale de Gauche à Droite (axe X / left) et assemblage final
        val result = mutableListOf<OcrTextLine>()
        for (band in horizontalBands) {
            band.sortBy { it.left }
            result.addAll(band)
        }

        return result
    }
}
