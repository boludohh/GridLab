package com.gridlab.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gridlab.app.core.preferences.AppLanguage
import com.gridlab.app.core.preferences.ThemePreferencesRepository
import com.gridlab.app.ui.AppLocale
import com.gridlab.app.feature.editor.grid.GridConfigScreen
import com.gridlab.app.feature.main.MainScreen
import com.gridlab.app.feature.settings.AboutScreen
import com.gridlab.app.feature.settings.LanguageAppearanceScreen
import com.gridlab.app.feature.settings.OpenSourceLicensesScreen
import com.gridlab.app.feature.settings.SettingsScreen
import com.gridlab.app.ui.configureEdgeToEdge
import com.gridlab.app.ui.theme.GridLabTheme
import com.gridlab.app.ui.theme.GridLabThemeMode
import com.gridlab.app.ui.theme.ThemeRevealOverlay
import com.gridlab.app.ui.theme.ThemeRevealState
import com.gridlab.app.ui.theme.captureActivityBitmap
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

private sealed interface RootDestination {
    data object Home : RootDestination
    data object Settings : RootDestination
    data object OpenSourceLicenses : RootDestination
    data object About : RootDestination
    data object LanguageAppearance : RootDestination
    data class Editor(val documentId: String) : RootDestination
}

private val RevealEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private const val RevealDurationMs = 520

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferencesRepository

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Permission result handled; no action needed here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        configureEdgeToEdge()
        setContent {
            val storedMode by themePreferences.themeMode.collectAsStateWithLifecycle(initialValue = null)
            val storedLanguage by themePreferences.language.collectAsStateWithLifecycle(initialValue = null)
            val systemDark = isSystemInDarkTheme()
            val resolvedMode = storedMode ?: if (systemDark) {
                GridLabThemeMode.Dark
            } else {
                GridLabThemeMode.Light
            }

            var themeMode by remember { mutableStateOf(resolvedMode) }
            LaunchedEffect(resolvedMode) {
                themeMode = resolvedMode
            }

            LaunchedEffect(storedLanguage) {
                AppLocale.apply(storedLanguage)
            }

            val revealState = remember { ThemeRevealState() }
            var themeChangePending by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val activity = this@MainActivity

            fun requestThemeChange(next: GridLabThemeMode, originInWindow: Offset) {
                if (revealState.isRunning || themeChangePending || next == themeMode) return
                themeChangePending = true
                captureActivityBitmap(activity) { bitmap ->
                    scope.launch {
                        try {
                            if (bitmap != null) {
                                revealState.begin(bitmap, originInWindow)
                            }
                            themeMode = next
                            themePreferences.setThemeMode(next)
                            if (bitmap != null) {
                                val progress = Animatable(0f)
                                progress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(
                                        durationMillis = RevealDurationMs,
                                        easing = RevealEasing,
                                    ),
                                ) {
                                    revealState.updateProgress(value)
                                }
                                revealState.end()
                            }
                        } finally {
                            themeChangePending = false
                        }
                    }
                }
            }

            GridLabTheme(themeMode = themeMode) {
                Box(modifier = Modifier.fillMaxSize()) {
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
                                    onOpenLanguageAppearance = {
                                        destination = RootDestination.LanguageAppearance
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
                                    onBack = { destination = RootDestination.About },
                                    modifier = pad,
                                )
                            }
                            is RootDestination.LanguageAppearance -> {
                                val configLocale = LocalConfiguration.current.locales[0]
                                val effectiveLanguage = storedLanguage
                                    ?: AppLanguage.fromTag(configLocale.language)
                                    ?: AppLanguage.English
                                LanguageAppearanceScreen(
                                    themeMode = themeMode,
                                    isThemeTransitionRunning = revealState.isRunning || themeChangePending,
                                    selectedLanguage = effectiveLanguage,
                                    onBack = { destination = RootDestination.Settings },
                                    onCycleTheme = { next, origin ->
                                        requestThemeChange(next, origin)
                                    },
                                    onSelectLanguage = { lang ->
                                        scope.launch {
                                            themePreferences.setLanguage(lang)
                                            AppLocale.apply(lang)
                                        }
                                    },
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
                    ThemeRevealOverlay(state = revealState)
                }
            }
        }
    }
}
