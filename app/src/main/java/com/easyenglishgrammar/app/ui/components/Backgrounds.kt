package com.easyenglishgrammar.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.geometry.Size
import com.easyenglishgrammar.app.ui.theme.Dusk
import com.easyenglishgrammar.app.ui.theme.Night
import com.easyenglishgrammar.app.ui.theme.NightDeep
import com.easyenglishgrammar.app.ui.theme.Sakura
import kotlin.random.Random

private data class Star(val x: Float, val y: Float, val r: Float, val phase: Float)
private data class Petal(val x: Float, val speed: Float, val sway: Float, val size: Float, val offset: Float)

/** Night-sky gradient with twinkling stars and drifting sakura petals. */
@Composable
fun AnimeSky(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val stars = remember {
        val rnd = Random(7)
        List(70) { Star(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat() * 2.2f + 0.6f, rnd.nextFloat()) }
    }
    val petals = remember {
        val rnd = Random(11)
        List(14) { Petal(rnd.nextFloat(), rnd.nextFloat() * 0.5f + 0.5f, rnd.nextFloat() * 40f + 10f, rnd.nextFloat() * 6f + 6f, rnd.nextFloat()) }
    }
    val transition = rememberInfiniteTransition(label = "sky")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "t",
    )

    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(NightDeep, Night, Dusk)))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            stars.forEach { s ->
                val twinkle = 0.35f + 0.65f * kotlin.math.abs(kotlin.math.sin((t + s.phase) * 6.283f * 2))
                drawCircle(Color.White.copy(alpha = twinkle * 0.8f), s.r, Offset(s.x * size.width, s.y * size.height * 0.7f))
            }
            petals.forEach { p ->
                val progress = (t * p.speed + p.offset) % 1f
                val y = progress * (size.height + 40f) - 20f
                val x = p.x * size.width + kotlin.math.sin(progress * 12f) * p.sway
                rotate(progress * 360f, pivot = Offset(x, y)) {
                    drawOval(
                        Sakura.copy(alpha = 0.55f),
                        topLeft = Offset(x - p.size, y - p.size / 2),
                        size = Size(p.size * 2, p.size),
                    )
                }
            }
        }
        content()
    }
}
