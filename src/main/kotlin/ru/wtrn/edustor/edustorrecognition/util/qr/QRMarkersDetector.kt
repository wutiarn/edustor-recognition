package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import ru.wtrn.edustor.edustorrecognition.util.toMat
import ru.wtrn.edustor.edustorrecognition.util.toMatOfPoint
import java.awt.image.BufferedImage
import kotlin.math.abs

class QRMarkersDetector() {

    companion object {
        init {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
        }
    }

    fun detect(image: BufferedImage): DetectionResult {
        val loadedMat = loadMat(image)
        val detectedMarkers = detectMarkers(loadedMat)
        val qrMarkers: List<RotatedRect> = detectedMarkers.qrMarkers
        if (qrMarkers.size != 3) {
            throw QrDetectionFailedException("Cannot detect QR code: found ${qrMarkers.size} markers")
        }

        val angle = QrRotationAngleCalculator.calculateQrCodeAngle(qrMarkers.map { it.center })
        val qrArea = findQrArea(qrMarkers)
        val qrMat = loadedMat.srcMat.submat(qrArea.boundingRect())
        Imgproc.cvtColor(qrMat, qrMat, Imgproc.COLOR_RGB2GRAY)

        val rotated = rotateImage(loadedMat.srcMat, angle)

        return DetectionResult(
                qrArea = qrArea,
                qrMat = qrMat,
                angle = angle, // TODO: Calculate actual qr code rotation
                detectedMarkers = detectedMarkers,
                imageMat = loadedMat
        )
    }

    private fun rotateImage(srcMat: Mat, angle: Double): Mat {
        val dstMat = Mat()
        val matSize = srcMat.size()
        val center = Point(matSize.width / 2, matSize.height / 2)
        val rotationMat = Imgproc.getRotationMatrix2D(center, angle, 1.0)
        Imgproc.warpAffine(srcMat, dstMat, rotationMat, matSize)
        return dstMat
    }

