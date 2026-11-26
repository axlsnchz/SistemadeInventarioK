package mx.edu.utez.sistemadeinventario.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite

@Composable
fun CampoBusqueda(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar por nombre o SKU...",
    containerColor: Color = CardDark,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(26.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = MediumGrey,
                fontSize = 15.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Buscar",
                tint = MediumGrey
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Limpiar búsqueda",
                        tint = MediumGrey
                    )
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedTextColor = OffWhite,
            unfocusedTextColor = OffWhite,
            cursorColor = ActiveWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
