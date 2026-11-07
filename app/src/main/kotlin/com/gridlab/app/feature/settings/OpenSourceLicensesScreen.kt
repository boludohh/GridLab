package com.gridlab.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gridlab.app.R
import com.gridlab.app.core.designsystem.theme.GridLabTheme
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

/** Border thickness for the table grid lines. */
private val TABLE_BORDER_WIDTH = 1.dp

/** Horizontal padding inside each table cell. */
private val CELL_PADDING_H = 10.dp

/** Vertical padding inside each table cell. */
private val CELL_PADDING_V = 8.dp

/**
 * Renders the third-party license attribution Markdown from assets,
 * choosing the Spanish version when the device locale is Spanish.
 */
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
                val loadedMarkdown = requireNotNull(markdownText)
                val scrollState = rememberScrollState()
                val bodyStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textPrimary,
                    lineHeight = 20.sp,
                )
                val tableStyle = MaterialTheme.typography.bodySmall.copy(
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                )
                val headerStyle = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )

                Markdown(
                    content = loadedMarkdown,
                    colors = markdownColor(
                        text = colors.textPrimary,
                        codeBackground = colors.e1,
                        inlineCodeBackground = colors.e2,
                        dividerColor = colors.borderDecoSurface,
                        tableBackground = colors.surface,
                    ),
                    typography = markdownTypography(
                        h1 = headerStyle.copy(fontSize = 22.sp),
                        h2 = headerStyle.copy(fontSize = 18.sp),
                        h3 = headerStyle.copy(fontSize = 16.sp),
                        h4 = headerStyle.copy(fontSize = 15.sp),
                        h5 = headerStyle.copy(fontSize = 14.sp),
                        h6 = headerStyle.copy(fontSize = 13.sp),
                        text = bodyStyle,
                        paragraph = bodyStyle,
                        ordered = bodyStyle,
                        bullet = bodyStyle,
                        list = bodyStyle,
                        quote = bodyStyle.copy(color = colors.textSecondary),
                        code = tableStyle.copy(
                            fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        ),
                        table = tableStyle,
                    ),
                    dimens = markdownDimens(
                        dividerThickness = TABLE_BORDER_WIDTH,
                    ),
                    components = markdownComponents(
                        table = { model ->
                            GithubStyleTable(
                                content = model.content,
                                node = model.node,
                                style = model.typography.table,
                                borderColor = colors.borderDecoSurface,
                                headerBackground = colors.e1,
                            )
                        },
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

/**
 * Top bar with back navigation for the licenses screen.
 */
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

/**
 * GitHub-style table with auto-width columns and full grid separators.
 * Each column is sized to fit its longest cell content (no truncation).
 * Cells are then re-measured to exactly fill their grid rectangle, so the
 * vertical and horizontal separators form continuous straight lines.
 * The table scrolls horizontally if it exceeds the available width.
 */
@Composable
private fun GithubStyleTable(
    content: String,
    node: ASTNode,
    style: TextStyle,
    borderColor: Color,
    headerBackground: Color,
    modifier: Modifier = Modifier,
) {
    val rows = remember(content, node) { extractTableCells(node) }
    if (rows.isEmpty()) return

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val borderWidthPx = with(density) { TABLE_BORDER_WIDTH.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Layout(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .border(TABLE_BORDER_WIDTH, borderColor, RectangleShape),
            content = {
                rows.forEachIndexed { rowIndex, row ->
                    row.forEachIndexed { colIndex, cellNode ->
                        val isHeader = rowIndex == 0
                        val isLastRow = rowIndex == rows.lastIndex
                        val isLastCol = colIndex == row.lastIndex
                        val cellBg = if (isHeader) headerBackground else Color.Transparent

                        Box(
                            modifier = Modifier
                                .layoutId(TableCellPosition(rowIndex, colIndex))
                                .background(cellBg)
                                .drawBehind {
                                    // Vertical separator (right edge of cell)
                                    if (!isLastCol) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(size.width - borderWidthPx / 2f, 0f),
                                            end = Offset(size.width - borderWidthPx / 2f, size.height),
                                            strokeWidth = borderWidthPx,
                                        )
                                    }
                                    // Horizontal separator (bottom edge of cell)
                                    if (!isLastRow) {
                                        drawLine(
                                            color = borderColor,
                                            start = Offset(0f, size.height - borderWidthPx / 2f),
                                            end = Offset(size.width, size.height - borderWidthPx / 2f),
                                            strokeWidth = borderWidthPx,
                                        )
                                    }
                                }
                                .padding(horizontal = CELL_PADDING_H, vertical = CELL_PADDING_V),
                        ) {
                            MarkdownTableBasicText(
                                content = content,
                                cell = cellNode,
                                style = style.copy(
                                    fontWeight = if (isHeader) FontWeight.SemiBold else style.fontWeight,
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                            )
                        }
                    }
                }
            },
            measurePolicy = { measurables, _ ->
                // Pass 1: measure every cell unconstrained to read its natural size.
                val natural = measurables.map { measurable ->
                    val position = measurable.layoutId as TableCellPosition
                    val placeable = measurable.measure(
                        Constraints(
                            maxWidth = Constraints.Infinity,
                            maxHeight = Constraints.Infinity,
                        ),
                    )
                    Triple(position, placeable, measurable)
                }

                // Column width = widest natural cell in that column.
                val numCols = (natural.maxOfOrNull { it.first.column } ?: 0) + 1
                val columnWidths = IntArray(numCols)
                natural.forEach { (pos, placeable, _) ->
                    columnWidths[pos.column] = maxOf(columnWidths[pos.column], placeable.width)
                }

                // Row height = tallest natural cell in that row.
                val numRows = rows.size
                val rowHeights = IntArray(numRows)
                natural.forEach { (pos, placeable, _) ->
                    rowHeights[pos.row] = maxOf(rowHeights[pos.row], placeable.height)
                }

                // Pass 2: force every cell to fill its grid rectangle exactly, so
                // separators drawn at cell edges line up into continuous straight lines.
                val cells = natural.map { (pos, _, measurable) ->
                    val placeable = measurable.measure(
                        Constraints(
                            minWidth = columnWidths[pos.column],
                            maxWidth = columnWidths[pos.column],
                            minHeight = rowHeights[pos.row],
                            maxHeight = rowHeights[pos.row],
                        ),
                    )
                    pos to placeable
                }

                val totalWidth = columnWidths.sum()
                val totalHeight = rowHeights.sum()

                layout(totalWidth, totalHeight) {
                    cells.forEach { (pos, placeable) ->
                        val x = columnWidths.take(pos.column).sum()
                        val y = rowHeights.take(pos.row).sum()
                        placeable.placeRelative(x, y)
                    }
                }
            },
        )
    }
}

/** Identifies a cell by its row and column index within the table layout. */
private data class TableCellPosition(val row: Int, val column: Int)

/**
 * Extracts cell AST nodes from a GFM table node.
 * Returns a list of rows; each row is a list of cell nodes.
 * The separator row (|---|---|) is excluded because it contains no CELL children.
 */
private fun extractTableCells(tableNode: ASTNode): List<List<ASTNode>> {
    return tableNode.children
        .filter { rowNode ->
            rowNode.children.any { it.type == GFMTokenTypes.CELL }
        }
        .map { rowNode ->
            rowNode.children.filter { it.type == GFMTokenTypes.CELL }
        }
}
