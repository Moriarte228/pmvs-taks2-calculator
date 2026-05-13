package com.example.calculator5.i18n

/**
 * Поддерживаемые языки интерфейса.
 */
enum class Language(val code: String, val displayName: String) {
    Russian("ru", "Русский"),
    English("en", "English"),
    Belarusian("be", "Беларуская"),
}

/**
 * Ключи строк. Используются и UI, и формами валидации.
 */
enum class StringKey {
    AppTitle,
    Principal,
    AnnualRate,
    Years,
    Compounding,
    CompMonthly,
    CompQuarterly,
    CompYearly,
    Calculate,
    Reset,
    FinalAmount,
    TotalProfit,
    GrowthChart,
    Language,
    History,
    NoHistory,
    Clear,
    Save,
    SavedHint,

    ErrPrincipalNegative,
    ErrPrincipalNan,
    ErrRateRange,
    ErrRateNan,
    ErrYearsPositive,
    ErrYearsNan,
    ErrYearsTooLarge,
}

private val ru: Map<StringKey, String> = mapOf(
    StringKey.AppTitle to "Финансовый калькулятор",
    StringKey.Principal to "Начальная сумма",
    StringKey.AnnualRate to "Годовая ставка, %",
    StringKey.Years to "Срок, лет",
    StringKey.Compounding to "Капитализация",
    StringKey.CompMonthly to "Ежемесячно",
    StringKey.CompQuarterly to "Ежеквартально",
    StringKey.CompYearly to "Ежегодно",
    StringKey.Calculate to "Рассчитать",
    StringKey.Reset to "Сбросить",
    StringKey.FinalAmount to "Конечная сумма",
    StringKey.TotalProfit to "Общая прибыль",
    StringKey.GrowthChart to "Рост капитала",
    StringKey.Language to "Язык",
    StringKey.History to "История",
    StringKey.NoHistory to "Нет сохранённых расчётов",
    StringKey.Clear to "Очистить",
    StringKey.Save to "Сохранить",
    StringKey.SavedHint to "Результат сохранён",
    StringKey.ErrPrincipalNegative to "Начальная сумма должна быть ≥ 0",
    StringKey.ErrPrincipalNan to "Введите число (начальная сумма)",
    StringKey.ErrRateRange to "Ставка должна быть от 0 до 1000",
    StringKey.ErrRateNan to "Введите число (ставка)",
    StringKey.ErrYearsPositive to "Срок должен быть > 0",
    StringKey.ErrYearsNan to "Введите число (срок)",
    StringKey.ErrYearsTooLarge to "Срок слишком велик (макс. 100 лет)",
)

private val en: Map<StringKey, String> = mapOf(
    StringKey.AppTitle to "Finance Calculator",
    StringKey.Principal to "Initial amount",
    StringKey.AnnualRate to "Annual rate, %",
    StringKey.Years to "Term, years",
    StringKey.Compounding to "Compounding",
    StringKey.CompMonthly to "Monthly",
    StringKey.CompQuarterly to "Quarterly",
    StringKey.CompYearly to "Yearly",
    StringKey.Calculate to "Calculate",
    StringKey.Reset to "Reset",
    StringKey.FinalAmount to "Final amount",
    StringKey.TotalProfit to "Total profit",
    StringKey.GrowthChart to "Capital growth",
    StringKey.Language to "Language",
    StringKey.History to "History",
    StringKey.NoHistory to "No saved calculations",
    StringKey.Clear to "Clear",
    StringKey.Save to "Save",
    StringKey.SavedHint to "Result saved",
    StringKey.ErrPrincipalNegative to "Initial amount must be ≥ 0",
    StringKey.ErrPrincipalNan to "Enter a number (initial amount)",
    StringKey.ErrRateRange to "Rate must be 0..1000",
    StringKey.ErrRateNan to "Enter a number (rate)",
    StringKey.ErrYearsPositive to "Term must be > 0",
    StringKey.ErrYearsNan to "Enter a number (term)",
    StringKey.ErrYearsTooLarge to "Term too long (max 100 years)",
)

private val be: Map<StringKey, String> = mapOf(
    StringKey.AppTitle to "Фінансавы калькулятар",
    StringKey.Principal to "Пачатковая сума",
    StringKey.AnnualRate to "Гадавая стаўка, %",
    StringKey.Years to "Тэрмін, гадоў",
    StringKey.Compounding to "Капіталізацыя",
    StringKey.CompMonthly to "Штомесяц",
    StringKey.CompQuarterly to "Штоквартальна",
    StringKey.CompYearly to "Штогод",
    StringKey.Calculate to "Разлічыць",
    StringKey.Reset to "Скінуць",
    StringKey.FinalAmount to "Канчатковая сума",
    StringKey.TotalProfit to "Агульны прыбытак",
    StringKey.GrowthChart to "Рост капіталу",
    StringKey.Language to "Мова",
    StringKey.History to "Гісторыя",
    StringKey.NoHistory to "Няма захаваных разлікаў",
    StringKey.Clear to "Ачысціць",
    StringKey.Save to "Захаваць",
    StringKey.SavedHint to "Вынік захаваны",
    StringKey.ErrPrincipalNegative to "Пачатковая сума павінна быць ≥ 0",
    StringKey.ErrPrincipalNan to "Увядзіце лік (пачатковая сума)",
    StringKey.ErrRateRange to "Стаўка павінна быць 0..1000",
    StringKey.ErrRateNan to "Увядзіце лік (стаўка)",
    StringKey.ErrYearsPositive to "Тэрмін павінен быць > 0",
    StringKey.ErrYearsNan to "Увядзіце лік (тэрмін)",
    StringKey.ErrYearsTooLarge to "Тэрмін занадта вялікі (макс. 100 гадоў)",
)

/**
 * Достать строку по ключу и языку. Если перевод отсутствует — fallback на
 * русский, потом на сам ключ.
 */
fun translate(key: StringKey, language: Language): String {
    val dict = when (language) {
        Language.Russian -> ru
        Language.English -> en
        Language.Belarusian -> be
    }
    return dict[key] ?: ru[key] ?: key.name
}
