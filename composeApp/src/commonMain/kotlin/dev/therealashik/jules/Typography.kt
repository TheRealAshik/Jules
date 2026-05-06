package dev.therealashik.jules

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import jules.composeapp.generated.resources.Res
import jules.composeapp.generated.resources.product_sans_bold
import jules.composeapp.generated.resources.product_sans_bold_italic
import jules.composeapp.generated.resources.product_sans_italic
import jules.composeapp.generated.resources.product_sans_regular
import org.jetbrains.compose.resources.Font

@androidx.compose.runtime.Composable
fun productSansFontFamily() = FontFamily(
    Font(Res.font.product_sans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.product_sans_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.product_sans_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.product_sans_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

@androidx.compose.runtime.Composable
fun AppTypography(): Typography {
    val fontFamily = productSansFontFamily()
    val default = Typography()
    return Typography(
        displayLarge = default.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = default.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = default.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = default.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = default.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = default.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = default.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = default.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = default.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = default.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = default.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = default.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = default.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = default.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = default.labelSmall.copy(fontFamily = fontFamily),
    )
}
