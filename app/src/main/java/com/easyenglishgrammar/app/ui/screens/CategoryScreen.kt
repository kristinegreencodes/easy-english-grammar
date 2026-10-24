package com.easyenglishgrammar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyenglishgrammar.app.data.Category
import com.easyenglishgrammar.app.data.ProgressStore
import com.easyenglishgrammar.app.ui.components.AnimeSky
import com.easyenglishgrammar.app.ui.components.BackBar
import com.easyenglishgrammar.app.ui.components.Pill
import com.easyenglishgrammar.app.ui.theme.Gold
import com.easyenglishgrammar.app.ui.theme.Mint

@Composable
fun CategoryScreen(
    category: Category,
    progress: ProgressStore,
    onBack: () -> Unit,
    onRule: (String) -> Unit,
) {
    val mastered by progress.mastered
    val read by progress.read

    AnimeSky {
        Column(Modifier.fillMaxSize()) {
            BackBar("Arc ${category.order}", onBack)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(Brush.linearGradient(listOf(category.colorDark, category.color)))
                            .padding(20.dp)
                    ) {
                        Text(category.emoji, fontSize = 52.sp)
                        Text(category.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 26.sp)
                        Text(category.subtitle, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        Text(category.intro, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                    }
                }
                itemsIndexed(category.rules, key = { _, r -> r.id }) { i, rule ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onRule(rule.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(category.color.copy(alpha = 0.35f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("EPISODE ${i + 1}", color = category.color, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                            Text(rule.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(rule.tagline, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(8.dp))
                        when {
                            rule.id in mastered -> Pill("⭐", Gold)
                            rule.id in read -> Pill("✓", Mint)
                        }
                    }
                }
                item { Spacer(Modifier.navigationBarsPadding()) }
            }
        }
    }
}
