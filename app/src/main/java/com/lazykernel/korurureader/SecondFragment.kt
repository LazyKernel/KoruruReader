package com.lazykernel.korurureader

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.lazykernel.korurureader.util.NLPUtil
import com.worksap.nlp.sudachi.Morpheme

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private val viewModel: ImageViewModel by activityViewModels()
    private var bottomSheetFragment: TextBottomSheetFragment? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.parsedText.observe(viewLifecycleOwner, Observer { text ->
            val textView: TextView = view.findViewById(R.id.textview_second)
            val morphemes: Iterable<List<Morpheme>> = NLPUtil().tokenizeString(text)
            val spanString = SpannableStringBuilder()

            morphemes.forEach { list ->
                list.forEach { word ->
                    // Ignore blanks (newlines etc.)
                    if (word.surface().isBlank()) {
                        spanString.append(word.surface())
                    }
                    else {
                        spanString.append(word.surface(), object: ClickableSpan() {
                            override fun onClick(widget: View) {
                                bottomSheetFragment = TextBottomSheetFragment(word)
                                bottomSheetFragment?.show(parentFragmentManager, "ParsedTextModalBottomSheet")
                            }
                        }, 0)
                    }
                }
            }

            textView.movementMethod = LinkMovementMethod.getInstance()
            textView.setText(spanString, TextView.BufferType.SPANNABLE)
        })

        view.findViewById<Button>(R.id.button_second).setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }
}