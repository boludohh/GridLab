package com.gridlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gridlab.app.feature.editor.grid.GridConfigScreen
import com.gridlab.app.feature.projects.ProjectListScreen
import com.gridlab.app.ui.theme.GridLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridLabTheme {
                var openDocumentId by remember { mutableStateOf<String?>(null) }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val pad = Modifier.padding(innerPadding)
                    val documentId = openDocumentId
                    if (documentId == null) {
                        ProjectListScreen(
                            onOpenProject = { id -> openDocumentId = id },
                            modifier = pad,
                        )
                    } else {
                        GridConfigScreen(
                            documentId = documentId,
                            onBack = { openDocumentId = null },
                            modifier = pad,
                        )
                    }
                }
            }
        }
    }
}
