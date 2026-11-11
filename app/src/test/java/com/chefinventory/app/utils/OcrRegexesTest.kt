package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et les performances
 * du registre centralisé d'expressions régulières [OcrRegexes].
 */
class OcrRegexesTest {

    @Test
    fun testNormalizerOcrOneCorrection() {
        val replaced1 = "| citron".replace(OcrRegexes.Normalizer.OCR_ONE_CORRECTION, "$1 1")
        assertWithMessage("Le tuyau '|' devant un mot doit être remplacé par le chiffre '1'")
            .that(replaced1)
            .contains("1")

        val replaced2 = "(l ail".replace(OcrRegexes.Normalizer.OCR_ONE_CORRECTION, "$1 1")
        assertWithMessage("La lettre 'l' devant un mot après parenthèse doit être remplacée par '1'")
            .that(replaced2)
            .contains("1")
    }

    @Test
    fun testGetInstructionPrefixPattern_ExcludesTimeKeywordsAndShortWords() {
        val keywordsWithTime = listOf("couper", "minutes", "heures", "de")
        val pattern = OcrRegexes.getInstructionPrefixPattern(keywordsWithTime)
        assertWithMessage("Le générateur de motif de préfixe d'instruction doit retourner un Regex non null")
            .that(pattern)
            .isNotNull()
    }

    @Test
    fun `Quantities FRACTION should match fractional quantities`() {
        val match = OcrRegexes.Quantities.FRACTION.find("1/2 verre d'eau")

        assertWithMessage("Le motif de fraction doit matcher '1/2'")
            .that(match)
            .isNotNull()

        assertWithMessage("Le numérateur extrait doit être '1'")
            .that(match!!.groupValues[1])
            .isEqualTo("1")

        assertWithMessage("Le dénominateur extrait doit être '2'")
            .that(match.groupValues[2])
            .isEqualTo("2")
    }

    @Test
    fun `Quantities RANGE should match quantity ranges`() {
        val match = OcrRegexes.Quantities.RANGE.find("10 à 15 minutes")

        assertWithMessage("Le motif de plage de quantité doit matcher '10 à 15'")
            .that(match)
            .isNotNull()

        assertWithMessage("La borne inférieure extraite doit être '10'")
            .that(match!!.groupValues[1])
            .isEqualTo("10")

        assertWithMessage("La borne supérieure extraite doit être '15'")
            .that(match.groupValues[2])
            .isEqualTo("15")
    }

    @Test
    fun `Quantities STANDARD matcher should extract digit quantity and rest`() {
        val matcher = OcrRegexes.Quantities.STANDARD.matcher("250 g de farine")

        assertWithMessage("Le matcher de quantité standard doit trouver un match")
            .that(matcher.find())
            .isTrue()

        assertWithMessage("La quantité extraite du premier groupe doit être '250'")
            .that(matcher.group(1))
            .isEqualTo("250")

        assertWithMessage("Le reste du texte extrait du deuxième groupe doit être 'g de farine'")
            .that(matcher.group(2))
            .isEqualTo("g de farine")
    }

    @Test
    fun `Ingredients PARENTHESES should extract supplemental info in brackets`() {
        val match = OcrRegexes.Ingredients.PARENTHESES.find("4 petites tomates (ou 2 grosses)")

        assertWithMessage("Le motif de parenthèses d'ingrédient doit trouver la sous-chaîne")
            .that(match)
            .isNotNull()

        assertWithMessage("L'information supplémentaire entre parenthèses doit être 'ou 2 grosses'")
            .that(match!!.groupValues[1])
            .isEqualTo("ou 2 grosses")
    }

    @Test
    fun `Ingredients VAGUE_QUANTITY should match vague quantity words`() {
        val match = OcrRegexes.Ingredients.VAGUE_QUANTITY.find("quelques branches de persil")

        assertWithMessage("Le motif de quantité vague doit trouver un match")
            .that(match)
            .isNotNull()

        assertWithMessage("Le mot de quantité vague extrait doit être 'quelques'")
            .that(match!!.groupValues[1])
            .isEqualTo("quelques")
    }

