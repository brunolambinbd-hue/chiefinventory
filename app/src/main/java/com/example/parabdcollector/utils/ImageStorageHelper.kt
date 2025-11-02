package com.example.parabdcollector.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageStorageHelper {

    fun saveImageToInternalStorage(context: Context, uri: Uri): Uri? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "IMG_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use {
            inputStream.copyTo(it)
        }

        inputStream.close()
        return Uri.fromFile(file)
    }
}
