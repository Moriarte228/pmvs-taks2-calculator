package com.example.calculator5.domain

import kotlin.math.pow

/**
 * Частота капитализации процентов.
 *
 * @property timesPerYear количество начислений процентов в году
 */
enum class Compounding(val timesPerYear: Int) {
    Monthly(12),
    Quarterly(4),
    Yearly(1);
}

/**
 * Входные параметры финансового калькулятора (вклад со сложным процентом).
 *
 * @property principal начальная сумма вклада (>= 0)
 * @property annualRatePercent годовая процентная ставка в процентах, например 7.5
 * @property years срок вклада в годах (> 0)
 * @property compounding частота капитализации
 */
data class DepositInput(
    val principal: Double,
    val annualRatePercent: Double,
    val years: Double,
    val compounding: Compounding,
)

/**
 * Точка графика роста капитала: время в годах и накопленная сумма.
 */
data class GrowthPoint(val year: Double, val amount: Double)

/**
 * Результат финансового расчёта.
 *
 * @property finalAmount конечная сумма
 * @property totalProfit общая прибыль = finalAmount - principal
 * @property growth серия точек роста капитала (для построения графика)
 */
data class DepositResult(
    val input: DepositInput,
    val finalAmount: Double,
    val totalProfit: Double,
    val growth: List<GrowthPoint>,
)

/**
 * Ошибки валидации входных параметров.
 */
sealed class ValidationError(val key: String) {
    object PrincipalNegative : ValidationError("err_principal_negative")
    object PrincipalNotANumber : ValidationError("err_principal_nan")
    object RateOutOfRange : ValidationError("err_rate_range")
    object RateNotANumber : ValidationError("err_rate_nan")
    object YearsNotPositive : ValidationError("err_years_positive")
    object YearsNotANumber : ValidationError("err_years_nan")
    object YearsTooLarge : ValidationError("err_years_too_large")
}

/**
 * Чистые функции расчёта сложного процента. Всё детерминировано — удобно
 * покрыть юнит-тестами.
 */
object FinanceCalculator {

    private const val POINTS_PER_YEAR = 12 // плотность точек для графика
    private const val MAX_YEARS = 100.0

    /**
     * Возвращает конечную сумму вклада по формуле сложного процента:
     *
     *     A = P * (1 + r / n) ^ (n * t)
     *
     * где P — начальная сумма, r — годовая ставка (в долях), n — число
     * начислений в году, t — срок в годах.
     */
    fun finalAmount(input: DepositInput): Double {
        val r = input.annualRatePercent / 100.0
        val n = input.compounding.timesPerYear
        val t = input.years
        return input.principal * (1.0 + r / n).pow(n * t)
    }

    /**
     * Возвращает прибыль = конечная сумма - начальная.
     */
    fun totalProfit(input: DepositInput): Double =
        finalAmount(input) - input.principal

    /**
     * Возвращает серию точек роста капитала с шагом примерно [POINTS_PER_YEAR]
     * на год. Удобно для графика.
     */
    fun growthSeries(input: DepositInput): List<GrowthPoint> {
        val totalPoints = (input.years * POINTS_PER_YEAR).toInt().coerceAtLeast(2)
        val r = input.annualRatePercent / 100.0
        val n = input.compounding.timesPerYear
        return (0..totalPoints).map { i ->
            val t = input.years * i / totalPoints
            val a = input.principal * (1.0 + r / n).pow(n * t)
            GrowthPoint(year = t, amount = a)
        }
    }

    /**
     * Полный расчёт, объединяющий итог и серию точек.
     */
    fun compute(input: DepositInput): DepositResult {
        val series = growthSeries(input)
        val finalA = series.lastOrNull()?.amount ?: input.principal
        return DepositResult(
            input = input,
            finalAmount = finalA,
            totalProfit = finalA - input.principal,
            growth = series,
        )
    }

    /**
     * Валидирует входные параметры. Возвращает список ошибок (пустой, если всё
     * хорошо).
     */
    fun validate(input: DepositInput): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (input.principal.isNaN()) errors += ValidationError.PrincipalNotANumber
        else if (input.principal < 0.0) errors += ValidationError.PrincipalNegative

        if (input.annualRatePercent.isNaN()) errors += ValidationError.RateNotANumber
        else if (input.annualRatePercent < 0.0 || input.annualRatePercent > 1000.0)
            errors += ValidationError.RateOutOfRange

        if (input.years.isNaN()) errors += ValidationError.YearsNotANumber
        else if (input.years <= 0.0) errors += ValidationError.YearsNotPositive
        else if (input.years > MAX_YEARS) errors += ValidationError.YearsTooLarge

        return errors
    }
}
