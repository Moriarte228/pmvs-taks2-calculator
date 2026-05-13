# Calculator5 — Финансовый калькулятор (Compose Multiplatform)

Кроссплатформенное приложение «Финансовый калькулятор» на Compose
Multiplatform. Считает сложный процент по вкладу: конечную сумму и общую
прибыль, визуализирует рост капитала на графике (Canvas API), сохраняет
историю расчётов и поддерживает три языка интерфейса.

Поддерживаемые таргеты: **Android**, **iOS**, **Linux/Desktop (JVM)**, **Web (wasmJs)**.

## Стек

| Категория      | Технология                                  |
| -------------- | ------------------------------------------- |
| Язык           | Kotlin 2.1.21                               |
| UI             | Compose Multiplatform 1.7.3 (Material 3)    |
| Сериализация   | kotlinx.serialization 1.7.3                 |
| Асинхронность  | Kotlin Coroutines 1.9.0                     |
| Хранилище      | multiplatform-settings 1.2.0                |
| Сборка         | Gradle 8.9 + AGP 8.7.3                      |
| Тесты          | kotlin.test + compose.uiTest                |

## Функциональность

- Поля ввода: начальная сумма, годовая процентная ставка, срок в годах,
  капитализация (ежемесячно / ежеквартально / ежегодно).
- Расчёт конечной суммы и общей прибыли по формуле сложного процента:
  `A = P · (1 + r/n)^(n·t)`.
- Визуализация роста капитала на графике (Canvas API).
- Сохранение истории расчётов (до 20 последних) и языка интерфейса
  через multiplatform-settings (SharedPreferences / NSUserDefaults /
  Properties / localStorage в зависимости от платформы).
- Валидация ввода с локализованными сообщениями об ошибках.
- Три языка: русский, английский, белорусский.
- Анимация появления результата и анимированная отрисовка графика.

## Архитектура

```
composeApp/src/
├── commonMain/        — общий код (UI, логика, i18n, данные)
│   ├── domain/        — FinanceCalculator, Formatting, валидация
│   ├── ui/            — Compose-экраны и компоненты, ViewModel
│   ├── i18n/          — словари переводов
│   ├── data/          — HistoryRepository
│   └── platform/      — expect val currentPlatform
├── androidMain/       — MainActivity, ресурсы, actual Platform = Android
├── iosMain/           — MainViewController, actual Platform = Ios
├── desktopMain/       — Main.kt (JVM), actual Platform = Desktop
├── wasmJsMain/        — Main.kt (browser), index.html, actual Platform = Web
├── commonTest/        — модульные и интеграционные тесты
└── desktopTest/       — UI-тесты Compose
```

## Платформенные отличия UI (как требует задание)

| Аспект               | Android                | iOS                   | Linux/Desktop                       | Web                              |
| -------------------- | ---------------------- | --------------------- | ----------------------------------- | -------------------------------- |
| Поля ввода           | OutlinedTextField      | OutlinedTextField     | TextField + кнопки `−` / `+` (SpinBox-стиль) | OutlinedTextField (адаптивно)   |
| Срок                 | Slider                 | Segmented Picker      | SpinBox                             | TextField                       |
| Ставка               | Slider                 | TextField             | SpinBox                             | TextField                       |
| Кнопки               | Material 3 Filled      | Плоские, `elevation=0`| Стандартные                         | Filled / Outlined / TextButton  |
| График — линия       | Толщина 3.5 dp         | Тонкая (2 dp)         | Средняя (2.5 dp), чёткие рамки     | 3 dp                             |
| График — заливка     | **Градиентная**        | Без градиента         | Без градиента                       | Градиентная                     |
| График — сетка       | Пунктирная             | Сплошная              | Сплошная                            | Пунктирная                      |
| График — интеракция  | —                      | —                     | —                                   | **Подсветка точки** под курсором |
| Лейаут               | Вертикальный скролл    | Вертикальный скролл   | Две колонки (форма + результаты)    | 1 или 2 колонки (адаптивно)     |
| Размеры              | По ширине экрана       | По ширине экрана      | Фиксированная ширина формы (320 dp) | Адаптивно по `BoxWithConstraints`|

