package com.lazykernel.korurureader

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.lazykernel.korurureader.util.FileUtil

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val viewModel: ImageViewModel by activityViewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.selectedTempBitmap.observe(viewLifecycleOwner, Observer { img ->
            val imageView: ImageView = view.findViewById(R.id.testImageView)
            imageView.setImageBitmap(img)
        })

//        viewModel.selectedImage.observe(viewLifecycleOwner, Observer { uri ->
//            val imageView: ImageView = view.findViewById(R.id.testImageView)
//            imageView.setImageBitmap(FileUtil.instance.loadUriToBitmap(uri))
//        })
        viewModel.textRegions.observe(viewLifecycleOwner, Observer { regions ->
            val imageView: ImageView = view.findViewById(R.id.testImageView)
            viewModel.selectedImage.value?.let {
                val bitmap = FileUtil.instance.loadUriToBitmap(it)
                val canvas = Canvas(bitmap)
                val paint = Paint().apply {
                    color = Color.CYAN
                    style = Paint.Style.STROKE
                }
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                regions.forEach { rect -> canvas.drawRect(rect, paint) }
                imageView.setImageBitmap(bitmap)
            }
        })
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }
}