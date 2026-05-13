package com.example.calculator5.data

import com.example.calculator5.domain.Compounding
import com.example.calculator5.domain.DepositInput
import com.example.calculator5.domain.DepositResult
import kotlinx.serialization.encodeToString
import com.example.calculator5.i18n.Language
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Сериализуемая запись истории расчёта. Содержит входные параметры и
 * результат (конечная сумма, прибыль).
 */
@Serializable
data class HistoryRecord(
    val principal: Double,
    val ratePercent: Double,
    val years: Double,
    val compounding: String, // имя enum'а
    val finalAmount: Double,
    val totalProfit: Double,
    val timestamp: Long,
) {
    fun toInput(): DepositInput = DepositInput(
        principal = principal,
        annualRatePercent = ratePercent,
        years = years,
        compounding = Compounding.valueOf(compounding),
    )
}

/**
 * Простой репозиторий настроек и истории. Использует абстракцию
 * multiplatform-settings, поэтому одинаково работает на Android, iOS, Desktop
 * и Web.
 */
class HistoryRepository(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Загрузить список записей. При ошибке десериализации возвращает пустой
     * список и пишет сообщение в консоль (см. нефункциональные требования).
     */
    fun loadHistory(): List<HistoryRecord> {
        val raw = settings.getStringOrNull(KEY_HISTORY) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (e: Throwable) {
            println("HistoryRepository: failed to decode history: ${e.message}")
            emptyList()
        }
    }

    /**
     * Сохранить запись (добавляется в начало списка, общий размер ограничен).
     */
    fun saveResult(result: DepositResult) {
        val record = HistoryRecord(
            principal = result.input.principal,
            ratePercent = result.input.annualRatePercent,
            years = result.input.years,
            compounding = result.input.compounding.name,
            finalAmount = result.finalAmount,
            totalProfit = result.totalProfit,
            timestamp = currentTimeMillis(),
        )
        val updated = (listOf(record) + loadHistory()).take(MAX_RECORDS)
        try {
            settings.putString(KEY_HISTORY, json.encodeToString(updated))
        } catch (e: Throwable) {
            println("HistoryRepository: failed to encode history: ${e.message}")
        }
    }

    fun clearHistory() {
        settings.remove(KEY_HISTORY)
    }

    fun loadLanguage(): Language {
        val code = settings.getStringOrNull(KEY_LANGUAGE) ?: return Language.Russian
        return Language.entries.firstOrNull { it.code == code } ?: Language.Russian
    }

    fun saveLanguage(language: Language) {
        settings.putString(KEY_LANGUAGE, language.code)
    }

    companion object {
        private const val KEY_HISTORY = "calc_history_v1"
        private const val KEY_LANGUAGE = "calc_language_v1"
        private const val MAX_RECORDS = 20
    }
}

/**
 * Системное время в миллисекундах. Платформенно-зависимо.
 */
expect fun currentTimeMillis(): Long
