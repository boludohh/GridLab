package com.gridlab.app.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OpenSourceLicensesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    val context = LocalContext.current
    var markdownText by remember { mutableStateOf<String?>(null) }
    var loadError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val locale = context.resources.configuration.locales[0]
        val assetName = if (locale.language == "es") {
            "licenses/third_party_licenses_es.md"
        } else {
            "licenses/third_party_licenses.md"
        }
        val text = withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(assetName).bufferedReader().use { it.readText() }
            }.getOrElse {
                // Fallback to English if the localized asset is missing.
                runCatching {
                    context.assets.open("licenses/third_party_licenses.md")
                        .bufferedReader()
                        .use { it.readText() }
                }.getOrNull()
            }
        }
        if (text == null) {
            loadError = true
        } else {
            markdownText = text
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        LicensesTopBar(onBack = onBack)

        when {
            loadError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.settings_licenses_load_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
            markdownText == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.iconActive)
                }
            }
            else -> {
                val scrollState = rememberScrollState()
                Markdown(
                    content = markdownText!!,
                    colors = markdownColor(
                        text = colors.textPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun LicensesTopBar(
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
            text = stringResource(R.string.settings_open_source_licenses),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
    }
}
