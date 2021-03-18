package ru.wtrn.edustor.edustorrecognition.util

import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import java.awt.image.BufferedImage
import java.io.InputStream

class PdfRenderer(pdfStream: InputStream, resolution: Int = 150) : Iterator<BufferedImage> {
    val renderer: SimpleRenderer
    val pdfDocument: PDFDocument

    var nextPage = 0
    val lastPage: Int

    init {
        pdfDocument = PDFDocument()
        pdfDocument.load(pdfStream)
        lastPage = pdfDocument.pageCount - 1

        renderer = SimpleRenderer()
        renderer.resolution = resolution
    }

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): BufferedImage {
        if (!hasNext()) throw NoSuchElementException()
        val curPage = nextPage++
        return renderer.render(pdfDocument, curPage, curPage)[0] as BufferedImage
    }
}