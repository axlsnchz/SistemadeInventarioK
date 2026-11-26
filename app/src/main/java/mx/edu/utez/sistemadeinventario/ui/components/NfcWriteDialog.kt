package mx.edu.utez.sistemadeinventario.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan

@Composable
fun NfcWriteDialog(
    onCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        shape = BentoCardShape,
        containerColor = CardDark,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PastelIconCircle(
                    icon = Icons.Rounded.Nfc,
                    backgroundColor = PastelCyan,
                    contentDescription = "Grabar etiqueta NFC",
                    size = 64.dp,
                    iconSize = 32.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Acercar etiqueta NFC",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Mantén la etiqueta pegada a la parte trasera del teléfono hasta confirmar la grabación.",
                    fontSize = 14.sp,
                    color = MediumGrey,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                CircularProgressIndicator(
                    color = PastelCyan,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCancelar,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveWhite,
                    contentColor = CharcoalBase
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}
