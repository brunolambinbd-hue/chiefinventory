package com.example.parabdcollector.ui

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.imagecomparison.ImageEmbedderHelper
import com.example.parabdcollector.model.CollectionItem
import com.example.parabdcollector.repo.CollectionRepository
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EditItemViewModel(application: Application, private val repository: CollectionRepository) : AndroidViewModel(application) {

    private val _item = MediatorLiveData<CollectionItem>()
    val item: LiveData<CollectionItem> = _item

    private var currentItemSource: LiveData<CollectionItem>? = null

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> = _imageUri

    private val imageEmbedderHelper: ImageEmbedderHelper = ImageEmbedderHelper(
        context = application,
        listener = null
    )

    fun loadItem(id: Long) {
        currentItemSource?.let { _item.removeSource(it) }
        val newSource = repository.getById(id)
        _item.addSource(newSource) {
            _item.value = it
        }
        currentItemSource = newSource
    }

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    suspend fun calculateSignature(bitmap: Bitmap): ByteArray? {
        val signature = imageEmbedderHelper.computeSignature(bitmap)
        return signature?.let {
            val floatArray = it.floatEmbedding()
            val byteBuffer = ByteBuffer.allocate(floatArray.size * 4).order(ByteOrder.LITTLE_ENDIAN)
            floatArray.forEach { value -> byteBuffer.putFloat(value) }
            byteBuffer.array()
        }
    }

    fun insert(item: CollectionItem) = viewModelScope.launch {
        repository.insert(item)
    }

    fun update(item: CollectionItem) = viewModelScope.launch {
        repository.update(item)
    }
}
