package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3Categorizer0_Context].
 */
class Ocr3Categorizer0_ContextTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testProcessBlock0_CategorizationContext_FullCoverageAndGeneratedDataClassMethods() {
        val vocabulary = OcrVocabularyLoader.loadFromProjectResources()
        val ctx = Ocr3Categorizer0_Context.processBlock0_CategorizationContext(vocabulary)

        assertWithMessage("Les verbes d'action culinaires du contexte doivent être renseignés")
            .that(ctx.actionVerbs)
            .isNotEmpty()

        assertWithMessage("Les ingrédients connus sans quantité du contexte doivent être renseignés")
            .that(ctx.commonIngredients)
            .isNotEmpty()

        assertWithMessage("Les mots-clés d'exclusion stricte du contexte doivent être renseignés")
            .that(ctx.excludedKeywords)
            .isNotEmpty()

        assertWithMessage("Les adjectifs de préparation du contexte doivent être renseignés")
            .that(ctx.preparationModifiers)
            .isNotEmpty()

        assertWithMessage("Les déclencheurs d'instructions du contexte doivent être renseignés")
            .that(ctx.instructionTriggers)
            .isNotEmpty()

        assertWithMessage("Les exclusions sémantiques du contexte doivent être renseignées")
            .that(ctx.semanticExclusions)
            .isNotEmpty()

        assertWithMessage("Les marqueurs de fin du contexte doivent être renseignés")
            .that(ctx.endMarkers)
            .isNotEmpty()

        assertWithMessage("Les sous-en-têtes d'ingrédients du contexte doivent être renseignés")
            .that(ctx.subHeaders)
            .isNotEmpty()

        assertWithMessage("Les en-têtes d'ingrédients du contexte doivent être renseignés")
            .that(ctx.ingredientHeaders)
            .isNotEmpty()

        assertWithMessage("Les en-têtes d'instructions du contexte doivent être renseignés")
            .that(ctx.instructionHeaders)
            .isNotEmpty()

        assertWithMessage("L'expression régulière de quantité stricte doit être pré-compilée")
            .that(ctx.hardQtyRegex)
            .isNotNull()

        assertWithMessage("L'expression régulière de quantité générale doit être pré-compilée")
            .that(ctx.qtyRegex)
            .isNotNull()

        assertWithMessage("L'expression régulière de présence de verbe d'action doit être pré-compilée")
            .that(ctx.containsActionRegex)
            .isNotNull()

        assertWithMessage("Les connecteurs d'ingrédients du contexte doivent être renseignés")
            .that(ctx.ingredientConnectors)
            .isNotEmpty()

        // Test Data class generated methods (componentN, copy, equals, hashCode, toString)
        val copyCtx = ctx.copy()
        assertWithMessage("La copie de CategorizationContext doit être égale à l'objet d'origine")
            .that(copyCtx)
            .isEqualTo(ctx)

        assertWithMessage("Le hashcode de la copie doit correspondre à l'original")
            .that(copyCtx.hashCode())
            .isEqualTo(ctx.hashCode())

        assertWithMessage("La méthode toString de CategorizationContext doit contenir le nom de la classe")
            .that(copyCtx.toString())
            .contains("CategorizationContext")

        val (action, common, excl, prep, instTrig, semExcl, endM, subH, ingrH, instH, hardQ, qty, containsA, ingrConn) = ctx
        assertWithMessage("La déconstruction du composant actionVerbs doit être valide")
            .that(action)
            .isEqualTo(ctx.actionVerbs)

        assertWithMessage("La déconstruction du composant commonIngredients doit être valide")
            .that(common)
            .isEqualTo(ctx.commonIngredients)

        assertWithMessage("La déconstruction du composant excludedKeywords doit être valide")
            .that(excl)
            .isEqualTo(ctx.excludedKeywords)

        assertWithMessage("La déconstruction du composant preparationModifiers doit être valide")
            .that(prep)
            .isEqualTo(ctx.preparationModifiers)

        assertWithMessage("La déconstruction du composant instructionTriggers doit être valide")
            .that(instTrig)
            .isEqualTo(ctx.instructionTriggers)

        assertWithMessage("La déconstruction du composant semanticExclusions doit être valide")
            .that(semExcl)
            .isEqualTo(ctx.semanticExclusions)

        assertWithMessage("La déconstruction du composant endMarkers doit être valide")
            .that(endM)
            .isEqualTo(ctx.endMarkers)

