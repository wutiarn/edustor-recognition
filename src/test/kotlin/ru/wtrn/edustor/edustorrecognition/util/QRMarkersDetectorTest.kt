package ru.wtrn.edustor.edustorrecognition.util

import com.google.zxing.BinaryBitmap
import com.google.zxing.Result
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opencv.core.Mat
import org.opencv.core.RotatedRect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import ru.wtrn.edustor.edustorrecognition.util.qr.QrDetectionFailedException
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.roundToInt

internal class QRMarkersDetectorTest {

    private val baseOutDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    @Test
    fun testNormalPage() {
        val detectionResult = testQrMarkersDetection("normal_page.png", "https://edustor.wtrn.ru/p/6eXLrkP5HKyJZjWDTQ1lyGBR7cMB")
        Assertions.assertEquals(detectionResult.angle.roundToInt(), 0)
        Assertions.assertEquals(3, detectionResult.detectedMarkers.potentialMarkers.size)
    }

    @Test
    fun testRotatedPage() {
        val detectionResult = testQrMarkersDetection("rotated_page.png", "https://edustor.wtrn.ru/p/6eXLrkP5HKyJZjWDTQ1lyGBR7cMB")
        Assertions.assertEquals(detectionResult.angle.roundToInt(), -90)
    }

    @Test
    fun testRotatedWithExtraContoursPage() {
        val detectionResult = testQrMarkersDetection("rotated_with_extra_contours.jpeg", "https://edustor.wtrn.ru/p/6eXLrkP5HKyJZjWDTQ1lyGBR7cMB")
        Assertions.assertEquals(detectionResult.angle.roundToInt(), -217)
        Assertions.assertTrue(detectionResult.detectedMarkers.potentialMarkers.size > 3)
    }

    @Test
    fun testQrCodeMarker() {
        val exception = Assertions.assertThrows(QrDetectionFailedException::class.java) {
            testQrMarkersDetection("qr_code_marker.jpeg", null)
        }
        Assertions.assertEquals("Cannot detect QR code: found 1 markers", exception.message)
    }

    fun testQrMarkersDetection(imageName: String, expectedPayload: String?): QRMarkersDetector.DetectionResult {
        val detector = QRMarkersDetector()
        val image = javaClass.getResource("/$imageName").readBytes().toBufferedImage()
        val outDirectory = File(baseOutDirectory, imageName.split(".").first()).also {
            it.mkdirs()
        }

        val loadedImageMat = detector.loadMat(image)
        val srcMat = loadedImageMat.srcMat.clone()
        val mat = loadedImageMat.mat.clone()
        File(outDirectory, "01_raw.png").writeBytes(srcMat.toPng())
        File(outDirectory, "02_preprocessed.png").writeBytes(mat.toPng())

        val markerDetectionResult = detector.detectMarkers(loadedImageMat)

        drawPotentialMarkers(srcMat.clone(), markerDetectionResult.potentialMarkers, outDirectory)

        drawMarkers(srcMat.clone(), markerDetectionResult.qrMarkers, outDirectory)

        val detectionResult = detector.detect(image)
        drawQrArea(srcMat.clone(), detectionResult.qrArea, outDirectory)

        File(outDirectory, "06_qr.png").writeBytes(detectionResult.qrMat.toPng())

        expectedPayload?.let {
            val qrPayload = readBarcode(detectionResult.qrMat.toBufferedImage())
            Assertions.assertEquals(expectedPayload, qrPayload)
        }

        return detectionResult
    }

    private fun drawPotentialMarkers(mat: Mat, list: List<QRMarkersDetector.PotentialMarker>, outDirectory: File) {
        val innerContours = list.map { it.contour.toMatOfPoint() }
        val parentContours = list.flatMap { it.parents }.map { it.contour.toMatOfPoint() }

        val innerColor = Scalar(0.0, 0.0, 255.0)
        val parentColor = Scalar(0.0, 255.0, 0.0)

        innerContours.forEachIndexed { i, _ ->
            Imgproc.drawContours(mat, innerContours, i, innerColor, 1)
        }
        parentContours.forEachIndexed { i, _ ->
            Imgproc.drawContours(mat, parentContours, i, parentColor, 1)
        }

        File(outDirectory, "03_potential_markers.png").writeBytes(mat.toPng())
    }

    private fun drawMarkers(mat: Mat, qrMarkers: List<RotatedRect>, outDirectory: File) {
        val color = Scalar(0.0, 255.0, 0.0)
        val markerListOfPoints = qrMarkers.map { it.toMatOfPoint() }
        markerListOfPoints.forEachIndexed { i, _ ->
            Imgproc.drawContours(mat, markerListOfPoints, i, color, 1)
        }
        File(outDirectory, "04_markers.png").writeBytes(mat.toPng())
    }

    private fun drawQrArea(mat: Mat, qrArea: RotatedRect, outDirectory: File) {
        val color = Scalar(0.0, 255.0, 0.0)
        Imgproc.drawContours(mat, listOf(qrArea.toMatOfPoint()), 0, color, 1)
        File(outDirectory, "05_qr_area.png").writeBytes(mat.toPng())
    }

    private fun readBarcode(image: BufferedImage): String {
        val zxingReader = QRCodeReader()
        val luminanceSource = BufferedImageLuminanceSource(image)
        val binarizer = HybridBinarizer(luminanceSource)
        val binaryBitmap = BinaryBitmap(binarizer)
        return zxingReader.decode(binaryBitmap).text
    }
}