# MLScanX

📷 An Android library for **Text Recognition** and **Barcode Scanning** using ML Kit.  
Easily plug-and-play scanning features into your app with minimal setup.

---

## 🚀 Features

- ✅ Real-time **Text Detection**
- ✅ Fast and reliable **Barcode Scanning**
- ✅ Built using **CameraX** and **ML Kit**

---

## 📦 Gradle Setup

### 1. Add JitPack to your `settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}
```

### 2. Add the dependency
```kotlin
implementation("com.github.akshayc04:MLScanX:1.0.0")
```

# Usage
### Text Scanner
```kotlin

class MainActivity : AppCompatActivity(), TextScanResultListener {

    private lateinit var textScanner: TextScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewView = findViewById<PreviewView>(R.id.previewView)

        textScanner = TextScanner(this, previewView, this)
        textScanner.startCamera()
    }

    override fun onTextFound(text: String) {
        //TODO: Use the detected text here
    }

    override fun onDestroy() {
        textScanner.stop()
        super.onDestroy()
    }
}
```

### Barcode Scanner
```kotlin

class MainActivity : AppCompatActivity(), BarcodeScanResultListener {

    private lateinit var barcodeScanner: BarcodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewView = findViewById<PreviewView>(R.id.previewView)

        barcodeScanner = BarcodeScanner(this, previewView, this)
        barcodeScanner.startCamera()
    }

    override fun onBarcodeScanned(value: String) {
        //TODO: Use the detected value here
    }

    override fun onDestroy() {
        barcodeScanner.stop()
        super.onDestroy()
    }
}

```

## Permissions
Don’t forget to request CAMERA permission in your AndroidManifest.xml and check ask for runtime permission from user:
```xml
<uses-permission android:name="android.permission.CAMERA" />
```

# 📄 License
This project is licensed under the Apache 2.0 License – see the LICENSE file for details.
