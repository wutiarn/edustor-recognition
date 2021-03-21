package ru.wtrn.edustor.edustorrecognition.controller

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wtrn.edustor.edustorrecognition.util.PdfRenderer
import ru.wtrn.edustor.edustorrecognition.util.QRMarkersDetector

@RestController
@RequestMapping("/recognize")
class PdfRecognitionController {
    @RequestMapping("/pdf")
    fun recognizePdf(@RequestBody file: ByteArray) {
        val renderer = PdfRenderer(file.inputStream())
        val image = renderer.next()

        val detector = QRMarkersDetector()
        val qrArea = detector.findQrArea(image)
    }
}
