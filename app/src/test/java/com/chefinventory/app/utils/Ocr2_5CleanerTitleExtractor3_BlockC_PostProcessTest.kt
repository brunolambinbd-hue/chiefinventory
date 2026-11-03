package com.chefinventory.app.utils

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Suite de tests unitaires dédiée 1-à-1 pour [Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess].
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Ocr2_5CleanerTitleExtractor3_BlockC_PostProcessTest {

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
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("de", true, verbs)).isFalse()

        // 2. Contains space -> false
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("salade verte", true, verbs)).isFalse()

        // 3. Feminine adj but noun doesn't end with "e" -> false
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("melon", true, verbs)).isFalse()

        // 4. Number word ("deux") -> false
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("deux", false, verbs)).isFalse()

        // 5. Action verb ("Chauffer") -> false
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("Chauffer", false, verbs)).isFalse()

        // 6. Stop word ("pour") -> false
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("pour", false, verbs)).isFalse()

        // 7. Eligible noun ("salade") -> true
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isEligibleOrphanNoun("salade", true, verbs)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testPostProcessTitle_SpellingRepairsAndInvalidRules() {
        val title = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.postProcessTitle("Salade de tonate", vocabulary)
        assertThat(title).isNotNull()

        // Invalid repair rule without |
        val invalidVocab = vocabulary.copy(titleSpellingRepairs = listOf("invalidRuleWithoutPipe"))
        val titleInvalid = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.postProcessTitle("Salade", invalidVocab)
        assertThat(titleInvalid).isEqualTo("Salade")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testIsMultiLineTitleConnector_WithVocabularyAndNull() {
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("au jambon", vocabulary)).isTrue()
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("aux crevettes", null)).isTrue()
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("aux crevettes")).isTrue()
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("farine", vocabulary)).isFalse()

        // Vocabulary with empty connectors
        val emptyConnectorsVocab = vocabulary.copy(titleMultilineConnectors = emptyList())
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.isMultiLineTitleConnector("au jambon", emptyConnectorsVocab)).isTrue()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRepairFirstWordLettrine_RepairsLettrineAndShortWords() {
        // 1. Repair lettrine
        val repaired = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("Drabe mangue", setOf("crabe"), vocabulary)
        assertThat(repaired).isEqualTo("Crabe mangue")

        // 2. First word < 3 chars
        val shortWord = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("Le gâteau", setOf("gâteau"), vocabulary)
        assertThat(shortWord).isEqualTo("Le gâteau")

        // 3. Empty candidate
        val empty = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.repairFirstWordLettrine("   ", setOf("crabe"), vocabulary)
        assertThat(empty).isEmpty()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testRecoverOrphanNoun_FullBranches() {
        // 1. null title -> null
        assertThat(Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun(null, mutableListOf(), vocabulary)).isNull()

        // 2. Title with feminine adjective "petite" and orphan noun "salade"
        val remainingFeminine = mutableListOf("100 g de sucre", "salade.")
        val titleFem = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingFeminine, vocabulary)
        assertThat(titleFem).isEqualTo("Petite salade")
        assertThat(remainingFeminine).doesNotContain("salade.")

        // 3. Title with "petites"
        val remainingPetites = mutableListOf("100 g de sucre", "tarte.")
        val titlePetites = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite s", remainingPetites, vocabulary)
        assertThat(titlePetites).isEqualTo("Petite tarte")

        // 4. Title with "grande"
        val remainingGrande = mutableListOf("100 g de sucre", "soupe.")
        val titleGrande = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Grande", remainingGrande, vocabulary)
        assertThat(titleGrande).isEqualTo("Grande soupe")

        // 5. Title with "belle"
        val remainingBelle = mutableListOf("100 g de sucre", "tarte.")
        val titleBelle = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Belle", remainingBelle, vocabulary)
        assertThat(titleBelle).isEqualTo("Belle tarte")

        // 6. Title with masculine adjective "grand" and orphan noun "potage"
        val remainingMasculine = mutableListOf("potage.")
        val titleMasc = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Grand", remainingMasculine, vocabulary)
        assertThat(titleMasc).isEqualTo("Grand potage")

        // 7. Title with trailing adjective but no valid orphan noun in remaining lines
        val remainingNoMatch = mutableListOf("couper les oignons", "cuire au four")
        val titleNoMatch = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingNoMatch, vocabulary)
        assertThat(titleNoMatch).isEqualTo("Petite")

        // 8. Title with no trailing adjective -> unchanged
        val remainingNoAdj = mutableListOf("salade")
        val titleNoAdj = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Tarte aux pommes", remainingNoAdj, vocabulary)
        assertThat(titleNoAdj).isEqualTo("Tarte aux pommes")

        // 9. Orphan noun candidate line with single word action verb ("Chauffer")
        val remainingWithVerb = mutableListOf("Chauffer", "tarte.")
        val titleVerb = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingWithVerb, vocabulary)
        assertThat(titleVerb).isEqualTo("Petite tarte")

        // 10. Orphan noun candidate line containing stop word ("pour")
        val remainingWithStop = mutableListOf("pour", "tarte.")
        val titleStop = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.recoverOrphanNoun("Petite", remainingWithStop, vocabulary)
        assertThat(titleStop).isEqualTo("Petite tarte")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testStripRecipeNumber_StripsPrefixDigitsAndEmpty() {
        val stripped = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber("12 Salade grecque")
        assertThat(stripped).isEqualTo("Salade grecque")

        val emptyStripped = Ocr2_5CleanerTitleExtractor3_BlockC_PostProcess.stripRecipeNumber("   ")
        assertThat(emptyStripped).isEmpty()
    }
}
