package com.lazykernel.korurureader

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.nio.Buffer

class ImageViewModel : ViewModel() {
    private val mutableImage = MutableLiveData<Uri>()
    val selectedImage: LiveData<Uri> get() = mutableImage

    private val mutableText = MutableLiveData<String>()
    val parsedText: LiveData<String> get() = mutableText

    private val mutableRegions = MutableLiveData<ArrayList<Rect>>()
    val textRegions: LiveData<ArrayList<Rect>> get() = mutableRegions

    private val mutableTempBitmap = MutableLiveData<Bitmap>()
    val selectedTempBitmap: LiveData<Bitmap> get() = mutableTempBitmap

    private val mutableSearchEntry = MutableLiveData<String>()
    val searchEntry: LiveData<String> get() = mutableSearchEntry

    fun setImage(uri: Uri) {
        mutableImage.value = uri
    }

    fun setParsedText(string: String) {
        mutableText.value = string
    }

    fun setTextRegions(regions: ArrayList<Rect>) {
        mutableRegions.value = regions
    }

    fun selectTempImage(img: Bitmap) {
        mutableTempBitmap.value = img
    }

    fun setSearchEntry(search: String) {
        mutableSearchEntry.value = search
    }
}