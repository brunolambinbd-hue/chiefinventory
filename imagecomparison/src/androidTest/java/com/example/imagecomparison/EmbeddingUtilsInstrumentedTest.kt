package com.example.imagecomparison

import android.content.Context
import android.graphics.Bitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test d'intégration pour EmbeddingUtils.
 * Ce test s'exécute sur un appareil ou un émulateur Android.
 */
@RunWith(AndroidJUnit4::class)
class EmbeddingUtilsInstrumentedTest {

    private lateinit var context: Context
    private lateinit var imageEmbedderHelper: ImageEmbedderHelper
    private lateinit var testBitmap: Bitmap

    @Before
    fun setup() {
        // On récupère le contexte de l'application de test.
        context = InstrumentationRegistry.getInstrumentation().targetContext

        // On crée un VRAI ImageEmbedderHelper, ce qui va charger le modèle TFLite.
        imageEmbedderHelper = ImageEmbedderHelper(context = context, listener = null)

        // On crée une image simple pour le test.
        testBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
    }

    @Test
    fun testEmbeddingUtils_withRealEmbedding() {
        // 1. On génère un VRAI embedding en utilisant le helper.
        val realEmbedding = imageEmbedderHelper.computeSignature(testBitmap)

        // Pré-condition : on s'assure qu'on a bien réussi à générer un embedding.
        assertNotNull("L'ImageEmbedderHelper devrait produire un embedding valide", realEmbedding)

        // 2. On teste la conversion aller-retour avec notre EmbeddingUtils.
        val byteArray = EmbeddingUtils.embeddingToByteArray(realEmbedding!!)
        val restoredMyEmbedding = EmbeddingUtils.byteArrayToMyEmbedding(byteArray, fromQuantized = realEmbedding.quantizedEmbedding() != null)

        // 3. On vérifie que la conversion a fonctionné correctement.
        assertNotNull("L'embedding restauré ne doit pas être nul", restoredMyEmbedding)

        if (realEmbedding.quantizedEmbedding() != null) {
            // Cas d'un embedding quantifié (en bytes)
            assertArrayEquals("Les bytes restaurés doivent correspondre aux originaux", realEmbedding.quantizedEmbedding(), restoredMyEmbedding.quantizedValues)
            assertNull("Les floats doivent être nuls pour un embedding quantifié", restoredMyEmbedding.floatValues)
            assertTrue("Le drapeau isQuantized doit être à true", restoredMyEmbedding.isQuantized)
        } else {
            // Cas d'un embedding non-quantifié (en floats)
            assertArrayEquals("Les floats restaurés doivent correspondre aux originaux", realEmbedding.floatEmbedding(), restoredMyEmbedding.floatValues, 0.0f)
            assertNull("Les bytes doivent être nuls pour un embedding float", restoredMyEmbedding.quantizedValues)
            assertFalse("Le drapeau isQuantized doit être à false", restoredMyEmbedding.isQuantized)
        }
    }
}
