package com.easyenglishgrammar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyenglishgrammar.app.audio.LocalAudio
import com.easyenglishgrammar.app.data.Cast
import com.easyenglishgrammar.app.data.Category
import com.easyenglishgrammar.app.data.GrammarRule
import com.easyenglishgrammar.app.data.ProgressStore
import com.easyenglishgrammar.app.data.QuizQuestion
import com.easyenglishgrammar.app.data.StoryPanel
import com.easyenglishgrammar.app.ui.components.AnimeSky
import com.easyenglishgrammar.app.ui.components.Avatar
import com.easyenglishgrammar.app.ui.components.BackBar
import com.easyenglishgrammar.app.ui.theme.Coral
import com.easyenglishgrammar.app.ui.theme.Gold
import com.easyenglishgrammar.app.ui.theme.Ink
import com.easyenglishgrammar.app.ui.theme.Mint
import com.easyenglishgrammar.app.ui.theme.Paper
import kotlinx.coroutines.delay

private val tabs = listOf("📖 Story", "🧩 Parts", "📜 History", "✏️ Examples", "🎯 Quiz")

@Composable
fun RuleScreen(
    rule: GrammarRule,
    category: Category,
    episode: Int,
    progress: ProgressStore,
    onBack: () -> Unit,
    onNext: (() -> Unit)?,
) {
    var tab by rememberSaveable(rule.id) { mutableIntStateOf(0) }

    AnimeSky {
        Column(Modifier.fillMaxSize()) {
            BackBar(category.title, onBack)
            Column(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(category.colorDark, category.color)))
                    .padding(16.dp)
            ) {
                Text(
                    "${category.emoji}  EPISODE $episode",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(rule.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(rule.tagline, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
            }
            ScrollableTabRow(
                selectedTabIndex = tab,
                containerColor = Color.Transparent,
                contentColor = Color.White,
                edgePadding = 12.dp,
                divider = {},
                indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[tab]), color = category.color)
                },
            ) {
                tabs.forEachIndexed { i, label ->
                    Tab(selected = tab == i, onClick = { tab = i }, text = { Text(label) })
                }
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when (tab) {
                    0 -> StoryTab(rule, category, onFinished = { progress.markRead(rule.id) }, onParts = { tab = 1 })
                    1 -> PartsTab(rule, category, onNext = { tab = 2 })
                    2 -> HistoryTab(rule, onNext = { tab = 3 })
                    3 -> ExamplesTab(rule, category, onNext = { tab = 4 })
                    else -> QuizTab(rule, category, onMastered = { progress.markMastered(rule.id) }, onNext = onNext)
                }
                Spacer(Modifier.navigationBarsPadding().height(24.dp))
            }
        }
    }
}

// ---------- Story ----------

@Composable
private fun StoryTab(rule: GrammarRule, category: Category, onFinished: () -> Unit, onParts: () -> Unit) {
    val voices = LocalAudio.current.voices
    val voicesOn by voices.enabled
    val speaking by voices.speakingKey
    var shown by rememberSaveable(rule.id) { mutableIntStateOf(1) }
    var autoPlay by rememberSaveable(rule.id) { mutableStateOf(false) }
    // The last panel that has finished being read (or shown, when voices are off).
    var doneIndex by remember(rule.id) { mutableIntStateOf(0) }
    val total = rule.story.size
    LaunchedEffect(shown) { if (shown >= total) onFinished() }

    // Read each newly revealed panel aloud in its character's voice.
    LaunchedEffect(rule.id, shown, voicesOn) {
        val panel = rule.story[shown - 1]
        if (voicesOn) voices.speakAndWait(panel.who, panel.line, panelKey(rule, shown - 1))
        doneIndex = shown
    }
    // In play mode, move on to the next panel once the current one is finished.
    LaunchedEffect(autoPlay, doneIndex) {
        if (autoPlay && doneIndex == shown) {
            delay(if (voicesOn) 450 else 2600)
            if (shown < total) shown++ else autoPlay = false
        }
    }
    DisposableEffect(rule.id) { onDispose { voices.stop() } }

    rule.story.take(shown).forEachIndexed { i, panel ->
        val k = panelKey(rule, i)
        key(rule.id, i) {
            Panel(panel, i, speaking = speaking == k) { voices.speak(panel.who, panel.line, k) }
        }
    }

    if (shown < total) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionButton(
                if (autoPlay) "⏸  Pause" else "▶  Play scene",
                Color.White.copy(alpha = 0.85f),
                Modifier.weight(1f),
            ) { autoPlay = !autoPlay }
            ActionButton("Next  ⏭  (${shown}/$total)", category.color, Modifier.weight(1f)) { shown++ }
        }
    } else {
        TrickCard(rule.trick)
        ActionButton("🔁  Replay scene", Color.White.copy(alpha = 0.85f)) {
            voices.stop()
            shown = 1
            doneIndex = 0
            autoPlay = true
        }
        ActionButton("See the parts of this rule  →", category.color, onClick = onParts)
    }
}

