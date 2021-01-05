package com.lazykernel.korurureader.util

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.lazykernel.korurureader.MainActivity
import java.io.File
import java.io.InputStream

/**
 * A singleton FileUtil class
 */
class FileUtil {

    companion object {
        val instance = FileUtil()
    }

    fun loadUriToBitmap(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(MainActivity.context.contentResolver, uri).copy(Bitmap.Config.ARGB_8888, true)
        } else {
            val source = ImageDecoder.createSource(MainActivity.context.contentResolver, uri)
            ImageDecoder.decodeBitmap(source).copy(Bitmap.Config.ARGB_8888, true)
        }
    }

    /**
     * Copies an asset file from assets to phone internal storage, if it doesn't already exist
     * Will be copied to path <prefix> + <assetName> in files directory
     * Returns true if copied, false otherwise (including if file already exists)
     */
    fun copyAssetToFilesIfNotExist(prefix: String, assetName: String): Boolean {
        val file = File(MainActivity.context.filesDir.absolutePath, prefix + assetName)
        if (file.exists()) {
            return false
        }

        val inputStream: InputStream = MainActivity.context.assets.open(assetName)
        File(MainActivity.context.filesDir.absolutePath, prefix).mkdirs()
        // Copy in 10mb chunks to avoid going oom for larger files
        inputStream.copyTo(file.outputStream(), 10000)
        inputStream.close()
        return true
    }

    /**
     * Returns an uri to a temporary file
     */
    fun createTempFile(name: String, extension: String): File {
        return File.createTempFile(name, extension, MainActivity.context.externalCacheDir)
    }

    /**
     * Deletes all temporary files in cache directory for this app
     */
    fun deleteTempFiles(): Boolean {
        return MainActivity.context.externalCacheDir?.let { deleteFiles(it) } ?: false
    }

    /**
     * Deletes all files in a directory
     */
    private fun deleteFiles(file: File): Boolean {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files != null) {
                for (f in files) {
                    if (f.isDirectory) {
                        deleteFiles(f)
                    }
                    else {
                        f.delete()
                    }
                }
            }
        }
        return file.delete()
    }
}