package com.easyenglishgrammar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyenglishgrammar.app.audio.LocalAudio
import com.easyenglishgrammar.app.data.Cast
import com.easyenglishgrammar.app.data.IrregularVerb
import com.easyenglishgrammar.app.data.ReferenceRepository
import com.easyenglishgrammar.app.ui.components.AnimeSky
import com.easyenglishgrammar.app.ui.components.BackBar
import com.easyenglishgrammar.app.ui.components.FilterPill
import com.easyenglishgrammar.app.ui.components.IntroBanner
import com.easyenglishgrammar.app.ui.components.SearchField
import com.easyenglishgrammar.app.ui.theme.Gold
import com.easyenglishgrammar.app.ui.theme.Mint
import com.easyenglishgrammar.app.ui.theme.Sakura
import com.easyenglishgrammar.app.ui.theme.Sky

private val VerbColor = Color(0xFF4DD0E1)
private val PluralColor = Color(0xFFFFB74D)

private data class PatternFilter(val code: String?, val label: String, val hint: String)

private val patterns = listOf(
    PatternFilter(null, "All", "Every rebel verb, A to Z."),
    PatternFilter("AAA", "A-A-A", "All three forms are the same: cut – cut – cut."),
    PatternFilter("ABB", "A-B-B", "Past and participle match: buy – bought – bought."),
    PatternFilter("ABA", "A-B-A", "Participle returns to the base: come – came – come."),
    PatternFilter("ABC", "A-B-C", "Three different forms: go – went – gone."),
)

@Composable
fun IrregularVerbsScreen(ref: ReferenceRepository, onBack: () -> Unit) {
    val voices = LocalAudio.current.voices
    val speaking by voices.speakingKey
    var query by rememberSaveable { mutableStateOf("") }
    var pattern by rememberSaveable { mutableStateOf<String?>(null) }
    DisposableEffect(Unit) { onDispose { voices.stop() } }

    val q = query.trim().lowercase()
    val shown = ref.verbs.filter { v ->
        (pattern == null || v.pattern == pattern) &&
            (q.isEmpty() || listOf(v.base, v.past, v.participle, v.meaning).any { it.lowercase().contains(q) })
    }

    AnimeSky {
        Column(Modifier.fillMaxSize()) {
            BackBar("Training Grounds", onBack)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    IntroBanner(
                        Cast.get("chrono"),
                        "⏳ Scroll of Rebel Verbs",
                        "These ${ref.verbs.size} verbs refuse to wear -ed. Learn their three forms: base, past, and past participle (used after have / has / had). Tap any verb and I'll say it for you.",
                        listOf(Color(0xFF1A237E), VerbColor),
                    )
                }
                item { SearchField(query, { query = it }, "Search a verb… (try \"go\" or \"swim\")") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(patterns) { p ->
                            FilterPill(p.label, pattern == p.code, VerbColor) { pattern = p.code }
                        }
                    }
                }
                item {
                    Text(
                        patterns.first { it.code == pattern }.hint + "  (${shown.size})",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                    )
                }
                item { VerbHeader() }
                items(shown, key = { it.base }) { v ->
                    val key = "verb#${v.base}"
                    VerbRow(v, speaking == key) {
                        voices.speak("chrono", "${v.base}. ${v.past.replace(" / ", " or ")}. ${v.participle.replace(" / ", " or ")}.", key)
                    }
                }
                if (shown.isEmpty()) item { EmptyNote("No verbs match. Try another word.") }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun VerbHeader() {
    Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
        listOf("BASE", "PAST", "PARTICIPLE").forEach {
            Text(it, color = VerbColor, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun VerbRow(v: IrregularVerb, speaking: Boolean, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = if (speaking) 0.16f else 0.08f))
            .border(if (speaking) 2.dp else 0.dp, if (speaking) Gold else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Say it", onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(v.base, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Text(v.past, color = Sakura, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
            Text(v.participle, color = Mint, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1f))
        }
        Text(
            (if (speaking) "🔊 " else "") + v.meaning,
            color = Color.White.copy(alpha = 0.65f),
            fontSize = 12.sp,
        )
    }
}

@Composable
fun IrregularPluralsScreen(ref: ReferenceRepository, onBack: () -> Unit) {
    val voices = LocalAudio.current.voices
    val speaking by voices.speakingKey
    var query by rememberSaveable { mutableStateOf("") }
    DisposableEffect(Unit) { onDispose { voices.stop() } }

    val q = query.trim().lowercase()
    val groups = ref.pluralGroups.mapNotNull { g ->
        val items = g.items.filter { q.isEmpty() || it.singular.lowercase().contains(q) || it.plural.lowercase().contains(q) }
        if (items.isEmpty()) null else g.copy(items = items)
    }
    val total = ref.pluralGroups.sumOf { it.items.size }

    AnimeSky {
        Column(Modifier.fillMaxSize()) {
            BackBar("Training Grounds", onBack)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    IntroBanner(
                        Cast.get("yumi"),
                        "🏮 Scroll of Rebel Plurals",
                        "Hehe, $total sneaky nouns that won't just add -s! One mouse, two mice. Tap a pair and I'll read it to you.",
                        listOf(Color(0xFFBF360C), PluralColor),
                    )
                }
                item { SearchField(query, { query = it }, "Search a noun… (try \"mouse\" or \"crisis\")") }
                groups.forEach { g ->
                    item(key = "title#${g.title}") {
                        Column(Modifier.padding(top = 8.dp)) {
                            Text("✦ ${g.title}", color = PluralColor, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(g.note, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                    }
                    items(g.items, key = { "${g.title}#${it.singular}" }) { pair ->
                        val key = "plural#${pair.singular}"
                        val isSpeaking = speaking == key
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = if (isSpeaking) 0.16f else 0.08f))
                                .border(if (isSpeaking) 2.dp else 0.dp, if (isSpeaking) Gold else Color.Transparent, RoundedCornerShape(16.dp))
                                .clickable(onClickLabel = "Say it") {
                                    val plural = pair.plural.substringBefore(" (")
                                    val line = if (g.title == "Only plural") "$plural." else "One ${pair.singular}. Two $plural."
                                    voices.speak("yumi", line, key)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(pair.singular, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                            Text("→", color = Color.White.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 8.dp))
                            Text(pair.plural, color = Sky, fontWeight = FontWeight.Bold, fontSize = 15.sp, modifier = Modifier.weight(1.3f))
                        }
                    }
                }
                if (groups.isEmpty()) item { EmptyNote("No nouns match. Try another word.") }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}

@Composable
private fun EmptyNote(text: String) {
    Text(
        text,
        color = Color.White.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(24.dp),
    )
}
