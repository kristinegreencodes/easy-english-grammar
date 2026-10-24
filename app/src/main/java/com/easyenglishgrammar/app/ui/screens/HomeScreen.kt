package com.easyenglishgrammar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.easyenglishgrammar.app.data.CastMember
import com.easyenglishgrammar.app.data.Category
import com.easyenglishgrammar.app.data.GrammarRepository
import com.easyenglishgrammar.app.data.GrammarRule
import com.easyenglishgrammar.app.data.ProgressStore
import com.easyenglishgrammar.app.ui.components.AnimeSky
import com.easyenglishgrammar.app.ui.components.Avatar
import com.easyenglishgrammar.app.ui.components.Pill
import com.easyenglishgrammar.app.ui.components.ProgressBar
import com.easyenglishgrammar.app.ui.components.SectionTitle
import com.easyenglishgrammar.app.ui.theme.Gold
import com.easyenglishgrammar.app.ui.theme.Lavender
import com.easyenglishgrammar.app.ui.theme.Sakura

@Composable
fun HomeScreen(
    repo: GrammarRepository,
    progress: ProgressStore,
    onCategory: (String) -> Unit,
    onRule: (String) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var castInfo by remember { mutableStateOf<CastMember?>(null) }
    val mastered by progress.mastered
    val read by progress.read
    val total = repo.allRules.size

    AnimeSky {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { Header() }

            item {
                JourneyCard(read = read.size, mastered = mastered.size, total = total) {
                    val pick = repo.allRules.firstOrNull { it.id !in read } ?: repo.allRules.random()
                    onRule(pick.id)
                }
            }

            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search a rule… (try \"comma\" or \"its\")") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Sakura,
                        unfocusedBorderColor = Lavender.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                    ),
                )
            }

            if (query.isNotBlank()) {
                val results = repo.search(query)
                item { SectionTitle("${results.size} rule${if (results.size == 1) "" else "s"} found") }
                items(results, key = { it.id }) { rule ->
                    val cat = repo.category(rule.categoryId)!!
                    SearchResult(rule, cat, rule.id in mastered) { onRule(rule.id) }
                }
            } else {
                item { SectionTitle("Meet the cast") }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(Cast.members.values.filter { it.id != "narrator" }.toList()) { m ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(72.dp)) {
                                Avatar(m, size = 56.dp) { castInfo = m }
                                Spacer(Modifier.height(4.dp))
                                Text(m.name.substringBefore(' '), color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1)
                            }
                        }
                    }
                }
                item { SectionTitle("Story arcs") }
                items(repo.categories, key = { it.id }) { cat ->
                    CategoryCard(cat, cat.rules.count { it.id in mastered }) { onCategory(cat.id) }
                }
                item {
                    Text(
                        "⭐ Ace a rule's quiz to master it.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }

    castInfo?.let { m ->
        AlertDialog(
            onDismissRequest = { castInfo = null },
            confirmButton = { TextButton(onClick = { castInfo = null }) { Text("Yoroshiku!") } },
            icon = { Avatar(m, size = 64.dp) },
            title = { Text(m.name) },
            text = { Text(m.role) },
        )
    }
}

@Composable
private fun Header() {
    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 24.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("文法の物語", color = Sakura, fontSize = 14.sp, letterSpacing = 4.sp)
        Text(
            "Easy English Grammar",
            style = MaterialTheme.typography.displaySmall,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Text(
            "Every rule has a story. Learn the tale, remember the rule.",
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun JourneyCard(read: Int, mastered: Int, total: Int, onContinue: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF7B2CBF), Color(0xFFFF7EB6))))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌸", fontSize = 36.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Hana's Journey", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text("$read of $total rules read · $mastered mastered", color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
        ProgressBar(if (total == 0) 0f else mastered / total.toFloat(), Gold)
        Spacer(Modifier.height(12.dp))
        Text(
            if (read == 0) "▶  Begin the adventure" else "▶  Continue your story",
            color = Color(0xFF2A1F3D),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Gold)
                .clickable(onClick = onContinue)
                .padding(horizontal = 18.dp, vertical = 10.dp),
        )
    }
}

@Composable
private fun CategoryCard(cat: Category, masteredCount: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.horizontalGradient(listOf(cat.colorDark, cat.color.copy(alpha = 0.85f))))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(cat.emoji, fontSize = 40.sp, modifier = Modifier.size(56.dp), textAlign = TextAlign.Center)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("ARC ${cat.order}", color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
            Text(cat.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
            Text(cat.subtitle, color = Color.White.copy(alpha = 0.9f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            ProgressBar(if (cat.rules.isEmpty()) 0f else masteredCount / cat.rules.size.toFloat(), Gold, height = 6.dp)
            Spacer(Modifier.height(4.dp))
            Text("$masteredCount / ${cat.rules.size} mastered", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun SearchResult(rule: GrammarRule, cat: Category, mastered: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(cat.emoji, fontSize = 28.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(rule.title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(rule.tagline, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp, maxLines = 2)
        }
        if (mastered) Pill("⭐", Gold)
    }
}
