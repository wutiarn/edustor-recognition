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
    fun renderToImage() {
        val fileBytes = javaClass.getResource("/test_page.pdf").readBytes()
        val renderer = PdfRenderer(fileBytes.inputStream())
        val image = renderer.next()
        File(outDirectory, "test_page.png").writeBytes(image.toPng())
    }
}