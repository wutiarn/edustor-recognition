package ru.wtrn.edustor.edustorrecognition.util

import org.junit.jupiter.api.Test
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File

internal class QRMarkersDetectorTest {

    private val outDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    @Test
    fun testQrMarkersDetection() {
        val image = javaClass.getResource("/test_page.png").readBytes().toBufferedImage()
        val detector = QRMarkersDetector(image)

        val (mat, srcMat) = detector.loadMat()
        File(outDirectory, "01_raw.png").writeBytes(srcMat.toPng())
        File(outDirectory, "02_preprocessed.png").writeBytes(mat.toPng())

        val (qrMarkers, contours, hierarchy) = detector.findContours()

        val color = Scalar(0.0, 255.0, 0.0)
        qrMarkers.forEachIndexed { i, contour ->
            Imgproc.drawContours(srcMat, qrMarkers, i, color, 1)
        }
        File(outDirectory, "03_markers.png").writeBytes(srcMat.toPng())

        val qrArea = detector.findQrArea()
        Imgproc.drawContours(srcMat, listOf(MatOfPoint(*qrArea.points)), 0, color, 1)
        File(outDirectory, "04_qr_area.png").writeBytes(srcMat.toPng())
    }
}