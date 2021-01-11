package com.lazykernel.korurureader.activityresultcontracts

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.activity.result.contract.ActivityResultContract
import com.lazykernel.korurureader.MainActivity

class ScreenCapture : ActivityResultContract<Void?, MediaProjection?>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val mediaProjectionManager: MediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return mediaProjectionManager.createScreenCaptureIntent()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): MediaProjection? {
        val mediaProjectionManager: MediaProjectionManager = MainActivity.context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return if (intent == null) return null else mediaProjectionManager.getMediaProjection(resultCode, intent)
    }
}