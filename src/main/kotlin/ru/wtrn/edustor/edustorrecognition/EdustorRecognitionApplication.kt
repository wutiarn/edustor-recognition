package ru.wtrn.edustor.edustorrecognition

import org.opencv.core.Core
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EdustorRecognitionApplication

fun main(args: Array<String>) {
	System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
	runApplication<EdustorRecognitionApplication>(*args)
}
