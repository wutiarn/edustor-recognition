package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.Mat
import org.opencv.core.Rect

object MetaFieldsExtractor {
    fun getArea(imageMat: Mat, qrAndMetaArea: Rect): Rect {
        val markerCellHeigth = 0.4 + 0.25
        val cellSide = qrAndMetaArea.height / (56 + markerCellHeigth)
        return Rect((qrAndMetaArea.x - cellSide * 6).toInt(), (qrAndMetaArea.y + cellSide * markerCellHeigth).toInt(), (cellSide * 8).toInt(), (cellSide * 2).toInt())
    }
}