    @Test
    fun `Ingredients PREPARATION_PREFIX should match prep prefixes like le jus de`() {
        val match = OcrRegexes.Ingredients.PREPARATION_PREFIX.find("le jus de 1/2 citron")

        assertWithMessage("Le préfixe de préparation 'jus' doit être extrait")
            .that(match)
            .isNotNull()

        assertWithMessage("Le mot-clé de préparation extrait doit être 'jus'")
            .that(match!!.groupValues[1])
            .isEqualTo("jus")
    }

    @Test
    fun `Normalizer PROTECTED_KEYWORDS should match ingrédient and instruction headers`() {
        val match = OcrRegexes.Normalizer.PROTECTED_KEYWORDS.find("Ingrédients")

        assertWithMessage("Le motif d'en-têtes protégés doit matcher 'Ingrédients'")
            .that(match)
            .isNotNull()

        assertWithMessage("Le suffixe de l'en-tête extrait doit correspondre à 'ngrédients'")
            .that(match!!.groupValues[1])
            .isEqualTo("ngrédients")
    }

    @Test
    fun `Normalizer SPOON_NORMALIZATION should contain spoon correction rules`() {
        assertWithMessage("La liste de normalisation des cuillères ne doit pas être vide")
            .that(OcrRegexes.Normalizer.SPOON_NORMALIZATION)
            .isNotEmpty()

        val casRule = OcrRegexes.Normalizer.SPOON_NORMALIZATION.find { it.second == "c. à soupe" }
        assertWithMessage("La règle de normalisation vers 'c. à soupe' doit figurer dans la liste")
            .that(casRule)
            .isNotNull()
    }

    @Test
    fun `Normalizer PREFIX_T_BEFORE_DIGIT should match t before digits`() {
        val replaced = "t 10 min".replace(OcrRegexes.Normalizer.PREFIX_T_BEFORE_DIGIT, "")
        assertWithMessage("Le préfixe 't ' devant le chiffre 10 doit être supprimé")
            .that(replaced)
            .isEqualTo("10 min")
    }

    @Test
    fun `Normalizer ENVIRON_WORD should match environ word`() {
        val replaced = "environ 250 g".replace(OcrRegexes.Normalizer.ENVIRON_WORD, "").trim()
        assertWithMessage("Le mot 'environ' devant une quantité doit être supprimé")
            .that(replaced)
            .isEqualTo("250 g")
    }

    @Test
    fun `Normalizer SYMBOL_BEFORE_DIGIT should match bullet symbols before digits`() {
        val replaced = "! 100 g".replace(OcrRegexes.Normalizer.SYMBOL_BEFORE_DIGIT, "")
        assertWithMessage("La puce '!' devant la quantité '100 g' doit être supprimée")
            .that(replaced)
            .isEqualTo("100 g")
    }

    @Test
    fun `Cleaner TECHNICAL_DIMENSION should match mm and cm dimensions`() {
        assertWithMessage("La dimension '5 mm sur 5 mm' doit être reconnue comme dimension technique")
            .that(OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn("5 mm sur 5 mm"))
            .isTrue()

        assertWithMessage("La dimension '1 cm x 2 cm' doit être reconnue comme dimension technique")
            .that(OcrRegexes.Cleaner.TECHNICAL_DIMENSION.containsMatchIn("1 cm x 2 cm"))
            .isTrue()
    }

    @Test
    fun `Metadata regexes should match servings prep time and kcal`() {
        assertWithMessage("Le motif de portions doit matcher 'pour 4 personnes'")
            .that(OcrRegexes.Metadata.SERVINGS.containsMatchIn("pour 4 personnes"))
            .isTrue()

        assertWithMessage("Le motif de temps de préparation doit matcher 'préparation : 15 min'")
            .that(OcrRegexes.Metadata.TIME_PREFIX.containsMatchIn("préparation : 15 min"))
            .isTrue()

        assertWithMessage("Le motif de kilocalories doit matcher '250 kcal'")
            .that(OcrRegexes.Metadata.KCAL.containsMatchIn("250 kcal"))
            .isTrue()
    }

    @Test
    fun `getAdPrefixPattern should return memoized regex pattern for ad exclusions`() {
        val pattern1 = OcrRegexes.getAdPrefixPattern("Assiette de présentation")
        val pattern2 = OcrRegexes.getAdPrefixPattern("Assiette de présentation")

        assertWithMessage("Le motif pré-compilé pour l'exclusion publicitaire doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif de préfixe publicitaire doit matcher 'Assiette de présentation disponible'")
            .that(pattern1.containsMatchIn("Assiette de présentation disponible"))
            .isTrue()
    }

