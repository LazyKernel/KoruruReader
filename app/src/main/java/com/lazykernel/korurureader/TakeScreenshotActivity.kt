package com.lazykernel.korurureader

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.result.launch
import androidx.core.net.toUri
import com.lazykernel.korurureader.activityresultcontracts.ScreenCapture
import com.lazykernel.korurureader.util.DateUtil
import com.lazykernel.korurureader.util.FileUtil
import java.io.FileOutputStream

class TakeScreenshotActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screenShot = registerForActivityResult(ScreenCapture()) { proj: MediaProjection? ->
            if (proj == null) {
                Toast.makeText(this@TakeScreenshotActivity, "Could not take a screenshot", Toast.LENGTH_SHORT).show()
            }
            else {
                setupVirtualDisplay(proj)
            }
        }
        screenShot.launch()
    }

    private fun setupVirtualDisplay(mediaProjection: MediaProjection) {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val windowHeight = metrics.heightPixels
        val windowWidth = metrics.widthPixels
        val density = metrics.densityDpi
        val handler = Handler(Looper.myLooper()!!)
        val imageReader = ImageReader.newInstance(windowWidth, windowHeight, ImageFormat.FLEX_RGBA_8888, 2)
        val virtualDisplay = mediaProjection.createVirtualDisplay("KoruruScreenShotVDisplay",
                windowWidth,
                windowHeight,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                imageReader.surface,
                null,
                handler
        )
        imageReader.setOnImageAvailableListener({ reader ->
            // Stop the media projection
            mediaProjection.stop()
            // Save to image to a file
            val image = imageReader.acquireLatestImage()
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * windowWidth
            val bitmap = Bitmap.createBitmap(windowWidth + rowPadding / pixelStride, windowHeight, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            val file = FileUtil.instance.createTempFile("tmpimg_${DateUtil.instance.stringTimestamp}_", ".jpg")
            val outputStream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
            // Start main activity
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("imageUri", file.toUri())
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }, handler)
    }
}