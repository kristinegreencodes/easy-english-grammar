package com.easyenglishgrammar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyenglishgrammar.app.data.Cast
import com.easyenglishgrammar.app.data.PracticeQuestion
import com.easyenglishgrammar.app.data.ProgressStore
import com.easyenglishgrammar.app.data.ReferenceRepository
import com.easyenglishgrammar.app.ui.components.AnimeSky
import com.easyenglishgrammar.app.ui.components.BackBar
import com.easyenglishgrammar.app.ui.components.FilterPill
import com.easyenglishgrammar.app.ui.components.IntroBanner
import com.easyenglishgrammar.app.ui.components.ProgressBar
import com.easyenglishgrammar.app.ui.theme.Coral
import com.easyenglishgrammar.app.ui.theme.Gold
import com.easyenglishgrammar.app.ui.theme.Ink
import com.easyenglishgrammar.app.ui.theme.Mint

private val DojoColor = Color(0xFF80CBC4)
private const val ALL = "All"

/** One question in a test, with its options in shuffled display order. */
private class TestItem(val question: PracticeQuestion, val order: List<Int>) {
    var chosen by mutableStateOf<Int?>(null)
    val correct get() = chosen == question.answer
}

private data class Rank(val title: String, val emoji: String, val line: String)

private fun rankFor(percent: Int) = when {
    percent >= 90 -> Rank("Grammar Kage", "👑", "Flawless technique. You write like a seasoned editor.")
    percent >= 75 -> Rank("Jōnin", "🥷", "Elite level. A few more drills and you'll be unstoppable.")
    percent >= 50 -> Rank("Chūnin", "⚔️", "Solid skills. Review the scrolls below and strike again.")
    else -> Rank("Genin", "🌱", "Every master was once a beginner. Study the explanations and retry.")
}

