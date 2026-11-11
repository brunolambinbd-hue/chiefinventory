package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant la beautification, la ponctuation et le recollage de consignes
 * suspendues par [Ocr5BeautifierInstruction].
 */
class Ocr5BeautifierInstructionTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_ActionVerbWithRecetteWordAndFinalStandaloneTrashFilter() {
        val input = listOf(
            "Incorporez les oeufs selon la recette.",
            "Servir chaud."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("Une ligne contenant un verbe d'action 'Incorporez' doit être conservée même si elle contient le mot 'recette'")
            .that(result)
            .contains("Incorporez les oeufs selon la recette.")

        assertWithMessage("La consigne finale 'Servir chaud.' doit être conservée")
            .that(result)
            .contains("Servir chaud.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_UncapitalizedNextAndStandaloneTrashFinalFilter() {
        val input = listOf(
            "Mélangez le sucre et.",
            "ajoutez la farine",
            "pour."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("La consigne tronquée se terminant par 'et.' doit fusionner avec 'ajoutez la farine' et éliminer le mot-poubelle 'pour.'")
            .that(result)
            .containsExactly("Mélangez le sucre et ajoutez la farine.")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_ConjunctionWithoutDotAndPluralRecettesHeader() {
        val input = listOf(
            "FICHE RECETTES",
            "mélangez le sucre et",
            "ajoutez la farine",
            "faire revenir ou",
            "faire mijoter à feu doux"
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits
        )

        assertWithMessage("Les consignes tronquées par 'et' doivent être majusculées et fusionnées")
            .that(result)
            .contains("Mélangez le sucre et ajoutez la farine.")

        assertWithMessage("Les consignes tronquées par 'ou' doivent être fusionnées et majusculées")
            .that(result)
            .contains("Faire revenir ou faire mijoter à feu doux.")

        assertWithMessage("L'en-tête 'FICHE RECETTES' sans verbe d'action doit être éliminé")
            .that(result)
            .doesNotContain("FICHE RECETTES")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_ExhaustiveBranchVariations() {
        val input = listOf(
            "Ə RECETTES DU CHEF",
            "Ə Fiche du chef",
            "consignes",
            "preparation",
            "Mélanger les ingrédients ou.",
            "Ajoutez du sel,.",
            "Poivrez.",
            "Servir chaud !",
            "Déguster chaud ?"
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits,
            vocabulary.ocrSpellingRepairs
        )

        assertWithMessage("Les consignes séparées par 'ou.' et ',.' doivent être fusionnées en une seule phrase fluide")
            .that(result)
            .contains("Mélanger les ingrédients ou ajoutez du sel, poivrez.")

        assertWithMessage("Le point d'exclamation final doit être conservé")
            .that(result)
            .contains("Servir chaud !")

        assertWithMessage("Le point d'interrogation final doit être conservé")
            .that(result)
            .contains("Déguster chaud ?")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_DefaultArgumentsAndBlankLinesAndTrailingPeriod() {
        val input = listOf(
            "",
            "   ",
            "PRÉPARATION",
            "INSTRUCTIONS",
            "CONSIGNES",
            "FICHE RECETTE",
            "mélangez l'huile d'olive",
            "   ",
            "laissez mariner au réfrigérateur"
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(input)

        assertWithMessage("La consigne 'mélangez l'huile d'olive' doit être majusculée et ponctuée")
            .that(result)
            .contains("Mélangez l'huile d'olive.")

        assertWithMessage("La consigne 'laissez mariner au réfrigérateur' doit être majusculée et ponctuée")
            .that(result)
            .contains("Laissez mariner au réfrigérateur.")

        assertWithMessage("L'en-tête de section 'PRÉPARATION' doit être éliminé")
            .that(result)
            .doesNotContain("PRÉPARATION")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterConjunctionEtDot() {
        val input = listOf(
            "Nappez de la sauce au yaourt et à la mayonnaise et.",
            "Garnissez de quelques branches de cerfeuil."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("La consigne tronquée par 'et.' doit être fusionnée et 'Garnissez' passé en minuscules 'garnissez'")
            .that(result)
            .containsExactly(
                "Nappez de la sauce au yaourt et à la mayonnaise et garnissez de quelques branches de cerfeuil."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterUnitAbbreviationCDot() {
        val input = listOf(
            "Mélangez les légumes, la feta et l'estragon haché aux 4 c.",
            "À soupe de yaourt."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors,
            vocabulary.culinaryMeasurementUnits
        )

        assertWithMessage("L'abréviation d'unité 'c.' ne doit pas couper la phrase et doit fusionner avec 'À soupe'")
            .that(result)
            .containsExactly(
                "Mélangez les légumes, la feta et l'estragon haché aux 4 c. à soupe de yaourt."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_MergeInstructionSentenceBrokenAfterComma() {
        val input = listOf(
            "Mélangez l'huile d'olive, le piment,",
            "Le thym et le romarin effeuillé."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("Une virgule de fin de ligne doit fusionner la phrase et passer 'Le' en minuscules 'le'")
            .that(result)
            .containsExactly(
                "Mélangez l'huile d'olive, le piment, le thym et le romarin effeuillé."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_LeaveCompleteSentencesEndingWithAPeriodSeparate() {
        val input = listOf(
            "Mélangez l'huile d'olive et le thym.",
            "Laissez mariner au réfrigérateur."
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("Deux phrases complètes se terminant par des points doivent rester séparées")
            .that(result)
            .containsExactly(
                "Mélangez l'huile d'olive et le thym.",
                "Laissez mariner au réfrigérateur."
            )
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testBeautifyInstructions_FiltersOutShortStandaloneOrphanScanNoise() {
        val input = listOf(
            "e t",
            "e",
            "r",
            "Délayez le curry avec la mayonnaise.",
            "e t"
        )

        val result = Ocr5BeautifierInstruction.beautifyInstructions(
            input,
            vocabulary.stepActionKeywords,
            vocabulary.hangingConnectors
        )

        assertWithMessage("Les bruits de lettres isolées 'e t', 'e', 'r' doivent être éliminés")
            .that(result)
            .containsExactly("Délayez le curry avec la mayonnaise.")

        assertWithMessage("Le bruit 'E t.' ne doit pas figurer dans le résultat")
            .that(result)
            .doesNotContain("E t.")

        assertWithMessage("Le bruit 'E.' ne doit pas figurer dans le résultat")
            .that(result)
            .doesNotContain("E.")

        assertWithMessage("Le bruit 'R.' ne doit pas figurer dans le résultat")
            .that(result)
            .doesNotContain("R.")
    }
}
