package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.RotatedRect

@Suppress("UNCHECKED_CAST")
fun RotatedRect.toPointsArray(): Array<Point> {
    val vertices = arrayOfNulls<Point>(4)
    this.points(vertices)
    return vertices as Array<Point>
}

fun RotatedRect.toMatOfPoint(): MatOfPoint {
    return MatOfPoint(*this.toPointsArray())
}