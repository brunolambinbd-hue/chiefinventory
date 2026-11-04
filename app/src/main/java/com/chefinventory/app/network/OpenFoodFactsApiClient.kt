package com.chefinventory.app.network

import com.chefinventory.app.model.OpenFoodFactsProduct
import com.parabd.lib.traceability.utils.LogManager
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Client réseau d'interrogation de l'API REST Open Food Facts v2 (Niveau 2).
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object OpenFoodFactsApiClient {

    private const val TAG = "OFFApiClient"
    internal var baseUrlProvider: (String) -> String = { "https://world.openfoodfacts.org/api/v2/product/$it.json" }

    /**
     * Parse une réponse JSON de l'API Open Food Facts v2 et construit un objet [OpenFoodFactsProduct].
     *
     * @param barcode Le code-barres EAN13 du produit.
     * @param jsonString La chaîne de réponse au format JSON.
     * @return L'objet [OpenFoodFactsProduct] si le produit est trouvé (`status` == 1), ou `null` sinon.
     */
    internal fun parseProductJson(barcode: String, jsonString: String): OpenFoodFactsProduct? {
        if (jsonString.isBlank() || barcode.isBlank()) return null
        return try {
            val root = JSONObject(jsonString)
            val status = root.optInt("status", 0)
            if (status != 1) {
                LogManager.d(TAG, "Produit non trouvé sur Open Food Facts pour le code-barres '$barcode' (status=$status)")
                return null
            }

            val product = root.optJSONObject("product") ?: return null

            val productName = product.optString("product_name_fr", "")
                .ifBlank { product.optString("product_name", "") }
                .trim()
                .ifBlank { null }

            val brand = product.optString("brands", "").trim().ifBlank { null }
            val quantityStr = product.optString("quantity", "").trim().ifBlank { null }
            val nutriScore = product.optString("nutriscore_grade", "").trim().uppercase().ifBlank { null }

            val imageUrl = product.optString("image_url", "")
                .ifBlank { product.optString("image_front_url", "") }
                .trim()
                .ifBlank { null }

            val ingredientsText = product.optString("ingredients_text_fr", "")
                .ifBlank { product.optString("ingredients_text", "") }
                .trim()
                .ifBlank { null }

            OpenFoodFactsProduct(
                barcode = barcode,
                productName = productName,
                brand = brand,
                quantityStr = quantityStr,
                nutriScore = nutriScore,
                imageUrl = imageUrl,
                ingredientsText = ingredientsText,
                timestampAdded = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            LogManager.e(TAG, "Erreur lors de l'analyse du JSON produit Open Food Facts", e)
            null
        }
    }

    /**
     * Interroge l'API REST v2 d'Open Food Facts pour récupérer les informations d'un produit par son code-barres [barcode].
     *
     * @param barcode Le code-barres EAN13 / UPC à rechercher.
     * @param timeoutMs Le délai d'expiration de la connexion HTTP en millisecondes.
     * @return L'objet [OpenFoodFactsProduct] si le produit existe et que le réseau est disponible, `null` sinon.
     */
    fun fetchProductByBarcode(barcode: String, timeoutMs: Int = 5000): OpenFoodFactsProduct? {
        if (barcode.isBlank()) return null
        var urlConnection: HttpURLConnection? = null
        return try {
            val url = URL(baseUrlProvider(barcode.trim()))
            urlConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = timeoutMs
                readTimeout = timeoutMs
                setRequestProperty("User-Agent", "ChefInventory-App/1.0")
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = urlConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = urlConnection.inputStream
                val jsonString = inputStream.readBytes().toString(Charsets.UTF_8)
                parseProductJson(barcode.trim(), jsonString)
            } else {
                LogManager.d(TAG, "API Open Food Facts a répondu avec le code HTTP $responseCode pour barcode '$barcode'")
                null
            }
        } catch (e: Exception) {
            LogManager.e(TAG, "Exception réseau lors de la recherche du produit '$barcode' sur Open Food Facts", e)
            null
        } finally {
            urlConnection?.disconnect()
        }
    }
}
