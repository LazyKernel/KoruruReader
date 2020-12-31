package com.lazykernel.korurureader.util

import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import com.googlecode.tesseract.android.TessBaseAPI
import com.lazykernel.korurureader.MainActivity
import java.lang.Exception

class TesseractUtil {
    companion object {
        val instance = TesseractUtil()
    }

    private val TESSERACT_BASE_DIR = MainActivity.context.filesDir.absolutePath + "/tesseract/"
    private val baseAPI: TessBaseAPI = TessBaseAPI()
    init {
        // Load language files from asset packs
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "eng.traineddata")
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn.traineddata")

        // Init with japanese, japanese vertical and english
        if (!baseAPI.init(TESSERACT_BASE_DIR, "jpn+eng")) {
            Toast.makeText(MainActivity.context, "Couldn't init Tesseract in $TESSERACT_BASE_DIR", Toast.LENGTH_SHORT).show()
        }
    }

    fun extractTextFromImage(uri: Uri): String {
        baseAPI.setImage(FileUtil.instance.loadUriToBitmap(uri))
        val text = baseAPI.utF8Text
        println(text)
        return text
    }

    fun destroy() {
        baseAPI.end()
    }
}