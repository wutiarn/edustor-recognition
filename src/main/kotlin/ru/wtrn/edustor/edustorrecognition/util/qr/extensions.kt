package ru.wtrn.edustor.edustorrecognition.util.qr

import org.opencv.core.Point
import kotlin.math.pow
import kotlin.math.sqrt

fun Point.dist(other: Point): Double {
    return sqrt((this.x - other.x).pow(2) + (this.y - other.y).pow(2))
}