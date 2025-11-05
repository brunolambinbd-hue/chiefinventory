package com.example.parabdcollector.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import com.example.parabdcollector.utils.CategoryMapper
import com.example.parabdcollector.utils.DescriptionParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class ImportViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    fun importCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.use {
                // On saute la première ligne (l'en-tête)
                var line = it.readLine()
                line = it.readLine()

                while (line != null) {
                    val tokens = line.split(";")
                    
                    val remoteId = tokens[0].toIntOrNull()
                    if (remoteId != null) {
                        val existingItem = repository.findByRemoteId(remoteId)

                        val description = tokens.getOrNull(10) ?: ""
                        val parsedInfo = DescriptionParser.parse(description)

                        val category = tokens.getOrNull(5)
                        val superCategory = category?.let { CategoryMapper.getSuperCategory(it) }

                        val item = CollectionItem(
                            id = existingItem?.id ?: 0,
                            remoteId = remoteId,
                            titre = tokens.getOrNull(1) ?: "",
                            univers = tokens.getOrNull(2),
                            editeur = tokens.getOrNull(3),
                            annee = tokens.getOrNull(4)?.toIntOrNull(),
                            categorie = category,
                            superCategorie = superCategory,
                            materiau = tokens.getOrNull(6),
                            tirage = parsedInfo.tirage,
                            dimensions = parsedInfo.dimensions,
                            prixAchat = tokens.getOrNull(7)?.toDoubleOrNull(),
                            valeurEstimee = tokens.getOrNull(8)?.toDoubleOrNull(),
                            lieuAchat = tokens.getOrNull(9),
                            notes = description, // On garde la description complète dans les notes
                            imageUri = null, // L'URI de l'image sera géré plus tard
                            localisation = tokens.getOrNull(11),
                            isPossessed = true
                        )

                        if (existingItem == null) {
                            repository.insert(item)
                        } else {
                            repository.update(item)
                        }
                    }
                    line = it.readLine()
                }
            }
        }
    }
}