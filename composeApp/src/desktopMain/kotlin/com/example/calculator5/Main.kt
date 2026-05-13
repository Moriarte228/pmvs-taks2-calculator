package com.example.calculator5

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.ui.App
import com.example.calculator5.ui.CalculatorViewModel
import com.russhwolf.settings.Settings

fun main() = application {
    val repository = HistoryRepository(Settings())
    val viewModel = CalculatorViewModel(repository)

    Window(
        onCloseRequest = ::exitApplication,
        title = "Calculator5",
        state = rememberWindowState(width = 1000.dp, height = 720.dp),
    ) {
        App(viewModel)
    }
}
