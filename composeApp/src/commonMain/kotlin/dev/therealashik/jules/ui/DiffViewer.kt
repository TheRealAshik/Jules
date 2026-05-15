package dev.therealashik.jules.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun UnifiedDiffViewer(patch: String, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    val addBg = colorScheme.tertiaryContainer
    val addText = colorScheme.onTertiaryContainer

    val removeBg = colorScheme.errorContainer
    val removeText = colorScheme.onErrorContainer

    val lines = remember(patch) { patch.lines() }

    Surface(
        modifier = modifier.heightIn(max = Dimens.gitPatchMaxHeight),
        shape = RoundedCornerShape(Dimens.spacingS),
        color = colorScheme.surface
    ) {
        LazyColumn {
            itemsIndexed(lines) { index, line ->
                val isAddition = line.startsWith("+") && !line.startsWith("+++")
                val isDeletion = line.startsWith("-") && !line.startsWith("---")
                val isHunkHeader = line.startsWith("@@")
                val isFileHeader = line.startsWith("diff ") || line.startsWith("index ") ||
                        line.startsWith("+++") || line.startsWith("---")

                val bgColor = when {
                    isAddition -> addBg
                    isDeletion -> removeBg
                    isHunkHeader -> colorScheme.surfaceVariant
                    else -> Color.Transparent
                }

                val textColor = when {
                    isAddition -> addText
                    isDeletion -> removeText
                    isHunkHeader -> colorScheme.onSurfaceVariant
                    isFileHeader -> colorScheme.primary
                    else -> colorScheme.onSurface
                }

                val fontWeight = if (isFileHeader) FontWeight.Bold else null
                val fontStyle = if (isHunkHeader) FontStyle.Italic else null

                Text(
                    text = line,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor,
                    fontWeight = fontWeight,
                    fontStyle = fontStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgColor)
                )
            }
        }
    }
}
