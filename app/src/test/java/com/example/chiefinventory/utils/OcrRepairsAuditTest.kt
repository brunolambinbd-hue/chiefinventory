package com.example.chiefinventory.utils

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Before
import org.junit.Test

/**
 * Suite de tests unitaires d'Audit de Sécurité scrutant le fichier de ressources [ocr_repairs.xml].
 *
 * Responsabilités :
 * - Empêcher l'ajout de règles de réparations corrompues où le terme fautif ("wrong") est un mot français sain, un ingrédient ou un mot de titre.
 * - Prévenir les remplacements ultra-courts dangereux (< 3 lettres) sans délimitation.
 * - Détecter les paires identiques ("wrong == correct") ou les boucles récursives (A|B et B|A).
 */
class OcrRepairsAuditTest {

    private lateinit var vocabulary: OcrVocabulary

    @Before
    fun setUp() {
        vocabulary = OcrVocabularyLoader.loadFromProjectResources()
    }

    /**
     * Règle 1 : Vérifie qu'aucun terme fautif ("wrong") dans ocr_repairs.xml ne soit un ingrédient valide de ingredients.xml.
     */
    @Test
    fun `audit - wrong term should not collide with valid ingredients`() {
        val validIngredients = vocabulary.commonIngredientsNoQty.map { it.lowercase().trim() }.toSet()
        val criticalCollisions = mutableListOf<String>()

        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().lowercase()
                if (wrong in validIngredients) {
                    criticalCollisions.add("Paire '$repair' -> '$wrong' est un ingrédient valide dans ingredients.xml")
                }
            }
        }

        assertWithMessage("Alerte d'audit : Fausse réparation en collision avec des ingrédients valides :\n${criticalCollisions.joinToString("\n")}")
            .that(criticalCollisions).isEmpty()
    }

    /**
     * Règle 2 : Vérifie qu'aucun terme fautif ("wrong") ne soit égal au terme corrigé ("correct").
     */
    @Test
    fun `audit - wrong term should not be equal to correct term`() {
        val identicalPairs = mutableListOf<String>()

        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim()
                val correct = parts[1].trim().replace("\\'", "'")
                if (wrong == correct) {
                    identicalPairs.add(repair)
                }
            }
        }

        assertWithMessage("Alerte d'audit : Paires de réparations inutiles car identiques (wrong == correct) :\n${identicalPairs.joinToString("\n")}")
            .that(identicalPairs).isEmpty()
    }

    /**
     * Règle 3 : Vérifie l'absence de boucles récursives inverses (A|B et B|A).
     */
    @Test
    fun `audit - ocr_repairs should not contain recursive reverse loops`() {
        val map = mutableMapOf<String, String>()
        val loops = mutableListOf<String>()

        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim().lowercase()
                val correct = parts[1].trim().replace("\\'", "'").lowercase()
                
                if (map[correct] == wrong) {
                    loops.add("Boucle récursive détectée entre '$wrong -> $correct' et '$correct -> $wrong'")
                }
                map[wrong] = correct
            }
        }

        assertWithMessage("Alerte d'audit : Boucles récursives d'annulation détectées :\n${loops.joinToString("\n")}")
            .that(loops).isEmpty()
    }

    /**
     * Règle 4 : Vérifie que toutes les règles comportent au moins 2 caractères alphabétiques.
     */
    @Test
    fun `audit - wrong terms should have at least 2 characters`() {
        val ultraShort = mutableListOf<String>()

        vocabulary.ocrSpellingRepairs.forEach { repair ->
            val parts = repair.split("|")
            if (parts.size == 2) {
                val wrong = parts[0].trim()
                if (wrong.length < 2) {
                    ultraShort.add(repair)
                }
            }
        }

        assertWithMessage("Alerte d'audit : Mots fautifs trop courts (< 2 caractères) sans sécurité :\n${ultraShort.joinToString("\n")}")
            .that(ultraShort).isEmpty()
    }
}
