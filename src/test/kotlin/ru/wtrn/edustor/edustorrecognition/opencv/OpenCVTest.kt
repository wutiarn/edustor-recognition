package ru.wtrn.edustor.edustorrecognition.opencv

import org.junit.jupiter.api.Test
import org.opencv.core.*
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

        val lines = Mat()
        val thetaModifier = Math.PI / 180 * 80
        Imgproc.HoughLines(mat, lines, 1.0, Math.PI / 180, 1350, 0.0, 0.0, 0.0 + thetaModifier, thetaModifier + Math.PI / 180 * 20)

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_GRAY2RGB)

        val rows = lines.rows()
        (0 until rows).forEach { i ->
            val data = lines.get(i, 0)
            val rho = data[0]
            val theta = data[1]
            val cosTheta = Math.cos(theta)
            val sinTheta = Math.sin(theta)
            val x0 = cosTheta * rho
            val y0 = sinTheta * rho

            val pt1 = Point(x0 + 10000 * (-sinTheta), y0 + 10000 * cosTheta)
            val pt2 = Point(x0 - 10000 * (-sinTheta), y0 - 10000 * cosTheta)

            Imgproc.line(mat, pt1, pt2, Scalar(0.0, 0.0, 255.0), 2)
            Imgproc.line(srcMat, pt1, pt2, Scalar(0.0, 0.0, 255.0), 2)
        }

        File(outDirectory, "result.png").writeBytes(srcMat.toPng())
        File(outDirectory, "mat.png").writeBytes(mat.toPng())
    }
}