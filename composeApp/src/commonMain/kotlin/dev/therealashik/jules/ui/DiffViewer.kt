package dev.therealashik.jules.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun UnifiedDiffViewer(patch: String, modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()

    val addBg = if (isDark) Color(0xFF1A3A1A) else Color(0xFFE6FFE6)
    val addText = if (isDark) Color(0xFF4CAF50) else Color(0xFF4CAF50)

    val removeBg = if (isDark) Color(0xFF3A1A1A) else Color(0xFFFFE6E6)
    val removeText = if (isDark) Color(0xFFEF5350) else Color(0xFFEF5350)

    Surface(
        modifier = modifier.heightIn(max = Dimens.gitPatchMaxHeight),
        shape = RoundedCornerShape(Dimens.spacingS),
        color = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn {
            items(patch.lines()) { line ->
                val isAddition = line.startsWith("+") && !line.startsWith("+++")
                val isDeletion = line.startsWith("-") && !line.startsWith("---")
                val isHunkHeader = line.startsWith("@@")
                val isFileHeader = line.startsWith("diff ") || line.startsWith("index ") ||
                        line.startsWith("+++") || line.startsWith("---")

                val bgColor = when {
                    isAddition -> addBg
                    isDeletion -> removeBg
                    isHunkHeader -> MaterialTheme.colorScheme.surfaceVariant
                    else -> Color.Transparent
                }

                val textColor = when {
                    isAddition -> addText
                    isDeletion -> removeText
                    isHunkHeader -> MaterialTheme.colorScheme.onSurfaceVariant
                    isFileHeader -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
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
