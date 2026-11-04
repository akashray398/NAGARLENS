package com.nagarlens.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nagarlens.app.core.designsystem.theme.NagarlensTheme
import com.nagarlens.app.core.shell.NagarlensApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NagarlensTheme {
                NagarlensApp()
            }
        }
    }
}
