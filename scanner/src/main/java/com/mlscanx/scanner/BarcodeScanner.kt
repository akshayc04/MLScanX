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
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeScanner(
    private val context: Context,
    private val previewView: PreviewView,
    private val resultListener: BarcodeScanResultListner
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var lastScannedCode = ""
    private var lastScanTime = 0L
    private val cooldownMillis = 3000L

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analyzer.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                BarcodeAnalyzer()
            )

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                context as androidx.lifecycle.LifecycleOwner,
                cameraSelector,
                preview,
                analyzer
            )

        }, ContextCompat.getMainExecutor(context))
    }

    fun stop() {
        cameraProvider?.unbindAll()
    }

    private inner class BarcodeAnalyzer : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeOptInUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image ?: run {
                imageProxy.close()
                return
            }

            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            BarcodeScanning.getClient()
                .process(image)
                .addOnSuccessListener { barcodes ->
                    val now = System.currentTimeMillis()
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        if (!rawValue.isNullOrBlank() &&
                            rawValue != lastScannedCode &&
                            now - lastScanTime >= cooldownMillis
                        ) {
                            lastScannedCode = rawValue
                            lastScanTime = now
                            resultListener.onBarcodeScanned(rawValue)
                            break
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("BarcodeScanner", "Failed to scan barcode", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }


}