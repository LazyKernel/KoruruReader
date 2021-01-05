package com.lazykernel.korurureader.structures

/**
 * r_ele
 *
 * The reading element typically contains the valid readings
 * of the word(s) in the kanji element using modern kanadzukai.
 * Where there are multiple reading elements, they will typically be
 * alternative readings of the kanji element. In the absence of a
 * kanji element, i.e. in the case of a word or phrase written
 * entirely in kana, these elements will define the entry.
 */
class ReadingElement {
    /**
     * reb
     *
     * this element content is restricted to kana and related
     * characters such as chouon and kurikaeshi. Kana usage will be
     * consistent between the keb and reb elements; e.g. if the keb
     * contains katakana, so too will the reb.
     */
    var block: String = ""

    /**
     * re_nokanji
     *
     * This element, which will usually have a null value, indicates
     * that the reb, while associated with the keb, cannot be regarded
     * as a true reading of the kanji. It is typically used for words
     * such as foreign place names, gairaigo which can be in kanji or
     * katakana, etc.
     */
    var noKanji: String? = null

    /**
     * re_restr
     *
     * This element is used to indicate when the reading only applies
     * to a subset of the keb elements in the entry. In its absence, all
     * readings apply to all kanji elements. The contents of this element
     * must exactly match those of one of the keb elements.
     */
    var restricted: ArrayList<String> = ArrayList(0)

    /**
     * re_inf
     *
     * General coded information pertaining to the specific reading.
     * Typically it will be used to indicate some unusual aspect of
     * the reading.
     */
    var information: ArrayList<String> = ArrayList(0)

    /**
     * re_pri
     *
     * See [KanjiElement.priority] for more details
     *
     * @see KanjiElement.priority
     */
    var priority: ArrayList<String> = ArrayList(0)
}