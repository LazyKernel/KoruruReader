package com.lazykernel.korurureader.util

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import com.googlecode.tesseract.android.TessBaseAPI
import com.lazykernel.korurureader.MainActivity
import java.util.*
import kotlin.collections.ArrayList

/**
 * A singleton class for interacting with Tesseract OCR
 */
class TesseractUtil {
    companion object {
        val instance = TesseractUtil()
    }

    private val TESSERACT_BASE_DIR = MainActivity.context.filesDir.absolutePath + "/tesseract/"
    private val baseAPI: TessBaseAPI = TessBaseAPI()
    private var currentImage: Bitmap? = null
    // regions[i] corresponds to textRegions[i]
    private var cachedRegions: ArrayList<Rect>? = null
    private var cachedTextRegions: ArrayList<String>? = null
    init {
        // Load language files from asset packs
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "eng.traineddata")
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn.traineddata")
        FileUtil.instance.copyAssetToFilesIfNotExist("tesseract/tessdata/", "jpn_vert.traineddata")

        // Init with japanese and japanese vertical
        if (!baseAPI.init(TESSERACT_BASE_DIR, "jpn+jpn_vert")) {
            Toast.makeText(MainActivity.context, "Couldn't init Tesseract in $TESSERACT_BASE_DIR", Toast.LENGTH_SHORT).show()
        }
    }


    // setImage -> getTextBlockRegions -> pre-processing -> extractTextFromImage
    // TODO: pre-processing: detect text orientation (horizontal vs vertical)

    /**
     * Prepares Tesseract for detecting text block regions
     *
     * Loads image from the specified [uri]
     *
     * Resets the cached regions and text regions
     *
     * @param   uri an uri to the image
     */
    fun setImage(uri: Uri) {
        // Reset cache
        cachedRegions = null
        cachedTextRegions = null

        // Reset baseAPI
        currentImage = FileUtil.instance.loadUriToBitmap(uri)
        baseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_AUTO_ONLY
        baseAPI.setImage(currentImage)
    }

    /**
     * Returns an [ArrayList] of [Rect]s that contains the largest non-overlapping regions returned
     * from Tesseract that completely contain other overlapping regions mistakenly identified by
     * Tesseract. This function runs in O(n log n) time on average using the following assumption:
     * If Tesseract's page segmentation algorithm returns more than 1 region, all outermost non-
     * overlapping AABBs will fully envelop all other regions within them
     *
     * The result of the function is cached. The cache is reset every time [setImage] is called.
     *
     * @return list of text block regions
     */
    fun getTextBlockRegions(): ArrayList<Rect> {
        // Return if cached
        cachedRegions?.let { return it }

        val rects: ArrayList<Rect> = baseAPI.regions.boxRects

        // Instantly return if empty or size 1
        if (rects.size <= 1) {
            cachedRegions = rects
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

        cachedRegions = outerRects
        return outerRects
    }

    /**
     * Returns an [ArrayList] of [String]s corresponding to the text regions returned from
     * [getTextBlockRegions]. [getTextBlockRegions] MUST be called after [setImage] and before this
     * function. Currently iterates over Tesseract OCR results on word by word basis and appends
     * text to the end of the correct text block. If Tesseract starts splitting words between
     * different text blocks, change the level to symbol level (slower but more accurate).
     *
     * Runs the actual OCR process so can take multiple minutes for larger images and on slower
     * devices.
     *
     * The result of the function is cached. The cache is reset every time [setImage] is called.
     *
     * @return list of strings corresponding to the text blocks
     */
    fun extractTextFromImage(): ArrayList<String> {
        // Return if cached
        cachedTextRegions?.let { return it }

        // Set page seg mode to single block (either horizontal or vertical based on pre-processing)
        // TODO: vertical vs horizontal pre-processing
        baseAPI.pageSegMode = TessBaseAPI.PageSegMode.PSM_SINGLE_BLOCK_VERT_TEXT
        // Clear previous result just detecting text blocks
        baseAPI.setImage(currentImage)
        // Calling to run ocr
        baseAPI.utF8Text

        val it = baseAPI.resultIterator
        // Call getTextBlockRegions first
        val list = ArrayList<String>(Collections.nCopies(cachedRegions!!.size, ""))
        it.begin()
        do {
            // Going for word level for now, change to symbol if having problems with accuracy
            val wordRect = it.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_WORD)
            val wordText = it.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_WORD)
            cachedRegions!!.forEachIndexed { index, rect ->
                if (Rect.intersects(wordRect, rect)) {
                    list[index] += wordText
                    return@forEachIndexed
                }
            }
        } while (it.next(TessBaseAPI.PageIteratorLevel.RIL_WORD))
        it.delete()
        cachedTextRegions = list
        return list
    }

    /**
     * Destroy the Tesseract BaseAPI
     * @see TessBaseAPI
     */
    fun destroy() {
        baseAPI.end()
    }
}