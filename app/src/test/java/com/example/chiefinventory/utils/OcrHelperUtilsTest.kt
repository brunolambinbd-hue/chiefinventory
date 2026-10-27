package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'ensemble des fonctions d'aide au parsing de [OcrHelperUtils].
 */
class OcrHelperUtilsTest {

    /**
     * Vérifie le comptage de séquences d'ingrédients simples et multiples sur une même ligne.
     */
    @Test
    fun `countIngredientSequences basic sequence counting`() {
        val line1 = "2 Carottes"
        val count1 = OcrHelperUtils.countIngredientSequences(line1)
        assertWithMessage("Détection d'une séquence unique d'ingrédient échouée pour : '$line1'")
            .that(count1).isEqualTo(1)

        val line2 = "2 Carottes 1 Pomme"
        val count2 = OcrHelperUtils.countIngredientSequences(line2)
        assertWithMessage("Comptage de séquences multiples d'ingrédients échoué pour : '$line2'")
            .that(count2).isEqualTo(2)
    }

    /**
     * Vérifie le comptage des séquences d'ingrédients utilisant des déclinaisons de chiffres mal lus (`l`, `I`, `|`, `!`).
     */
    @Test
    fun `countIngredientSequences OCR variant detection`() {
        val variants = mapOf(
            "l Carotte" to "Variante OCR 'l'",
            "I Carotte" to "Variante OCR 'I'",
            "| Carotte" to "Variante OCR '|'",
            "! Carotte" to "Variante OCR '!'"
        )

        variants.forEach { (line, label) ->
            val count = OcrHelperUtils.countIngredientSequences(line)
            assertWithMessage("Reconnaissance du chiffre 1 erroné ($label) pour : '$line'")
                .that(count).isEqualTo(1)
        }
    }

    /**
     * Vérifie que les plages de quantités reliées par des connecteurs ("2 ou 3 Carottes") ne sont comptées que comme 1 seule séquence.
     */
    @Test
    fun `countIngredientSequences connector exclusion check`() {
        val connectorLines = listOf("2 ou 3 Carottes", "4 à 5 Pommes", "6 - 7 Poires")
        connectorLines.forEach { line ->
            val count = OcrHelperUtils.countIngredientSequences(line)
            assertWithMessage("Les connecteurs de plage ne doivent pas compter comme plusieurs ingrédients pour : '$line'")
                .that(count).isEqualTo(1)
        }
    }

    /**
     * Vérifie que les durées et dimensions techniques ("10 min", "5 mm") ne sont pas comptées comme séquences d'ingrédients.
     */
    @Test
    fun `countIngredientSequences technical unit exclusion`() {
        val technicalLines = listOf(
            "Cuire 10 min",
            "Cuire 10 sec",
            "Couper en 5 mm",
            "Couper en 5 cm"
        )
        technicalLines.forEach { line ->
            val count = OcrHelperUtils.countIngredientSequences(line)
            assertWithMessage("Les unités techniques/durées ne doivent pas être comptées comme séquences d'ingrédients pour : '$line'")
                .that(count).isEqualTo(0)
        }
    }

    /**
     * Vérifie l'isolation des quantités entre parenthèses ("1 Oignon (poids 200g)").
     */
    @Test
    fun `countIngredientSequences parentheses isolation`() {
        val line1 = "1 Oignon (poids 200g)"
        val count1 = OcrHelperUtils.countIngredientSequences(line1)
        assertWithMessage("La quantité entre parenthèses ne doit pas être surcomptée pour : '$line1'")
            .that(count1).isEqualTo(1)

        val line2 = "(2 Tomates)"
        val count2 = OcrHelperUtils.countIngredientSequences(line2)
        assertWithMessage("Une ligne entière entre parenthèses ne doit pas être comptée comme séquence d'ingrédient pour : '$line2'")
            .that(count2).isEqualTo(0)
    }

    /**
     * Vérifie le comportement du compteur de séquences sur chaîne vide ou sans quantité.
     */
    @Test
    fun `countIngredientSequences empty or non matching string`() {
        val count1 = OcrHelperUtils.countIngredientSequences("")
        assertWithMessage("Une chaîne vide doit renvoyer 0 séquence")
            .that(count1).isEqualTo(0)

        val line2 = "Sans ingredients"
        val count2 = OcrHelperUtils.countIngredientSequences(line2)
        assertWithMessage("Une ligne sans quantité ni variante OCR doit renvoyer 0 pour : '$line2'")
            .that(count2).isEqualTo(0)
    }

