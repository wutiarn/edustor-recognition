package ru.wtrn.edustor.edustorrecognition.util

import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

fun ByteArray.toImageMat(): Mat {
    val matOfByte = MatOfByte(*this)
    return Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED)
}

fun ByteArray.toBufferedImage(): BufferedImage {
    return ImageIO.read(ByteArrayInputStream(this))
}

fun BufferedImage.toMat(): Mat {
    val byteArrayOutputStream = ByteArrayOutputStream()
    ImageIO.write(this, "png", byteArrayOutputStream)
    byteArrayOutputStream.flush()
    return byteArrayOutputStream.toByteArray().toImageMat()
}

fun Mat.toPng(): ByteArray {
    val mob = MatOfByte()
    Imgcodecs.imencode(".png", this, mob)
    return mob.toArray()
}

fun Mat.toBufferedImage(): BufferedImage {
    return this.toPng().toBufferedImage()
}