@Composable
fun PracticeScreen(ref: ReferenceRepository, progress: ProgressStore, onBack: () -> Unit) {
    var topic by remember { mutableStateOf(ALL) }
    var length by remember { mutableIntStateOf(10) }
    var test by remember { mutableStateOf<List<TestItem>?>(null) }
    var index by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    fun start() {
        val pool = ref.practice.filter { topic == ALL || it.topic == topic }.shuffled()
        test = pool.take(length).map { TestItem(it, it.options.indices.shuffled()) }
        index = 0
        finished = false
    }

    AnimeSky {
        Column(Modifier.fillMaxSize()) {
            BackBar(if (test == null) "Training Grounds" else "Practice Dojo") {
                if (test != null) { test = null; finished = false } else onBack()
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val current = test
                if (current == null) {
                    Setup(ref, progress, topic, length, { topic = it }, { length = it }, ::start)
                } else if (finished) {
                    Results(current, topic, progress, onRetry = ::start, onMenu = { test = null })
                } else {
                    QuestionView(
                        items = current,
                        index = index,
                        onNext = { if (index < current.size - 1) index++ else finished = true },
                    )
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

@Composable
private fun Setup(
    ref: ReferenceRepository,
    progress: ProgressStore,
    topic: String,
    length: Int,
    onTopic: (String) -> Unit,
    onLength: (Int) -> Unit,
    onStart: () -> Unit,
) {
    IntroBanner(
        Cast.get("kage"),
        "🥋 Practice Dojo",
        "Welcome to professional-level training. These questions test the grammar of reports, emails and exams. Choose your discipline, then strike.",
        listOf(Color(0xFF263238), DojoColor),
    )
    Text("Discipline", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    val topics = listOf(ALL) + ref.practiceTopics
    FlowPills(topics, topic, onTopic) { t ->
        val count = if (t == ALL) ref.practice.size else ref.practice.count { it.topic == t }
        val best = progress.bestPractice(t)
        "$t ($count)" + if (best >= 0) "  ⭐$best%" else ""
    }
    Text("Questions per round", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10, 20, 30).forEach { n -> FilterPill("$n", length == n, DojoColor) { onLength(n) } }
    }
    Text(
        "Ranks: 🌱 Genin (under 50%) · ⚔️ Chūnin (50%+) · 🥷 Jōnin (75%+) · 👑 Grammar Kage (90%+)",
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 13.sp,
    )
    BigButton("Begin training  ⚔️", DojoColor, onStart)
}

/** Wraps pills onto as many lines as needed. */
@Composable
private fun FlowPills(values: List<String>, selected: String, onPick: (String) -> Unit, label: (String) -> String) {
    // Two pills per row keeps labels readable on small phones.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        values.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { v -> FilterPill(label(v), v == selected, DojoColor) { onPick(v) } }
            }
        }
    }
}

@Composable
private fun QuestionView(items: List<TestItem>, index: Int, onNext: () -> Unit) {
    val item = items[index]
    val q = item.question
    val chosen = item.chosen

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Question ${index + 1} of ${items.size}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(q.topic.uppercase(), color = DojoColor, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
    }
    ProgressBar((index + if (chosen != null) 1 else 0) / items.size.toFloat(), DojoColor)
    Card {
        Text(q.question, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp, lineHeight = 24.sp)
    }
    item.order.forEachIndexed { position, oi ->
        val bg = when {
            chosen == null -> Color.White.copy(alpha = 0.08f)
            oi == q.answer -> Mint.copy(alpha = 0.35f)
            oi == chosen -> Coral.copy(alpha = 0.35f)
            else -> Color.White.copy(alpha = 0.04f)
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(1.dp, if (chosen == null) DojoColor.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(14.dp))
                .clickable(enabled = chosen == null) { item.chosen = oi }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("${'A' + position}", color = DojoColor, fontWeight = FontWeight.Black, modifier = Modifier.width(24.dp))
            Text(q.options[oi], color = Color.White, modifier = Modifier.weight(1f))
            if (chosen != null && oi == q.answer) Text("✓", color = Mint, fontWeight = FontWeight.Black)
            if (chosen == oi && oi != q.answer) Text("✗", color = Coral, fontWeight = FontWeight.Black)
        }
    }
    if (chosen != null) {
        Card {
            Text(
                if (item.correct) "🥷 Kage: Clean strike!" else "🥷 Kage: Not quite. Study this:",
                color = if (item.correct) Mint else Coral,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(q.why, color = Color.White.copy(alpha = 0.9f), fontSize = 15.sp, lineHeight = 22.sp)
        }
        BigButton(if (index < items.size - 1) "Next question  ▶" else "See my rank  👑", Gold, onNext)
    }
}

@Composable
private fun Results(items: List<TestItem>, topic: String, progress: ProgressStore, onRetry: () -> Unit, onMenu: () -> Unit) {
    val score = items.count { it.correct }
    val percent = score * 100 / items.size
    val rank = rankFor(percent)
    LaunchedEffect(items) { progress.recordPractice(topic, percent) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF263238), Color(0xFF7B2CBF), Color(0xFFFF7EB6))))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(rank.emoji, fontSize = 52.sp)
        Text(rank.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
        Text("$score / ${items.size} correct · $percent%", color = Gold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text(rank.line, color = Color.White, textAlign = TextAlign.Center)
    }
    val missed = items.filter { !it.correct }
    if (missed.isNotEmpty()) {
        Text("📜 Review your missed questions", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        missed.forEach { m ->
            Card {
                Text(m.question.question, color = Color.White, fontWeight = FontWeight.Bold)
                m.chosen?.let { Text("❌  Your answer: ${m.question.options[it]}", color = Coral) }
                Text("✅  Correct: ${m.question.options[m.question.answer]}", color = Mint)
                Text(m.question.why, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
            }
        }
    }
    BigButton("Train again  ↻", DojoColor, onRetry)
    BigButton("Choose another discipline", Color.White.copy(alpha = 0.85f), onMenu)
}

@Composable
private fun Card(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
private fun BigButton(label: String, color: Color, onClick: () -> Unit) {
    Text(
        label,
        color = Ink,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}
