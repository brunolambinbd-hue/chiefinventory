package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Test

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerIngredientDetector].
 */
class Ocr3CategorizerIngredientDetectorTest {

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCountIngredientSequences_ExhaustiveBranches() {
        assertWithMessage("Une ligne vide ne doit pas contenir de séquence d'ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences(""))
            .isEqualTo(0)

        assertWithMessage("Une ligne d'espaces blancs ne doit pas contenir de séquence d'ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("   "))
            .isEqualTo(0)

        assertWithMessage("La ligne '200 g 100' doit compter 1 séquence d'ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g 100"))
            .isEqualTo(1)

        assertWithMessage("La ligne '2 Carottes 1 Pomme' doit compter 2 séquences d'ingrédients")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("2 Carottes 1 Pomme"))
            .isEqualTo(2)

        assertWithMessage("La ligne '2 Carottes' doit compter 1 séquence d'ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("2 Carottes"))
            .isEqualTo(1)

        // Inside and outside parentheses branches
        assertWithMessage("La quantité entre parenthèses '(100 g)' ne doit pas être comptée comme séquence supplémentaire")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g (100 g de sucre)"))
            .isEqualTo(1)

        assertWithMessage("Deux ingrédients quantifiés hors parenthèses doivent compter 2 séquences")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("200 g de farine 100 g de sucre"))
            .isEqualTo(2)

        // Excluded next words branch
        assertWithMessage("Une durée '10 min' ne doit pas être comptée comme séquence d'ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("10 min"))
            .isEqualTo(0)

        assertWithMessage("Deux nombres isolés '10 20' ne doivent pas être comptés comme ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("10 20"))
            .isEqualTo(0)

        assertWithMessage("Un nombre suivi de ponctuation '100 , ; .' ne doit pas être compté comme ingrédient")
            .that(Ocr3CategorizerIngredientDetector.countIngredientSequences("100 , ; ."))
            .isEqualTo(0)
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testCleanIngredientSemantics_ExhaustiveBranches() {
        // Line length / no letters
        assertWithMessage("Une ligne vide doit retourner une chaîne nettoyée vide")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("", emptyList()))
            .isEmpty()

        assertWithMessage("Une ligne d'espaces doit retourner une chaîne nettoyée vide")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("   ", emptyList()))
            .isEmpty()

        assertWithMessage("Un symbole parasite '!' sans lettre doit retourner une chaîne vide")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("!", emptyList()))
            .isEmpty()

        assertWithMessage("Une série de symboles '!!!' sans lettre doit retourner une chaîne vide")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("!!!", emptyList()))
            .isEmpty()

        assertWithMessage("Une lettre seule 'a' sans quantité doit retourner une chaîne vide")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("a", emptyList()))
            .isEmpty()

        // Proper name detection
        assertWithMessage("Un nom propre 'MICHEL LAROCHE' doit être éliminé des ingrédients")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("MICHEL LAROCHE", emptyList()))
            .isEmpty()

        // Excluded keywords
        assertWithMessage("Le mot-clé d'exclusion 'PAGE' doit être éliminé")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("PAGE", listOf("page")))
            .isEmpty()

        // Semantic exclusions
        assertWithMessage("Le mot d'exclusion sémantique 'BIO' doit être éliminé")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("BIO", emptyList(), listOf("bio")))
            .isEmpty()

        // Standalone articles
        assertWithMessage("L'article isolé 'du' doit être éliminé")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("du", emptyList()))
            .isEmpty()

        assertWithMessage("L'article isolé 'le' présent dans la liste fournie doit être éliminé")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("le", emptyList(), emptyList(), listOf("le")))
            .isEmpty()

        // Valid ingredient line (single word and multi-word)
        assertWithMessage("La puce '• Tomate' doit être nettoyée en 'Tomate'")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("• Tomate", emptyList()))
            .isEqualTo("Tomate")

        assertWithMessage("Le tiret '- Oignon' doit être nettoyé en 'Oignon'")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("- Oignon", emptyList()))
            .isEqualTo("Oignon")

        assertWithMessage("L'ingrédient multi-mots 'Tomate cerise' doit être conservé")
            .that(Ocr3CategorizerIngredientDetector.cleanIngredientSemantics("Tomate cerise", emptyList()))
            .isEqualTo("Tomate cerise")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testHasExplicitWeight() {
        assertWithMessage("La ligne '200 g de farine' doit être reconnue comme ayant un poids explicite")
            .that(Ocr3CategorizerIngredientDetector.hasExplicitWeight("200 g de farine"))
            .isTrue()

        assertWithMessage("La ligne '5 cl d'huile' doit être reconnue comme ayant un volume explicite")
            .that(Ocr3CategorizerIngredientDetector.hasExplicitWeight("5 cl d'huile"))
            .isTrue()

        assertWithMessage("La ligne 'Sel et poivre' sans chiffre ne doit pas être qualifiée de poids explicite")
            .that(Ocr3CategorizerIngredientDetector.hasExplicitWeight("Sel et poivre"))
            .isFalse()
    }
}
