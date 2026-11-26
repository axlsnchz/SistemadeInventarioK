package mx.edu.utez.sistemadeinventario.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PixelDarkColorScheme = darkColorScheme(
    primary = ActiveWhite,
    onPrimary = CharcoalBase,
    primaryContainer = CardDark,
    onPrimaryContainer = OffWhite,
    secondary = PastelCyan,
    onSecondary = CharcoalBase,
    secondaryContainer = SurfaceTonal,
    onSecondaryContainer = OffWhite,
    tertiary = PastelMint,
    onTertiary = CharcoalBase,
    background = CharcoalBase,
    onBackground = OffWhite,
    surface = CharcoalBase,
    onSurface = OffWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = MediumGrey,
    surfaceContainerLowest = CharcoalBase,
    surfaceContainerLow = SurfaceInput,
    surfaceContainer = CardDark,
    surfaceContainerHigh = CardDark,
    surfaceContainerHighest = SurfaceTonal,
    error = PastelPink,
    onError = CharcoalBase,
    errorContainer = ErrorSurface,
    onErrorContainer = PastelPink,
    outline = SurfaceTonal,
    outlineVariant = SurfaceTonal
)

@Composable
fun SistemadeInventarioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PixelDarkColorScheme,
        typography = Typography,
        content = content
    )
}
