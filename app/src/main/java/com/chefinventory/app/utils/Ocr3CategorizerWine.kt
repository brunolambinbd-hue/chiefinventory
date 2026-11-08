package com.chefinventory.app.utils

import android.content.res.Resources
import android.util.Log
import com.chefinventory.app.R

/**
 * Moteur de l'Étape 3 (Catégorisation) dédié à la détection, au traitement et à l'extraction des recommandations de vins.
 *
 * Responsabilités :
 * - Détecter si une ligne correspond à une recommandation d'accord met-vin (`isWineLine`).
 * - Valider les appellations, châteaux et producteurs de vins contre les dictionnaires métier XML (`wine_arrays.xml`).
 * - Exclure rigoureusement les ingrédients culinaires (ex: "vinaigre de vin", "huile") pour éviter les fausses détections.
 * - Gérer l'état mutable de la section Vin ([WineState]) et la machine à états de capture.
 */
@Suppress("FoldInitializerAndIfNull", "kotlin:S6529")
object Ocr3CategorizerWine {
    private const val TAG = "RecipeOCR"

    /**
     * Conteneur des listes de ressources de vins (appellations, producteurs, mots-clés, en-têtes et motif de suppression).
     */
    data class WineResources(
        val appellations: List<String>,
        val producers: List<String>,
        val keywords: List<String>,
        val titleKeywords: List<String>,
        val removePattern: String,
        val genericWineWords: List<String> = emptyList()
    )

    /**
     * Charge les ressources de vins à partir des [Resources] Android (`wine_arrays.xml`).
     */
    fun loadResources(res: Resources): WineResources {
        return WineResources(
            appellations = try { res.getStringArray(R.array.wine_appellations).toList() } catch (_: Exception) { emptyList() },
            producers = try { res.getStringArray(R.array.wine_producers).toList() } catch (_: Exception) { emptyList() },
            keywords = try { res.getStringArray(R.array.wine_keywords).toList() } catch (_: Exception) { emptyList() },
            titleKeywords = try { res.getStringArray(R.array.wine_title_keywords).toList() } catch (_: Exception) { emptyList() },
            removePattern = try { res.getString(R.string.wine_remove_pattern) } catch (_: Exception) { "" },
            genericWineWords = try { res.getStringArray(R.array.generic_wine_words).toList() } catch (_: Exception) { emptyList() }
        )
    }

    /**
     * Encapsule l'état mutable de la section Vin au cours du balayage de la recette.
     */
    class WineState {
        var isWineSectionClosed: Boolean = false
        var isWineHeaderActive: Boolean = false
        val detectedWineList: MutableList<String> = mutableListOf()
    }

    /**
     * Détermine si la ligne [line] correspond à une recommandation de vin en utilisant [vocabulary].
     */
    fun isWineLine(line: String, vocabulary: OcrVocabulary): Boolean {
        return isWineLine(
            line,
            WineResources(
                appellations = vocabulary.wineAppellations,
                producers = vocabulary.wineProducers,
                keywords = vocabulary.wineKeywords,
                titleKeywords = vocabulary.wineTitleKeywords,
                removePattern = vocabulary.wineRemovePattern,
                genericWineWords = vocabulary.genericWineWords
            )
        )
    }

    /**
     * Détermine si la ligne [line] correspond à une recommandation de vin en utilisant [resources].
     */
    fun isWineLine(line: String, resources: WineResources): Boolean {
        val lower = line.lowercase().trim()
        val genericWords = resources.genericWineWords.ifEmpty { listOf("vin", "vin blanc", "vin rouge", "du vin blanc", "du vin rouge") }.map { it.lowercase().trim() }.toSet()
        if (lower in genericWords) return false

        // 1. Exclusion si contient un ingrédient culinaire ou une unité culinaire
        if (OcrRegexes.Wines.CULINARY_EXCLUSIONS.containsMatchIn(line)) return false
        if (OcrRegexes.Wines.CULINARY_UNITS.containsMatchIn(line)) return false

        // 2. Règle forte : Année + Volume (ex : 2015 75cl)
        if (OcrRegexes.Wines.YEAR.containsMatchIn(line) && OcrRegexes.Wines.VOLUME.containsMatchIn(line)) return true

        // 3. Règle spécifique : Format bouteille standard (75 cl) sans signal culinaire
        if (OcrRegexes.Wines.STANDARD_BOTTLE.containsMatchIn(line)) return true

        // 4. Match contre les listes de ressources
        val matchesResource = if (resources.appellations.any { containsWholeWord(line, it) }) {
            true
        } else if (resources.producers.any { containsWholeWord(line, it) }) {
            true
        } else if (resources.keywords.any { containsWholeWord(line, it) }) {
            true
        } else {
            resources.titleKeywords.any { containsWholeWord(line, it) }
        }

        if (matchesResource) return true

        // 5. Millésime seul (si la ligne est courte et contient une année réaliste)
        if (line.length < 15 && OcrRegexes.Wines.YEAR.matches(line.trim())) return true

        return false
    }

