package com.lazykernel.korurureader

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_dictionary, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values.toJSONObject(JSONArray().put(position))
        val readings = item.getJSONArray("japanese")
        val firstReading = readings.toJSONObject(JSONArray().put(0))

        holder.slugView.text = parseReading(firstReading)
        holder.commonTagView.visibility = parseCommon(item.getBoolean("is_common"))
        val jlptInfo = parseJLPT(item.getJSONArray("jlpt"))
        holder.jlptTagView.visibility = jlptInfo.first
        holder.jlptTagView.text = jlptInfo.second
        holder.sensesView.text = parseSenses(item.getJSONArray("senses"))
        holder.otherFormsView.text = parseOtherForms(readings)
    }

    override fun getItemCount(): Int = values.length()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val slugView: FuriganaTextView = view.findViewById(R.id.dictSlug)
        val commonTagView: TextView = view.findViewById(R.id.commonTag)
        val jlptTagView: TextView = view.findViewById(R.id.jlptTag)
        val sensesView: TextView = view.findViewById(R.id.dictSenses)
        val otherFormsView: TextView = view.findViewById(R.id.dictOtherForms)

        override fun toString(): String {
            return super.toString() + " '" + slugView.text + "'"
        }
    }

    private fun parseReading(firstReading: JSONObject): String {
        // If first reading contains the key word, we know that the slug
        // has kanji in it, otherwise it's just kana
        if (firstReading.has("word")) {
            // First reading should always be the slug
            val word = firstReading.getString("word")
            val reading = firstReading.getString("reading")
            val wordParts: ArrayList<Pair<String, String>> = arrayListOf()
            wordParts.add(Pair("", ""))
            var lastUnicodeScript: Character.UnicodeScript? = null
            for (i in word.indices) {
                if (lastUnicodeScript == null || Character.UnicodeScript.of(word[i].toInt()) == lastUnicodeScript) {
                    wordParts[wordParts.size - 1] = Pair(wordParts.last().first + word[i], wordParts.last().second + reading[i])
                }
                else {
                    wordParts.add(Pair(word[i].toString(), reading[i].toString()))
                }
                lastUnicodeScript = Character.UnicodeScript.of(word[i].toInt())
            }

            return wordParts.joinToString { pair ->
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

    private fun parseCommon(isCommon: Boolean): Int {
        return if (isCommon) {
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
        // TODO: parse senses
        return ""
    }

    private fun parseOtherForms(japanese: JSONArray): String {
        val otherForms: ArrayList<String> = arrayListOf()
        // There should always be at least one japanese element
        for (i in 1 until japanese.length()) {
            val word = japanese.toJSONObject(JSONArray().put(i))
            // There should always be a word element in the object since these are different kanji
            // forms, but you never know with japanese
            if (word.has("word")) {
                otherForms.add("${word.getString("word")} 【${word.getString("reading")}】")
            }
            else {
                otherForms.add(word.getString("reading"))
            }
        }

        return "Other forms\n${otherForms.joinToString("、")}"
    }
}