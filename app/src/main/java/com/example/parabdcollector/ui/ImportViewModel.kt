package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.imagecomparison.EmbeddingUtils
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.DescriptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private lateinit var imageEmbedderHelper: ImageEmbedderHelper
    private val imageLoader = ImageLoader(application)
    private val baseImageUrl = "https://frankpe.com/images/bdg_new/" // URL Web correcte

    init {
        imageEmbedderHelper = ImageEmbedderHelper(context = getApplication(), listener = null)
    }

    fun importCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val lines = BufferedReader(InputStreamReader(inputStream)).readLines()

            for (line in lines.drop(1)) {
                val tokens = line.split(";")
                
                val remoteId = tokens.getOrNull(0)?.toIntOrNull()
                if (remoteId != null) {
                    val existingItem = repository.findByRemoteId(remoteId)

                    val year = tokens.getOrNull(1)?.toIntOrNull()
                    val month = tokens.getOrNull(2)?.toIntOrNull()
                    val category = tokens.getOrNull(3)?.trim()
                    val titre = tokens.getOrNull(4)?.trim()
                    val editeur = tokens.getOrNull(5)?.trim()
                    val description = tokens.getOrNull(6)?.trim() ?: ""
                    val superCategorie = tokens.getOrNull(10)?.trim()

                    val parsedInfo = DescriptionParser.parse(titre, description)
                    val imageUrl = buildImageUrl(remoteId)
                    Log.d("ImportViewModel", "Item $remoteId - Generated URL: $imageUrl") // LOG INCONDITIONNEL

                    var itemToSave = CollectionItem(
                        id = existingItem?.id ?: 0,
                        remoteId = remoteId,
                        titre = titre ?: "",
                        editeur = editeur,
                        annee = year,
                        mois = month,
                        categorie = category,
                        superCategorie = superCategorie,
                        materiau = null,
                        tirage = parsedInfo.tirage,
                        dimensions = parsedInfo.dimensions,
                        prixAchat = null,
                        valeurEstimee = null,
                        lieuAchat = null,
                        description = description,
                        imageUri = imageUrl,
                        imageEmbedding = existingItem?.imageEmbedding, // On préserve l'ancienne signature
                        locationId = null,
                        isPossessed = true
                    )

                    val imageUriString = itemToSave.imageUri // Crée une référence stable AVANT le if
                    // Calcul de la signature si elle est manquante
                    if (itemToSave.imageEmbedding == null && !imageUriString.isNullOrBlank()) {
                        try {
                            val request = ImageRequest.Builder(getApplication())
                                .data(imageUriString.toUri()) // Le smart cast est maintenant possible
                                .allowHardware(false) // Nécessaire pour le traitement bitmap
                                .build()
                            val bitmap = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap

                            Log.d("ImportViewModel", "Item $remoteId - Bitmap loading result: ${if (bitmap == null) "NULL" else "OK"}")

                            if (bitmap != null) {
                                val signature = imageEmbedderHelper.computeSignature(bitmap)
                                if (signature != null) {
                                    itemToSave = itemToSave.copy(imageEmbedding = EmbeddingUtils.embeddingToByteArray(signature))
                                    Log.i("ImportViewModel", "Item $remoteId - Signature calculée avec succès.")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ImportViewModel", "Item $remoteId - Erreur lors du calcul de la signature", e)
                        }
                    }

                    if (existingItem == null) {
                        repository.insert(itemToSave)
                    } else {
                        val finalUri = if (existingItem.imageUri?.startsWith("content://") == true) {
                            existingItem.imageUri
                        } else {
                            imageUrl
                        }
                        repository.update(itemToSave.copy(imageUri = finalUri))
                    }
                }
            }
        }
    }

    private fun buildImageUrl(remoteId: Int): String {
        val folder = (remoteId / 100) * 100
        val prefix = "frank"
        return "$baseImageUrl$folder/$prefix$remoteId-1.jpg"
    }

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
