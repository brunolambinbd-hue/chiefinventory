/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.imagecomparison // Changed package name

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.Embedding
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder.ImageEmbedderOptions

class ImageEmbedderHelper(
    private val context: Context,
    var currentDelegate: Int,
    var currentModel: Int,
    var listener: EmbedderListener? = null
) {
    // Secondary constructor that provides default values.
    constructor(context: Context, listener: EmbedderListener?) : this(
        context,
        DELEGATE_CPU,
        MODEL_MOBILENETV3_LARGE,
        listener
    )

    private var imageEmbedder: ImageEmbedder? = null

    init {
        setupImageEmbedder()
    }

    fun setupImageEmbedder() {
        Log.i(TAG, "setupImageEmbedder() called") // LOG AJOUTÉ
        val modelName = when (currentModel) {
            MODEL_MOBILENETV3_LARGE -> "mobilenet_v3_large.tflite"
            MODEL_MOBILENETV3_SMALL -> "mobilenet_v3_small.tflite"
            else -> "mobilenet_v3_large.tflite"
        }

        Log.i(TAG, "Attempting to load model: $modelName")

        val baseOptionsBuilder = BaseOptions.builder().setModelAssetPath(modelName)

        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                baseOptionsBuilder.setDelegate(Delegate.GPU)
            }
        }

        val options = ImageEmbedderOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .setL2Normalize(true)
            .setQuantize(false)
            .setRunningMode(RunningMode.IMAGE)
            .build()

        Log.d(TAG, "Using ImageEmbedderOptions: ${'$'}options") // LOG AJOUTÉ
        try {
            imageEmbedder = ImageEmbedder.createFromOptions(context, options)
            Log.i(TAG, "ImageEmbedder created successfully.")
        } catch (e: Exception) {
            val errorMsg = "Image embedder failed to load. See error logs for details"
            val errorCode = if (currentDelegate == DELEGATE_GPU) GPU_ERROR else UNKNOWN_ERROR
            listener?.onError(errorMsg, errorCode)
            Log.e(TAG, "TFLite failed to load model with error: " + e.message, e)
        }
    }

    fun computeSignature(bitmap: Bitmap): Embedding? {
        imageEmbedder?.let {
            val mpImage = BitmapImageBuilder(bitmap).build()
            return it.embed(mpImage).embeddingResult().embeddings().first()
        }
        Log.e(TAG, "computeSignature called, but imageEmbedder is null.")
        return null
    }

    @Suppress("unused")
    fun compareBitmapWithSignature(bitmapToCompare: Bitmap, savedSignature: Embedding): Double? {
        val newSignature = computeSignatureForComparison(bitmapToCompare)
        newSignature?.let {
            return ImageEmbedder.cosineSimilarity(it, savedSignature)
        }        
        return null
    }
    
    @Suppress("unused")
     fun computeSignatureForComparison(bitmap: Bitmap): Embedding? {
        imageEmbedder?.let {
            val mpImage = BitmapImageBuilder(bitmap).build()
            return it.embed(mpImage).embeddingResult().embeddings().first()
        }
        return null
    }

    @Suppress("unused")
    fun embed(firstBitmap: Bitmap, secondBitmap: Bitmap): ResultBundle? {
        // Inference time is the difference between the system time at the start and finish of the
        // process
        val startTime = SystemClock.uptimeMillis()

        val firstMpImage = BitmapImageBuilder(firstBitmap).build()
        val secondMpImage = BitmapImageBuilder(secondBitmap).build()
        imageEmbedder?.let {
            val firstEmbed =
                it.embed(firstMpImage).embeddingResult().embeddings().first()
            val secondEmbed =
                it.embed(secondMpImage).embeddingResult().embeddings().first()
            val inferenceTimeMs = SystemClock.uptimeMillis() - startTime
            return ResultBundle(
                ImageEmbedder.cosineSimilarity(firstEmbed, secondEmbed),
                inferenceTimeMs
            )
        }
        return null
    }

    fun clearImageEmbedder() {
        imageEmbedder?.close()
        imageEmbedder = null
    }

    data class ResultBundle(
        val similarity: Double,
        val inferenceTime: Long,
    )

    interface EmbedderListener {
        fun onError(error: String, errorCode: Int = UNKNOWN_ERROR)
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val MODEL_MOBILENETV3_LARGE = 0
        const val MODEL_MOBILENETV3_SMALL = 1

        const val UNKNOWN_ERROR = 0
        const val GPU_ERROR = 1
        private const val TAG = "ImageEmbedderHelper"
    }
}
