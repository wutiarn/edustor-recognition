package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.RotatedRect
import ru.wtrn.edustor.edustorrecognition.util.qr.dist

object MetaMarkerDifferentiator {
    /**
     * Find outlying marker that is far away from other markers.
     *
     * Distance between markers in a group should be at least two times less than between outlying marker and any marker from the group.
     */
    fun findMetaMarker(qrMarkers: List<RotatedRect>): RotatedRect {
        return qrMarkers.asSequence()
                .mapNotNull { marker -> tryToFindMetaMarker(marker, qrMarkers.filter { it != marker }) }
                .first()
    }

    private fun tryToFindMetaMarker(primary: RotatedRect, other: List<RotatedRect>): RotatedRect? {
        val distances = other.associateWith { primary.center.dist(it.center) }
        val (mostDistantMarker, maxDistance) = distances.maxByOrNull { it.value } ?: return null

        val remainingDistances = distances.filterKeys { it != mostDistantMarker }
        if (remainingDistances.all { it.value * 2 < maxDistance }) {
            return mostDistantMarker
        }
        return null
    }
}