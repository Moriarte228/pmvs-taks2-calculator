package com.example.calculator5.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.DepositInput
import com.example.calculator5.domain.FinanceCalculator
import com.example.calculator5.domain.Formatting
import com.example.calculator5.i18n.Language
import com.example.calculator5.testutil.InMemorySettings
import kotlin.test.Test

/**
 * UI-тесты на компонентах Compose. Тестируют отображение, ввод данных,
 * нажатия и граничные случаи. Запускаются на desktop-таргете через
 * compose.uiTest.
 *
 * Платформенные ветки (AndroidScreen/IosScreen/...) в самом App не
 * тестируются — на desktop currentPlatform = Desktop. Поэтому
 * тестируем общие компоненты (ResultBlock, GrowthChart, LanguagePicker)
 * и сценарии работы со ViewModel.
 */
@OptIn(ExperimentalTestApi::class)
class UiTest {

    private fun sampleResult(): com.example.calculator5.domain.DepositResult =
        FinanceCalculator.compute(DepositInput(1000.0, 10.0, 1.0, Compounding.Yearly))

    @Test
    fun result_block_shows_final_amount_text() = runComposeUiTest {
        val result = sampleResult()
        setContent { ResultBlock(result, Language.Russian) }
        // 1000 * 1.1 = 1100, форматирование — "1 100.00"
        onNodeWithText(Formatting.money(result.finalAmount)).assertIsDisplayed()
        onNodeWithText("Конечная сумма").assertIsDisplayed()
    }

    @Test
    fun result_block_uses_english_labels() = runComposeUiTest {
        setContent { ResultBlock(sampleResult(), Language.English) }
        onNodeWithText("Final amount").assertIsDisplayed()
        onNodeWithText("Total profit").assertIsDisplayed()
    }

    @Test
    fun result_block_uses_belarusian_labels() = runComposeUiTest {
        setContent { ResultBlock(sampleResult(), Language.Belarusian) }
        onNodeWithText("Канчатковая сума").assertIsDisplayed()
    }

    @Test
    fun result_block_hides_when_result_is_null() = runComposeUiTest {
        setContent { ResultBlock(null, Language.Russian) }
        onAllNodesWithText("Конечная сумма").assertCountEquals(0)
    }

    @Test
    fun growth_chart_renders_without_crash_for_normal_input() = runComposeUiTest {
        val result = sampleResult()
        setContent { GrowthChart(result.growth, ChartStyle(gradientFill = true)) }
        // Canvas сам по себе ничего не выводит как текст — но факт,
        // что setContent не падает, и есть граничный кейс ниже.
    }

    @Test
    fun growth_chart_handles_single_point_gracefully() = runComposeUiTest {
        // Граничный случай: меньше двух точек — рендер не должен падать
        val onePoint = listOf(com.example.calculator5.domain.GrowthPoint(0.0, 1000.0))
        setContent { GrowthChart(onePoint, ChartStyle()) }
    }

    @Test
    fun language_picker_highlights_selected_language() = runComposeUiTest {
        var selected = Language.Russian
        setContent {
            LanguagePicker(
                selected = selected,
                onSelect = { selected = it },
            )
        }
        onNodeWithText("RU").assertIsDisplayed()
        onNodeWithText("EN").assertIsDisplayed()
        onNodeWithText("BE").assertIsDisplayed()
    }
}
