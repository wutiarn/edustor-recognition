package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.Mat
import org.opencv.core.Rect

object MetaFieldsExtractor {
    fun getArea(imageMat: Mat, qrArea: Rect): Rect {
        val cellSide = qrArea.height / 2.0
        return Rect((qrArea.x - cellSide * 6).toInt(), (qrArea.y - cellSide * 54).toInt(), (cellSide * 8).toInt(), (cellSide * 2).toInt())
    }
}