package com.mlscanx.scanner

interface BarcodeScanResultListner {
    fun onBarcodeScanned(value: String)
}