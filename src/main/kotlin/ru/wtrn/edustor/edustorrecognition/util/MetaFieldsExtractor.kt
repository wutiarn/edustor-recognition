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
         * 0.4 is size of marker side, 0.5 - margin between marker and grid.
         * Both of this values are fractions of single cell side.
         */
        val markerCellHeigth = 0.4 + 0.25
        val cellSide = qrAndMetaArea.height / (56 + markerCellHeigth)
        return Rect(
            (qrAndMetaArea.x - cellSide * 6).toInt(),
            (qrAndMetaArea.y + cellSide * markerCellHeigth).toInt(),
            (cellSide * 8).toInt(),
            (cellSide * 2).toInt()
        )
    }
}