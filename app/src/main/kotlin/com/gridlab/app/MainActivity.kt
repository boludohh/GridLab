package com.gridlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.gridlab.app.feature.editor.paper.PaperConfigScreen
import com.gridlab.app.ui.theme.GridLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PaperConfigScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
