package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertWithMessage
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr3CategorizerTitleExtractor3_BlockC_PostProcess].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr3CategorizerTitleExtractor3_BlockC_PostProcessTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsEligibleOrphanNoun_DirectCallAllBranches() {
        val verbs = vocabulary.stepActionKeywords

        // 1. Length too short (<3) -> false
        assertWithMessage("Un mot trop court (< 3 caractères) 'de' ne doit pas être qualifié de nom orphelin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("de", true, verbs))
            .isFalse()

        // 2. Contains space -> false
        assertWithMessage("Une expression multi-mots 'salade verte' ne doit pas être qualifiée de nom orphelin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("salade verte", true, verbs))
            .isFalse()

        // 3. Feminine adj but noun doesn't end with "e" -> false
        assertWithMessage("Un nom masculin 'melon' ne doit pas valider un adjectif féminin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("melon", true, verbs))
            .isFalse()

        // 4. Number word ("deux") -> false
        assertWithMessage("Un nombre 'deux' ne doit pas être qualifié de nom orphelin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("deux", false, verbs))
            .isFalse()

        // 5. Action verb ("Chauffer") -> false
        assertWithMessage("Un verbe d'action 'Chauffer' ne doit pas être qualifié de nom orphelin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("Chauffer", false, verbs))
            .isFalse()

        // 6. Stop word ("pour") -> false
        assertWithMessage("Un mot vide 'pour' ne doit pas être qualifié de nom orphelin")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("pour", false, verbs))
            .isFalse()

        // 7. Eligible noun ("salade") -> true
        assertWithMessage("Le mot 'salade' doit être qualifié de nom orphelin éligible")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("salade", true, verbs))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPostProcessTitle_SpellingRepairsAndInvalidRules() {
        val title = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.postProcessTitle("Salade de tonate", vocabulary)
        assertWithMessage("Le post-traitement du titre doit retourner une chaîne non nulle")
            .that(title)
            .isNotNull()

        // Invalid repair rule without |
        val invalidVocab = vocabulary.copy(titleSpellingRepairs = listOf("invalidRuleWithoutPipe"))
        val titleInvalid = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.postProcessTitle("Salade", invalidVocab)
        assertWithMessage("Les règles de réparations invalides sans pipe '|' doivent être ignorées sans erreur")
            .that(titleInvalid)
            .isEqualTo("Salade")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsMultiLineTitleConnector_WithVocabularyAndNull() {
        assertWithMessage("Le connecteur 'au jambon' doit être reconnu comme connecteur multiligne de titre")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("au jambon", vocabulary))
            .isTrue()

        assertWithMessage("Le connecteur 'aux crevettes' avec vocabulaire null doit être reconnu")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("aux crevettes", null))
            .isTrue()

        assertWithMessage("Le connecteur 'aux crevettes' sans deuxième argument doit être reconnu")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("aux crevettes"))
            .isTrue()

        assertWithMessage("Le mot 'farine' ne doit pas être reconnu comme connecteur de titre")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("farine", vocabulary))
            .isFalse()

        // Vocabulary with empty connectors
        val emptyConnectorsVocab = vocabulary.copy(titleMultilineConnectors = emptyList())
        assertWithMessage("Un dictionnaire de connecteurs vide doit utiliser la liste de secours")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("au jambon", emptyConnectorsVocab))
            .isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRepairFirstWordLettrine_RepairsLettrineAndShortWords() {
        // 1. Repair lettrine
        val repaired = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("Drabe mangue", setOf("crabe"), vocabulary)
        assertWithMessage("La lettrine 'Drabe' doit être réparée en 'Crabe mangue'")
            .that(repaired)
            .isEqualTo("Crabe mangue")

        // 2. First word < 3 chars
        val shortWord = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("Le gâteau", setOf("gâteau"), vocabulary)
        assertWithMessage("Un premier mot court (< 3 chars) 'Le' doit être retourné inchangé")
            .that(shortWord)
            .isEqualTo("Le gâteau")

        // 3. Empty candidate
        val empty = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("   ", setOf("crabe"), vocabulary)
        assertWithMessage("Un candidat titre vide doit retourner une chaîne vide")
            .that(empty)
            .isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecoverOrphanNoun_FullBranches() {
        // 1. null title -> null
        assertWithMessage("Un titre null doit retourner null")
            .that(Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun(null, mutableListOf(), vocabulary))
            .isNull()

        // 2. Title with feminine adjective "petite" and orphan noun "salade"
        val remainingFeminine = mutableListOf("100 g de sucre", "salade.")
        val titleFem = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingFeminine, vocabulary)
        assertWithMessage("Le titre 'Petite' doit récupérer le nom orphelin 'salade' pour former 'Petite salade'")
            .that(titleFem)
            .isEqualTo("Petite salade")

        assertWithMessage("Le nom orphelin récupéré 'salade.' doit être retiré de la liste restante")
            .that(remainingFeminine)
            .doesNotContain("salade.")

        // 3. Title with "petites"
        val remainingPetites = mutableListOf("100 g de sucre", "tarte.")
        val titlePetites = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite s", remainingPetites, vocabulary)
        assertWithMessage("Le titre 'Petite s' doit récupérer 'tarte' pour former 'Petite tarte'")
            .that(titlePetites)
            .isEqualTo("Petite tarte")

        // 4. Title with "grande"
        val remainingGrande = mutableListOf("100 g de sucre", "soupe.")
        val titleGrande = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Grande", remainingGrande, vocabulary)
        assertWithMessage("Le titre 'Grande' doit récupérer 'soupe' pour former 'Grande soupe'")
            .that(titleGrande)
            .isEqualTo("Grande soupe")

        // 5. Title with "belle"
        val remainingBelle = mutableListOf("100 g de sucre", "tarte.")
        val titleBelle = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Belle", remainingBelle, vocabulary)
        assertWithMessage("Le titre 'Belle' doit récupérer 'tarte' pour former 'Belle tarte'")
            .that(titleBelle)
            .isEqualTo("Belle tarte")

        // 6. Title with masculine adjective "grand" and orphan noun "potage"
        val remainingMasculine = mutableListOf("potage.")
        val titleMasc = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Grand", remainingMasculine, vocabulary)
        assertWithMessage("Le titre masculin 'Grand' doit récupérer 'potage' pour former 'Grand potage'")
            .that(titleMasc)
            .isEqualTo("Grand potage")

        // 7. Title with trailing adjective but no valid orphan noun in remaining lines
        val remainingNoMatch = mutableListOf("couper les oignons", "cuire au four")
        val titleNoMatch = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingNoMatch, vocabulary)
        assertWithMessage("Un adjectif sans nom orphelin éligible doit retourner le titre inchangé")
            .that(titleNoMatch)
            .isEqualTo("Petite")

        // 8. Title with no trailing adjective -> unchanged
        val remainingNoAdj = mutableListOf("salade")
        val titleNoAdj = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Tarte aux pommes", remainingNoAdj, vocabulary)
        assertWithMessage("Un titre sans adjectif suspendu doit être retourné inchangé")
            .that(titleNoAdj)
            .isEqualTo("Tarte aux pommes")

        // 9. Orphan noun candidate line with single word action verb ("Chauffer")
        val remainingWithVerb = mutableListOf("Chauffer", "tarte.")
        val titleVerb = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingWithVerb, vocabulary)
        assertWithMessage("Le verbe d'action 'Chauffer' doit être ignoré pour récupérer le nom 'tarte'")
            .that(titleVerb)
            .isEqualTo("Petite tarte")

        // 10. Orphan noun candidate line containing stop word ("pour")
        val remainingWithStop = mutableListOf("pour", "tarte.")
        val titleStop = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingWithStop, vocabulary)
        assertWithMessage("Le mot-mort 'pour' doit être ignoré pour récupérer le nom 'tarte'")
            .that(titleStop)
            .isEqualTo("Petite tarte")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStripRecipeNumber_StripsPrefixDigitsAndEmpty() {
        val stripped = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber("12 Salade grecque")
        assertWithMessage("Le numéro de recette préfixe '12' doit être retiré")
            .that(stripped)
            .isEqualTo("Salade grecque")

        val emptyStripped = Ocr3CategorizerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber("   ")
        assertWithMessage("Une chaîne d'espaces doit retourner une chaîne vide")
            .that(emptyStripped)
            .isEmpty()
    }
}
