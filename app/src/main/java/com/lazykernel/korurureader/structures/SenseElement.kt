package com.lazykernel.korurureader.structures

class SenseElement {
    var tagKanji: ArrayList<String> = ArrayList(0)
    var tagReading: ArrayList<String> = ArrayList(0)
    var crossReference: ArrayList<String> = ArrayList(0)
    var antonym: ArrayList<String> = ArrayList(0)
    var partOfSpeech: ArrayList<String> = ArrayList(0)
    var field: ArrayList<String> = ArrayList(0)
    var misc: ArrayList<String> = ArrayList(0)

    class LanguageSource {
        var word: String = ""
        var language: String = "eng"
        var type: String = "full"
        var wasei: Boolean = false
    }

    var languageSource: ArrayList<LanguageSource> = ArrayList(0)
    var dialect: ArrayList<String> = ArrayList(0)

    class Gloss {
        var gloss: String = ""
        var language: String = "eng"
        var gender: String? = null
    }

    var glosses: ArrayList<Gloss> = ArrayList(0)
    var information: ArrayList<String> = ArrayList(0)
}