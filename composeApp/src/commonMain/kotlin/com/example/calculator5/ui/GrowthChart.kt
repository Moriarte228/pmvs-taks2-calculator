package com.example.calculator5.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.calculator5.domain.GrowthPoint

/**
 * Параметры отрисовки графика, по которым отличаются платформы.
 */
data class ChartStyle(
    /** Толщина линии графика. iOS — тонкая, Android — средняя, Linux — стандартная. */
    val lineWidthDp: Float = 3f,
    /** Включена ли градиентная заливка под линией (Android: да; Linux/iOS: нет). */
    val gradientFill: Boolean = false,
    /** Пунктирная сетка. */
    val dashedGrid: Boolean = true,
    /** Поддержка интерактивности (масштабирование/тап) — для Web. */
    val interactive: Boolean = false,
)

/**
 * График роста капитала. Использует Canvas API из Compose.
 *
 * Поддерживает анимацию появления (прогрессивный «прорисовывающийся» путь)
 * и опциональную интерактивность (выбор точки тапом — только для Web).
 */
@Composable
fun GrowthChart(
    points: List<GrowthPoint>,
    style: ChartStyle,
    modifier: Modifier = Modifier,
) {
    if (points.size < 2) {
        Box(modifier = modifier.height(220.dp).fillMaxWidth())
        return
    }

    val animProgress = remember(points) { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing),
        )
    }

    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    // Состояние интерактивного выбора (для Web)
    val selectedIndex = remember { androidx.compose.runtime.mutableStateOf<Int?>(null) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .padding(12.dp),
    ) {
        val chartMod = if (style.interactive) {
            Modifier.fillMaxSize().pointerInput(points) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pos = event.changes.firstOrNull()?.position
                        if (pos != null) {
                            val width = size.width.toFloat()
                            val ratio = (pos.x / width).coerceIn(0f, 1f)
                            selectedIndex.value =
                                (ratio * (points.size - 1)).toInt().coerceIn(0, points.size - 1)
                        }
                    }
                }
            }
        } else Modifier.fillMaxSize()

        Canvas(modifier = chartMod) {
            val w = size.width
            val h = size.height
            val maxY = points.maxOf { it.amount }
            val minY = points.minOf { it.amount }
            val rangeY = (maxY - minY).coerceAtLeast(1e-9)

            // Сетка
            val gridColor = onSurface.copy(alpha = 0.12f)
            val gridStroke = Stroke(
                width = 1f,
                pathEffect = if (style.dashedGrid)
                    PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                else null,
            )
            for (i in 0..4) {
                val y = h * i / 4f
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = gridStroke.width,
                    pathEffect = gridStroke.pathEffect,
                )
            }

            // Координаты точек
            fun pointX(i: Int): Float = w * i / (points.size - 1).toFloat()
            fun pointY(amount: Double): Float =
                h - (((amount - minY) / rangeY) * h).toFloat()

            // «Прорисовывающаяся» линия
            val visiblePoints = (points.size * animProgress.value).toInt().coerceAtLeast(2)
            val linePath = Path().apply {
                moveTo(pointX(0), pointY(points[0].amount))
                for (i in 1 until visiblePoints) {
                    lineTo(pointX(i), pointY(points[i].amount))
                }
            }

            // Градиентная заливка под линией (только Android)
            if (style.gradientFill && visiblePoints >= 2) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(pointX(visiblePoints - 1), h)
                    lineTo(pointX(0), h)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.35f),
                            primary.copy(alpha = 0.0f),
                        ),
                        startY = 0f,
                        endY = h,
                    ),
                )
            }

            // Сама линия
            drawPath(
                path = linePath,
                color = primary,
                style = Stroke(width = style.lineWidthDp),
            )

            // Маркер выбранной точки (Web)
            val idx = selectedIndex.value
            if (style.interactive && idx != null && idx < visiblePoints) {
                val px = pointX(idx)
                val py = pointY(points[idx].amount)
                drawLine(
                    color = primary.copy(alpha = 0.5f),
                    start = Offset(px, 0f),
                    end = Offset(px, h),
                    strokeWidth = 1.5f,
                )
                drawCircle(
                    color = primary,
                    radius = 6f,
                    center = Offset(px, py),
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = Offset(px, py),
                )
            }
        }
    }
}
