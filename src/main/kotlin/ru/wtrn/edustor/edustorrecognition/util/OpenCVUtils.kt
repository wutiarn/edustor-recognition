package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.RotatedRect

fun RotatedRect.toMatOfPoint(): MatOfPoint {
    val vertices = arrayOfNulls<Point>(4)
    this.points(vertices)
    return MatOfPoint(*vertices)
}