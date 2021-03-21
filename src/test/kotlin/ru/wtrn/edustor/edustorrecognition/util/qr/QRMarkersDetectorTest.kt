package ru.wtrn.edustor.edustorrecognition.util.qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import ru.wtrn.edustor.edustorrecognition.util.MetaFieldsExtractor
import ru.wtrn.edustor.edustorrecognition.util.toBufferedImage
import ru.wtrn.edustor.edustorrecognition.util.toMatOfPoint
import ru.wtrn.edustor.edustorrecognition.util.toPng
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

internal class QRMarkersDetectorTest {

    private val baseOutDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    @Test
    fun testNormalPage() {
        val detectionResult = testQrMarkersDetection("normal_page.png", "https://edustor.wtrn.ru/p/PUNOrbAi9Kdi919XEGxcCGBXnb0B")
        Assertions.assertTrue(detectionResult.angle.roundToInt().absoluteValue < 5)
    }

    @Test
    fun testRotatedPage() {
        val detectionResult = testQrMarkersDetection("rotated_page.png", "https://edustor.wtrn.ru/p/PUNOrbAi9Kdi919XEGxcCGBXnb0B")
        Assertions.assertTrue(detectionResult.angle.roundToInt().absoluteValue > 175)
    }

    @Test
    fun testHorizontalPage() {
        testQrMarkersDetection("horizontal_page.png", "https://edustor.wtrn.ru/p/PUNOrbAi9Kdi919XEGxcCGBXnb0B")
    }

    @Test
    fun testWithExtraContoursPage() {
        val detectionResult = testQrMarkersDetection("test_with_extra_contours.png", "https://edustor.wtrn.ru/p/PUNOrbAi9Kdi919XEGxcCGBXnb0B")
        Assertions.assertTrue(detectionResult.detectedMarkers.potentialMarkers.size > 10)
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
        File(outDirectory, "07_rotated_image.png").writeBytes(detectionResult.rotatedImageMat.toPng())

        expectedPayload?.let {
            val qrCodeReader = QrCodeReader()
            val qrPayload = qrCodeReader.readBarcode(detectionResult.qrMat.toBufferedImage())
            Assertions.assertEquals(expectedPayload, qrPayload)
        }

        val metaFieldsArea = MetaFieldsExtractor.getArea(detectionResult.rotatedQrAndMetaMarkersArea)
        drawMetaFieldsArea(detectionResult.rotatedImageMat.clone(), metaFieldsArea, outDirectory)
        return detectionResult
    }

    private fun drawPotentialMarkers(mat: Mat, list: List<QRMarkersDetector.PotentialMarker>, outDirectory: File) {
        val innerColor = Scalar(0.0, 255.0, 255.0)
        list.forEach { marker ->
            val parentColor = when {
                marker.rejectionReason == null ->  Scalar(0.0, 255.0, 0.0)
                else -> Scalar(0.0, 0.0, 255.0)
            }
            Imgproc.drawContours(mat, listOf(marker.contour.toMatOfPoint()), 0, innerColor, 1)
            marker.parentContour?.let {
                    Imgproc.drawContours(mat, listOf(it.toMatOfPoint()), 0, parentColor, 1)
            }
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

    private fun drawMetaFieldsArea(mat: Mat, metaFieldsArea: Rect, outDirectory: File) {
        val color = Scalar(0.0, 255.0, 0.0)

        Imgproc.rectangle(mat, metaFieldsArea, color, 1)
        File(outDirectory, "08_meta_fields.png").writeBytes(mat.toPng())
    }
}