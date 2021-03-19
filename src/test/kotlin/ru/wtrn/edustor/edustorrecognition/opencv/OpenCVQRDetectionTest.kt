package ru.wtrn.edustor.edustorrecognition.opencv

import org.junit.jupiter.api.Test
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.QRCodeDetector
import ru.wtrn.edustor.edustorrecognition.util.toImageMat
import ru.wtrn.edustor.edustorrecognition.util.toPng
import java.io.File


class OpenCVQRDetectionTest {

    private val outDirectory = File("build/test-results/img/qr").also {
        it.mkdirs()
    }

    @Test
    fun testOpenCV() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        val srcMat = javaClass.getResource("/test_page.png").readBytes().toImageMat()
        val mat = Mat()
        Imgproc.cvtColor(srcMat, mat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.blur(mat, mat, Size(3.0, 3.0))
        Core.bitwise_not(mat, mat)
        Imgproc.threshold(mat, mat, 100.0, 255.0, Imgproc.THRESH_BINARY)
//        Core.bitwise_not(mat, mat)
        Imgproc.Canny(mat, mat, 5.0, 30.0, 3, false)

//        Imgproc.blur(mat, mat, Size(3.0, 3.0))
//        Imgproc.threshold(mat, mat, 100.0, 255.0, Imgproc.THRESH_BINARY)

//        Imgproc.adaptiveThreshold(mat, mat, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 20.0)

//        val eroceElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
//        Imgproc.erode(mat, mat, eroceElement);

//        val dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
//        Imgproc.dilate(mat, mat, dilateElement);

        val contours: List<MatOfPoint> = ArrayList()
        val hierarchy = Mat()
        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        val parentsCache = HashMap<Int, Int>()
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB)
        contours.forEachIndexed { i, contour ->
            val meta = hierarchy[0, i]
            val parent = meta[3]
            val color = when (parent > 0) {
                true -> Scalar(0.0, 255.0, 0.0)
                false -> Scalar(0.0, 0.0, 255.0)
            }
            if (calculateParentsCount(contourIndex = i, hierarchy = hierarchy, parentsCache = parentsCache) >= 5) {
                Imgproc.drawContours(mat, contours, i, color, 1)
                Imgproc.drawContours(srcMat, contours, i, color, 1)
            }
        }

        File(outDirectory, "result.png").writeBytes(srcMat.toPng())
        File(outDirectory, "mat.png").writeBytes(mat.toPng())
    }

    fun calculateParentsCount(contourIndex: Int, hierarchy: Mat, parentsCache: HashMap<Int, Int>): Int {
        parentsCache[contourIndex]?.let {
            return it
        }

        val hierarchyMeta = hierarchy[0, contourIndex]
        val parentIndex = hierarchyMeta[3]
        if (parentIndex < 0) {
            parentsCache[contourIndex] = 0
            return 0
        }

        val parentsCount = calculateParentsCount(parentIndex.toInt(), hierarchy, parentsCache) + 1
        parentsCache[contourIndex] = parentsCount
        return parentsCount
    }
}