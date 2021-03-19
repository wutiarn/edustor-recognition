package ru.wtrn.edustor.edustorrecognition.controller

import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wtrn.edustor.edustorrecognition.util.PdfRenderer
import ru.wtrn.edustor.edustorrecognition.util.toBufferedImage
import ru.wtrn.edustor.edustorrecognition.util.toMat

@RestController
@RequestMapping("/recognize")
class PdfRecognitionController {
    @RequestMapping("/pdf")
    fun recognizePdf(@RequestBody file: ByteArray) {
        val renderer = PdfRenderer(file.inputStream())
        val image = renderer.next()

        val srcMat = image.toMat()

        val grayImage = Mat()
        Imgproc.cvtColor(srcMat, grayImage, Imgproc.COLOR_RGB2GRAY)

        val recoded = grayImage.toBufferedImage()
    }
}
