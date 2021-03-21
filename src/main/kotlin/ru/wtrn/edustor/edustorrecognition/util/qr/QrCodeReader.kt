package ru.wtrn.edustor.edustorrecognition.util.qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import java.awt.image.BufferedImage

class QrCodeReader {
    private val zxingReader = QRCodeReader()

    fun readBarcode(image: BufferedImage): String {
        val luminanceSource = BufferedImageLuminanceSource(image)
        val binarizer = HybridBinarizer(luminanceSource)
        val binaryBitmap = BinaryBitmap(binarizer)
        return zxingReader.decode(binaryBitmap).text
    }
}