package com.lazykernel.korurureader.util

import android.net.Uri
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import com.lazykernel.korurureader.MainActivity

class TesseractUtil {
    companion object {
        val instance = TesseractUtil()
    }

    val TESSERACT_BASE_DIR = MainActivity.context.cacheDir.path
    val baseAPI: TessBaseAPI = TessBaseAPI()
    init {
        if (!baseAPI.init(TESSERACT_BASE_DIR, "jp")) {
            Toast.makeText(MainActivity.context, "Couldn't init Tesseract in $TESSERACT_BASE_DIR", Toast.LENGTH_SHORT).show()
        }
    }

    fun extractTextFromImage(uri: Uri) {

    }

    fun destroy() {
        baseAPI.end()
    }
}