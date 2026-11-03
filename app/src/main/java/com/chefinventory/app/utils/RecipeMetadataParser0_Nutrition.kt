package com.chefinventory.app.utils

import android.util.Log

/**
 * Sous-composant du Bloc 0 de l'Étape 2.5 : Extraction et normalisation des valeurs nutritionnelles pour [RecipeMetadataParser].
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object RecipeMetadataParser0_Nutrition {
    private const val TAG = "RecipeMetadataParser0_Nut"

    /**
     * Normalise les valeurs numériques nutritionnelles corrompues par l'OCR.
     *
     * @param rawValue La chaîne numérique brute extraite.
     * @return La valeur numérique nettoyée sous forme décimale (ex: "8.8", "9.7", "1.0").
     */
    internal fun normalizeNoisyNutritionalValue(rawValue: String): String {
        var clean = rawValue.trim().replace(" ", "")
        // Remplace les confusions de chiffres LO0 / L00 / I0 -> 1.0
        clean = clean.replace(Regex("(?i)^[lI]00?$"), "1.0")
            .replace(Regex("(?i)^[lI][oO]0$"), "1.0")
            .replace(Regex("(?i)^0,(\\d{2})$"), "0.$1")
            .replace(",", ".")

        // Si 3 chiffres (ex: "880" -> "8.8", "107" -> "10.7", "970" -> "9.7")
        if (clean.matches(Regex("^\\d{3}$"))) {
            val starts10 = clean.startsWith("10")
            val starts11 = clean.startsWith("11")
            val starts12 = clean.startsWith("12")
            val isTenElevenTwelve = starts10 || starts11 || starts12
            return if (isTenElevenTwelve) {
                "${clean.substring(0, 2)}.${clean.substring(2)}"
            } else {
                "${clean[0]}.${clean[1]}"
            }
        }

        // Si 2 chiffres isolés pour grammage (ex: "97" -> "9.7")
        if (clean.matches(Regex("^\\d{2}$"))) {
            val valInt = clean.toInt()
            val isInRange = valInt >= 15
            if (isInRange) {
                return "${clean[0]}.${clean[1]}"
            }
        }
        return clean
    }

    /**
     * Tente d'extraire le bloc de valeurs nutritionnelles de la ligne [workingLine].
     *
     * @return L'objet [MetadataResult] si la ligne contenait un bloc nutritionnel, ou null sinon.
     */
    internal fun processNutritionBlock(workingLine: String, lower: String, results: RawSections): MetadataResult? {
        val isNutritionHeader = OcrRegexes.Metadata.NUTRITION_HEADER.containsMatchIn(workingLine)
        val fatMatch = OcrRegexes.Metadata.NUTRITION_FAT.find(workingLine)
        val satFatMatch = OcrRegexes.Metadata.NUTRITION_SAT_FAT.find(workingLine)
        val carbsMatch = OcrRegexes.Metadata.NUTRITION_CARBS.find(workingLine)
        val sugarMatch = OcrRegexes.Metadata.NUTRITION_SUGAR.find(workingLine)
        val proteinMatch = OcrRegexes.Metadata.NUTRITION_PROTEIN.find(workingLine)
        val saltMatch = OcrRegexes.Metadata.NUTRITION_SALT.find(workingLine)
        val fiberMatch = OcrRegexes.Metadata.NUTRITION_FIBER.find(workingLine)

        val hasFatOrSat = if (fatMatch != null) true else satFatMatch != null
        val hasCarbsOrSugar = if (carbsMatch != null) true else sugarMatch != null
        val hasProt = proteinMatch != null
        val hasSalt = saltMatch != null
        val hasFiber = fiberMatch != null
        val hasOtherNut = if (hasProt) {
            true
        } else if (hasSalt) {
            true
        } else {
            hasFiber
        }

        val hasAnyNutMatch = if (isNutritionHeader) {
            true
        } else if (hasFatOrSat) {
            true
        } else if (hasCarbsOrSugar) {
            true
        } else {
            hasOtherNut
        }

        if (hasAnyNutMatch) {
            val kcalMatchInNut = OcrRegexes.Metadata.NUTRITION_KCAL.find(workingLine)
            if (kcalMatchInNut != null) {
                if (results.detectedKcal == null) {
                    results.detectedKcal = kcalMatchInNut.groupValues[1]
                } else {
                    Log.d(TAG, "  [INFO] Kcal déjà détecté")
                }
            }

            val nutParts = mutableListOf<String>()
            val hasKcal = results.detectedKcal != null
            val existingInfo = results.detectedNutritionInfo
            val hasKcalInInfo = if (existingInfo != null) existingInfo.contains("kcal") else false

            if (hasKcal && !hasKcalInInfo) {
                nutParts.add("${results.detectedKcal} kcal")
            }

            if (fatMatch != null) {
                val fatVal = normalizeNoisyNutritionalValue(fatMatch.groupValues[1])
                val satVal = if (satFatMatch != null) {
                    " (dont saturés: ${normalizeNoisyNutritionalValue(satFatMatch.groupValues[1])}g)"
                } else {
                    ""
                }
                nutParts.add("Lipides: ${fatVal}g$satVal")
            } else if (satFatMatch != null) {
                val satVal = normalizeNoisyNutritionalValue(satFatMatch.groupValues[1])
                val existing = results.detectedNutritionInfo
                val hasLipides = if (existing != null) existing.contains("Lipides:") else false
                val hasSat = if (existing != null) existing.contains("dont saturés") else false

                if (hasLipides) {
                    if (!hasSat) {
                        results.detectedNutritionInfo = existing!!.replace(Regex("Lipides:\\s*(\\d+(?:[.,]\\d+)?g)")) { m ->
                            "Lipides: ${m.groupValues[1]} (dont saturés: ${satVal}g)"
                        }
                    } else {
                        Log.d(TAG, "  [INFO] Saturés déjà présents")
                    }
                } else {
                    nutParts.add("Acides gras saturés: ${satVal}g")
                }
            }

            if (carbsMatch != null) {
                val carbVal = normalizeNoisyNutritionalValue(carbsMatch.groupValues[1])
                val sugarVal = if (sugarMatch != null) {
                    " (dont sucres: ${normalizeNoisyNutritionalValue(sugarMatch.groupValues[1])}g)"
                } else {
                    ""
                }
                nutParts.add("Glucides: ${carbVal}g$sugarVal")
            } else if (sugarMatch != null) {
                val sugVal = normalizeNoisyNutritionalValue(sugarMatch.groupValues[1])
                val existing = results.detectedNutritionInfo
                val hasCarbs = if (existing != null) existing.contains("Glucides:") else false
                val hasSugar = if (existing != null) existing.contains("dont sucres") else false

                if (hasCarbs) {
                    if (!hasSugar) {
                        results.detectedNutritionInfo = existing!!.replace(Regex("Glucides:\\s*(\\d+(?:[.,]\\d+)?g)")) { m ->
                            "Glucides: ${m.groupValues[1]} (dont sucres: ${sugVal}g)"
                        }
                    } else {
                        Log.d(TAG, "  [INFO] Sucres déjà présents")
                    }
                } else {
                    nutParts.add("Sucres: ${sugVal}g")
                }
            }

            if (proteinMatch != null) {
                nutParts.add("Protéines: ${normalizeNoisyNutritionalValue(proteinMatch.groupValues[1])}g")
            }
            if (fiberMatch != null) {
                nutParts.add("Fibres: ${normalizeNoisyNutritionalValue(fiberMatch.groupValues[1])}g")
            }
            if (saltMatch != null) {
                nutParts.add("Sel: ${normalizeNoisyNutritionalValue(saltMatch.groupValues[1])}g")
            }

            if (nutParts.isNotEmpty()) {
                val has100gNoSpace = lower.contains("100g")
                val has100gSpace = lower.contains("100 g")
                val has1000 = lower.contains("1000")
                val has100g = if (has100gNoSpace) {
                    true
                } else if (has100gSpace) {
                    true
                } else {
                    has1000
                }
                val refStr = if (has100g) "/100g" else ""
                val newSummary = nutParts.joinToString(", ")
                val existing = results.detectedNutritionInfo
                results.detectedNutritionInfo = if (existing == null) {
                    "Valeurs$refStr : $newSummary"
                } else {
                    "$existing, $newSummary"
                }
            }
            return MetadataResult(true, "")
        }
        return null
    }
}
