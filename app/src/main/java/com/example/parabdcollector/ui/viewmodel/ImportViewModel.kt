package com.example.parabdcollector.ui.viewmodel

import android.app.Application
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.imagecomparison.EmbeddingUtils
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.model.ImportSession
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.repo.ImportRepository
import com.example.parabdcollector.utils.DescriptionParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

/**
 * ViewModel responsible for handling the data import process from a CSV file.
 *
 * This class orchestrates the parsing of the CSV, fetching existing items, calculating image signatures
 * for new or updated items, and finally inserting or updating the items in the database.
 */
class ImportViewModel(
    application: Application, 
    private val repository: CollectionRepository,
    private val importRepository: ImportRepository
) : AndroidViewModel(application) {

    /**
     * Observable list of all import sessions.
     */
    val allSessions = importRepository.allSessions

    /**
     * Deletes an import session from the history.
     */
    fun deleteSession(session: ImportSession) {
        viewModelScope.launch {
            importRepository.delete(session)
        }
    }

    private val imageEmbedderHelper = ImageEmbedderHelper(context = getApplication(), listener = null)
    private val imageLoader = ImageLoader(application)
    private val baseImageUrl = "https://frankpe.com/images/bdg_new/"

    /**
     * Imports collection items from a CSV file specified by its URI.
     */
    fun importCsv(uri: Uri, dispatcher: CoroutineDispatcher = Dispatchers.IO): Job {
        return viewModelScope.launch(dispatcher) {
            val fileName = getFileName(uri) ?: "import_csv"
            
            // 1. Create and start a new session
            var session = ImportSession(
                fileName = fileName,
                status = "IN_PROGRESS"
            )
            val sessionId = importRepository.insert(session)
            session = session.copy(id = sessionId)

            val itemsToInsert = mutableListOf<CollectionItem>()
            val itemsToUpdate = mutableListOf<CollectionItem>()

            var addedCount = 0
            var updatedCount = 0

            try {
                val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
                // Encodage Windows-1252 pour les fichiers Excel
                val reader = BufferedReader(InputStreamReader(inputStream, "Windows-1252"))
                val lines = reader.readLines()

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
                        
                        // --- LOGIQUE DE RECHERCHE D'IMAGE LOCALE ---
                        val internalFile = File(getApplication<Application>().getExternalFilesDir("import_images"), "frank$remoteId-1.jpg")
                        
                        // Log pour debug : on va voir dans Logcat si le fichier est détecté
                        Log.d("ImportViewModel", "Vérification image locale : ${internalFile.absolutePath} -> Existe : ${internalFile.exists()}")

                        val finalImageUri = if (internalFile.exists()) {
                            internalFile.toUri().toString()
                        } else {
                            imageUrl
                        }

                        var itemToSave = CollectionItem(
                            id = existingItem?.id ?: 0,
                            remoteId = remoteId,
                            titre = titre ?: "",
                            editeur = editeur,
                            annee = year,
                            mois = month,
                            categorie = category,
                            superCategorie = superCategorie,
                            tirage = parsedInfo.tirage,
                            dimensions = parsedInfo.dimensions,
                            description = description,
                            imageUri = finalImageUri, // Utilise soit l'URI locale, soit l'URL web
                            imageEmbedding = existingItem?.imageEmbedding,
                            isPossessed = false,
                            lastSessionId = sessionId
                        )

                        // Signature calculation
                        if (itemToSave.imageEmbedding == null && !itemToSave.imageUri.isNullOrBlank()) {
                            try {
                                val request = ImageRequest.Builder(getApplication())
                                    .data(itemToSave.imageUri!!.toUri())
                                    .allowHardware(false)
                                    .build()
                                val bitmap = (imageLoader.execute(request).drawable as? BitmapDrawable)?.bitmap

                                if (bitmap != null) {
                                    val signature = imageEmbedderHelper.computeSignature(bitmap)
                                    if (signature != null) {
                                        itemToSave = itemToSave.copy(imageEmbedding = EmbeddingUtils.embeddingToByteArray(signature))
                                        Log.i("ImportViewModel", "Item $remoteId - Signature calculée")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("ImportViewModel", "Item $remoteId - Erreur signature", e)
                            }
                        }

                        if (existingItem == null) {
                            itemsToInsert.add(itemToSave)
                            addedCount++
                        } else {
                            // On garde l'image existante si c'est une photo prise avec l'appareil (content://)
                            // Sinon, on met à jour avec la nouvelle URI (locale ou web)
                            val finalUri = if (existingItem.imageUri?.startsWith("content://") == true) {
                                existingItem.imageUri
                            } else {
                                finalImageUri
                            }
                            itemsToUpdate.add(itemToSave.copy(imageUri = finalUri))
                            updatedCount++
                        }
                        
                        // Log progress every 10 items
                        if ((addedCount + updatedCount) % 10 == 0) {
                            Log.d("ImportViewModel", "Progrès : ${addedCount + updatedCount} items traités...")
                        }

                        // Save in batches to avoid memory issues and keep progress
                        if (itemsToInsert.size + itemsToUpdate.size >= 20) {
                            repository.insertAll(itemsToInsert)
                            repository.updateAll(itemsToUpdate)
                            itemsToInsert.clear()
                            itemsToUpdate.clear()
                            importRepository.update(session.copy(itemsAdded = addedCount, itemsUpdated = updatedCount))
                        }
                    }
                }
                
                // Final save
                if (itemsToInsert.isNotEmpty()) repository.insertAll(itemsToInsert)
                if (itemsToUpdate.isNotEmpty()) repository.updateAll(itemsToUpdate)
                
                // 2. Mark session as successful
                importRepository.update(session.copy(
                    itemsAdded = addedCount,
                    itemsUpdated = updatedCount,
                    status = "SUCCESS"
                ))
                
            } catch (e: Exception) {
                Log.e("ImportViewModel", "Erreur globale pendant l'import", e)
                importRepository.update(session.copy(
                    itemsAdded = addedCount,
                    itemsUpdated = updatedCount,
                    status = "ERROR"
                ))
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = getApplication<Application>().contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) result = cursor.getString(index)
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path?.let { path ->
                val cut = path.lastIndexOf('/')
                if (cut != -1) path.substring(cut + 1) else path
            }
        }
        return result
    }

    private fun buildImageUrl(remoteId: Int): String {
        val folder = (remoteId / 100) * 100
        return "$baseImageUrl$folder/frank$remoteId-1.jpg"
    }

    override fun onCleared() {
        super.onCleared()
        imageEmbedderHelper.clearImageEmbedder()
    }
}
