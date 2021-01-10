package com.lazykernel.korurureader

import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.json.JSONArray
import org.json.JSONObject
import se.fekete.furiganatextview.furiganaview.FuriganaTextView
import java.util.*
import kotlin.collections.ArrayList

/**
 * [RecyclerView.Adapter] that can display a response from jisho.
 */
class DictionaryRecyclerViewAdapter(
    private val values: JSONArray
) : RecyclerView.Adapter<DictionaryRecyclerViewAdapter.ViewHolder>() {

    private val kanaPattern = Regex("[\\u3041-\\u309e\\uff66-\\uff9d\\u30a1-\\u30fe]+")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_dictionary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values.getJSONObject(position)
        val readings = item.getJSONArray("japanese")
        val firstReading = readings.getJSONObject(0)

        // TODO: Move these to datasource class
        holder.slugView.setFuriganaText(parseReading(firstReading))
        holder.commonTagView.visibility = parseCommon(item)
        val jlptInfo = parseJLPT(item.getJSONArray("jlpt"))
        holder.jlptTagView.visibility = jlptInfo.first
        holder.jlptTagView.text = jlptInfo.second
        holder.sensesView.text = Html.fromHtml(parseSenses(item.getJSONArray("senses")), Html.FROM_HTML_MODE_COMPACT)
        holder.sensesView.movementMethod = LinkMovementMethod.getInstance()
        holder.otherFormsView.text = parseOtherForms(readings)
        holder.jishoDetailsView.text = Html.fromHtml(getJishoText(item.getString("slug")), Html.FROM_HTML_MODE_COMPACT)
        holder.jishoDetailsView.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun getItemCount(): Int = values.length()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val slugView: FuriganaTextView = view.findViewById(R.id.dictSlug)
        val commonTagView: TextView = view.findViewById(R.id.commonTag)
        val jlptTagView: TextView = view.findViewById(R.id.jlptTag)
        val sensesView: TextView = view.findViewById(R.id.dictSenses)
        val otherFormsView: TextView = view.findViewById(R.id.dictOtherForms)
        val jishoDetailsView: TextView = view.findViewById(R.id.jishoDetailsLink)

        override fun toString(): String {
            return super.toString() + " '" + slugView.text + "'"
        }
    }

    private fun parseReading(firstReading: JSONObject): String {
        // Some entries might not have readings
        if (!firstReading.has("reading")) {
            return firstReading.getString("word")
        }

        // If first reading contains the key word, we know that the slug
        // has kanji in it, otherwise it's just kana
        if (firstReading.has("word")) {
            // First reading is usually the slug, although for some entries, the slug is just a
            // token string
            val word = firstReading.getString("word")
            val reading = firstReading.getString("reading")
            val wordParts: ArrayList<Pair<String, String>> = arrayListOf()
            val matches = kanaPattern.findAll(word)
            // Simple replace if the word contains no kana
            if (matches.count() == 0) {
                wordParts.add(Pair(word, reading))
            }
            else {
                // Create a new regex for selecting kana from reading corresponding to kanji in word
                var maskPattern = ""
                var lastEnd = 0

                matches.forEach { match ->
                    if (match.range.first == 0) {
                        maskPattern = match.value
                        wordParts.add(Pair(match.value, match.value))
                        lastEnd = match.range.last + 1
                    }
                    else {
                        // we want greedy searching
                        maskPattern += "(.*)" + match.value
                        wordParts.add(Pair(word.substring(lastEnd, match.range.first), ""))
                        wordParts.add(Pair(match.value, match.value))
                        lastEnd = match.range.last + 1
                    }
                }

                println(maskPattern)
                val maskMatch = Regex(maskPattern).find(reading)
                var i = 0
                // If we have a match, fix word parts array, else return simple substitution
                if (maskMatch != null) {
                    maskMatch.groupValues.forEachIndexed { idx, match ->
                        // skip first element, which is the entire string
                        if (idx > 0) {
                            println(match)
                            while (i < wordParts.size && wordParts[i].second.isNotEmpty()) {
                                i++
                            }

                            if (i >= wordParts.size)
                                return@forEachIndexed

                            wordParts[i] = Pair(wordParts[i].first, match)
                        }
                    }
                }
                else {
                    wordParts.clear()
                    wordParts.add(Pair(word, reading))
                }
            }

            return wordParts.joinToString("") { pair ->
                // if the unicode scripts aren't the same, we have kanji in first and its reading in second
                if (Character.UnicodeScript.of(pair.first[0].toInt()) != Character.UnicodeScript.of(pair.second[0].toInt())) {
                    "<ruby>${pair.first}<rt>${pair.second}</rt></ruby>"
                }
                else {
                    pair.first
                }
            }
        }
        else {
            return firstReading.getString("reading")
        }
    }

    private fun parseCommon(item: JSONObject): Int {
        if (!item.has("is_common")) {
            return View.GONE
        }

        return if (item.getBoolean("is_common")) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun parseJLPT(jlptArray: JSONArray): Pair<Int, String> {
        return if (jlptArray.length() == 0) {
            Pair(View.GONE, "")
        } else {
            Pair(View.VISIBLE, jlptArray.getString(0).toUpperCase(Locale.getDefault()).replace('-', ' '))
        }
    }

    private fun parseSenses(senses: JSONArray): String {
        var text = "<ol>"

        for (i in 0 until senses.length()) {
            val sense = senses.getJSONObject(i)
            // definitions and pos should always exist, others might be empty
            val definitions = sense.getJSONArray("english_definitions")
            val pos = sense.getJSONArray("parts_of_speech")
            val restrictions = sense.getJSONArray("restrictions")
            val seeAlso = sense.getJSONArray("see_also")

            // JSONArray.join quotes strings which is exactly what we don't want
            text += joinJSONArray(pos, ", ")
            text += "<li>" + joinJSONArray(definitions, "; ")

            if (restrictions.length() > 0) {
                text += " Only applies to " + joinJSONArray(restrictions,", ")
            }

            if (seeAlso.length() > 0) {
                text += " See also " + joinJSONArray(seeAlso, ", ", ::getJishoSearch)
            }
            text += "</li>"
        }

        text += "</ol>"
        return text
    }

    private fun parseOtherForms(japanese: JSONArray): String {
        val otherForms: ArrayList<String> = arrayListOf()
        // There should always be at least one japanese element
        for (i in 1 until japanese.length()) {
            val word = japanese.getJSONObject(i)
            // There should always be a word element in the object since these are different kanji
            // forms, but you never know with japanese
            if (word.has("word")) {
                otherForms.add("${word.getString("word")} 【${word.getString("reading")}】")
            }
            else {
                otherForms.add(word.getString("reading"))
            }
        }

        if (otherForms.isEmpty()) {
            return ""
        }

        return "Other forms\n${otherForms.joinToString("、")}"
    }

    private fun getJishoText(word: String): String = "<a href=\"https://jisho.org/word/${word}\">${MainActivity.context.getString(R.string.more_on_jisho)}</a>"
    private fun getJishoSearch(word: String): String = "<a href=\"https://jisho.org/search/${word}\">${word}</a>"

    private fun joinJSONArray(array: JSONArray, separator: String? = "", transform: ((str: String) -> String)? = null): String {
        var text = ""
        for (i in 0 until array.length()) {
            text += if (transform != null) {
                transform(array.getString(i))
            } else {
                array.getString(i)
            }
            if (i != array.length() - 1) {
                text += separator
            }
        }
        return text
    }
}