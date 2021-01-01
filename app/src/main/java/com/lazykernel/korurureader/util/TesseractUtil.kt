package com.lazykernel.korurureader.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import com.lazykernel.korurureader.MainActivity
import kotlin.collections.ArrayList

class TesseractUtil {
    companion object {
        val instance = TesseractUtil()
    }

    private val TESSERACT_BASE_DIR = MainActivity.context.filesDir.absolutePath + "/tesseract/"
    private val baseAPI: TessBaseAPI = TessBaseAPI()
    private var currentImage: Bitmap? = null
    init {
        // Load language files from asset packs
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "eng.traineddata")
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn.traineddata")
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn_vert.traineddata")

        // Init with japanese, japanese vertical and english
        if (!baseAPI.init(TESSERACT_BASE_DIR, "jpn+jpn_vert")) {
            Toast.makeText(MainActivity.context, "Couldn't init Tesseract in $TESSERACT_BASE_DIR", Toast.LENGTH_SHORT).show()
        }
    }


    // setImage -> getTextBlockRegions -> pre-processing -> extractTextFromImage -> getImageTextRegions -> post-processing
    // pre-processing: detect text orientation (horizontal vs vertical)
    // post-processing: iterate through words and place them in the correct text block (intersecting block)

    fun setImage(uri: Uri) {
        currentImage = FileUtil.instance.loadUriToBitmap(uri)
        baseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO_ONLY
        baseAPI.setImage(currentImage)
    }

    fun getImageTextRegions(): ArrayList<Rect> {
        val it = baseAPI.resultIterator
        val list = ArrayList<Rect>()
        it.begin()
        do {
            list.add(it.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_BLOCK))
        } while (it.next(TessBaseAPI.PageIteratorLevel.RIL_BLOCK))
        it.delete()
        return list
    }

    fun getTextBlockRegions(): ArrayList<Rect> {
        val rects: ArrayList<Rect> = baseAPI.regions.boxRects

        // Instantly return if empty or size 1
        if (rects.size <= 1) {
            return rects
        }

        // Sort by x coordinate
        rects.sortBy { rect -> rect.left }

        fun rectSize(rect: Rect): Int = rect.width() * rect.height()

        // First element is guaranteed to be largest
        // because the outer most box on every side must be part of the largest one
        // exception being if there are multiple rects with the exact same smallest x coordinate
        // however, this is checked above
        //
        // Worst case O(n^2), average O(n)
        // since outerRects should be quite small, usually around 1 or 2
        val outerRects = ArrayList<Rect>()
        outerRects.add(rects[0])
        rects.subList(1, rects.size - 1).forEach { rect ->
            var result = 0
            val rectsToRemove = ArrayList<Rect>()
            for (r in outerRects) {
                if (Rect.intersects(rect, r)) {
                    // If rects intersect and have the same x coordinate
                    if (rect.left == r.left) {
                        // If this new rect is larger than the last outer rect with the same x coordinate
                        // we want to have this as the outer one instead
                        // In that case, we'll also have to check that this new rect doesn't overlap any
                        // existing ones (done automatically since we use a continue instead of a break)
                        // Using >= operator for mega edge cases where the rects are the exact same size
                        // to guarantee that there won't be duplicate rects of the exact same dimensions
                        if (rectSize(rect) >= rectSize(r)) {
                            result = 2
                            rectsToRemove.add(r)
                            continue
                        } else {
                            break
                        }
                    }
                    result = 1
                    break
                }
            }

            when (result) {
                0 -> outerRects.add(rect)
                2 -> {
                    rectsToRemove.forEach { r -> outerRects.remove(r) }
                    outerRects.add(rect)
                }
            }
        }
        return outerRects
    }

    fun extractTextFromImage(): String {
        // Set page seg mode to single block (either horizontal or vertical based on preprocessing)
        baseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT
        // Clear previous result just detecting text blocks
        baseAPI.setImage(currentImage)
        return baseAPI.utF8Text
    }

    fun destroy() {
        baseAPI.end()
    }
}