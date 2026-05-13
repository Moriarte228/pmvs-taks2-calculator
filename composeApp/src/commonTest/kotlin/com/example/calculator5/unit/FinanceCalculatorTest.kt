package com.example.calculator5.unit

import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.DepositInput
import com.example.calculator5.domain.FinanceCalculator
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Модульные тесты для логики сложного процента.
 *
 * Эталонные значения посчитаны вручную и независимо проверены.
 */
class FinanceCalculatorTest {

    private fun near(actual: Double, expected: Double, eps: Double = 1e-2): Boolean =
        abs(actual - expected) < eps

    @Test
    fun yearly_compounding_one_year() {
        // 1000 * (1 + 0.10) ^ 1 = 1100
        val input = DepositInput(1000.0, 10.0, 1.0, Compounding.Yearly)
        assertTrue(near(FinanceCalculator.finalAmount(input), 1100.0))
        assertTrue(near(FinanceCalculator.totalProfit(input), 100.0))
    }

    @Test
    fun monthly_compounding_one_year() {
        // 1000 * (1 + 0.12/12)^12 = 1126.825...
        val input = DepositInput(1000.0, 12.0, 1.0, Compounding.Monthly)
        assertTrue(
            near(FinanceCalculator.finalAmount(input), 1126.825030131969, eps = 1e-6),
        )
    }

    @Test
    fun quarterly_compounding_two_years() {
        // 1000 * (1 + 0.08/4)^(4*2) = 1171.6593...
        val input = DepositInput(1000.0, 8.0, 2.0, Compounding.Quarterly)
        assertTrue(
            near(FinanceCalculator.finalAmount(input), 1171.6593810022, eps = 1e-6),
        )
    }

    @Test
    fun zero_rate_keeps_principal() {
        val input = DepositInput(5000.0, 0.0, 10.0, Compounding.Monthly)
        assertEquals(5000.0, FinanceCalculator.finalAmount(input), 1e-9)
        assertEquals(0.0, FinanceCalculator.totalProfit(input), 1e-9)
    }

    @Test
    fun growth_series_is_monotonic_for_positive_rate() {
        val input = DepositInput(1000.0, 5.0, 3.0, Compounding.Monthly)
        val series = FinanceCalculator.growthSeries(input)
        assertTrue(series.size >= 2)
        for (i in 1 until series.size) {
            assertTrue(
                series[i].amount >= series[i - 1].amount,
                "growth must be monotonic non-decreasing",
            )
        }
        // Последняя точка совпадает с finalAmount
        assertTrue(
            near(series.last().amount, FinanceCalculator.finalAmount(input), eps = 1e-6),
        )
    }

    @Test
    fun compute_links_input_result_and_series() {
        val input = DepositInput(10000.0, 7.5, 5.0, Compounding.Monthly)
        val result = FinanceCalculator.compute(input)
        assertEquals(input, result.input)
        assertEquals(result.finalAmount - input.principal, result.totalProfit, 1e-9)
        assertTrue(result.growth.isNotEmpty())
    }
}
