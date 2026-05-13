package com.example.calculator5.domain

import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Форматирование чисел не зависит от платформенной локали, чтобы
 * результаты выглядели одинаково на Android, iOS, Linux и в браузере и
 * чтобы тесты были стабильными.
 */
object Formatting {

    /**
     * Форматирует число как денежное значение с двумя знаками после запятой
     * и пробелами между группами разрядов целой части.
     *
     * Примеры: 1234.5 -> "1 234.50",  -42.0 -> "-42.00".
     */
    fun money(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "—"
        val sign = if (value < 0) "-" else ""
        val cents = (abs(value) * 100.0).roundToLong()
        val whole = cents / 100
        val fraction = cents % 100
        val wholeStr = groupDigits(whole.toString())
        val fractionStr = fraction.toString().padStart(2, '0')
        return "$sign$wholeStr.$fractionStr"
    }

    /**
     * Простая группировка разрядов справа налево пробелами по три цифры.
     */
    private fun groupDigits(digits: String): String {
        if (digits.length <= 3) return digits
        val sb = StringBuilder()
        val rev = digits.reversed()
        for (i in rev.indices) {
            if (i > 0 && i % 3 == 0) sb.append(' ')
            sb.append(rev[i])
        }
        return sb.reverse().toString()
    }

    /**
     * Парсит число, поддерживая запятую как десятичный разделитель и
     * пробелы как разделители групп. Возвращает null, если не удалось.
     */
    fun parseNumber(text: String): Double? {
        val cleaned = text.trim().replace(" ", "").replace(',', '.')
        if (cleaned.isEmpty()) return null
        return cleaned.toDoubleOrNull()
    }
}
