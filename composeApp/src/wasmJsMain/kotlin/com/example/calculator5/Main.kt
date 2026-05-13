package com.example.calculator5

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.ui.App
import com.example.calculator5.ui.CalculatorViewModel
import com.russhwolf.settings.Settings
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repository = HistoryRepository(Settings())
    val viewModel = CalculatorViewModel(repository)

    ComposeViewport(document.body!!) {
        App(viewModel)
    }
}
