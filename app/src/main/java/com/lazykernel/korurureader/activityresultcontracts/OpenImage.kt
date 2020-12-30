package com.lazykernel.korurureader.activityresultcontracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class OpenImage : ActivityResultContract<Void?, Uri>() {
    override fun createIntent(context: Context, input: Void?): Intent {
        val getIntent: Intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        val pickIntent: Intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).setType("image/*")
        return Intent.createChooser(getIntent, "Select image").putExtra(Intent.EXTRA_INITIAL_INTENTS, Array(1) { pickIntent })
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if (intent == null || resultCode != Activity.RESULT_OK) null else intent.data
    }
}