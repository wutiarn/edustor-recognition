package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.lang.IllegalArgumentException

class QRMarkersDetector() {

    companion object {
        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }
    }

    fun findQrArea(image: BufferedImage): QrArea {
        val loadedMat = loadMat(image)
        val foundContours = findMarkers(loadedMat)
        val qrMarkers: List<MatOfPoint> = foundContours.qrMarkers
        if (qrMarkers.size != 3) {
            throw IllegalArgumentException("Cannot detect QR code: found ${qrMarkers.size} markers")
        }
        val concatMat = MatOfPoint()
        Core.vconcat(qrMarkers, concatMat)
        val mat2f = MatOfPoint2f()
        concatMat.convertTo(mat2f, CvType.CV_32FC2)
        val rect = Imgproc.minAreaRect(mat2f)

        val qrMat = loadedMat.srcMat.submat(rect.boundingRect())
        Imgproc.cvtColor(qrMat, qrMat, Imgproc.COLOR_RGB2GRAY)

        return QrArea(
                rect = rect,
                qrMat = qrMat,
                angle = 0.0 // TODO: Calculate actual qr code rotation
        )
    }

    internal fun loadMat(image: BufferedImage): LoadedImageMat {
        val srcMat = image.toMat()
        val mat = Mat()
        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2RGB)
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
        return LoadedImageMat(
                mat = mat,
                srcMat = srcMat
        )
    }

    internal fun findMarkers(loadedMat: LoadedImageMat): MarkerFindResult {
        val mat = loadedMat.mat
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()

        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        val parentsCache = HashMap<Int, Int>()

        val qrMarkers = contours
                .mapIndexedNotNull { i, _ ->
                    val parentsCount = calculateParentsCount(contourIndex = i, hierarchy = hierarchy, parentsCache = parentsCache)
                    if (parentsCount < 5) {
                        return@mapIndexedNotNull null
                    }
                    val rootIndex = (0 until parentsCount)
                            .fold(i) { childIndex, _ -> getParentIndex(childIndex, hierarchy)!! }
                    contours[rootIndex]
                }
                .map {
                    val point2f = MatOfPoint2f()
                    it.convertTo(point2f, CvType.CV_32FC2)
                    val rect = Imgproc.minAreaRect(point2f)
                    rect.toMatOfPoint()
                }

        return MarkerFindResult(
                contours = contours,
                hierarchy = hierarchy,
                qrMarkers = qrMarkers
        )
    }

    internal fun calculateParentsCount(contourIndex: Int, hierarchy: Mat, parentsCache: HashMap<Int, Int>): Int {
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

    private fun getParentIndex(childIndex: Int, hierarchy: Mat): Int? {
        val hierarchyMeta = hierarchy[0, childIndex]
        val parentIndex = hierarchyMeta[3]
        return when {
            parentIndex < 0 -> null
            else -> parentIndex.toInt()
        }
    }

    data class LoadedImageMat(
            val mat: Mat,
            val srcMat: Mat
    )

    data class MarkerFindResult(
            val qrMarkers: List<MatOfPoint>,
            val contours: List<MatOfPoint>,
            val hierarchy: Mat
    )

    data class QrArea(
            val rect: RotatedRect,
            val qrMat: Mat,
            val angle: Double
    )
}