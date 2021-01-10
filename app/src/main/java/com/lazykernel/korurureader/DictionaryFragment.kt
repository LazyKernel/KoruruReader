package com.lazykernel.korurureader

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.android.volley.Response
import com.lazykernel.korurureader.util.JishoUtil
import org.json.JSONArray

/**
 * A fragment representing a list of Items.
 */
class DictionaryFragment : Fragment() {

    private val viewModel: ImageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dictionary_list, container, false)

        viewModel.searchEntry.observe(viewLifecycleOwner, Observer { text ->
            JishoUtil.instance.getJishoEntries(text, Response.Listener { json ->
                if (view is RecyclerView) {
                    with(view) {
                        adapter = DictionaryRecyclerViewAdapter(json.getJSONArray("data"))
                    }
                }
            },
            Response.ErrorListener { error ->
                println(error.message)
            })
        })

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = DictionaryRecyclerViewAdapter(JSONArray())
            }
        }
        return view
    }
}