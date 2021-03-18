package ru.wtrn.edustor.edustorrecognition.util

import com.spire.pdf.PdfDocument
import java.awt.image.BufferedImage
import java.io.InputStream

class PdfRenderer(pdfStream: InputStream, resolution: Int = 150) : Iterator<BufferedImage> {
    val pdfDocument: PdfDocument

    var nextPage = 0
    val lastPage: Int

    init {
        pdfDocument = PdfDocument()
        pdfDocument.loadFromStream(pdfStream)
        lastPage = pdfDocument.pages.count - 1
    }

    override fun hasNext(): Boolean {
        return nextPage <= lastPage
    }

    override fun next(): BufferedImage {
        if (!hasNext()) throw NoSuchElementException()
        val curPage = nextPage++
        return pdfDocument.saveAsImage(curPage)
    }
}