    /**
     * Vérifie le découpage basique d'une ligne d'ingrédients combinés par quantités ("1 Oignon 2 Tomates").
     */
    @Test
    fun `splitCombinedIngredients basic quantity split`() {
        val input = "1 Oignon 2 Tomates"
        val result = OcrHelperUtils.splitCombinedIngredients(input, emptyList())
        assertWithMessage("Découpage par quantité d'ingrédients fusionnés échoué pour : '$input'")
            .that(result).containsExactly("1 Oignon", "2 Tomates").inOrder()
    }

    /**
     * Vérifie le découpage d'ingrédients connus sans quantité ("Sel Poivre").
     */
    @Test
    fun `splitCombinedIngredients common items split`() {
        val common = listOf("Sel", "Poivre")
        val input = "Sel Poivre"
        val result = OcrHelperUtils.splitCombinedIngredients(input, common)
        assertWithMessage("Découpage par ingrédients connus sans quantité échoué pour : '$input'")
            .that(result).containsExactly("Sel", "Poivre").inOrder()
    }

    /**
     * Vérifie la logique de découpage sur variantes de mots de préparation ("persil haché").
     */
    @Test
    fun `splitCombinedIngredients hache variant logic`() {
        val input = "persil haché de l'ail"
        val result = OcrHelperUtils.splitCombinedIngredients(input, emptyList())
        assertWithMessage("La ligne combinée avec 'haché' doit être conservée ou découpée proprement pour : '$input'")
            .that(result).isNotEmpty()
    }

    /**
     * Vérifie l'effacement du bruit de numérotation double en début de ligne ("12 34 1 Oignon").
     */
    @Test
    fun `splitCombinedIngredients leading double digit removal`() {
        val input = "12 34 1 Oignon"
        val result = OcrHelperUtils.splitCombinedIngredients(input, emptyList())
        assertWithMessage("Nettoyage du bruit numérique OCR en début de ligne échoué pour : '$input'")
            .that(result).containsExactly("Oignon")
    }

    /**
     * Vérifie la protection contre le découpage abusif d'informations entre parenthèses ("1 Oignon (200 g)").
     */
    @Test
    fun `splitCombinedIngredients parentheses protection`() {
        val input = "1 Oignon (200 g) 2 Tomates"
        val result = OcrHelperUtils.splitCombinedIngredients(input, emptyList())
        assertWithMessage("Découpage d'ingrédients avec poids entre parenthèses échoué pour : '$input'")
            .that(result).containsExactly("1 Oignon (200 g)", "2 Tomates").inOrder()
    }

    /**
     * Vérifie la protection contre le découpage abusif de phrases reliées par le connecteur "et".
     */
    @Test
    fun `splitCombinedIngredients connector protection`() {
        val input = "1 kg de tomates et 2 carottes"
        val result = OcrHelperUtils.splitCombinedIngredients(input, emptyList())
        assertWithMessage("Les connecteurs de phrase 'et' ne doivent pas être abusivement découpés pour : '$input'")
            .that(result).containsExactly("1 kg de tomates et 2 carottes")
    }

    /**
     * Vérifie la détection positive des noms propres d'auteurs ou d'établissements de 2 à 4 mots.
     */
    @Test
    fun `isLikelyProperNameOrSource positive detection`() {
        val validNames = listOf("Jean Pierre", "NAHIT YILMAZ", "NAHIT YILMAZ Jean Pierre")
        validNames.forEach { name ->
            assertWithMessage("Détection de nom propre/source échouée pour : '$name'")
                .that(OcrHelperUtils.isLikelyProperNameOrSource(name)).isTrue()
        }

        val invalidName = "NAHIT YILMAZ Jean Pierre Jean"
        assertWithMessage("Un nom dépasse la limite de mots (5 mots) et doit être refusé : '$invalidName'")
            .that(OcrHelperUtils.isLikelyProperNameOrSource(invalidName)).isFalse()
    }

    /**
     * Vérifie le rejet des noms propres contenant des chiffres.
     */
    @Test
    fun `isLikelyProperNameOrSource digit rejection`() {
        val line = "Jean 2 Pierre"
        assertWithMessage("Un nom propre contenant un chiffre doit être refusé : '$line'")
            .that(OcrHelperUtils.isLikelyProperNameOrSource(line)).isFalse()
    }

    /**
     * Vérifie le respect des bornes de longueur en nombre de mots pour les noms propres (2 à 4 mots).
     */
    @Test
    fun `isLikelyProperNameOrSource word count boundaries`() {
        val singleWord = "Jean"
        assertWithMessage("Un mot unique ne doit pas être considéré comme un nom/source complet : '$singleWord'")
            .that(OcrHelperUtils.isLikelyProperNameOrSource(singleWord)).isFalse()

        val longSentence = "Jean Pierre Paul Jacques Philippe"
        assertWithMessage("Une phrase de plus de 4 mots ne doit pas être considérée comme un nom propre : '$longSentence'")
            .that(OcrHelperUtils.isLikelyProperNameOrSource(longSentence)).isFalse()
    }