Платформа определяется через `expect val currentPlatform`, а `App()`
делегирует отрисовку в соответствующую функцию `AndroidScreen`,
`IosScreen`, `DesktopScreen` или `WebScreen`.

## Тестирование

В проекте реализовано три вида тестов:

### Модульные тесты (`commonTest/unit/`)

- `FinanceCalculatorTest` — формулы сложного процента, граничные случаи,
  монотонность серии роста.
- `ValidationTest` — все правила валидации, парсинг чисел, форматирование.
- `I18nTest` — целостность словарей переводов.

### Интеграционные тесты (`commonTest/integration/`)

- `CalculatorViewModelIntegrationTest` — сквозной флоу
  ViewModel + Repository + математика: расчёт, ошибки, сохранение и
  загрузка истории между «сессиями», смена языка с персистентностью,
  очистка истории, сброс формы.

### UI-тесты (`desktopTest/ui/`)

- `UiTest` — рендеринг результатов, переключение языка (3 локали),
  скрытие блока при `null`, рендеринг графика, граничный случай с
  одной точкой графика.

### Запуск тестов

```bash
./gradlew :composeApp:desktopTest    # UI + общие тесты
./gradlew check                       # все проверки
```

## Сборка

```bash
# Android APK
./gradlew :composeApp:assembleDebug

# Desktop (Linux/Mac/Win) — distributable
./gradlew :composeApp:packageDistributionForCurrentOS

# Web (wasmJs)
./gradlew :composeApp:wasmJsBrowserDistribution

# Web — dev-сервер
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# iOS framework — собирается через ./gradlew, открывается через iosApp/iosApp.xcodeproj
```

## CI

`.github/workflows/build.yml` собирает все четыре платформы и запускает
тесты на каждый push и pull request. Артефакты (APK, AppImage, .deb,
веб-бандл, iOS-фреймворк, отчёты тестов) сохраняются в Actions.

## Маппинг требований лабораторной работы

| Требование                                                | Где реализовано                                              |
| --------------------------------------------------------- | ------------------------------------------------------------ |
| Финансовый калькулятор, формула сложного процента          | `domain/FinanceCalculator.kt`                                |
| Поля ввода: сумма, ставка, срок, капитализация             | `ui/App.kt` (все 4 платформенных экрана)                     |
| Расчёт конечной суммы и прибыли                            | `FinanceCalculator.compute()`                                |
| График роста капитала (Canvas API в Compose)               | `ui/GrowthChart.kt`                                          |
| Валидация ввода                                            | `FinanceCalculator.validate()`, `Formatting.parseNumber()`   |
| Android — TextField, Slider, Material 3, градиентная заливка | `AndroidScreen()` + `ChartStyle(gradientFill = true)`      |
| iOS — Picker вместо Slider, плоские кнопки, тонкие линии    | `IosScreen()` + `elevation = 0`, `ChartStyle(lineWidthDp=2f)` |
| Linux — SpinBox, простые графики, фиксированные размеры    | `DesktopScreen()` + `SpinBoxField`, чёткие рамки             |
| Web — интерактивные графики, адаптивные формы               | `WebScreen()` + `ChartStyle(interactive = true)`             |
| Анимация                                                   | `AnimatedVisibility` в `ResultBlock`, `Animatable` в `GrowthChart` |
| Сохранение результатов                                     | `HistoryRepository`                                          |
| Визуализация данных                                        | `GrowthChart.kt`                                             |
| Модульные тесты (3–7)                                      | 16 тестов в `commonTest/unit/`                               |
| UI-тесты (3–7)                                             | 7 тестов в `desktopTest/ui/`                                 |
| Интеграционные тесты (3–7)                                 | 7 тестов в `commonTest/integration/`                         |
| 3 языка интерфейса                                         | `i18n/Strings.kt` (ru / en / be)                             |
| Обработка исключений с выводом в консоль                    | `try/catch` + `println(...)` в `HistoryRepository`, `ViewModel` |
| GitHub Actions автосборка                                  | `.github/workflows/build.yml`                                |

## Лицензия

Учебный проект.
