package com.example.calculator5.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculator5.data.HistoryRecord
import com.example.calculator5.domain.DepositResult
import com.example.calculator5.domain.Formatting
import com.example.calculator5.i18n.Language
import com.example.calculator5.i18n.StringKey
import com.example.calculator5.i18n.translate

/**
 * Карточка с результатом: конечная сумма + общая прибыль.
 * Анимировано появление, чтобы выполнить требование «приложение содержит
 * анимацию».
 */
@Composable
fun ResultBlock(result: DepositResult?, language: Language, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = result != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
    ) {
        val r = result ?: return@AnimatedVisibility
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = translate(StringKey.FinalAmount, language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Text(
                    text = Formatting.money(r.finalAmount),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = translate(StringKey.TotalProfit, language),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
                Text(
                    text = Formatting.money(r.totalProfit),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

/**
 * Селектор языка — горизонтальные «чипы».
 */
@Composable
fun LanguagePicker(
    selected: Language,
    onSelect: (Language) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            translate(StringKey.Language, selected) + ":",
            style = MaterialTheme.typography.labelMedium,
        )
        Language.entries.forEach { lang ->
            val isSelected = lang == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(lang) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = lang.code.uppercase(),
                    color = if (isSelected) Color.White
                    else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

/**
 * Горизонтальный список истории расчётов. Тап загружает запись в форму.
 */
@Composable
fun HistoryStrip(
    history: List<HistoryRecord>,
    language: Language,
    onSelect: (HistoryRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) {
        Text(
            text = translate(StringKey.NoHistory, language),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = modifier,
        )
        return
    }
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(history, key = { it.timestamp }) { rec ->
            Card(
                modifier = Modifier.clickable { onSelect(rec) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = Formatting.money(rec.principal),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "→ " + Formatting.money(rec.finalAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "${rec.ratePercent}% · ${rec.years}y",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    )
                }
            }
            Spacer(Modifier.width(0.dp))
        }
    }
}
