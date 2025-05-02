package com.akshay.mlscanx

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mlscanx.scanner.TextScanResultListner
import com.mlscanx.scanner.TextScanner
import android.Manifest
import android.widget.Button
import com.mlscanx.scanner.BarcodeScanResultListner
import com.mlscanx.scanner.BarcodeScanner

class MainActivity : AppCompatActivity(), TextScanResultListner, BarcodeScanResultListner {

    private lateinit var textButton: Button
    private lateinit var barcodeButton: Button
    private lateinit var previewView: PreviewView;
    private lateinit var textScanner: TextScanner
    private lateinit var barcodeScanner: BarcodeScanner
    private val CAMERA_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        textButton = findViewById(R.id.button)
        barcodeButton = findViewById(R.id.button2)
        previewView = findViewById(R.id.previewView)

        textButton.setOnClickListener {
            checkCameraPermission(true)
        }

        barcodeButton.setOnClickListener {
            checkCameraPermission(false)
        }

    }

    private fun checkCameraPermission(value: Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            if(value) {
                startScanner()
            }else{
                startBarcodeScanner()
            }
        }
    }

    private fun startBarcodeScanner(){
        barcodeScanner = BarcodeScanner(this, previewView, this)
        barcodeScanner.startCamera()
    }

    private fun startScanner() {
        textScanner = TextScanner(this,previewView,this)
        textScanner.startCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTextFound(text: String) {
        Log.d("MainActivity", "Detected text: $text")
        runOnUiThread {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        textScanner.stop()
        barcodeScanner.stop()
        super.onDestroy()
    }

    override fun onBarcodeScanned(value: String) {
        Log.d("MainActivity", "Barcode text: $value")
        runOnUiThread {
            Toast.makeText(this, "Scanned: $value", Toast.LENGTH_SHORT).show()
        }
    }
}