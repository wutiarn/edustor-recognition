package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.*
import org.opencv.imgproc.Imgproc

class QRMarkersDetector(private val imageBytes: ByteArray) {

    internal val contours = ArrayList<MatOfPoint>()
    internal val hierarchy = Mat()

    internal val srcMat: Mat
    internal val mat: Mat

    private val parentsCache = HashMap<Int, Int>()
    val qrCenterPoints: List<Point>

    companion object {
        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }
    }

    init {
        srcMat = imageBytes.toImageMat()
        mat = Mat()
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

        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB)

        val qrMarkers = contours.filterIndexed { i, _ ->
            calculateParentsCount(contourIndex = i) >= 5
        }

        qrCenterPoints = qrMarkers.map { calculateMassCenter(it) }
    }


    internal fun calculateParentsCount(contourIndex: Int): Int {
        parentsCache[contourIndex]?.let {
            return it
        }

        val hierarchyMeta = hierarchy[0, contourIndex]
        val parentIndex = hierarchyMeta[3]
        if (parentIndex < 0) {
            parentsCache[contourIndex] = 0
            return 0
        }

        val parentsCount = calculateParentsCount(parentIndex.toInt()) + 1
        parentsCache[contourIndex] = parentsCount
        return parentsCount
    }

    private fun calculateMassCenter(contour: MatOfPoint): Point {
        val moments = Imgproc.moments(contour)
        val centroid = Point()
        centroid.x = moments._m10 / moments._m00;
        centroid.y = moments._m01 / moments._m00;
        return centroid
    }
}