    /**
     * Vérifie la présence d'un mot entier dans une chaîne de texte (délimiteur Unicode aware).
     */
    fun containsWholeWord(text: String, word: String): Boolean {
        if (text.isBlank() || word.isBlank()) return false
        val pattern = OcrRegexes.getWholeWordPattern(word)
        return pattern.containsMatchIn(text)
    }

    /**
     * Évalue si la ligne courante constitue une recommandation de vin et la traite dans l'objet [wineState].
     */
    fun processWineLine(
        workingLine: String,
        currentSection: Int,
        isIngredientListClosed: Boolean,
        containsAction: Boolean,
        startsWithConnector: Boolean,
        vocabulary: OcrVocabulary,
        wineState: WineState
    ): Boolean {
        if (containsAction || startsWithConnector) return false

        val lower = workingLine.lowercase().trim()
        val isExplicitWineHeader = vocabulary.ocr3ExplicitWineHeaders.any { keyword ->
            lower.contains(keyword.lowercase())
        }

        if (isExplicitWineHeader) {
            wineState.isWineHeaderActive = true
        } else {
            // Aucun en-tête de vin explicite détecté sur cette ligne
        }

        if (wineState.isWineHeaderActive) {
            if (!wineState.isWineSectionClosed) {
                val cleanedWine = Ocr3CategorizerWineCleaner.cleanWineLine(workingLine, vocabulary)
                if (cleanedWine.isNotBlank()) {
                    val isAlreadyAdded = wineState.detectedWineList.any { it.equals(cleanedWine, ignoreCase = true) }
                    if (!isAlreadyAdded) {
                        wineState.detectedWineList.add(cleanedWine)
                    } else {
                        if (vocabulary.logOcr3WineDuplicateIgnored.isNotBlank()) {
                            Log.d(TAG, vocabulary.logOcr3WineDuplicateIgnored)
                        }
                    }
                } else {
                    if (vocabulary.logOcr3WineEmptyIgnored.isNotBlank()) {
                        Log.d(TAG, vocabulary.logOcr3WineEmptyIgnored)
                    }
                }
                val hasVolume = Ocr3CategorizerWineCleaner.hasWineVolume(workingLine)
                val hasVintageYear = OcrRegexes.Wines.YEAR.containsMatchIn(workingLine)
                val hasKnownBrand = vocabulary.knownSourceBrands.any { brand -> brand.isNotBlank() && lower.contains(brand.lowercase()) }

                if (hasVolume || hasVintageYear || hasKnownBrand) {
                    wineState.isWineSectionClosed = true
                    wineState.isWineHeaderActive = false
                    if (vocabulary.logOcr3WineSectionClosed.isNotBlank()) {
                        Log.d(TAG, vocabulary.logOcr3WineSectionClosed)
                    }
                } else {
                    if (vocabulary.logOcr3WineSectionActive.isNotBlank()) {
                        Log.d(TAG, vocabulary.logOcr3WineSectionActive)
                    }
                }
            } else {
                if (vocabulary.logOcr3WineAlreadyClosedIgnored.isNotBlank()) {
                    Log.d(TAG, vocabulary.logOcr3WineAlreadyClosedIgnored)
                }
            }
            return true
        }

        val isNotInIngredientsSection = currentSection != Ocr3Categorizer.SECTION_INGREDIENTS
        val hasExplicitVolume = Ocr3CategorizerWineCleaner.hasWineVolume(workingLine)
        val allowWineDetection = isNotInIngredientsSection || isIngredientListClosed || hasExplicitVolume

        if (allowWineDetection && isWineLine(workingLine, vocabulary)) {
            if (!wineState.isWineSectionClosed) {
                val cleanedWine = Ocr3CategorizerWineCleaner.cleanWineLine(workingLine, vocabulary)
                if (cleanedWine.isNotBlank()) {
                    val isAlreadyAdded = wineState.detectedWineList.any { it.equals(cleanedWine, ignoreCase = true) }
                    if (!isAlreadyAdded) {
                        wineState.detectedWineList.add(cleanedWine)
                        if (Ocr3CategorizerWineCleaner.hasWineVolume(workingLine)) {
                            wineState.isWineSectionClosed = true
                            if (vocabulary.logOcr3WineImplicitVolumeClosed.isNotBlank()) {
                                Log.d(TAG, vocabulary.logOcr3WineImplicitVolumeClosed)
                            }
                        } else {
                            if (vocabulary.logOcr3WineImplicitNoVolumeOpen.isNotBlank()) {
                                Log.d(TAG, vocabulary.logOcr3WineImplicitNoVolumeOpen)
                            }
                        }
                    } else {
                        if (vocabulary.logOcr3WineImplicitDuplicateIgnored.isNotBlank()) {
                            Log.d(TAG, vocabulary.logOcr3WineImplicitDuplicateIgnored)
                        }
                    }
                } else {
                    if (vocabulary.logOcr3WineImplicitEmptyIgnored.isNotBlank()) {
                        Log.d(TAG, vocabulary.logOcr3WineImplicitEmptyIgnored)
                    }
                }
            }
            return true
        } else {
            return false
        }
    }
}
