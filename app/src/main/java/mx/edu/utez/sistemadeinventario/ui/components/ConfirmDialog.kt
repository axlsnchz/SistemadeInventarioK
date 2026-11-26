package mx.edu.utez.sistemadeinventario.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    icon: ImageVector = Icons.Rounded.HelpOutline,
    iconTintBg: Color = PastelPeach,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = BentoCardShape,
        containerColor = CardDark,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PastelIconCircle(
                    icon = icon,
                    backgroundColor = iconTintBg,
                    size = 54.dp,
                    iconSize = 26.dp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = MediumGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveWhite,
                    contentColor = CharcoalBase
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = MediumGrey)
            }
        }
    )
}
