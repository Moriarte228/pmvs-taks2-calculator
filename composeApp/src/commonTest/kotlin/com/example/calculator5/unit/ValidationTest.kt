package com.example.calculator5.unit

import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.DepositInput
import com.example.calculator5.domain.FinanceCalculator
import com.example.calculator5.domain.Formatting
import com.example.calculator5.domain.ValidationError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ValidationTest {

    @Test
    fun valid_input_produces_no_errors() {
        val input = DepositInput(1000.0, 5.0, 3.0, Compounding.Monthly)
        assertTrue(FinanceCalculator.validate(input).isEmpty())
    }

    @Test
    fun negative_principal_is_reported() {
        val input = DepositInput(-100.0, 5.0, 3.0, Compounding.Monthly)
        val errors = FinanceCalculator.validate(input)
        assertTrue(ValidationError.PrincipalNegative in errors)
    }

    @Test
    fun nan_principal_is_reported() {
        val input = DepositInput(Double.NaN, 5.0, 3.0, Compounding.Monthly)
        val errors = FinanceCalculator.validate(input)
        assertTrue(ValidationError.PrincipalNotANumber in errors)
    }

    @Test
    fun zero_years_is_reported() {
        val input = DepositInput(1000.0, 5.0, 0.0, Compounding.Monthly)
        val errors = FinanceCalculator.validate(input)
        assertTrue(ValidationError.YearsNotPositive in errors)
    }

    @Test
    fun out_of_range_rate_is_reported() {
        val input = DepositInput(1000.0, 5000.0, 1.0, Compounding.Monthly)
        val errors = FinanceCalculator.validate(input)
        assertTrue(ValidationError.RateOutOfRange in errors)
    }

    @Test
    fun parse_number_supports_comma_and_spaces() {
        assertEquals(1234.56, Formatting.parseNumber("1 234,56"))
        assertEquals(0.0, Formatting.parseNumber("0"))
        assertNull(Formatting.parseNumber("abc"))
        assertNull(Formatting.parseNumber(""))
    }

    @Test
    fun money_formatting_groups_digits() {
        // 1234567.5 -> "1 234 567.50"
        assertEquals("1 234 567.50", Formatting.money(1234567.5))
        assertEquals("0.00", Formatting.money(0.0))
        assertEquals("-42.10", Formatting.money(-42.1))
    }
}