    /**
     * Vérifie le test d'exclusion stricte insensible à la casse.
     */
    @Test
    fun `isExcluded case insensitive match`() {
        assertWithMessage("Exclusion stricte insensible à la casse échouée pour 'PAGE'")
            .that(OcrHelperUtils.isExcluded("PAGE", listOf("page"))).isTrue()

        assertWithMessage("Exclusion stricte avec espaces inutiles échouée pour 'SAVEUR'")
            .that(OcrHelperUtils.isExcluded("  SAVEUR  ", listOf("saveur"))).isTrue()
    }

    /**
     * Vérifie le retrait des puces typographiques (`•`, `-`, `*`) lors du nettoyage sémantique.
     */
    @Test
    fun `cleanIngredientSemantics bullet removal`() {
        assertWithMessage("Suppression de la puce '•' échouée")
            .that(OcrHelperUtils.cleanIngredientSemantics("• Tomate", emptyList())).isEqualTo("Tomate")

        assertWithMessage("Suppression du tiret '-' échouée")
            .that(OcrHelperUtils.cleanIngredientSemantics("- Oignon", emptyList())).isEqualTo("Oignon")

        assertWithMessage("Suppression de l'étoile '*' échouée")
            .that(OcrHelperUtils.cleanIngredientSemantics("* Poivre", emptyList())).isEqualTo("Poivre")
    }

    /**
     * Vérifie la suppression des chiffres isolés et caractères spéciaux sans lettres.
     */
    @Test
    fun `cleanIngredientSemantics length and letter validation`() {
        assertWithMessage("Un chiffre isolé doit être supprimé par le nettoyeur sémantique")
            .that(OcrHelperUtils.cleanIngredientSemantics("1", emptyList())).isEmpty()

        assertWithMessage("Des caractères spéciaux sans lettres doivent être supprimés")
            .that(OcrHelperUtils.cleanIngredientSemantics("!!!", emptyList())).isEmpty()
    }

    /**
     * Vérifie que les noms propres d'auteurs ou d'établissements sont vider par le nettoyeur d'ingrédients.
     */
    @Test
    fun `cleanIngredientSemantics proper name exclusion`() {
        val name = "Nahit Yilmaz"
        assertWithMessage("Un nom propre de chef/source doit être exclu des ingrédients : '$name'")
            .that(OcrHelperUtils.cleanIngredientSemantics(name, emptyList())).isEmpty()
    }

    /**
     * Vérifie la suppression des lignes correspondant à une exclusion sémantique exacte (`ingredient_exclusion_arrays.xml`).
     */
    @Test
    fun `cleanIngredientSemantics semantic list exclusion`() {
        val text = "Pour 4 personnes"
        assertWithMessage("Une exclusion sémantique exacte doit vider la ligne : '$text'")
            .that(OcrHelperUtils.cleanIngredientSemantics(text, emptyList(), listOf("POUR 4 PERSONNES"))).isEmpty()
    }

    /**
     * Vérifie la préservation des informations de poids/détails entre parenthèses dans un ingrédient.
     */
    @Test
    fun `cleanIngredientSemantics parentheses retention`() {
        val line = "6 aubergines (500g)"
        assertWithMessage("Les informations entre parenthèses dans un ingrédient doivent être conservées")
            .that(OcrHelperUtils.cleanIngredientSemantics(line, emptyList())).isEqualTo(line)
    }

    /**
     * Vérifie la détection d'un poids ou volume d'ingrédient explicite (Ligne 17).
     */
    @Test
    fun `hasExplicitWeight should return true when explicit weight or volume is present`() {
        assertWithMessage("250 g contient un poids explicite")
            .that(OcrHelperUtils.hasExplicitWeight("250 g de farine")).isTrue()

        assertWithMessage("Poivre noir sans quantité ne contient pas de poids explicite")
            .that(OcrHelperUtils.hasExplicitWeight("Poivre noir")).isFalse()
    }

    /**
     * Vérifie la détection d'un caractère situé entre parenthèses (Ligne 71).
     */
    @Test
    fun `isInsideParentheses should return true when index is inside parentheses`() {
        val text = "Farine (250 g) de blé"
        assertWithMessage("L'index 8 dans 'Farine (250 g) de blé' doit être détecté entre parenthèses")
            .that(OcrHelperUtils.isInsideParentheses(text, 8)).isTrue()

        assertWithMessage("L'index 0 doit être hors des parenthèses")
            .that(OcrHelperUtils.isInsideParentheses(text, 0)).isFalse()
    }
}
