package sk.upjs.cookwell.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = MainColor,
    onPrimary = Color.Black,
    background = BackgroundGrey,
    secondary = Color.White,
    onSecondary = buttonBlue

)

private val darkScheme = darkColorScheme(
    primary = MainColorDark,
    onPrimary = DarkText,
    background = BackgroundGreyDark,
    secondary = DarkerGrey,
    onSecondary = Purple40
)

@Composable
fun CookWellTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme : ColorScheme
    if (darkTheme) {
        colorScheme = darkScheme
    } else {
        colorScheme = lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}