package com.lazykernel.korurureader.util

import android.util.Xml
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.lazykernel.korurureader.MainActivity
import com.lazykernel.korurureader.structures.DictionaryEntry
import com.lazykernel.korurureader.structures.KanjiElement
import com.lazykernel.korurureader.structures.ReadingElement
import com.lazykernel.korurureader.structures.SenseElement
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream

class DictionaryUtil {
    companion object {
        val instance = DictionaryUtil()
    }

    private val dictionaryPath = MainActivity.context.filesDir.absolutePath + "/dictionary/JMdict_e.xml"
    private val dictionary: MutableMap<String, ArrayList<Long>> = mutableMapOf()
    private val attributes: MutableMap<String, String> = mutableMapOf()

    init {
        //FileUtil.instance.copyAssetToFilesIfNotExist("dictionary/", "JMdict_e.xml")

        // Load dictionary file from asset packs
        prepareDictionary()
        prepareAttributes()
    }

    private fun prepareAttributes() {
        val inputStream: InputStream = MainActivity.context.assets.open("entities.json")
        inputStream.use { stream ->
            val attributesObj = Parser.default().parse(stream) as JsonObject
            attributesObj.forEach { t, u -> attributes[t] = u.toString() }
        }
        inputStream.close()
    }

    private fun prepareDictionary() {
        val inputStream = MainActivity.context.assets.open("JMdict_e.xml")
        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
            parser.setInput(stream, "utf-8")
            var eventType = parser.eventType
            var currentTag = ""
            var currentEntry = DictionaryEntry()

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = parser.name
                    }
                    XmlPullParser.TEXT -> parseEntry(currentTag, parser.text, currentEntry)
                    XmlPullParser.END_TAG -> {
                        currentTag = ""
                        if (parser.name == "entry") {
                            insertEntryToDictionary(currentEntry, 0)
                            currentEntry = DictionaryEntry()
                        }
                    }
                }

                eventType = parser.next()
            }
        }
        inputStream.close()
    }

    fun getDictionaryEntries(search: String): ArrayList<DictionaryEntry> {
        val dictionaryEntries: ArrayList<DictionaryEntry> = arrayListOf()

        if (!dictionary.containsKey(search))
            return dictionaryEntries

        val inputStream: InputStream = MainActivity.context.assets.open("JMdict_e.xml")

        inputStream.use { stream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL, false)
            parser.setInput(stream, "utf-8")
            var eventType = parser.eventType
            var currentTag = ""
            var currentEntry = DictionaryEntry()

            for (pos in dictionary[search]!!) {
                stream.skip(pos)
                loop@ while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            currentTag = parser.name
                            // We can only get attributes in START_TAG event
                            when (currentTag) {
                                "lsource" -> {
                                    currentEntry.senseElements.last().languageSource.add(SenseElement.LanguageSource())
                                    for (i in 0 until parser.attributeCount) {
                                        parseEntry("lsource" + parser.getAttributeName(i), parser.getAttributeValue(i), currentEntry)
                                    }
                                }
                                "gloss" -> {
                                    currentEntry.senseElements.last().glosses.add(SenseElement.Gloss())
                                    for (i in 0 until parser.attributeCount) {
                                        parseEntry("gloss" + parser.getAttributeName(i), parser.getAttributeValue(i), currentEntry)
                                    }
                                }
                            }
                        }
                        XmlPullParser.TEXT -> parseEntry(currentTag, parser.text, currentEntry)
                        XmlPullParser.END_TAG -> {
                            currentTag = ""
                            if (parser.name == "entry") {
                                dictionaryEntries.add(currentEntry)
                                currentEntry = DictionaryEntry()
                                break@loop
                            }
                        }
                    }

                    eventType = parser.next()
                }
            }
        }

        inputStream.close()
        return dictionaryEntries
    }

    private fun parseEntry(tag: String, text: String, entry: DictionaryEntry) {
        if (tag.isEmpty())
            return

        when (tag) {
            "ent_seq"           -> entry.entrySequence = text.toInt()
            "k_ele"             -> entry.kanjiElements.add(KanjiElement())
            "r_ele"             -> entry.readingElements.add(ReadingElement())
            "sense"             -> entry.senseElements.add(SenseElement())
            // Kanji Element
            "keb"               -> entry.kanjiElements.last().block = text
            "ke_inf"            -> entry.kanjiElements.last().information.add(text)
            "ke_pri"            -> entry.kanjiElements.last().priority.add(text)
            // Reading Element
            "reb"               -> entry.readingElements.last().block = text
            "re_nokanji"        -> entry.readingElements.last().noKanji = text
            "re_restr"          -> entry.readingElements.last().restricted.add(text)
            "re_inf"            -> entry.readingElements.last().information.add(text)
            "re_pri"            -> entry.readingElements.last().priority.add(text)
            // Sense Element
            "stagk"             -> entry.senseElements.last().tagKanji.add(text)
            "stagr"             -> entry.senseElements.last().tagReading.add(text)
            "xref"              -> entry.senseElements.last().crossReference.add(text)
            "ant"               -> entry.senseElements.last().antonym.add(text)
            "pos"               -> entry.senseElements.last().partOfSpeech.add(text)
            "field"             -> entry.senseElements.last().field.add(text)
            "misc"              -> entry.senseElements.last().misc.add(text)
            "lsource"           -> entry.senseElements.last().languageSource.last().word = text
            "lsourcexml:lang"   -> entry.senseElements.last().languageSource.last().language = text
            "lsourcels_type"    -> entry.senseElements.last().languageSource.last().type = text
            "lsourcels_wasei"   -> entry.senseElements.last().languageSource.last().wasei = text == "y"
            "dial"              -> entry.senseElements.last().dialect.add(text)
            "gloss"             -> entry.senseElements.last().glosses.last().gloss = text
            "glossxml:lang"     -> entry.senseElements.last().glosses.last().language = text
            "glossg_gend"       -> entry.senseElements.last().glosses.last().gender = text
            "s_inf"             -> entry.senseElements.last().information.add(text)
        }
    }

    private fun insertEntryToDictionary(entry: DictionaryEntry, offset: Long) {
        fun insertInto(key: String) {
            if (dictionary.containsKey(key)) {
                dictionary[key]!!.add(offset)
            }
            else {
                dictionary[key] = arrayListOf(offset)
            }
        }

        entry.kanjiElements.forEach { elem -> insertInto(elem.block) }
        entry.readingElements.forEach { elem -> insertInto(elem.block) }
    }
}