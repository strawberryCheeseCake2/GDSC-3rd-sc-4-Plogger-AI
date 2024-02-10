package com.example.garbagedetection

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GarbageDetector {

    /*
    * Identify the most prominent object in the image from a set of 7 garbage categories:
    * {0: 'cardboard', 1: 'glass', 2: 'metal', 3: 'non_garbage', 4: 'paper', 5: 'plastic', 6: 'trash'}
    * */

    companion object {

        private const val CARDBOARD = 0
        private const val GLASS = 1
        private const val METAL = 2
        private const val NON_GARBAGE = 3
        private const val PAPER = 4
        private const val PLASTIC = 5
        private const val TRASH = 6

        private fun getModel(): LocalModel {
            return LocalModel.Builder()
                .setAssetFilePath("model.tflite")
                .build()
        }

        private fun getLabeler(): ImageLabeler {
            val model = getModel()
            val customImageLabelerOptions = CustomImageLabelerOptions.Builder(model)
                .setConfidenceThreshold(0.5f)
                .setMaxResultCount(5)
                .build()

            val labeler = ImageLabeling.getClient(customImageLabelerOptions)

            return labeler
        }
        
        suspend fun processImage(bitmap: Bitmap): Int = suspendCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)

            val labeler = getLabeler()

            labeler.process(image)
                .addOnSuccessListener { labels ->
                    // Task completed successfully
                    val label = labels.first().index
                    continuation.resume(label)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
        
        suspend fun isGarbage(bitmap: Bitmap): Boolean {
            val result = processImage(bitmap)

            return result != NON_GARBAGE
        }


    }
}