private fun panelKey(rule: GrammarRule, index: Int) = "${rule.id}#$index"

@Composable
private fun Panel(panel: StoryPanel, index: Int, speaking: Boolean, onReplay: () -> Unit) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(state, enter = fadeIn() + slideInVertically { it / 3 }) {
        val member = Cast.get(panel.who)
        if (panel.who == "narrator") {
            Text(
                panel.line,
                color = Color(0xFFFFF3C4),
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .border(
                        if (speaking) 2.dp else 1.dp,
                        Color(0xFFFFE082).copy(alpha = if (speaking) 1f else 0.5f),
                        RoundedCornerShape(10.dp),
                    )
                    .clickable(onClickLabel = "Read aloud", onClick = onReplay)
                    .padding(12.dp),
            )
        } else {
            val onRight = index % 2 == 1
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = if (onRight) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.Top,
            ) {
                if (!onRight) { Avatar(member); Spacer(Modifier.width(10.dp)) }
                Column(
                    Modifier
                        .widthIn(max = 290.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = if (onRight) 18.dp else 4.dp,
                                topEnd = if (onRight) 4.dp else 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp,
                            )
                        )
                        .background(Paper)
                        .border(
                            if (speaking) 4.dp else 2.dp,
                            if (speaking) Gold else member.color,
                            RoundedCornerShape(
                                topStart = if (onRight) 18.dp else 4.dp,
                                topEnd = if (onRight) 4.dp else 18.dp,
                                bottomStart = 18.dp,
                                bottomEnd = 18.dp,
                            )
                        )
                        .clickable(onClickLabel = "Read aloud", onClick = onReplay)
                        .padding(12.dp)
                ) {
                    Text(
                        member.name + if (speaking) "  🔊" else "",
                        color = member.color.darken(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                    )
                    Text(panel.line, color = Ink, fontSize = 15.sp, lineHeight = 21.sp)
                }
                if (onRight) { Spacer(Modifier.width(10.dp)); Avatar(member) }
            }
        }
    }
}

private fun Color.darken(): Color = Color(red * 0.6f, green * 0.6f, blue * 0.6f, alpha)

// ---------- Parts ----------

@Composable
private fun PartsTab(rule: GrammarRule, category: Category, onNext: () -> Unit) {
    rule.parts.forEachIndexed { i, part ->
        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${i + 1}",
                    color = Ink,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(category.color)
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(part.name, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
            }
            Text(part.detail, color = Color.White.copy(alpha = 0.9f))
            if (part.example.isNotBlank()) ExampleLine(part.example, category.color)
        }
    }
    ActionButton("Why is it this way?  →", category.color, onClick = onNext)
}

// ---------- History ----------

@Composable
private fun HistoryTab(rule: GrammarRule, onNext: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF7E7C6))
            .border(3.dp, Color(0xFFB08850), RoundedCornerShape(18.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "📜 The Legend Behind the Rule",
                color = Color(0xFF5D3A1A),
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            ListenChip("sensei", rule.history, "${rule.id}#history", Color(0xFF5D3A1A))
        }
        rule.history.split("\n\n").forEach {
            Text(it, color = Color(0xFF3E2A14), fontSize = 15.sp, lineHeight = 23.sp)
        }
    }
    ActionButton("Show me examples  →", Color(0xFFB08850), onClick = onNext)
}

// ---------- Examples ----------

