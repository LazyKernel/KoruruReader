package com.lazykernel.korurureader.util

import com.lazykernel.korurureader.MainActivity
import com.worksap.nlp.sudachi.DictionaryFactory
import com.worksap.nlp.sudachi.Morpheme
import com.worksap.nlp.sudachi.Tokenizer

class NLPUtil {
    private val SUDACHI_DATA_PATH = MainActivity.context.filesDir.absolutePath + "/sudachi"
    private val settings = """
        {
            "systemDict" : "${SUDACHI_DATA_PATH}/system_small.dic",
            "characterDefinitionFile" : "${SUDACHI_DATA_PATH}/char.def",
            "inputTextPlugin" : [
              { "class" : "com.worksap.nlp.sudachi.DefaultInputTextPlugin" },
              { "class" : "com.worksap.nlp.sudachi.ProlongedSoundMarkInputTextPlugin",
                  "prolongedSoundMarks": ["ー", "〜", "〰"],
                  "replacementSymbol": "ー"}
            ],
            "oovProviderPlugin" : [
                { "class" : "com.worksap.nlp.sudachi.SimpleOovProviderPlugin",
                  "oovPOS" : [ "名詞", "普通名詞", "一般", "*", "*", "*" ],
                  "leftId" : 8,
                  "rightId" : 8,
                  "cost" : 6000 }
            ],
            "formatterPlugin" : [
              { "class" : "com.worksap.nlp.sudachi.SimpleMorphemeFormatter" }
            ]
        }
    """.trimIndent()

    private var tokenizer: Tokenizer? = null
    init {
        // Load language files from asset packs
        FileUtil.instance.copyAssetToFilesIfNotExist("sudachi/", "system_small.dic")
        FileUtil.instance.copyAssetToFilesIfNotExist("sudachi/", "char.def")
        val dict = DictionaryFactory().create(settings)
        tokenizer = dict.create()
    }

    fun tokenizeString(str: String): Iterable<List<Morpheme>> {
        return tokenizer!!.tokenizeSentences(str)
    }
}