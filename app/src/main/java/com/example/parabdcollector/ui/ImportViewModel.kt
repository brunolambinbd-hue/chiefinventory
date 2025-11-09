package com.example.parabdcollector.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.DescriptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val baseImageUrl = "https://frankpe.com/images/bdg_new/"

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

                    val item = CollectionItem(
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
                        localisation = null,
                        isPossessed = true
                    )

                    if (existingItem == null) {
                        repository.insert(item)
                    } else {
                        repository.update(item.copy(imageUri = existingItem.imageUri ?: imageUrl))
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
}