    private fun findQrArea(qrMarkers: List<RotatedRect>): RotatedRect {
        val concatMat = MatOfPoint()
        Core.vconcat(qrMarkers.map { it.toMatOfPoint() }, concatMat)
        val mat2f = MatOfPoint2f()
        concatMat.convertTo(mat2f, CvType.CV_32FC2)
        return Imgproc.minAreaRect(mat2f)
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

    internal fun detectMarkers(loadedMat: LoadedImageMat): MarkerDetectionResult {
        val mat = loadedMat.mat
        val contours = ArrayList<MatOfPoint>()
        val hierarchy = Mat()

        Imgproc.findContours(mat, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        val potentialMarkers = ArrayList<PotentialMarker>()

        val parentsCache = HashMap<Int, Int>()

        val qrMarkers = contours.mapIndexedNotNull { i, contour ->
            if (!checkHasNoChildContour(i, hierarchy)) {
                return@mapIndexedNotNull null
            }
            val parentsCount = calculateParentsCount(contourIndex = i, hierarchy = hierarchy, parentsCache = parentsCache)
            if (parentsCount < 3) {
                return@mapIndexedNotNull null
            }

            val internalContour = getMinAreaRect(contour)
            if (validateMarkerForm(internalContour) != null) {
                return@mapIndexedNotNull null
            }

            val potentialMarker = PotentialMarker(
                    index = i,
                    contour = internalContour)
            potentialMarkers.add(potentialMarker)
            findExternalContour(potentialMarker, hierarchy, contours)
        }

        return MarkerDetectionResult(
                contours = contours,
                potentialMarkers = potentialMarkers,
                hierarchy = hierarchy,
                qrMarkers = qrMarkers
        )
    }

    private fun findExternalContour(potentialMarker: PotentialMarker, hierarchy: Mat, contours: List<MatOfPoint>): RotatedRect? {
        val internalIndex = potentialMarker.index
        val internalContour = potentialMarker.contour

        var externalContour: RotatedRect? = null
        var middleLayersCount: Int = -1
        var parentIndex = getParentIndex(internalIndex, hierarchy)!!
        while (true) {
            val candidate = getMinAreaRect(contours[parentIndex])
            val rejectionReason = validateMarkerForm(candidate)
                    ?: validateCenterDistance(internalContour, candidate)
            if (rejectionReason != null) {
                if (externalContour == null) {
                    // Set rejectionReason only if we didn't find any external contours
                    potentialMarker.parentContour = candidate
                    potentialMarker.rejectionReason = rejectionReason
                }
                break
            }
            externalContour = candidate
            middleLayersCount++
            parentIndex = getParentIndex(parentIndex, hierarchy) ?: break
        }

        potentialMarker.parentContour = externalContour
        potentialMarker.externalContourMiddleLayersCount = middleLayersCount

        if (middleLayersCount < 1) {
            potentialMarker.rejectionReason = PotentialMarker.RejectionReason.NOT_ENOUGH_MIDDLE_LAYERS
            return null
        }

        return externalContour?.takeIf {
            val rejectionReason = validateMarkerAreaRatio(internalContour, externalContour)
            potentialMarker.rejectionReason = rejectionReason
            rejectionReason == null
        }
    }

    private fun getMinAreaRect(mop: MatOfPoint): RotatedRect {
        val point2f = MatOfPoint2f()
        mop.convertTo(point2f, CvType.CV_32FC2)
        return Imgproc.minAreaRect(point2f)
    }

    private fun validateCenterDistance(internalContour: RotatedRect, externalContour: RotatedRect): PotentialMarker.RejectionReason? {
        // Check that both rectangles has same center
        val maxCenterDistance = (internalContour.size.width * 0.01).coerceAtLeast(1.0)
        val centerDistance = externalContour.center.dist(internalContour.center)
        if (centerDistance > maxCenterDistance) {
            return PotentialMarker.RejectionReason.INCORRECT_CENTER_DISTANCE
        }

        return null // TODO: Implement marker validation
    }

    private fun validateMarkerAreaRatio(internalContour: RotatedRect, externalContour: RotatedRect): PotentialMarker.RejectionReason? {
        val areaRatio = internalContour.size.area() / externalContour.size.area()
        val perfectRatio = 0.1836
        val maxRatioDelta = 0.03
        val ratioDelta = abs(areaRatio - perfectRatio)
        if (ratioDelta > maxRatioDelta) {
            return PotentialMarker.RejectionReason.INCORRECT_AREA_RATIO
        }
        return null
    }

    /**
     * Check that marker has square form
     */
    private fun validateMarkerForm(contour: RotatedRect): PotentialMarker.RejectionReason? {
        // Allow up to 10% width/height difference (or up to 3 pixels if 10% is smaller).
        val maxPixelDelta = (contour.size.width * 0.1).coerceAtLeast(3.0)
        val actualPixelDelta = Math.abs(contour.size.width - contour.size.height)
        if (actualPixelDelta > maxPixelDelta) {
            return PotentialMarker.RejectionReason.NON_SQUARE_FORM
        }
        return null
    }

    private fun checkHasNoChildContour(contourIndex: Int, hierarchy: Mat): Boolean {
        val hierarchyMeta = hierarchy[0, contourIndex]
        return hierarchyMeta[2] < 0
    }

    private fun calculateParentsCount(contourIndex: Int, hierarchy: Mat, parentsCache: HashMap<Int, Int>): Int {
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

    data class DetectionResult(
            val qrArea: RotatedRect,
            val qrMat: Mat,
            val angle: Double,
            val detectedMarkers: MarkerDetectionResult,
            val imageMat: LoadedImageMat
    )

    data class LoadedImageMat(
            val mat: Mat,
            val srcMat: Mat
    )

    data class MarkerDetectionResult(
            val qrMarkers: List<RotatedRect>,
            val potentialMarkers: List<PotentialMarker>,
            val contours: List<MatOfPoint>,
            val hierarchy: Mat
    )

    data class PotentialMarker(
            val index: Int,
            val contour: RotatedRect,
            var parentContour: RotatedRect? = null,
            var externalContourMiddleLayersCount: Int? = null,
            var rejectionReason: RejectionReason? = null
    ) {
        enum class RejectionReason {
            NOT_ENOUGH_MIDDLE_LAYERS,
            NON_SQUARE_FORM,
            INCORRECT_CENTER_DISTANCE,
            INCORRECT_AREA_RATIO
        }
    }
}