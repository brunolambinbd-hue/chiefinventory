package com.chefinventory.app.utils

/**
 * Assistant de l'Étape 2.7 : Trieur spatial BoundingBox de la Colonne 1 (Ingrédients).
 *
 * Responsabilités :
 * - Effectuer le tri spatial BoundingBox Y/X uniquement sur les lignes de la Colonne 1 (left < 450 px).
 * - Préserver l'ordre initial des lignes des autres colonnes (Instructions, Vin, left >= 450 px).
 */
object Ocr2DSpatialColumnSorter {

    private const val COL1_MAX_LEFT_PX = 450

    /**
     * Re-ordonne spatialement les lignes nettoyées de la Colonne 1 (Ingrédients)
     * tout en conservant l'ordre initial des autres colonnes.
     *
     * @param cleanedLines La liste des lignes de texte nettoyées issues de [Ocr2Cleaner].
     * @param lines2D La liste des lignes 2D scannées avec leurs coordonnées BoundingBox.
     * @return La nouvelle liste de lignes de texte avec la Colonne 1 ré-ordonnée géométriquement.
     */
    fun sortColumn1Spatially(
        cleanedLines: List<String>,
        lines2D: List<OcrTextLine>
    ): List<String> {
        if (cleanedLines.isEmpty() || lines2D.isEmpty()) return cleanedLines

        val col1Lines2D = mutableListOf<OcrTextLine>()
        val otherLines2D = mutableListOf<OcrTextLine>()

        for (line2D in lines2D) {
            val text = line2D.text.trim()
            if (text.isBlank()) continue

            if (line2D.left < COL1_MAX_LEFT_PX) {
                col1Lines2D.add(line2D)
            } else {
                otherLines2D.add(line2D)
            }
        }

        if (col1Lines2D.isEmpty()) return cleanedLines

        val sortedCol1Lines2D = Ocr2DSpatialLayoutSorter.sortSpatially(col1Lines2D)
        val combinedLines2D = sortedCol1Lines2D + otherLines2D
        return combinedLines2D.map { it.text }
    }
}
