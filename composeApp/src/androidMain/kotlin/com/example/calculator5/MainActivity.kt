package com.example.calculator5

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.calculator5.data.HistoryRepository
import com.example.calculator5.ui.App
import com.example.calculator5.ui.CalculatorViewModel
import com.russhwolf.settings.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = HistoryRepository(Settings())
        val viewModel = CalculatorViewModel(repository)

        setContent { App(viewModel) }
    }
}
