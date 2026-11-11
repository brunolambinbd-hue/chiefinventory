package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant l'extraction d'ingrédients implicites par [Ocr3NarrativeIngredientExtractor].
 */
class Ocr3NarrativeIngredientExtractorTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFormatImpliedIngredient_NullQuantityAndShortName_ReturnsNull() {
        val parsedNullQty = IngredientParser.ParsedIngredient(name = "farine", quantity = null)
        val resNull = Ocr3NarrativeIngredientExtractor.formatImpliedIngredient(parsedNullQty, emptySet())
        assertWithMessage("Un ingrédient implicite sans quantité doit retourner null")
            .that(resNull)
            .isNull()

        val parsedBlankName = IngredientParser.ParsedIngredient(name = "  ", quantity = 1.0)
        val resBlank = Ocr3NarrativeIngredientExtractor.formatImpliedIngredient(parsedBlankName, emptySet())
        assertWithMessage("Un ingrédient implicite avec nom blanc doit retourner null")
            .that(resBlank)
            .isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_ShortActionVerbsTechnicalDimensionsInNameAndPartialSeasoning() {
        // 1. Short action verb of length 2 ("va") in vocabulary (covers lowerWord in actionVerbs == true)
        val vocabShortAction = vocabulary.copy(stepActionKeywords = listOf("va", "couper", "mélanger"))
        val results1 = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(listOf("Incorporez 2 g de va farine."), vocabShortAction, results1)
        assertWithMessage("L'ingrédient implicite '2 g farine' doit être extrait après purge du verbe court")
            .that(results1.rawIngredientsList)
            .contains("2 g farine")

        // 2. Technical dimension in parsed.name (covers isTechName == true, L129)
        val results2 = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(listOf("Ajoutez 10 5cm x 10cm de beurre."), vocabulary, results2)

        // 3. Partial seasoning where "poivre" is already in seenKeywords from Step 2
        val results3 = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(listOf("Ajoutez 2 g de poivre.", "Assaisonnez avec du sel et du poivre."), vocabulary, results3)
        assertWithMessage("L'ingrédient '2 g poivre' doit être conservé")
            .that(results3.rawIngredientsList)
            .contains("2 g poivre")

        assertWithMessage("Le doublon 'Sel et poivre' ne doit pas être ré-ajouté")
            .that(results3.rawIngredientsList)
            .doesNotContain("Sel et poivre")

        // 4. Partial seasoning where "sel" is already in seenKeywords from Step 2
        val results4 = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(listOf("Ajoutez 2 g de sel.", "Assaisonnez avec du sel et du poivre."), vocabulary, results4)
        assertWithMessage("L'ingrédient '2 g sel' doit être conservé")
            .that(results4.rawIngredientsList)
            .contains("2 g sel")

        assertWithMessage("Le doublon 'Sel et poivre' ne doit pas être ré-ajouté")
            .that(results4.rawIngredientsList)
            .doesNotContain("Sel et poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_BlankParsedNameSegmentAndShortName() {
        val results = RawSections()
        val instructions = listOf(
            "Incorporez 1 c. à soupe.",
            "Ajoutez 250 g puis 1 g et 2 g d'a.",
            "Incorporez 2 g de farine de blé."
        )

        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructions, vocabulary, results)

        assertWithMessage("L'ingrédient implicite '2 g farine de blé' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("2 g farine de blé")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsDurationOrTime_DetectsDurationsAndTemperaturesAndRejectsFood() {
        assertWithMessage("La durée '15 min' doit être qualifiée de temps")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("15 min", "min", "minutes"))
            .isTrue()

        assertWithMessage("La durée '20 mn' doit être qualifiée de temps")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("20 mn", "mn", "mn"))
            .isTrue()

        assertWithMessage("La durée '30 sec' doit être qualifiée de temps")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("30 sec", "sec", "secondes"))
            .isTrue()

        assertWithMessage("La durée '2 h' doit être qualifiée de temps")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("2 h", "h", "heures"))
            .isTrue()

        assertWithMessage("La température '180°C' doit être qualifiée de temps/température")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("180°C", null, "180°C"))
            .isTrue()

        assertWithMessage("La température '200 °' doit être qualifiée de temps/température")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("200 °", null, "200 °"))
            .isTrue()

        assertWithMessage("La quantité d'aliment '200 g de farine' ne doit pas être qualifiée de durée/température")
            .that(Ocr3NarrativeIngredientExtractor.isDurationOrTime("200 g de farine", "g", "farine"))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_QuantitiesWithAndWithoutUnitsAndDecimalQuantities() {
        val results = RawSections()
        val instructions = listOf(
            "Ajoutez 0.5 l de lait et 3 oeufs.",
            "Incorporez 250 g de farine et 1 sachet de levure.",
            "Enfournez à 180°C pendant 20 minutes.",
            "Assaisonnez avec du sel."
        )

        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructions, vocabulary, results)

        assertWithMessage("L'ingrédient implicite '0.5 l lait' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("0.5 l lait")

        assertWithMessage("L'ingrédient implicite '3 oeufs' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("3 oeufs")

        assertWithMessage("L'ingrédient implicite '250 g farine' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("250 g farine")

        assertWithMessage("L'assaisonnement implicite 'Sel' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("Sel")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_TechnicalDimensionsAndShortWordsAndDuplicates() {
        val results = RawSections()
        results.rawIngredientsList.add("Sel")

        val instructions = listOf(
            "Couper le concombre en tronçons de 5 mm sur 5 mm.",
            "Ajoutez 2 g d'ail pour vous et nous sur feu moyen.",
            "Ajoutez 2 cuillères à soupe d'huile.",
            "Incorporez 200 g de farine puis encore 100 g de farine.",
            "Ajoutez 2 g de de a b.",
            "Agrémentez de poivre."
        )

        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(instructions, vocabulary, results)

        assertWithMessage("L'ingrédient implicite '2 g ail et sur' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("2 g ail et sur")

        assertWithMessage("L'assaisonnement implicite 'Poivre' doit être extrait")
            .that(results.rawIngredientsList)
            .contains("Poivre")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testExtractImpliedIngredients_EmptyInstructionLines_ReturnsEmpty() {
        val results = RawSections()
        Ocr3NarrativeIngredientExtractor.extractImpliedIngredients(emptyList(), vocabulary, results)

        assertWithMessage("Une liste d'instructions vide doit laisser la liste d'ingrédients bruts vide")
            .that(results.rawIngredientsList)
            .isEmpty()
    }
}
