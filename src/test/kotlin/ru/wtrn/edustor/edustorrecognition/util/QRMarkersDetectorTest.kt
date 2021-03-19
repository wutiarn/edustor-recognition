package ru.wtrn.edustor.edustorrecognition.util

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
        val imageBytes = javaClass.getResource("/test_page.png").readBytes()
        val detector = QRMarkersDetector(imageBytes)

        val contours = detector.contours
        val hierarchy = detector.hierarchy

        detector.qrMarkers.forEachIndexed { i, contour ->
            val color = Scalar(0.0, 255.0, 0.0)
            Imgproc.drawContours(detector.mat, detector.qrMarkers, i, color, 1)
            Imgproc.drawContours(detector.srcMat, detector.qrMarkers, i, color, 1)
        }

//        val qrCenterPoints = detector.qrCenterPoints
//        qrCenterPoints.forEach {
//            Imgproc.circle(detector.srcMat, it, 10, Scalar(0.0, 255.0, 0.0), Imgproc.FILLED)
//        }

        File(outDirectory, "result.png").writeBytes(detector.srcMat.toPng())
        File(outDirectory, "mat.png").writeBytes(detector.mat.toPng())
    }
}