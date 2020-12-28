package com.lazykernel.korurureader

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    private val mutableImage = MutableLiveData<Uri>()
    val selectedImage: LiveData<Uri> get() = mutableImage

    fun selectImage(uri: Uri) {
        mutableImage.value = uri
    }
}