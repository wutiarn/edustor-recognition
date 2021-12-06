package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.Point
import org.opencv.core.RotatedRect
import ru.wtrn.edustor.edustorrecognition.util.toPointsArray
import java.lang.IllegalStateException
import kotlin.math.*

object QrRotationAngleCalculator {
    // Meta marker is smaller and is located a bit righter than right QR code marker.
    private const val META_MARKER_X_OFFSET = 2

    fun calculateQrCodeAngle(metaMarker: RotatedRect, qrMarkersLocation: List<RotatedRect>): Double {
        if (qrMarkersLocation.size != 3) {
            throw IllegalStateException("qrMarkersLocation must contain exactly 3 points")
        }

        val rightQrMarker = qrMarkersLocation.minByOrNull { it.center.dist(metaMarker.center) }!!

        /**
         * Determine whether page is upside down using meta marker (since it is located higher than qr markers).
         * Note, that OpenCV axis starts at top left corner, so Y axis is inverted
         */
        val pageIsUpsideDown = metaMarker.center.y > rightQrMarker.center.y

        // Find right marker points instead of
        val slopePoints =
            if (!pageIsUpsideDown) Pair(
                metaMarker.toPointsArray().maxByOrNull { it.x }!!,
                rightQrMarker.toPointsArray().maxByOrNull { it.x }!!.also { it.x += META_MARKER_X_OFFSET }
            ) else Pair(
                metaMarker.toPointsArray().minByOrNull { it.x }!!,
                rightQrMarker.toPointsArray().minByOrNull { it.x }!!.also { it.x -= META_MARKER_X_OFFSET }
            )

        val slope = calculateSlope(slopePoints.first, slopePoints.second)
        val rotationRad =
            atan(slope) //  +Pi/2 is because OpenCV has inverted Y axis (zero is at top left corner, not at bottom left)
        var rotationDegrees = rotationRad * 180 / Math.PI - 90// Convert rad to degrees

        /**
         * Tan has 180 degrees period (from -90 deg to 90 deg). We subtract 90 degrees above, which can lead to period overflow.
         * So we add period value to make sure value is within valid boundaries.
         */
        if (rotationDegrees.absoluteValue > 90) {
            rotationDegrees += -1 * rotationDegrees.sign * 180
        }

        /**
         * Tan cannot determine whether page is upside down or not.
         * But we already know it and can change rotationDegrees if necessary.
         */
        if (pageIsUpsideDown) {
            rotationDegrees += -1 * rotationDegrees.sign * 180
        }

        return rotationDegrees
    }

    private fun calculateSlope(p1: Point, p2: Point): Double {
        return (p1.y - p2.y) / (p1.x - p2.x)
    }
}