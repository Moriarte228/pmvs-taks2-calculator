package com.example.calculator5.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator5.data.HistoryRecord
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.DepositInput
import com.example.calculator5.domain.DepositResult
import com.example.calculator5.domain.FinanceCalculator
import com.example.calculator5.domain.Formatting
import com.example.calculator5.domain.ValidationError
import com.example.calculator5.i18n.Language
import com.example.calculator5.i18n.StringKey
import com.example.calculator5.i18n.translate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Состояние экрана калькулятора. Поля ввода — строки (для удобной правки),
 * результат и ошибки валидации — отдельно.
 */
data class CalculatorUiState(
    val principalText: String = "10000",
    val ratePercentText: String = "7.5",
    val yearsText: String = "5",
    val compounding: Compounding = Compounding.Monthly,
    val language: Language = Language.Russian,

    val result: DepositResult? = null,
    val errors: List<ValidationError> = emptyList(),
    val savedHintVisible: Boolean = false,

    val history: List<HistoryRecord> = emptyList(),
)

class CalculatorViewModel(
    private val repository: HistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CalculatorUiState())
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    init {
        val lang = repository.loadLanguage()
        val history = repository.loadHistory()
        _state.update { it.copy(language = lang, history = history) }
        // Считаем результат для дефолтных значений сразу, чтобы экран не был пустым.
        calculate()
    }

    fun onPrincipalChange(v: String) =
        _state.update { it.copy(principalText = v, savedHintVisible = false) }

    fun onRateChange(v: String) =
        _state.update { it.copy(ratePercentText = v, savedHintVisible = false) }

    fun onYearsChange(v: String) =
        _state.update { it.copy(yearsText = v, savedHintVisible = false) }

    fun onCompoundingChange(c: Compounding) =
        _state.update { it.copy(compounding = c, savedHintVisible = false) }

    fun onLanguageChange(lang: Language) {
        repository.saveLanguage(lang)
        _state.update { it.copy(language = lang) }
    }

    /**
     * Главная команда: распарсить поля, провалидировать и посчитать.
     * Ошибки парсинга/валидации отображаются под полями.
     */
    fun calculate() {
        val s = _state.value
        val principal = Formatting.parseNumber(s.principalText) ?: Double.NaN
        val rate = Formatting.parseNumber(s.ratePercentText) ?: Double.NaN
        val years = Formatting.parseNumber(s.yearsText) ?: Double.NaN

        val input = DepositInput(
            principal = principal,
            annualRatePercent = rate,
            years = years,
            compounding = s.compounding,
        )
        val errors = FinanceCalculator.validate(input)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors, result = null) }
            println("Calculator: validation failed: ${errors.joinToString { e -> e.key }}")
            return
        }
        try {
            val result = FinanceCalculator.compute(input)
            _state.update { it.copy(errors = emptyList(), result = result) }
        } catch (t: Throwable) {
            println("Calculator: unexpected error: ${t.message}")
            _state.update { it.copy(errors = emptyList(), result = null) }
        }
    }

    fun reset() {
        _state.update {
            CalculatorUiState(language = it.language, history = it.history)
        }
        calculate()
    }

    fun saveCurrentResult() {
        val r = _state.value.result ?: return
        repository.saveResult(r)
        viewModelScope.launch {
            _state.update {
                it.copy(history = repository.loadHistory(), savedHintVisible = true)
            }
        }
    }

    fun clearHistory() {
        repository.clearHistory()
        _state.update { it.copy(history = emptyList()) }
    }

    fun loadFromHistory(record: HistoryRecord) {
        _state.update {
            it.copy(
                principalText = Formatting.money(record.principal).replace(" ", ""),
                ratePercentText = record.ratePercent.toString(),
                yearsText = record.years.toString(),
                compounding = Compounding.valueOf(record.compounding),
            )
        }
        calculate()
    }

    /**
     * Удобный хелпер для получения локализованного сообщения об ошибке.
     */
    fun errorText(error: ValidationError): String {
        val key = when (error) {
            ValidationError.PrincipalNegative -> StringKey.ErrPrincipalNegative
            ValidationError.PrincipalNotANumber -> StringKey.ErrPrincipalNan
            ValidationError.RateOutOfRange -> StringKey.ErrRateRange
            ValidationError.RateNotANumber -> StringKey.ErrRateNan
            ValidationError.YearsNotPositive -> StringKey.ErrYearsPositive
            ValidationError.YearsNotANumber -> StringKey.ErrYearsNan
            ValidationError.YearsTooLarge -> StringKey.ErrYearsTooLarge
        }
        return translate(key, _state.value.language)
    }

    fun t(key: StringKey): String = translate(key, _state.value.language)
}
