package com.lazykernel.korurureader.structures

/**
 * Entries consist of kanji elements, reading elements,
 * general information and sense elements. Each entry must have at
 * least one reading element and one sense element. Others are optional.
 *
 * Comments copied from JMDict documentation from https://www.edrdg.org/jmdict/jmdict_dtd_h.html
 *
 * Initial capacity of 0 indicates that there are 0 or more elements present in JMDict
 * and 1 that there are 1 or more elements
 */
class DictionaryEntry {
    /**
     * ent_seq
     *
     * A unique numeric sequence number for each entry
     */
    var entrySequence: Int = 0

    var kanjiElements: ArrayList<KanjiElement> = ArrayList(0)
    var readingElements: ArrayList<ReadingElement> = ArrayList(1)
    var senseElements: ArrayList<SenseElement> = ArrayList(1)








}