package com.example.calculator5

import androidx.compose.ui.window.ComposeUIViewController
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.ui.App
import com.example.calculator5.ui.CalculatorViewModel
import com.russhwolf.settings.Settings

@Suppress("FunctionName", "unused") // используется из Swift через MainViewControllerKt
fun MainViewController() = ComposeUIViewController {
    val repository = HistoryRepository(Settings())
    val viewModel = CalculatorViewModel(repository)
    App(viewModel)
}
