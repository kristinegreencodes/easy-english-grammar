package com.easyenglishgrammar.app.data

import androidx.compose.ui.graphics.Color

/** One line of the manga-style story: a cast member speaking. */
data class StoryPanel(val who: String, val line: String)

/** A building block of the rule (e.g. "subject", "-ed ending"). */
data class RulePart(val name: String, val detail: String, val example: String)

data class Example(val good: String, val bad: String?, val note: String)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val answer: Int,
    val why: String,
)

data class GrammarRule(
    val id: String,
    val categoryId: String,
    val title: String,
    val tagline: String,
    val story: List<StoryPanel>,
    val parts: List<RulePart>,
    val history: String,
    val examples: List<Example>,
    val trick: String,
    val quiz: List<QuizQuestion>,
)

data class Category(
    val id: String,
    val order: Int,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val color: Color,
    val colorDark: Color,
    val intro: String,
    val rules: List<GrammarRule>,
)

/** A recurring member of the story cast. */
data class CastMember(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val role: String,
)

object Cast {
    val members: Map<String, CastMember> = listOf(
        CastMember("sensei", "Sensei Fukurō", "🦉", Color(0xFFB388FF), "The wise owl who guards the Grammar Shrine"),
        CastMember("hana", "Hana", "🌸", Color(0xFFFF7EB6), "A curious student on her quest to master English"),
        CastMember("kaito", "Kaito", "⚔️", Color(0xFF64B5F6), "A bold swordsman who charges in and makes mistakes"),
        CastMember("yumi", "Yumi", "🦊", Color(0xFFFFB74D), "A kitsune trickster who loves confusing words"),
        CastMember("chrono", "Chrono", "⏳", Color(0xFF4DD0E1), "The time wizard who rules the tenses"),
        CastMember("kage", "Kage", "🥷", Color(0xFF90A4AE), "Leader of the Punctuation Ninja Clan"),
        CastMember("narrator", "Narrator", "📜", Color(0xFFFFE082), "Tells the tale"),
    ).associateBy { it.id }

    fun get(id: String): CastMember =
        members[id] ?: CastMember(id, id.replaceFirstChar { it.uppercase() }, "✨", Color(0xFFE1BEE7), "")
}