@Composable
private fun ExamplesTab(rule: GrammarRule, category: Category, onNext: () -> Unit) {
    rule.examples.forEach { ex ->
        Card {
            Text("✅  ${ex.good}", color = Mint, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            ex.bad?.let { Text("❌  $it", color = Coral, fontSize = 15.sp) }
            if (ex.note.isNotBlank()) Text(ex.note, color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
    TrickCard(rule.trick)
    ActionButton("Take the quiz  🎯", category.color, onClick = onNext)
}

// ---------- Quiz ----------

@Composable
private fun QuizTab(rule: GrammarRule, category: Category, onMastered: () -> Unit, onNext: (() -> Unit)?) {
    var attempt by rememberSaveable(rule.id) { mutableIntStateOf(0) }
    val answers = remember(rule.id, attempt) { mutableStateMapOf<Int, Int>() }
    // Shuffle option order on every attempt so the right answer isn't always in the same spot.
    val orders = remember(rule.id, attempt) { rule.quiz.map { it.options.indices.shuffled() } }
    val done = answers.size == rule.quiz.size
    val score = answers.count { (q, a) -> rule.quiz[q].answer == a }
    val perfect = done && score == rule.quiz.size

    LaunchedEffect(perfect) { if (perfect) onMastered() }

    rule.quiz.forEachIndexed { qi, q ->
        QuestionCard(qi, q, orders[qi], answers[qi], category.color) { if (qi !in answers) answers[qi] = it }
    }

    if (done) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (perfect) Brush.linearGradient(listOf(Color(0xFFFFB300), Color(0xFFFF7EB6)))
                    else Brush.linearGradient(listOf(Color(0xFF5E35B1), Color(0xFF3949AB)))
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (perfect) "🌟" else "💪", fontSize = 44.sp)
            Text(
                if (perfect) "Sugoi! Rule mastered!" else "$score / ${rule.quiz.size}. Ganbatte!",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
            Text(
                if (perfect) "Hana adds a star to her grammar scroll." else "Sensei Fukurō says: read the story once more, then try again.",
                color = Color.White,
                textAlign = TextAlign.Center,
            )
        }
        if (!perfect) ActionButton("Try again  ↻", category.color) { attempt++ }
        if (onNext != null) ActionButton("Next episode  ▶", Gold, onClick = onNext)
    }
}

@Composable
private fun QuestionCard(index: Int, q: QuizQuestion, order: List<Int>, chosen: Int?, accent: Color, onPick: (Int) -> Unit) {
    Card {
        Text("Q${index + 1}. ${q.question}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        order.forEachIndexed { position, oi ->
            val option = q.options[oi]
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
                    .border(1.dp, if (chosen == null) accent.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(14.dp))
                    .clickable(enabled = chosen == null) { onPick(oi) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${'A' + position}", color = accent, fontWeight = FontWeight.Black, modifier = Modifier.width(24.dp))
                Text(option, color = Color.White, modifier = Modifier.weight(1f))
                if (chosen != null && oi == q.answer) Text("✓", color = Mint, fontWeight = FontWeight.Black)
                if (chosen == oi && oi != q.answer) Text("✗", color = Coral, fontWeight = FontWeight.Black)
            }
        }
        if (chosen != null && q.why.isNotBlank()) {
            Text(
                (if (chosen == q.answer) "Correct! " else "Not quite. ") + q.why,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
            )
        }
    }
}

// ---------- Shared pieces ----------

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
private fun ExampleLine(text: String, accent: Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.18f))
            .padding(10.dp)
    ) {
        Text("“$text”", color = Color.White, fontStyle = FontStyle.Italic)
    }
}

@Composable
private fun TrickCard(trick: String) {
    if (trick.isBlank()) return
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFFFE066), Color(0xFFFFB74D))))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("✨ Memory Spell", color = Ink, fontWeight = FontWeight.Black, fontSize = 16.sp, modifier = Modifier.weight(1f))
            ListenChip("sensei", trick, "trick#$trick", Ink)
        }
        Text(trick, color = Ink, fontSize = 15.sp, lineHeight = 21.sp)
    }
}

/** A small "Listen" button that reads a block of text in a character's voice. */
@Composable
private fun ListenChip(who: String, text: String, key: String, color: Color) {
    val voices = LocalAudio.current.voices
    val speaking by voices.speakingKey
    val voicesOn by voices.enabled
    if (!voicesOn) return
    val playing = speaking == key
    Text(
        if (playing) "⏹ Stop" else "🔊 Listen",
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.5.dp, color, RoundedCornerShape(50))
            .clickable { if (playing) voices.stop() else voices.speak(who, text, key) }
            .padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun ActionButton(label: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Text(
        label,
        color = Ink,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(color)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    )
}
