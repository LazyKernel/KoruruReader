package com.lazykernel.korurureader.util

import com.lazykernel.korurureader.MainActivity
import java.io.File

/**
 * A singleton FileUtil class
 */
class FileUtil {

    companion object {
        val instance = FileUtil()
    }

    /**
     * Returns an uri to a temporary file
     */
    fun createTempFile(name: String, extension: String): File {
        return File.createTempFile(name, extension, MainActivity.context.cacheDir)
    }

    /**
     * Deletes all temporary files in cache directory for this app
     */
    fun deleteTempFiles(): Boolean {
        return deleteFiles(MainActivity.context.cacheDir)
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