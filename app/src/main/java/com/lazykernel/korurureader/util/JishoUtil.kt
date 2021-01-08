package com.lazykernel.korurureader.util

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.lazykernel.korurureader.MainActivity
import org.json.JSONObject

class JishoUtil {
    companion object {
        val instance = JishoUtil()
    }

    private var queue: RequestQueue? = null
    private val baseUrl = "https://jisho.org/api/v1/search/words?keyword="

    init {
        queue = Volley.newRequestQueue(MainActivity.context)
    }

    fun getJishoEntries(search: String, listener: Response.Listener<JSONObject>, errorListener: Response.ErrorListener?) {
        val jsonRequest = JsonObjectRequest(Request.Method.GET, baseUrl + search, null,
            listener, errorListener
        )
        queue?.add(jsonRequest)
    }
}