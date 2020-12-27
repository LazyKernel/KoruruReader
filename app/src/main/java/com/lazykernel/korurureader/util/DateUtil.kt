package com.lazykernel.korurureader.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

class DateUtil {
    companion object {
        val instance = DateUtil()
    }

    val stringTimestamp: String
        @SuppressLint("SimpleDateFormat")
        get() = SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSS").format(Date())
}