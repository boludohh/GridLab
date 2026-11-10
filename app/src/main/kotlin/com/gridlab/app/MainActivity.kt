package com.gridlab.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gridlab.app.feature.editor.grid.GridConfigScreen
import com.gridlab.app.feature.main.MainScreen
import com.gridlab.app.feature.settings.AboutScreen
import com.gridlab.app.feature.settings.OpenSourceLicensesScreen
import com.gridlab.app.feature.settings.SettingsScreen
import com.gridlab.app.ui.configureEdgeToEdge
import com.gridlab.app.ui.theme.GridLabTheme
import dagger.hilt.android.AndroidEntryPoint

private sealed interface RootDestination {
    data object Home : RootDestination
    data object Settings : RootDestination
    data object OpenSourceLicenses : RootDestination
    data object About : RootDestination
    data class Editor(val documentId: String) : RootDestination
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Permission result handled; no action needed here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request storage permission on app launch for API 27-28 only
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        configureEdgeToEdge()
        setContent {
            GridLabTheme {
                var destination by remember {
                    mutableStateOf<RootDestination>(RootDestination.Home)
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val pad = Modifier.padding(innerPadding)
                    when (val dest = destination) {
                        is RootDestination.Home -> {
                            MainScreen(
                                onOpenProject = { id ->
                                    destination = RootDestination.Editor(id)
                                },
                                onOpenSettings = {
                                    destination = RootDestination.Settings
                                },
                                modifier = pad,
                            )
                        }
                        is RootDestination.Settings -> {
                            SettingsScreen(
                                onBack = { destination = RootDestination.Home },
                                onOpenAbout = {
                                    destination = RootDestination.About
                                },
                                onOpenSourceLicenses = {
                                    destination = RootDestination.OpenSourceLicenses
                                },
                                modifier = pad,
                            )
                        }
                        is RootDestination.About -> {
                            AboutScreen(
                                onBack = { destination = RootDestination.Settings },
                                onOpenSourceLicenses = {
                                    destination = RootDestination.OpenSourceLicenses
                                },
                                modifier = pad,
                            )
                        }
                        is RootDestination.OpenSourceLicenses -> {
                            OpenSourceLicensesScreen(
                                onBack = { destination = RootDestination.Settings },
                                modifier = pad,
                            )
                        }
                        is RootDestination.Editor -> {
                            GridConfigScreen(
                                documentId = dest.documentId,
                                onBack = { destination = RootDestination.Home },
                                modifier = pad,
                            )
                        }
                    }
                }
            }
        }
    }
}