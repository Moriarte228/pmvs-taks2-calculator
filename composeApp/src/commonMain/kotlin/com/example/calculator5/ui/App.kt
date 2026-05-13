package com.example.calculator5.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.Formatting
import com.example.calculator5.i18n.StringKey
import com.example.calculator5.platform.Platform
import com.example.calculator5.platform.currentPlatform

@Composable
fun App(viewModel: CalculatorViewModel) {
    CalculatorTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val state by viewModel.state.collectAsStateWithLifecycle()
            when (currentPlatform) {
                Platform.Android -> AndroidScreen(state, viewModel)
                Platform.Ios -> IosScreen(state, viewModel)
                Platform.Desktop -> DesktopScreen(state, viewModel)
                Platform.Web -> WebScreen(state, viewModel)
            }
        }
    }
}

/* ============================================================
 * Android: Material 3 — TextField, Slider, скруглённые кнопки,
 * график с градиентной заливкой.
 * ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AndroidScreen(state: CalculatorUiState, vm: CalculatorViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vm.t(StringKey.AppTitle)) },
                actions = {
                    LanguagePicker(state.language, vm::onLanguageChange,
                        modifier = Modifier.padding(end = 8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.principalText,
                onValueChange = vm::onPrincipalChange,
                label = { Text(vm.t(StringKey.Principal)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Слайдер для ставки — это специфика Android
            val rateValue = Formatting.parseNumber(state.ratePercentText) ?: 7.5
            Column {
                Text("${vm.t(StringKey.AnnualRate)}: ${rateValue.formatFixed(1)}")
                Slider(
                    value = rateValue.toFloat().coerceIn(0f, 30f),
                    onValueChange = { vm.onRateChange(it.toString()) },
                    valueRange = 0f..30f,
                    steps = 60,
                )
            }

            // Слайдер для срока в годах
            val yearsValue = Formatting.parseNumber(state.yearsText) ?: 5.0
            Column {
                Text("${vm.t(StringKey.Years)}: ${yearsValue.formatFixed(0)}")
                Slider(
                    value = yearsValue.toFloat().coerceIn(1f, 30f),
                    onValueChange = { vm.onYearsChange(it.toInt().toString()) },
                    valueRange = 1f..30f,
                    steps = 28,
                )
            }

            CompoundingSegmented(state, vm)
            ErrorList(state, vm)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::calculate, modifier = Modifier.weight(1f)) {
                    Text(vm.t(StringKey.Calculate))
                }
                OutlinedButton(onClick = vm::reset) {
                    Text("↻")
                }
            }

            ResultBlock(state.result, state.language)

            state.result?.let { r ->
                Text(vm.t(StringKey.GrowthChart), style = MaterialTheme.typography.titleMedium)
                GrowthChart(
                    points = r.growth,
                    style = ChartStyle(
                        lineWidthDp = 3.5f,
                        gradientFill = true,
                        dashedGrid = true,
                    ),
                )
                Button(
                    onClick = vm::saveCurrentResult,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(vm.t(StringKey.Save))
                }
                if (state.savedHintVisible) {
                    Text(
                        vm.t(StringKey.SavedHint),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            HistorySection(state, vm)
            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ============================================================
 * iOS: плоские кнопки, без теней, Picker (SingleChoiceSegmentedButtonRow)
 * вместо слайдеров, тонкие линии в графике.
 * ============================================================ */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IosScreen(state: CalculatorUiState, vm: CalculatorViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(vm.t(StringKey.AppTitle)) },
                actions = {
                    LanguagePicker(state.language, vm::onLanguageChange,
                        modifier = Modifier.padding(end = 8.dp))
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.principalText,
                onValueChange = vm::onPrincipalChange,
                label = { Text(vm.t(StringKey.Principal)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = state.ratePercentText,
                onValueChange = vm::onRateChange,
                label = { Text(vm.t(StringKey.AnnualRate)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            // Вместо слайдера срока — Picker (сегментированное переключение)
            Text(vm.t(StringKey.Years), style = MaterialTheme.typography.labelMedium)
            val yearOptions = listOf(1, 3, 5, 10, 15, 20)
            val selectedYears = Formatting.parseNumber(state.yearsText)?.toInt() ?: 5
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                yearOptions.forEachIndexed { i, y ->
                    SegmentedButton(
                        selected = selectedYears == y,
                        onClick = { vm.onYearsChange(y.toString()) },
                        shape = SegmentedButtonDefaults.itemShape(i, yearOptions.size),
                    ) { Text("$y") }
                }
            }

            CompoundingSegmented(state, vm)
            ErrorList(state, vm)

            // Плоские кнопки без elevation
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = vm::calculate,
                    modifier = Modifier.weight(1f),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 0.dp,
                    ),
                ) { Text(vm.t(StringKey.Calculate)) }
                OutlinedButton(onClick = vm::reset) { Text(vm.t(StringKey.Reset)) }
            }

            ResultBlock(state.result, state.language)

            state.result?.let { r ->
                Text(vm.t(StringKey.GrowthChart), style = MaterialTheme.typography.titleMedium)
                GrowthChart(
                    points = r.growth,
                    style = ChartStyle(
                        lineWidthDp = 2f, // тоньше
                        gradientFill = false,
                        dashedGrid = false,
                    ),
                )
                OutlinedButton(
                    onClick = vm::saveCurrentResult,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(vm.t(StringKey.Save)) }
                if (state.savedHintVisible) {
                    Text(
                        vm.t(StringKey.SavedHint),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }

            HistorySection(state, vm)
            Spacer(Modifier.height(16.dp))
        }
    }
}

