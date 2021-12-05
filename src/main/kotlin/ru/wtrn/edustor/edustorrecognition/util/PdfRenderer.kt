package ru.wtrn.edustor.edustorrecognition.util

import org.ghost4j.document.PDFDocument
import org.ghost4j.renderer.SimpleRenderer
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.InputStream

class PdfRenderer(pdfStream: InputStream, resolution: Int = 150) : Iterator<BufferedImage>, Closeable {
    private val renderer = SimpleRenderer().also {
        it.resolution = resolution
    }
    private val pdfDocument: PDFDocument = PDFDocument().also {
        it.load(pdfStream)
    }

    private var nextPage = 0
    private val lastPage: Int = pdfDocument.pageCount - 1

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): BufferedImage {
        if (!hasNext()) throw NoSuchElementException()
        val curPage = nextPage++
        return renderer.render(pdfDocument, curPage, curPage)[0] as BufferedImage
    }

    override fun close() {
        // Nothing here for now
    }
}