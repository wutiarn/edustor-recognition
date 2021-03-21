package ru.wtrn.edustor.edustorrecognition.controller

import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.wtrn.edustor.edustorrecognition.util.MetaFieldsExtractor
import ru.wtrn.edustor.edustorrecognition.util.PdfRenderer
import ru.wtrn.edustor.edustorrecognition.util.qr.QRMarkersDetector
import ru.wtrn.edustor.edustorrecognition.util.qr.QrCodeReader
import ru.wtrn.edustor.edustorrecognition.util.toBufferedImage
import ru.wtrn.edustor.edustorrecognition.util.toPng
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/recognize")
class PdfRecognitionController {
    val barcodeReader = QrCodeReader()

    @RequestMapping("/pdf/meta", produces = ["image/png"])
    fun recognizePdf(@RequestBody file: ByteArray, response: HttpServletResponse): ByteArray {
        val renderer = PdfRenderer(file.inputStream())
        val image = renderer.next()

        val detector = QRMarkersDetector()
        val detectionResult = detector.detect(image)

        val qrPayload = barcodeReader.readBarcode(detectionResult.qrMat.toBufferedImage())

        response.setHeader("Edustor-QR-Payload", qrPayload)
        return MetaFieldsExtractor.extract(detectionResult.rotatedImageMat, detectionResult.rotatedQrAndMetaMarkersArea).toPng()
    }
}
