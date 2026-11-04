package com.chefinventory.app.network

import com.google.common.truth.Truth.assertThat
import com.parabd.lib.traceability.CoveredRequirement
import org.junit.After
import org.junit.Test
import java.net.InetAddress
import java.net.ServerSocket

/**
 * Suite de tests unitaires de Type B vérifiant le comportement et la résilience réseau de [OpenFoodFactsApiClient].
 */
class OpenFoodFactsApiClientTest {

    @After
    fun tearDown() {
        OpenFoodFactsApiClient.baseUrlProvider = { "https://world.openfoodfacts.org/api/v2/product/$it.json" }
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseProductJson_ValidProduct() {
        val json = """
            {
              "status": 1,
              "product": {
                "product_name_fr": "Nutella",
                "brands": "Ferrero",
                "quantity": "400 g",
                "nutriscore_grade": "e",
                "image_url": "https://images.openfoodfacts.org/nutella.jpg",
                "ingredients_text_fr": "Sucre, huile de palme, noisettes"
              }
            }
        """.trimIndent()

        val product = OpenFoodFactsApiClient.parseProductJson("3017620422003", json)

        assertThat(product).isNotNull()
        assertThat(product?.barcode).isEqualTo("3017620422003")
        assertThat(product?.productName).isEqualTo("Nutella")
        assertThat(product?.brand).isEqualTo("Ferrero")
        assertThat(product?.quantityStr).isEqualTo("400 g")
        assertThat(product?.nutriScore).isEqualTo("E")
        assertThat(product?.imageUrl).isEqualTo("https://images.openfoodfacts.org/nutella.jpg")
        assertThat(product?.ingredientsText).isEqualTo("Sucre, huile de palme, noisettes")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseProductJson_AllFieldFallbackBranches() {
        val jsonBlankFields = """
            {
              "status": 1,
              "product": {
                "product_name_fr": "",
                "product_name": "",
                "brands": "   ",
                "quantity": "",
                "nutriscore_grade": " ",
                "image_url": "",
                "image_front_url": "",
                "ingredients_text_fr": "",
                "ingredients_text": ""
              }
            }
        """.trimIndent()

        val prod = OpenFoodFactsApiClient.parseProductJson("123", jsonBlankFields)
        assertThat(prod).isNotNull()
        assertThat(prod?.productName).isNull()
        assertThat(prod?.brand).isNull()
        assertThat(prod?.quantityStr).isNull()
        assertThat(prod?.nutriScore).isNull()
        assertThat(prod?.imageUrl).isNull()
        assertThat(prod?.ingredientsText).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseProductJson_ProductNotFoundAndStatusZero() {
        val jsonNotFound = """{"status": 0, "status_verbose": "product not found"}"""
        val product = OpenFoodFactsApiClient.parseProductJson("0000000000000", jsonNotFound)

        assertThat(product).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseProductJson_EmptyAndBlankGuards() {
        assertThat(OpenFoodFactsApiClient.parseProductJson("", "{}")).isNull()
        assertThat(OpenFoodFactsApiClient.parseProductJson("123", "")).isNull()
        assertThat(OpenFoodFactsApiClient.parseProductJson("123", "   ")).isNull()
        assertThat(OpenFoodFactsApiClient.parseProductJson("123", "{ malformed json }")).isNull()
        assertThat(OpenFoodFactsApiClient.parseProductJson("123", """{"status": 1}""")).isNull()

        // Test default lambda baseUrlProvider execution (L16)
        val defaultUrl = OpenFoodFactsApiClient.baseUrlProvider("123456")
        assertThat(defaultUrl).isEqualTo("https://world.openfoodfacts.org/api/v2/product/123456.json")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testParseProductJson_FallbackToGenericProductNameAndFrontImageUrl() {
        val json = """
            {
              "status": 1,
              "product": {
                "product_name": "Lait demi-écrémé",
                "image_front_url": "https://images.openfoodfacts.org/lait.jpg",
                "ingredients_text": "Lait de vache"
              }
            }
        """.trimIndent()

        val product = OpenFoodFactsApiClient.parseProductJson("3228857000166", json)

        assertThat(product).isNotNull()
        assertThat(product?.productName).isEqualTo("Lait demi-écrémé")
        assertThat(product?.imageUrl).isEqualTo("https://images.openfoodfacts.org/lait.jpg")
        assertThat(product?.ingredientsText).isEqualTo("Lait de vache")
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFetchProductByBarcode_InvalidBarcodeOrOffline_ReturnsNull() {
        assertThat(OpenFoodFactsApiClient.fetchProductByBarcode("")).isNull()
        assertThat(OpenFoodFactsApiClient.fetchProductByBarcode("   ")).isNull()

        // Point to non-connectable local port
        OpenFoodFactsApiClient.baseUrlProvider = { "http://127.0.0.1:1/api/v2/product/$it.json" }
        val product = OpenFoodFactsApiClient.fetchProductByBarcode("0000000000000", timeoutMs = 100)
        assertThat(product).isNull()
    }

    @Test
    @CoveredRequirement("6.1.1.9")
    fun testFetchProductByBarcode_MalformedUrlException_NullUrlConnectionDisconnectCoverage() {
        OpenFoodFactsApiClient.baseUrlProvider = { "invalid_protocol_url_string" }
        val product = OpenFoodFactsApiClient.fetchProductByBarcode("123456")
        assertThat(product).isNull()
    }

    @Test
    @CoveredRequirement("6.3.2.9")
    fun testFetchProductByBarcode_LocalServerSocket_Http200And404() {
        val serverSocket = ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"))
        val port = serverSocket.localPort

        val thread = Thread {
            try {
                while (!serverSocket.isClosed) {
                    val socket = try { serverSocket.accept() } catch (_: Exception) { break }
                    val reader = socket.getInputStream().bufferedReader()
                    val requestLine = reader.readLine() ?: ""

                    val responseBody = if (requestLine.contains("404")) {
                        "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                    } else {
                        val json = """{"status": 1, "product": {"product_name": "Biscuits"}}"""
                        "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nContent-Length: ${json.toByteArray().size}\r\nConnection: close\r\n\r\n$json"
                    }

                    socket.getOutputStream().write(responseBody.toByteArray())
                    socket.getOutputStream().flush()
                    socket.close()
                }
            } catch (_: Exception) {}
        }
        thread.start()

        try {
            OpenFoodFactsApiClient.baseUrlProvider = { "http://127.0.0.1:$port/api/v2/product/$it.json" }

            val prod200 = OpenFoodFactsApiClient.fetchProductByBarcode("123456", timeoutMs = 1000)
            assertThat(prod200?.productName).isEqualTo("Biscuits")

            val prod404 = OpenFoodFactsApiClient.fetchProductByBarcode("404", timeoutMs = 1000)
            assertThat(prod404).isNull()
        } finally {
            try { serverSocket.close() } catch (_: Exception) {}
            try { thread.join(1000) } catch (_: Exception) {}
        }
    }
}
