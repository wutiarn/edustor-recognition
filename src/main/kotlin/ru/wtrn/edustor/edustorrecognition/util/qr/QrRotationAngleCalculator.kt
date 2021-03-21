package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.Point
import java.lang.IllegalStateException
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

object QrRotationAngleCalculator {
    fun calculateQrCodeAngle(qrMarkersLocation: List<Point>): Double {
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

        val slope = calculateSlope(p1, p2) // OpenCV has inverted Y axis (zero is at top left corner, not at bottom left)
        val perpendicularDistance = calculatePerpendicularDistance(outlierPoint, p1, p2)
        val rotationRad = Math.atan(slope) - Math.PI / 4 + Math.PI / 2
        var rotationDegrees = rotationRad * 180 / Math.PI // Convert rad to degrees

        if (perpendicularDistance > 0) {
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

    private fun calculatePerpendicularDistance(point: Point, lineP1: Point, lineP2: Point): Double {
        // See https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line#Line_defined_by_two_points
        val slope = calculateSlope(lineP1, lineP2)
        val a = -1 * slope
        val b = 1.0
        val c = (slope * lineP2.x) - lineP2.y

        val dist = (a * point.x + b * point.y + c) / sqrt(a.pow(2) + b.pow(2))
        return dist
    }
}