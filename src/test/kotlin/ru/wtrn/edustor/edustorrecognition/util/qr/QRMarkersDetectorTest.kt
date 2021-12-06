package ru.wtrn.edustor.edustorrecognition.util.qr

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import ru.wtrn.edustor.edustorrecognition.util.*
import java.awt.image.BufferedImage
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

internal class QRMarkersDetectorTest {

    companion object {
        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }
    }

    private val baseOutDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    private val expectedPayload = "https://edustor.wtrn.ru/p/917128303644119040"

    @Test
    fun testDigitalPage() {
        val image = javaClass.getResource("/digital_page.png").readBytes().toBufferedImage()
        val detectionResult = testQrMarkersDetection(image, "digital_page", "https://edustor.wtrn.ru/p/917440436001095680")
        Assertions.assertTrue(detectionResult.angle.roundToInt().absoluteValue < 5)
    }

    @Test
    fun testNormalPage() {
        val image = javaClass.getResource("/test_page.png").readBytes().toBufferedImage()
        val detectionResult = testQrMarkersDetection(image, "normal_page", expectedPayload)
        Assertions.assertTrue(detectionResult.angle.roundToInt().absoluteValue < 5)
    }
    @Test
    fun testRotatedPage() {
        val image = javaClass.getResource("/rotated_page.png").readBytes().toBufferedImage()
        val detectionResult = testQrMarkersDetection(image, "rotated_page", expectedPayload)
        Assertions.assertEquals(8, detectionResult.angle.toInt())
    }

    @Test
    fun testUpsideDownPage() {
        val mat = javaClass.getResource("/rotated_page.png").readBytes().toImageMat()
        val targetMat = Mat()
        Core.rotate(mat, targetMat, Core.ROTATE_180)
        val detectionResult = testQrMarkersDetection(targetMat.toBufferedImage(), "upside_down_page", expectedPayload)
        Assertions.assertEquals(-171, detectionResult.angle.toInt())
    }

    @Test
    fun testHorizontalPage() {
        val mat = javaClass.getResource("/test_page.png").readBytes().toImageMat()
        val targetMat = Mat()
        Core.rotate(mat, targetMat, Core.ROTATE_90_CLOCKWISE)
        testQrMarkersDetection(targetMat.toBufferedImage(),"horizontal_page", expectedPayload)
    }

    @Test
    fun testWithExtraContoursPage() {
        val image = javaClass.getResource("/test_with_extra_contours.png").readBytes().toBufferedImage()
        val detectionResult = testQrMarkersDetection(image,"test_with_extra_contours", "https://edustor.wtrn.ru/p/PUNOrbAi9Kdi919XEGxcCGBXnb0B")
        Assertions.assertTrue(detectionResult.detectedMarkers.potentialMarkers.size > 10)
    }

    private fun testQrMarkersDetection(image: BufferedImage, imageName: String, expectedPayload: String?): QRMarkersDetector.DetectionResult {
        val detector = QRMarkersDetector()
        val outDirectory = File(baseOutDirectory, imageName).also {
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
        drawMetaFieldsArea(detectionResult.rotatedImageMat.clone(), metaFieldsArea, detectionResult.rotatedQrAndMetaMarkersArea, outDirectory)

        File(outDirectory, "10_qr_payload.txt").writeText(expectedPayload ?: "null")

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

    private fun drawMetaFieldsArea(mat: Mat, metaFieldsArea: Rect, rotatedQrAndMetaMarkersArea: Rect, outDirectory: File) {
        val color = Scalar(0.0, 255.0, 0.0)

        val metaFieldsMat = mat.submat(metaFieldsArea)
        File(outDirectory, "09_meta_fields.png").writeBytes(metaFieldsMat.toPng())

        Imgproc.rectangle(mat, metaFieldsArea, color, 1)
//        Imgproc.rectangle(mat, rotatedQrAndMetaMarkersArea, color, 1)
        File(outDirectory, "08_meta_fields_location.png").writeBytes(mat.toPng())
    }
}