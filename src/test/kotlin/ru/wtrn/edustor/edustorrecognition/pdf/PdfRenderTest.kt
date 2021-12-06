package ru.wtrn.edustor.edustorrecognition.pdf

import org.junit.jupiter.api.Test
import ru.wtrn.edustor.edustorrecognition.util.PdfRenderer
import ru.wtrn.edustor.edustorrecognition.util.toBufferedImage
import ru.wtrn.edustor.edustorrecognition.util.toPng
import java.io.File

class PdfRenderTest {

    private val outDirectory = File("build/test-results/img/pdf").also {
        it.mkdirs()
    }

    @Test
    fun test_scanned_page() {
        renderToImage("test_page")
    }

    @Test
    fun test_digital_page() {
        renderToImage("digital_page")
    }

    private fun renderToImage(name: String) {
        val fileBytes = javaClass.getResource("/$name.pdf").readBytes()
        val image = PdfRenderer(fileBytes.inputStream()).use {
            it.next()
        }
        File(outDirectory, "$name.png").writeBytes(image.toPng())
    }
}