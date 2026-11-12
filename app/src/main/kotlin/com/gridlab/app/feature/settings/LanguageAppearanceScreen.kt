package com.gridlab.app.feature.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme
import com.gridlab.app.ui.theme.GridLabThemeMode
import com.gridlab.app.ui.theme.iconRes
import com.gridlab.app.ui.theme.next
import kotlinx.coroutines.launch

private val ThemeIconEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
private const val ThemeIconAnimMs = 520

/**
 * Language & appearance settings. Theme is cycled from the top-bar icon
 * (Light → Dark → Night). Language controls will be added later.
 */
@Composable
fun LanguageAppearanceScreen(
    themeMode: GridLabThemeMode,
    isThemeTransitionRunning: Boolean,
    onBack: () -> Unit,
    onCycleTheme: (next: GridLabThemeMode, originInWindow: Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    val scope = rememberCoroutineScope()
    val rotation = remember { Animatable(0f) }
    var displayedMode by remember { mutableStateOf(themeMode) }
    LaunchedEffect(themeMode) {
        if (!isThemeTransitionRunning) {
            displayedMode = themeMode
        }
    }
    var iconCenterInWindow by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.e2)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.settings_back_content_description),
                    tint = colors.iconActive,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.size(10.dp))
            Text(
                text = stringResource(R.string.settings_language_appearance),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.e2)
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInWindow()
                        iconCenterInWindow = Offset(
                            pos.x + coords.size.width / 2f,
                            pos.y + coords.size.height / 2f,
                        )
                    }
                    .clickable(enabled = !isThemeTransitionRunning) {
                        if (isThemeTransitionRunning) return@clickable
                        val next = themeMode.next()
                        val origin = iconCenterInWindow
                        scope.launch {
                            rotation.snapTo(0f)
                            displayedMode = next
                            rotation.animateTo(
                                targetValue = 360f,
                                animationSpec = tween(
                                    durationMillis = ThemeIconAnimMs,
                                    easing = ThemeIconEasing,
                                ),
                            )
                            rotation.snapTo(0f)
                        }
                        onCycleTheme(next, origin)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(displayedMode.iconRes()),
                    contentDescription = stringResource(R.string.settings_theme_toggle_content_description),
                    tint = colors.iconActive,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(rotation.value),
                )
            }
        }

        // Placeholder body — language options will land here later.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            Text(
                text = stringResource(R.string.settings_language_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}
