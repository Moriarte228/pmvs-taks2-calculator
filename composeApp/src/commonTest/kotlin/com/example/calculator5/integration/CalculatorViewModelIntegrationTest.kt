package com.example.calculator5.integration

import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.domain.Compounding
import com.example.calculator5.i18n.Language
import com.example.calculator5.testutil.InMemorySettings
import com.example.calculator5.ui.CalculatorViewModel
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Интеграционные тесты: проверяют сквозную работу
 * ViewModel + Repository + математика + i18n.
 */
class CalculatorViewModelIntegrationTest {

    private fun newVm(): CalculatorViewModel {
        val repo = HistoryRepository(InMemorySettings())
        return CalculatorViewModel(repo)
    }

    @Test
    fun fresh_viewmodel_calculates_default_input_on_start() {
        val vm = newVm()
        val state = vm.state.value
        assertNotNull(state.result, "result must be computed on bootstrap")
        assertTrue(state.errors.isEmpty())
        assertTrue(state.result!!.finalAmount > 0.0)
    }

    @Test
    fun changing_inputs_and_calculating_updates_result() {
        val vm = newVm()
        vm.onPrincipalChange("1000")
        vm.onRateChange("10")
        vm.onYearsChange("1")
        vm.onCompoundingChange(Compounding.Yearly)
        vm.calculate()
        val result = vm.state.value.result
        assertNotNull(result)
        // 1000 * 1.10 = 1100
        assertTrue(abs(result.finalAmount - 1100.0) < 1e-6)
    }

    @Test
    fun invalid_input_produces_localized_errors_and_no_result() {
        val vm = newVm()
        vm.onPrincipalChange("not a number")
        vm.onRateChange("5")
        vm.onYearsChange("1")
        vm.calculate()
        val state = vm.state.value
        assertNull(state.result)
        assertTrue(state.errors.isNotEmpty())
        val message = vm.errorText(state.errors.first())
        assertTrue(message.isNotBlank())
    }

    @Test
    fun save_and_load_history_persists_results() {
        val settings = InMemorySettings()
        // Первая «сессия» — сохраняем результат
        run {
            val vm = CalculatorViewModel(HistoryRepository(settings))
            vm.onPrincipalChange("2000")
            vm.onRateChange("6")
            vm.onYearsChange("2")
            vm.calculate()
            vm.saveCurrentResult()
            assertTrue(vm.state.value.history.isNotEmpty())
        }
        // Вторая «сессия» — те же settings: история должна подгрузиться
        run {
            val vm = CalculatorViewModel(HistoryRepository(settings))
            assertTrue(vm.state.value.history.isNotEmpty())
            assertEquals(2000.0, vm.state.value.history.first().principal, 1e-6)
        }
    }

    @Test
    fun changing_language_is_persisted() {
        val settings = InMemorySettings()
        run {
            val vm = CalculatorViewModel(HistoryRepository(settings))
            vm.onLanguageChange(Language.English)
            assertEquals(Language.English, vm.state.value.language)
        }
        run {
            val vm = CalculatorViewModel(HistoryRepository(settings))
            assertEquals(Language.English, vm.state.value.language)
        }
    }

    @Test
    fun clear_history_removes_all_records() {
        val vm = newVm()
        vm.calculate()
        vm.saveCurrentResult()
        assertTrue(vm.state.value.history.isNotEmpty())
        vm.clearHistory()
        assertTrue(vm.state.value.history.isEmpty())
    }

    @Test
    fun reset_returns_form_to_defaults_but_preserves_language_and_history() {
        val vm = newVm()
        vm.onLanguageChange(Language.Belarusian)
        vm.onPrincipalChange("99999")
        vm.calculate()
        vm.saveCurrentResult()
        vm.reset()
        assertEquals(Language.Belarusian, vm.state.value.language)
        assertTrue(vm.state.value.history.isNotEmpty())
        assertEquals("10000", vm.state.value.principalText) // дефолт
    }
}
