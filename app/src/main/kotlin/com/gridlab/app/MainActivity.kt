package com.gridlab.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.gridlab.app.ui.theme.GridLabTheme
import com.gridlab.app.ui.theme.GridLabThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GridLabTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PlaceholderContent(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceholderContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreviewLight() {
    GridLabTheme(themeMode = GridLabThemeMode.Light) {
        PlaceholderContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreviewDark() {
    GridLabTheme(themeMode = GridLabThemeMode.Dark) {
        PlaceholderContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderPreviewNight() {
    GridLabTheme(themeMode = GridLabThemeMode.Night) {
        PlaceholderContent()
    }
}
