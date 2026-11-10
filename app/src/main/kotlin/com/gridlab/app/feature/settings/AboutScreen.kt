package com.gridlab.app.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gridlab.app.R
import com.gridlab.app.ui.theme.GridLabTheme

/**
 * About GridLab — identity, developer profile and project links.
 * External URLs open with the system default handler (no chooser).
 */
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenSourceLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    val context = LocalContext.current
    val versionLabel = remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)
            val name = info.versionName ?: ""
            val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                info.versionCode.toLong()
            }
            "$name ($code)"
        }.getOrElse { "" }
    }

    val githubProfileUrl = stringResource(R.string.about_url_github_profile)
    val githubRepoUrl = stringResource(R.string.about_url_github_repo)
    val githubReleasesUrl = stringResource(R.string.about_url_github_releases)
    val instagramUrl = stringResource(R.string.about_url_instagram)
    val tiktokUrl = stringResource(R.string.about_url_tiktok)
    val avatarUrl = stringResource(R.string.about_url_github_avatar)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        AboutTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AboutCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(colors.e1),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(72.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.about_app_name),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.e2)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = versionLabel.ifBlank {
                                stringResource(R.string.about_version_fallback)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }

            AboutCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val developerName = stringResource(R.string.about_developer_name)
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(colors.e2)
                            .border(2.dp, colors.e3, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = developerName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            placeholder = painterResource(R.drawable.ic_circle_filled),
                            error = painterResource(R.drawable.ic_circle_filled),
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.about_developed_by),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = developerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SocialIconButton(
                            iconRes = R.drawable.ic_github,
                            contentDescription = stringResource(R.string.about_github),
                            onClick = { openUrl(context, githubProfileUrl) },
                        )
                        SocialIconButton(
                            iconRes = R.drawable.ic_instagram,
                            contentDescription = stringResource(R.string.about_instagram),
                            onClick = { openUrl(context, instagramUrl) },
                        )
                        SocialIconButton(
                            iconRes = R.drawable.ic_tiktok,
                            contentDescription = stringResource(R.string.about_tiktok),
                            onClick = { openUrl(context, tiktokUrl) },
                        )
                    }
                }
            }

            AboutCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AboutLinkRow(
                        iconRes = R.drawable.ic_code,
                        title = stringResource(R.string.about_source_code),
                        onClick = { openUrl(context, githubRepoUrl) },
                    )
                    AboutLinkRow(
                        iconRes = R.drawable.ic_new_releases,
                        title = stringResource(R.string.about_releases),
                        onClick = { openUrl(context, githubReleasesUrl) },
                    )
                    AboutLinkRow(
                        iconRes = R.drawable.ic_article_filled,
                        title = stringResource(R.string.settings_open_source_licenses),
                        onClick = onOpenSourceLicenses,
                    )
                }
            }

            Text(
                text = stringResource(R.string.about_distribution_note),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/** Opens [url] with the default app/browser. Does not show a chooser. */
private fun openUrl(context: Context, url: String) {
    val uri = Uri.parse(url)
    val intent = Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No handler available; ignore quietly for About polish.
    }
}

@Composable
private fun AboutTopBar(
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
            text = stringResource(R.string.about_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary,
        )
    }
}

@Composable
private fun AboutCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = GridLabTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface),
    ) {
        content()
    }
}

@Composable
private fun SocialIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(colors.e2)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = colors.iconActive,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun AboutLinkRow(
    @DrawableRes iconRes: Int,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = GridLabTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_circle_filled),
                contentDescription = null,
                tint = colors.e2,
                modifier = Modifier.fillMaxSize(),
            )
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colors.iconActive,
                modifier = Modifier.size(18.dp),
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
