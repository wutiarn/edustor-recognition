package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.Point
import java.lang.IllegalStateException
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object QrRotationAngleCalculator {
    fun calculateQrCodeAngle(metaMarker: Point, qrMarkersLocation: List<Point>): Double {
        if (qrMarkersLocation.size != 3) {
            throw IllegalStateException("qrMarkersLocation must contain exactly 3 points")
        }

        var other = listOf<Point>()
        val outlierPoint = qrMarkersLocation.first { candidate ->
            other = qrMarkersLocation.filter { it != candidate }
            isTopLeft(candidate, other)
        }

        other = other.sortedBy { it.x }

        val p1 = other[0]
        val p2 = other[1]

        val rightQrMarker = when {
            p1.dist(metaMarker) < p2.dist(metaMarker) -> p1
            else -> p2
        }

        val slope = calculateSlope(rightQrMarker, metaMarker)
        val rotationRad = Math.atan(slope) //  +Pi/2 is because OpenCV has inverted Y axis (zero is at top left corner, not at bottom left)
        var rotationDegrees = rotationRad * 180 / Math.PI - 90// Convert rad to degrees

        if (metaMarker.y > rightQrMarker.y) {
            // Page is upside down (since meta marker is higher than qr markers, and Y axis is inverted)
            rotationDegrees -= 180
        }

        return rotationDegrees
    }

    private fun isTopLeft(point: Point, other: List<Point>): Boolean {
        val distances = other.map { point.dist(it) }
        val d1 = distances[0]
        val d2 = distances[1]

        val delta = abs(d1 - d2)
        val maxDelta = d1 * 0.05
        return delta < maxDelta
    }

    private fun calculateSlope(p1: Point, p2: Point): Double {
        return (p1.y - p2.y) / (p1.x - p2.x)
    }
}