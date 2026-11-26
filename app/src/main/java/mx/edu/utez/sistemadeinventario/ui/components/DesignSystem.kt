package mx.edu.utez.sistemadeinventario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.NavInactiveGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelMint
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceInput
import java.util.Locale

/** Esquinas de tarjeta individual / Bento Grid según guía (26.dp). */
val BentoCardShape = RoundedCornerShape(26.dp)

/**
 * Forma para ítems apilados dentro de una lista agrupada (2.dp de separación):
 * 24.dp en las esquinas exteriores (superior del primero, inferior del último)
 * y 4.dp en las esquinas interiores.
 */
fun groupedItemShape(index: Int, count: Int): RoundedCornerShape {
    return when {
        count <= 1 -> RoundedCornerShape(24.dp)
        index == 0 -> RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomStart = 4.dp,
            bottomEnd = 4.dp
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        )
        else -> RoundedCornerShape(4.dp)
    }
}

/** Formatea un precio en pesos con 2 decimales. */
fun formatPrice(precio: Double): String =
    String.format(Locale.US, "$%.2f", precio)

/** Devuelve el tono pastel adecuado según el nivel de stock. */
fun stockPastelColor(stock: Int, stockMinimo: Int): Color = when {
    stock <= 0 -> PastelPink
    stock <= stockMinimo -> PastelPeach
    else -> PastelMint
}

/** Ícono destacado dentro de un círculo con tono pastel suave y trazo oscuro #141414. */
@Composable
fun PastelIconCircle(
    icon: ImageVector,
    backgroundColor: Color,
    contentDescription: String? = null,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = CharcoalBase,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Botón circular minimalista sin bordes (para acciones en cabecera). */
@Composable
fun CircleActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    active: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg = if (active) ActiveWhite else CardDark
    val tint = if (active) CharcoalBase else OffWhite
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Título de pantalla grande arriba a la izquierda (26.sp, Medium). */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (leading != null) {
                leading()
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column {
                Text(
                    text = title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite,
                    lineHeight = 32.sp
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MediumGrey
                    )
                }
            }
        }
        if (actions != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

/** Encabezado de sección en 14.sp SemiBold con margen inferior de 10.dp. */
@Composable
fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MediumGrey,
        modifier = modifier.padding(start = 6.dp, bottom = 10.dp)
    )
}

/** Campo de texto minimalista sin bordes ni subrayados. */
@Composable
fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    containerColor: Color = SurfaceInput,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = placeholder?.let { { Text(it, color = MediumGrey) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = shape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.6f),
            focusedTextColor = OffWhite,
            unfocusedTextColor = OffWhite,
            disabledTextColor = MediumGrey,
            focusedLabelColor = PastelCyan,
            unfocusedLabelColor = MediumGrey,
            disabledLabelColor = MediumGrey,
            cursorColor = ActiveWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedLeadingIconColor = OffWhite,
            unfocusedLeadingIconColor = MediumGrey,
            focusedTrailingIconColor = OffWhite,
            unfocusedTrailingIconColor = MediumGrey
        )
    )
}

/**
 * Ítem de la barra de navegación flotante inferior.
 */
data class PillNavItem(
    val route: String,
    val icon: ImageVector,
    val contentDescription: String,
    val badgeCount: Int = 0
)

/**
 * Barra de navegación inferior flotante centrada en forma de píldora (CircleShape),
 * separada del borde inferior por 16.dp, fondo #272727.
 * Seleccionado: círculo blanco sólido #EFEFEF con ícono #141414.
 * No seleccionados: solo ícono #CFCFCF sin etiquetas de texto.
 */
@Composable
fun FloatingPillNavBar(
    currentRoute: String?,
    cartCount: Int,
    onNavigate: (String) -> Unit,
    inventarioRoute: String,
    scannerRoute: String,
    posRoute: String,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        PillNavItem(inventarioRoute, Icons.Rounded.Inventory2, "Inventario"),
        PillNavItem(scannerRoute, Icons.Rounded.Nfc, "Escáner NFC"),
        PillNavItem(posRoute, Icons.Rounded.ShoppingCart, "Punto de venta", badgeCount = cartCount)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = CardDark,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (selected) OffWhite else Color.Transparent)
                            .clickable {
                                if (!selected) onNavigate(item.route)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            tint = if (selected) CharcoalBase else NavInactiveGrey,
                            modifier = Modifier.size(24.dp)
                        )
                        if (item.badgeCount > 0 && !selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 10.dp, end = 10.dp)
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(PastelMint)
                            )
                        }
                    }
                }
            }
        }
    }
}
