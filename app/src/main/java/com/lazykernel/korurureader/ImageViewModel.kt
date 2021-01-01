package com.lazykernel.korurureader

import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    private val mutableImage = MutableLiveData<Uri>()
    val selectedImage: LiveData<Uri> get() = mutableImage

    private val mutableText = MutableLiveData<String>()
    val selectedText: LiveData<String> get() = mutableText

    private val mutableRegions = MutableLiveData<ArrayList<Rect>>()
    val selectedRegions: LiveData<ArrayList<Rect>> get() = mutableRegions

    fun selectImage(uri: Uri) {
        mutableImage.value = uri
    }

    fun selectText(string: String) {
        mutableText.value = string
    }

    fun selectRegions(regions: ArrayList<Rect>) {
        mutableRegions.value = regions
    }
}