package com.gridlab.app.feature.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        SettingsTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsSectionCard {
                SettingsNavRow(
                    iconRes = R.drawable.ic_info_filled,
                    title = stringResource(R.string.settings_about),
                    onClick = onOpenAbout,
                )
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier
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
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun SettingsSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = GridLabTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(vertical = 4.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsNavRow(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    // Same proportion as About link rows / social buttons: 44.dp circle, 22.dp glyph.
    val circleSize = 44.dp
    val glyphSize = 22.dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(circleSize)
                .clip(CircleShape)
                .background(colors.e2),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colors.iconActive,
                modifier = Modifier.size(glyphSize),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(22.dp),
        )
    }
}
