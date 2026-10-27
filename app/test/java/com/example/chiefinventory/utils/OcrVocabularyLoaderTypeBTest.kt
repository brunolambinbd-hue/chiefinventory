package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

/**
 * Suite de tests unitaires de Type B vérifiant le chargement des ressources XML OCR,
 * la gestion des exceptions et la couverture de 100% de [OcrVocabularyLoader].
 */
class OcrVocabularyLoaderTypeBTest {

    @Test
    fun `loadFromProjectResources should parse production OCR vocabulary from res values directory`() {
        val vocab = OcrVocabularyLoader.loadFromProjectResources()

        assertThat(vocab.culinaryMeasurementUnits).isNotEmpty()
        assertThat(vocab.culinaryMeasurementUnits).contains("g")
        assertThat(vocab.stepActionKeywords).isNotEmpty()
    }

    @Test
    fun `loadFromProjectResources should throw IllegalStateException when res values directory does not exist`() {
        val invalidDir = File("Z:/non_existent_drive_folder_12345/test")
        if (invalidDir.exists()) invalidDir.deleteRecursively()

        val exception = assertThrows(IllegalStateException::class.java) {
            OcrVocabularyLoader.loadFromProjectResources(invalidDir)
        }

        assertThat(exception.message).contains("Could not locate 'res/values' directory")
    }

    @Test
    fun `parseXmlResourceFile should catch exception gracefully on corrupt or malformed XML file`() {
        val tempCorruptFile = File.createTempFile("corrupt_vocab", ".xml")
        tempCorruptFile.writeText("THIS IS NOT XML CONTENT AT ALL <<<<>>>>")

        val stringArrays = mutableMapOf<String, List<String>>()
        val strings = mutableMapOf<String, String>()

        OcrVocabularyLoader.parseXmlResourceFile(tempCorruptFile, stringArrays, strings)

        assertThat(stringArrays).isEmpty()
        assertThat(strings).isEmpty()

        tempCorruptFile.delete()
    }
}
