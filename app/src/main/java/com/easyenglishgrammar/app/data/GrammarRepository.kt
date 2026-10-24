package com.easyenglishgrammar.app.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads every category from the JSON files in assets/grammar. Adding a new rule or a
 * whole new category is just a matter of editing or adding a JSON file.
 */
class GrammarRepository(context: Context) {

    val categories: List<Category> = loadAll(context)

    private val rulesById: Map<String, GrammarRule> =
        categories.flatMap { it.rules }.associateBy { it.id }

    val allRules: List<GrammarRule> get() = categories.flatMap { it.rules }

    fun category(id: String): Category? = categories.firstOrNull { it.id == id }

    fun rule(id: String): GrammarRule? = rulesById[id]

    fun search(query: String): List<GrammarRule> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        return allRules.filter {
            it.title.lowercase().contains(q) || it.tagline.lowercase().contains(q)
        }
    }

    private fun loadAll(context: Context): List<Category> {
        val assets = context.assets
        val files = assets.list("grammar").orEmpty().filter { it.endsWith(".json") }
        return files.map { name ->
            val text = assets.open("grammar/$name").bufferedReader().use { it.readText() }
            parseCategory(JSONObject(text))
        }.sortedBy { it.order }
    }

    private fun parseCategory(o: JSONObject): Category {
        val id = o.getString("id")
        return Category(
            id = id,
            order = o.optInt("order", 99),
            title = o.getString("title"),
            subtitle = o.optString("subtitle"),
            emoji = o.optString("emoji", "📖"),
            color = parseColor(o.optString("color", "#FF7EB6")),
            colorDark = parseColor(o.optString("colorDark", "#5E35B1")),
            intro = o.optString("intro"),
            rules = o.getJSONArray("rules").objects().map { parseRule(it, id) },
        )
    }

    private fun parseRule(o: JSONObject, categoryId: String) = GrammarRule(
        id = o.getString("id"),
        categoryId = categoryId,
        title = o.getString("title"),
        tagline = o.optString("tagline"),
        story = o.optJSONArray("story").objects().map {
            StoryPanel(it.getString("who"), it.getString("line"))
        },
        parts = o.optJSONArray("parts").objects().map {
            RulePart(it.getString("name"), it.getString("detail"), it.optString("example"))
        },
        history = o.optString("history"),
        examples = o.optJSONArray("examples").objects().map {
            Example(
                good = it.getString("good"),
                bad = it.optString("bad").ifBlank { null },
                note = it.optString("note"),
            )
        },
        trick = o.optString("trick"),
        quiz = o.optJSONArray("quiz").objects().map {
            QuizQuestion(
                question = it.getString("q"),
                options = it.getJSONArray("options").strings(),
                answer = it.getInt("answer"),
                why = it.optString("why"),
            )
        },
    )

    private fun parseColor(hex: String): Color =
        Color(android.graphics.Color.parseColor(hex))

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else List(length()) { getJSONObject(it) }

    private fun JSONArray.strings(): List<String> = List(length()) { getString(it) }
}
