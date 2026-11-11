package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement de [OcrInstructionUtils].
 */
class OcrInstructionUtilsTest {

    @Before
    fun setUp() {
        OcrVocabularyLoader.clearCache()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasInstructionTrigger_DetectsTriggerKeywordsAndHandlesBlankEmpty() {
        val triggers = listOf("préparation", "étape", "réalisation")
        assertWithMessage("La ligne 'Préparation :' doit être qualifiée de déclencheur d'instruction")
            .that(OcrInstructionUtils.hasInstructionTrigger("Préparation :", triggers))
            .isTrue()

        assertWithMessage("Le mot 'Ingrédients' ne doit pas être qualifié de déclencheur d'instruction")
            .that(OcrInstructionUtils.hasInstructionTrigger("Ingrédients", triggers))
            .isFalse()

        // Blank line & empty triggers
        assertWithMessage("Une ligne vide ne doit pas être qualifiée de déclencheur")
            .that(OcrInstructionUtils.hasInstructionTrigger("", triggers))
            .isFalse()

        assertWithMessage("Une ligne d'espaces ne doit pas être qualifiée de déclencheur")
            .that(OcrInstructionUtils.hasInstructionTrigger("   ", triggers))
            .isFalse()

        assertWithMessage("Une liste de déclencheurs vide doit retourner faux")
            .that(OcrInstructionUtils.hasInstructionTrigger("Préparation", emptyList()))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasActionVerb_DetectsImperativeActionVerbsAndHandlesBlankEmptyFilter() {
        val keywords = listOf("Coupez", "Mélangez", "Servez")
        assertWithMessage("La consigne 'Coupez les légumes en dés' doit être reconnue comme contenant un verbe d'action")
            .that(OcrInstructionUtils.hasActionVerb("Coupez les légumes en dés", keywords))
            .isTrue()

        assertWithMessage("La ligne d'ingrédient '200 g de farine' ne doit pas contenir de verbe d'action")
            .that(OcrInstructionUtils.hasActionVerb("200 g de farine", keywords))
            .isFalse()

        // Blank line & empty keywords
        assertWithMessage("Une ligne vide ne doit pas contenir de verbe d'action")
            .that(OcrInstructionUtils.hasActionVerb("", keywords))
            .isFalse()

        assertWithMessage("Une liste de verbes d'action vide doit retourner faux")
            .that(OcrInstructionUtils.hasActionVerb("Coupez", emptyList()))
            .isFalse()

        // Keywords filtered out (short < 3 chars or in excluded words list)
        assertWithMessage("Les mots-clés trop courts (< 3 chars) ou dans la liste des mots-morts doivent être ignorés")
            .that(OcrInstructionUtils.hasActionVerb("couper", listOf("a", "minutes", "heures")))
            .isFalse()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasActionVerb_DetectsNewlyIntegratedActionVerbs() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()

        assertWithMessage("La consigne 'Maintenez une petite ébullition' doit être reconnue comme action")
            .that(OcrInstructionUtils.hasActionVerb("Maintenez une petite ébullition", vocabulary.stepActionKeywords))
            .isTrue()

        assertWithMessage("La consigne 'Poursuivez la cuisson 15 min' doit être reconnue comme action")
            .that(OcrInstructionUtils.hasActionVerb("Poursuivez la cuisson 15 min", vocabulary.stepActionKeywords))
            .isTrue()

        assertWithMessage("La consigne 'jusqu'à ce que vous obteniez une belle salade' doit être reconnue comme action")
            .that(OcrInstructionUtils.hasActionVerb("jusqu'à ce que vous obteniez une belle salade", vocabulary.stepActionKeywords))
            .isTrue()

        assertWithMessage("La consigne 'Conservez au frais' doit être reconnue comme action")
            .that(OcrInstructionUtils.hasActionVerb("Conservez au frais", vocabulary.stepActionKeywords))
            .isTrue()
    }
}
