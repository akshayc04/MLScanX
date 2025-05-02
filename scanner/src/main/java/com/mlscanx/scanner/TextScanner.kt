package com.mlscanx.scanner

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TextScanner(private val context: Context,
                  private val previewView: PreviewView,
                  private val resultListener: TextScanResultListner) {
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(cameraExecutor, TextAnalyzer(resultListener))

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    context as androidx.lifecycle.LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("TextScanner", "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        cameraExecutor.shutdown()
    }

    private class TextAnalyzer(
        private val listener: TextScanResultListner
    ) : ImageAnalysis.Analyzer {

        private var lastDetectedText: String = ""
        private var lastEmitTime = 0L
        private val cooldownMillis = 3000L // 3 seconds

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener { visionText ->
                    val newText = visionText.text.trim()
                    val now = System.currentTimeMillis()

                    if (newText.isNotBlank() &&
                        newText != lastDetectedText &&
                        (now - lastEmitTime >= cooldownMillis)
                    ) {
                        lastDetectedText = newText
                        lastEmitTime = now
                        listener.onTextFound(newText)
                    }
                }
                .addOnFailureListener {
                    Log.e("TextScanner", "Text recognition failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }


}