/* ============================================================
 * Desktop / Linux: классические SpinBox (числовые поля + кнопки -/+),
 * без градиентов в графике, чёткие фиксированные размеры.
 * ============================================================ */
@Composable
private fun DesktopScreen(state: CalculatorUiState, vm: CalculatorViewModel) {
    Row(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        // Левая колонка — форма (фиксированная ширина)
        Column(
            modifier = Modifier.widthIn(min = 320.dp, max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(vm.t(StringKey.AppTitle),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            LanguagePicker(state.language, vm::onLanguageChange)

            // SpinBox-стиль: текстовое поле + кнопки уменьшения/увеличения
            SpinBoxField(
                label = vm.t(StringKey.Principal),
                value = state.principalText,
                onValueChange = vm::onPrincipalChange,
                step = 1000.0,
            )
            SpinBoxField(
                label = vm.t(StringKey.AnnualRate),
                value = state.ratePercentText,
                onValueChange = vm::onRateChange,
                step = 0.5,
            )
            SpinBoxField(
                label = vm.t(StringKey.Years),
                value = state.yearsText,
                onValueChange = vm::onYearsChange,
                step = 1.0,
            )
            CompoundingSegmented(state, vm)
            ErrorList(state, vm)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = vm::calculate) { Text(vm.t(StringKey.Calculate)) }
                OutlinedButton(onClick = vm::reset) { Text(vm.t(StringKey.Reset)) }
                TextButton(onClick = vm::saveCurrentResult) { Text(vm.t(StringKey.Save)) }
            }
            if (state.savedHintVisible) {
                Text(vm.t(StringKey.SavedHint),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        Spacer(Modifier.width(24.dp))
        // Правая колонка — результаты и график
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ResultBlock(state.result, state.language)
            state.result?.let { r ->
                Text(vm.t(StringKey.GrowthChart),
                    style = MaterialTheme.typography.titleMedium)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp),
                        ),
                ) {
                    GrowthChart(
                        points = r.growth,
                        style = ChartStyle(
                            lineWidthDp = 2.5f,
                            gradientFill = false, // без градиентов
                            dashedGrid = false,
                        ),
                    )
                }
            }
            HistorySection(state, vm)
        }
    }
}

/* ============================================================
 * Web: адаптивные формы (1 или 2 колонки в зависимости от ширины),
 * интерактивный график с подсветкой выбранной точки.
 * ============================================================ */
