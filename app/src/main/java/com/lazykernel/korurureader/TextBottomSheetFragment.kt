package com.lazykernel.korurureader

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.worksap.nlp.sudachi.Morpheme

/**
 * A simple [Fragment] subclass.
 */
class TextBottomSheetFragment(private val word: Morpheme) : BottomSheetDialogFragment() {

    private val viewModel: ImageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_text_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // word.surface() original
        // word.dictionaryForm() for searching
        val textView: TextView = view.findViewById(R.id.bottomSheetWordInfo)
        textView.text = getString(R.string.word_and_reading, word.normalizedForm(), word.readingForm())

        view.findViewById<Button>(R.id.jishoQuickSearchButton).setOnClickListener {
            viewModel.setSearchEntry(word.dictionaryForm())
            findNavController().navigate(R.id.action_SecondFragment_to_dictionaryFragment)
            dismiss()
        }
    }
}