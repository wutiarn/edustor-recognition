package ru.wtrn.edustor.edustorrecognition.util

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.image.BufferedImage
import java.io.Closeable
import java.io.InputStream

class PdfRenderer(pdfStream: InputStream, resolution: Int = 300) : Iterator<BufferedImage>, Closeable {
    private val pdfDocument: PDDocument = PDDocument.load(pdfStream)
    private val renderer = PDFRenderer(pdfDocument)
    private val scale = resolution / 72f

    private var nextPage = 0
    private val lastPage: Int = pdfDocument.numberOfPages - 1

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): BufferedImage {
        if (!hasNext()) throw NoSuchElementException()
        val curPage = nextPage++
        return renderer.renderImage(curPage, scale, ImageType.GRAY)
    }

    override fun close() {
        pdfDocument.close()
    }
}