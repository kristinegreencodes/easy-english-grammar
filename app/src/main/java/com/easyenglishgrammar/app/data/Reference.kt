package com.easyenglishgrammar.app.data

import android.content.Context
import org.json.JSONObject

data class IrregularVerb(
    val base: String,
    val past: String,
    val participle: String,
    val meaning: String,
    /** AAA (cut-cut-cut), ABB (buy-bought-bought), ABA (come-came-come) or ABC (go-went-gone). */
    val pattern: String,
)

data class PluralPair(val singular: String, val plural: String)

data class PluralGroup(val title: String, val note: String, val items: List<PluralPair>)

data class PracticeQuestion(
    val topic: String,
    val question: String,
    val options: List<String>,
    val answer: Int,
    val why: String,
)

/** The verb and plural tables and the professional practice bank, from assets. */
class ReferenceRepository(context: Context) {

    private val assets = context.assets

    val verbs: List<IrregularVerb> by lazy {
        val arr = read("reference/irregular_verbs.json").getJSONArray("verbs")
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            IrregularVerb(
                o.getString("base"), o.getString("past"), o.getString("participle"),
                o.getString("meaning"), o.getString("pattern"),
            )
        }
    }

    val pluralGroups: List<PluralGroup> by lazy {
        val arr = read("reference/irregular_plurals.json").getJSONArray("groups")
        List(arr.length()) { i ->
            val g = arr.getJSONObject(i)
            val items = g.getJSONArray("items")
            PluralGroup(
                g.getString("title"),
                g.getString("note"),
                List(items.length()) { j ->
                    val p = items.getJSONObject(j)
                    PluralPair(p.getString("singular"), p.getString("plural"))
                },
            )
        }
    }

    val practice: List<PracticeQuestion> by lazy {
        val arr = read("practice/professional.json").getJSONArray("questions")
        List(arr.length()) { i ->
            val o = arr.getJSONObject(i)
            val opts = o.getJSONArray("options")
            PracticeQuestion(
                o.getString("topic"), o.getString("q"),
                List(opts.length()) { opts.getString(it) },
                o.getInt("answer"), o.getString("why"),
            )
        }
    }

    val practiceTopics: List<String> get() = practice.map { it.topic }.distinct()

    private fun read(path: String) =
        JSONObject(assets.open(path).bufferedReader().use { it.readText() })
}
