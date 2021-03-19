package ru.wtrn.edustor.edustorrecognition.opencv

import org.junit.jupiter.api.Test
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import ru.wtrn.edustor.edustorrecognition.util.toImageMat
import ru.wtrn.edustor.edustorrecognition.util.toPng
import java.io.File

class OpenCVTest {

    private val outDirectory = File("build/test-results/img").also {
        it.mkdirs()
    }

    @Test
    fun testOpenCV() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

        val srcMat = javaClass.getResource("/test_page.png").readBytes().toImageMat()
        val mat = Mat()
        Imgproc.cvtColor(srcMat, mat, Imgproc.COLOR_RGB2GRAY)
        Imgproc.blur(mat, mat, Size(3.0, 3.0))
        Imgproc.Canny(mat, mat, 5.0, 30.0, 3, false)

        val dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(5.0, 5.0))
        Imgproc.dilate(mat, mat, dilateElement);

        val resultBytes = mat.toPng()
        val outFile = File(outDirectory, "opencv.png").writeBytes(resultBytes)
    }
}