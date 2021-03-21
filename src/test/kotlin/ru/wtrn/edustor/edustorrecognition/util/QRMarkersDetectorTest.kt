package ru.wtrn.edustor.edustorrecognition.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.jupiter.api.Test
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

internal class QRMarkersDetectorTest {

    private val outDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    @Test
    fun testQrMarkersDetection() {
        val detector = QRMarkersDetector()
        val image = javaClass.getResource("/test_page.png").readBytes().toBufferedImage()

        val loadedImageMat = detector.loadMat(image)
        val srcMat = loadedImageMat.srcMat.clone()
        val mat = loadedImageMat.mat.clone()
        File(outDirectory, "01_raw.png").writeBytes(srcMat.toPng())
        File(outDirectory, "02_preprocessed.png").writeBytes(mat.toPng())

        val qrMarkers = detector.findMarkers(loadedImageMat).qrMarkers

        val color = Scalar(0.0, 255.0, 0.0)
        qrMarkers.forEachIndexed { i, contour ->
            Imgproc.drawContours(srcMat, qrMarkers, i, color, 1)
        }
        File(outDirectory, "03_markers.png").writeBytes(srcMat.toPng())

        val qrArea = detector.findQrArea(image)
        Imgproc.drawContours(srcMat, listOf(qrArea.rect.toMatOfPoint()), 0, color, 1)
        File(outDirectory, "04_qr_area.png").writeBytes(srcMat.toPng())

        val zxingReader = QRCodeReader()
        val luminanceSource = BufferedImageLuminanceSource(qrArea.qrMat.toBufferedImage())
        val binarizer = HybridBinarizer(luminanceSource)
        val binaryBitmap = BinaryBitmap(binarizer)
        val result = zxingReader.decode(binaryBitmap)
    }
}