        assertWithMessage("La déconstruction du composant subHeaders doit être valide")
            .that(subH)
            .isEqualTo(ctx.subHeaders)

        assertWithMessage("La déconstruction du composant ingredientHeaders doit être valide")
            .that(ingrH)
            .isEqualTo(ctx.ingredientHeaders)

        assertWithMessage("La déconstruction du composant instructionHeaders doit être valide")
            .that(instH)
            .isEqualTo(ctx.instructionHeaders)

        assertWithMessage("La déconstruction du composant hardQtyRegex doit être valide")
            .that(hardQ)
            .isEqualTo(ctx.hardQtyRegex)

        assertWithMessage("La déconstruction du composant qtyRegex doit être valide")
            .that(qty)
            .isEqualTo(ctx.qtyRegex)

        assertWithMessage("La déconstruction du composant containsActionRegex doit être valide")
            .that(containsA)
            .isEqualTo(ctx.containsActionRegex)

        assertWithMessage("La déconstruction du composant ingredientConnectors doit être valide")
            .that(ingrConn)
            .isEqualTo(ctx.ingredientConnectors)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRawSections_DataClassGeneratedMethodsCoverage() {
        val sec1 = RawSections(detectedTitle = "Tarte")
        val sec2 = RawSections(detectedTitle = "Tarte")

        assertWithMessage("Les deux objets RawSections doivent être égaux")
            .that(sec1)
            .isEqualTo(sec2)

        assertWithMessage("Le hashcode des deux objets RawSections doit être identique")
            .that(sec1.hashCode())
            .isEqualTo(sec2.hashCode())

        assertWithMessage("La méthode toString de RawSections doit contenir le titre")
            .that(sec1.toString())
            .contains("Tarte")

        assertWithMessage("Le premier composant de RawSections doit être rawIngredientsList")
            .that(sec1.component1())
            .isEmpty()

        assertWithMessage("Le second composant de RawSections doit être rawInstructionsList")
            .that(sec1.component2())
            .isEmpty()

        assertWithMessage("Le troisième composant de RawSections doit être detectedWineList")
            .that(sec1.component3())
            .isEmpty()

        assertWithMessage("Le quatrième composant de RawSections doit être detectedSourceList")
            .that(sec1.component4())
            .isEmpty()

        assertWithMessage("Le cinquième composant de RawSections doit être detectedTitle")
            .that(sec1.component5())
            .isEqualTo("Tarte")

        assertWithMessage("Le quatorzième composant de RawSections doit être detectedNutriScore")
            .that(sec1.component14())
            .isNull()

        assertWithMessage("Le quinzième composant de RawSections doit être ingredientKeywords")
            .that(sec1.component15())
            .isEmpty()

        val secNutri = sec1.copy(detectedNutriScore = "A")
        assertWithMessage("La copie avec detectedNutriScore doit conserver la valeur 'A'")
            .that(secNutri.detectedNutriScore)
            .isEqualTo("A")

        val defaultSec = RawSections()
        assertWithMessage("Default RawSections rawIngredientsList").that(defaultSec.rawIngredientsList).isEmpty()
        assertWithMessage("Default RawSections rawInstructionsList").that(defaultSec.rawInstructionsList).isEmpty()
        assertWithMessage("Default RawSections detectedWineList").that(defaultSec.detectedWineList).isEmpty()
        assertWithMessage("Default RawSections detectedSourceList").that(defaultSec.detectedSourceList).isEmpty()
        assertWithMessage("Default RawSections ingredientKeywords").that(defaultSec.ingredientKeywords).isEmpty()

        // Synthetic default constructor reflection coverage
        val defaultConstructor = RawSections::class.java.declaredConstructors.firstOrNull { c ->
            c.parameterTypes.any { it.name.contains("DefaultConstructorMarker") }
        }
        if (defaultConstructor != null) {
            defaultConstructor.isAccessible = true
            val paramCount = defaultConstructor.parameterCount
            val args = Array<Any?>(paramCount) { i ->
                val pType = defaultConstructor.parameterTypes[i]
                when {
                    pType == String::class.java -> null
                    pType == Int::class.javaPrimitiveType || pType == Int::class.java -> -1
                    pType == List::class.java || pType == MutableList::class.java -> null
                    pType == Set::class.java -> null
                    else -> null
                }
            }
            defaultConstructor.newInstance(*args)
        }
    }
}
