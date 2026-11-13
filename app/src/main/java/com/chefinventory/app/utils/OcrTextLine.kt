package com.chefinventory.app.utils

/**
 * Représente une ligne de texte scannée accompagnée de ses coordonnées spatiales BoundingBox 2D en pixels.
 *
 * @property text La chaîne de texte de la ligne.
 * @property top La coordonnée Y du haut du rectangle de texte en pixels.
 * @property left La coordonnée X de la gauche du rectangle de texte en pixels.
 * @property bottom La coordonnée Y du bas du rectangle de texte en pixels.
 * @property right La coordonnée X de la droite du rectangle de texte en pixels.
 */
data class OcrTextLine(
    val text: String,
    val top: Int = 0,
    val left: Int = 0,
    val bottom: Int = 0,
    val right: Int = 0
) {
    /** La coordonnée Y du centre vertical de la ligne en pixels. */
    val centerY: Int get() = (top + bottom) / 2

    /** La hauteur de la boîte englobante en pixels. */
    val height: Int get() = bottom - top

    /** La largeur de la boîte englobante en pixels. */
    val width: Int get() = right - left

    companion object {
        private const val DEFAULT_LINE_HEIGHT = 25
        private const val DEFAULT_LINE_SPACING = 30
        private const val DEFAULT_CHAR_WIDTH = 10

        /**
         * Crée une instance rétro-compatible d'[OcrTextLine] à partir d'une chaîne brute et d'un index de ligne virtuel.
         *
         * @param text Le texte de la ligne.
         * @param lineIndex L'index séquentiel de la ligne (défaut: 0).
         * @return L'instance d'[OcrTextLine] alimentée de coordonnées spatiales virtuelles.
         */
        fun fromText(text: String, lineIndex: Int = 0): OcrTextLine {
            val topVal = lineIndex * DEFAULT_LINE_SPACING
            val bottomVal = topVal + DEFAULT_LINE_HEIGHT
            val rightVal = text.length * DEFAULT_CHAR_WIDTH
            return OcrTextLine(
                text = text,
                top = topVal,
                left = 0,
                bottom = bottomVal,
                right = rightVal
            )
        }
    }
}
