package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.Point
import java.lang.IllegalStateException
import kotlin.math.abs

object QrRotationAngleCalculator {
    fun calculateQrCodeAngle(qrMarkersLocation: List<Point>): Double {
        if (qrMarkersLocation.size != 3) {
            throw IllegalStateException("qrMarkersLocation must contain exactly 3 points")
        }

        var other = listOf<Point>()
        val topLeftPoint = qrMarkersLocation.first { candidate ->
            other = qrMarkersLocation.filter { it != candidate }
            isTopLeft(candidate, other)
        }

        val p1 = other[0]
        val p2 = other[1]

        val slope = calculateSlope(p1, p2)
        val rotationRad = Math.atan(slope) - Math.PI / 4

        return rotationRad * 180 / Math.PI // Convert rad to degrees
    }

    private fun isTopLeft(point: Point, other: List<Point>): Boolean {
        val distances = other.map { point.dist(it) }
        val d1 = distances[0]
        val d2 = distances[1]

        val delta = d1 - d2
        val maxDelta = d1 * 0.01
        return delta < maxDelta
    }

    private fun calculateSlope(p1: Point, p2: Point): Double {
        return abs(p1.y - p2.y) / abs(p1.x - p2.x)
    }
}