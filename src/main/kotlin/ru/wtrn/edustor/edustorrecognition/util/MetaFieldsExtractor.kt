package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.Mat
import org.opencv.core.Rect

object MetaFieldsExtractor {
    fun extract(imageMat: Mat, qrAndMetaArea: Rect): Mat {
        val area = getArea(qrAndMetaArea)
        return imageMat.submat(area)
    }

    fun getArea(qrAndMetaArea: Rect): Rect {
        /**
         * 0.4 is size of marker side, 0.25 - margin between marker and grid.
         * Both of these values are fractions of single cell side.
         */
        val markerCellHeight = 0.4 + 0.25
        val cellSide = qrAndMetaArea.height / (56 + markerCellHeight)
        return Rect(
            (qrAndMetaArea.x + qrAndMetaArea.width - cellSide * 8).toInt(),
            (qrAndMetaArea.y + 1 + cellSide * markerCellHeight).toInt(), // +1 here fixes "off by one error" (probably)
            (cellSide * 8).toInt(),
            (cellSide * 2).toInt()
        )
    }
}