    @Test
    fun `getUnitPattern should return memoized regex pattern for culinary units`() {
        val pattern1 = OcrRegexes.getUnitPattern("c. à soupe")
        val pattern2 = OcrRegexes.getUnitPattern("c. à soupe")

        assertWithMessage("Le motif pré-compilé pour l'unité culinaire doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif d'unité doit matcher 'c. à soupe de farine'")
            .that(pattern1.containsMatchIn("c. à soupe de farine"))
            .isTrue()
    }

    @Test
    fun `Beautifier regexes should match step numbers and bullets`() {
        assertWithMessage("Le préfixe de numéro d'étape '1. Couper' doit être reconnu")
            .that(OcrRegexes.Beautifier.STEP_DIGIT_PREFIX.containsMatchIn("1. Couper"))
            .isTrue()

        assertWithMessage("Le chiffre isolé '1 Plongez' devant majuscule doit être reconnu")
            .that(OcrRegexes.Beautifier.STEP_NUMBER_BEFORE_CAPITAL.containsMatchIn("1 Plongez"))
            .isTrue()

        assertWithMessage("La puce '• Faire cuire' doit être reconnue")
            .that(OcrRegexes.Beautifier.BULLET_PREFIX.containsMatchIn("• Faire cuire"))
            .isTrue()
    }

    @Test
    fun `getInstructionPrefixPattern should return memoized regex pattern for instruction prefixes`() {
        val keywords = listOf("Coupez", "Plongez", "Mélangez")
        val pattern1 = OcrRegexes.getInstructionPrefixPattern(keywords)
        val pattern2 = OcrRegexes.getInstructionPrefixPattern(keywords)

        assertWithMessage("Le motif d'instructions doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif d'instruction doit matcher '1. Coupez les tomates'")
            .that(pattern1.containsMatchIn("1. Coupez les tomates"))
            .isTrue()
    }

    @Test
    fun `getSpellingRepairPattern should return memoized pattern for spelling repairs`() {
        val pattern1 = OcrRegexes.getSpellingRepairPattern("lhuile")
        val pattern2 = OcrRegexes.getSpellingRepairPattern("lhuile")

        assertWithMessage("Le motif de réparation orthographique doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif de réparation orthographique doit matcher 'lhuile'")
            .that(pattern1.matcher("lhuile").find())
            .isTrue()
    }

    @Test
    fun `getStemWordPattern should return memoized regex pattern for ingredient stem`() {
        val pattern1 = OcrRegexes.getStemWordPattern("mangue")
        val pattern2 = OcrRegexes.getStemWordPattern("mangue")

        assertWithMessage("Le motif de radical d'ingrédient doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif de radical d'ingrédient doit matcher 'Salade de mangues'")
            .that(pattern1.containsMatchIn("Salade de mangues"))
            .isTrue()
    }

    @Test
    fun `getWholeWordPattern should return memoized regex pattern for whole words`() {
        val pattern1 = OcrRegexes.getWholeWordPattern("Bordeaux")
        val pattern2 = OcrRegexes.getWholeWordPattern("Bordeaux")

        assertWithMessage("Le motif de mot entier doit être mis en cache (même instance)")
            .that(pattern1)
            .isSameInstanceAs(pattern2)

        assertWithMessage("Le motif de mot entier doit matcher 'Un vin de Bordeaux'")
            .that(pattern1.containsMatchIn("Un vin de Bordeaux"))
            .isTrue()
    }

    @Test
    fun `DIFFICULTY regex should match recipe difficulty but ignore feu moyen and chaleur moyenne`() {
        assertWithMessage("La difficulté 'Difficulté : Moyen' doit être reconnue")
            .that(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("Difficulté : Moyen"))
            .isTrue()

        assertWithMessage("La difficulté 'Facile' doit être reconnue")
            .that(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("Facile"))
            .isTrue()

        assertWithMessage("Le terme de cuisson 'à feu moyen' ne doit pas être qualifié de difficulté de recette")
            .that(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("à feu moyen"))
            .isFalse()

        assertWithMessage("Le terme de cuisson 'a feu moyen' ne doit pas être qualifié de difficulté")
            .that(OcrRegexes.Metadata.DIFFICULTY.containsMatchIn("a feu moyen"))
            .isFalse()
    }
}