@Composable
private fun WebScreen(state: CalculatorUiState, vm: CalculatorViewModel) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        val twoColumn = maxWidth >= 800.dp
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(vm.t(StringKey.AppTitle),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold)
                LanguagePicker(state.language, vm::onLanguageChange)
            }

            val form: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = state.principalText,
                        onValueChange = vm::onPrincipalChange,
                        label = { Text(vm.t(StringKey.Principal)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.ratePercentText,
                        onValueChange = vm::onRateChange,
                        label = { Text(vm.t(StringKey.AnnualRate)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.yearsText,
                        onValueChange = vm::onYearsChange,
                        label = { Text(vm.t(StringKey.Years)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    CompoundingSegmented(state, vm)
                    ErrorList(state, vm)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = vm::calculate, modifier = Modifier.weight(1f)) {
                            Text(vm.t(StringKey.Calculate))
                        }
                        OutlinedButton(onClick = vm::reset) {
                            Text(vm.t(StringKey.Reset))
                        }
                        TextButton(onClick = vm::saveCurrentResult) {
                            Text(vm.t(StringKey.Save))
                        }
                    }
                    if (state.savedHintVisible) {
                        Text(vm.t(StringKey.SavedHint),
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            val results: @Composable () -> Unit = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ResultBlock(state.result, state.language)
                    state.result?.let { r ->
                        Text(vm.t(StringKey.GrowthChart),
                            style = MaterialTheme.typography.titleMedium)
                        GrowthChart(
                            points = r.growth,
                            style = ChartStyle(
                                lineWidthDp = 3f,
                                gradientFill = true,
                                dashedGrid = true,
                                interactive = true, // только Web — поддержка мыши
                            ),
                        )
                    }
                    HistorySection(state, vm)
                }
            }

            if (twoColumn) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Box(Modifier.weight(1f)) { form() }
                    Box(Modifier.weight(1f)) { results() }
                }
            } else {
                form()
                results()
            }
        }
    }
}

/* -------------------- Вспомогательные компоненты -------------------- */

/**
 * SpinBox-стиль ввода для Desktop: число + кнопки уменьшения/увеличения.
 */
@Composable
private fun SpinBoxField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    step: Double,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = {
                    val cur = Formatting.parseNumber(value) ?: 0.0
                    onValueChange((cur - step).coerceAtLeast(0.0).removeTrailingZeros())
                },
                modifier = Modifier.width(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text("−") }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            )
            OutlinedButton(
                onClick = {
                    val cur = Formatting.parseNumber(value) ?: 0.0
                    onValueChange((cur + step).removeTrailingZeros())
                },
                modifier = Modifier.width(40.dp),
                contentPadding = PaddingValues(0.dp),
            ) { Text("+") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompoundingSegmented(state: CalculatorUiState, vm: CalculatorViewModel) {
    val items = listOf(
        Compounding.Monthly to StringKey.CompMonthly,
        Compounding.Quarterly to StringKey.CompQuarterly,
        Compounding.Yearly to StringKey.CompYearly,
    )
    Column {
        Text(vm.t(StringKey.Compounding), style = MaterialTheme.typography.labelMedium)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            items.forEachIndexed { i, (c, key) ->
                SegmentedButton(
                    selected = state.compounding == c,
                    onClick = { vm.onCompoundingChange(c) },
                    shape = SegmentedButtonDefaults.itemShape(i, items.size),
                ) { Text(vm.t(key)) }
            }
        }
    }
}

@Composable
private fun ErrorList(state: CalculatorUiState, vm: CalculatorViewModel) {
    if (state.errors.isEmpty()) return
    Column {
        state.errors.forEach { err ->
            Text(
                text = vm.errorText(err),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun HistorySection(state: CalculatorUiState, vm: CalculatorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(vm.t(StringKey.History), style = MaterialTheme.typography.titleMedium)
            if (state.history.isNotEmpty()) {
                TextButton(onClick = vm::clearHistory) { Text(vm.t(StringKey.Clear)) }
            }
        }
        HistoryStrip(state.history, state.language, vm::loadFromHistory)
    }
}

/**
 * Округлить число до N знаков после запятой и вернуть строку.
 * Нужно, потому что `String.format()` есть только на JVM, а не на wasm/iOS.
 */
private fun Double.formatFixed(digits: Int): String {
    var factor = 1.0
    repeat(digits) { factor *= 10.0 }
    val rounded = kotlin.math.round(this * factor) / factor
    val s = rounded.toString()
    return if (digits == 0) {
        s.substringBefore('.')
    } else {
        val dot = s.indexOf('.')
        if (dot < 0) s + "." + "0".repeat(digits)
        else {
            val frac = s.substring(dot + 1)
            if (frac.length >= digits) s.substring(0, dot + 1 + digits)
            else s + "0".repeat(digits - frac.length)
        }
    }
}

private fun Double.removeTrailingZeros(): String {
    val s = this.toString()
    return if ('.' in s) s.trimEnd('0').trimEnd('.') else s
}
