package com.easyenglishgrammar.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easyenglishgrammar.app.audio.LocalAudio
import com.easyenglishgrammar.app.data.CastMember

@Composable
fun BackBar(title: String, onBack: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        SoundBar(Modifier.padding(end = 8.dp))
    }
}

/** Music and voice toggles, shown in the top corner of every screen. */
@Composable
fun SoundBar(modifier: Modifier = Modifier) {
    val audio = LocalAudio.current
    val muted by audio.music.muted
    val voices by audio.voices.enabled
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        SoundToggle(if (muted) "🔇" else "🎵", on = !muted, label = if (muted) "Music off" else "Music on") {
            audio.music.toggleMute()
        }
        SoundToggle(if (voices) "🗣️" else "🤐", on = voices, label = if (voices) "Voices on" else "Voices off") {
            audio.voices.toggle()
        }
    }
}

@Composable
private fun SoundToggle(icon: String, on: Boolean, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(if (on) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.3f))
            .border(1.dp, Color.White.copy(alpha = if (on) 0.5f else 0.2f), CircleShape)
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(icon, fontSize = 18.sp)
    }
}

@Composable
fun Pill(text: String, color: Color, modifier: Modifier = Modifier, textColor: Color = Color(0xFF2A1F3D)) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, color = textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier, height: Dp = 8.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.15f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(height)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun Avatar(member: CastMember, size: Dp = 48.dp, onClick: (() -> Unit)? = null) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(member.color.copy(alpha = 0.3f))
            .border(2.dp, member.color, CircleShape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        Text(member.emoji, fontSize = (size.value * 0.5f).sp)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("✦", color = MaterialTheme.colorScheme.tertiary, fontSize = 16.sp)
        Text(text, style = MaterialTheme.typography.titleLarge, color = Color.White)
    }
}
