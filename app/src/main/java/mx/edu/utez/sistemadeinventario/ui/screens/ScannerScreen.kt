package mx.edu.utez.sistemadeinventario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.DarkTextField
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.ScreenHeader
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelMint
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ScanState
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ScannerViewModel

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel,
    nfcManager: NfcManager,
    onProductoEncontrado: (Long) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var manualNfcId by rememberSaveable { mutableStateOf("") }

    // Escucha lecturas NFC solo mientras esta pantalla está activa
    LaunchedEffect(nfcManager) {
        nfcManager.nfcEvents.collect { texto ->
            viewModel.onNfcLeido(texto)
        }
    }

    LaunchedEffect(state) {
        if (state is ScanState.Encontrado) {
            val id = (state as ScanState.Encontrado).producto.id
            viewModel.reiniciar()
            onProductoEncontrado(id)
        }
    }

    val nfcDisponible = nfcManager.isAvailable
    val nfcActivo = nfcManager.isEnabled

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBase)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 120.dp)
        ) {
            ScreenHeader(
                title = "Escáner NFC",
                subtitle = "Identifica un producto acercando su etiqueta"
            )

            Spacer(modifier = Modifier.height(22.dp))

            // Tarjeta principal tipo Bento
            Surface(
                shape = BentoCardShape,
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val (icon, bgColor, titulo, subtitulo) = when (val s = state) {
                        is ScanState.Buscando -> Quadruple(
                            Icons.Rounded.Nfc,
                            PastelCyan,
                            "Buscando producto...",
                            "Consultando la etiqueta en el inventario"
                        )
                        is ScanState.Encontrado -> Quadruple(
                            Icons.Rounded.CheckCircle,
                            PastelMint,
                            "Producto encontrado",
                            s.producto.nombre
                        )
                        is ScanState.Error -> Quadruple(
                            Icons.Rounded.ErrorOutline,
                            PastelPink,
                            "No se pudo identificar",
                            s.mensaje
                        )
                        is ScanState.Esperando -> when {
                            !nfcDisponible -> Quadruple(
                                Icons.Rounded.Nfc,
                                PastelPeach,
                                "NFC no disponible",
                                "Este dispositivo no cuenta con sensor NFC. Usa la búsqueda manual abajo."
                            )
                            !nfcActivo -> Quadruple(
                                Icons.Rounded.Nfc,
                                PastelPeach,
                                "NFC desactivado",
                                "Activa el NFC en los ajustes del teléfono para escanear etiquetas."
                            )
                            else -> Quadruple(
                                Icons.Rounded.Nfc,
                                PastelCyan,
                                "Listo para escanear",
                                "Acerca una etiqueta NFC a la parte trasera del teléfono"
                            )
                        }
                    }

                    PastelIconCircle(
                        icon = icon,
                        backgroundColor = bgColor,
                        size = 84.dp,
                        iconSize = 40.dp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = titulo,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = OffWhite,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = subtitulo,
                        fontSize = 14.sp,
                        color = MediumGrey,
                        textAlign = TextAlign.Center
                    )

                    if (state is ScanState.Buscando) {
                        Spacer(modifier = Modifier.height(20.dp))
                        CircularProgressIndicator(
                            color = PastelCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    if (state is ScanState.Error) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.reiniciar() },
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveWhite,
                                contentColor = CharcoalBase
                            )
                        ) {
                            Text(
                                text = "Intentar de nuevo",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SectionHeader("Búsqueda manual por ID NFC")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DarkTextField(
                    value = manualNfcId,
                    onValueChange = { manualNfcId = it },
                    label = "Código NFC",
                    placeholder = "Ej. INV-A1B2C3D4",
                    containerColor = CardDark,
                    shape = groupedItemShape(0, 2),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            viewModel.onNfcLeido(manualNfcId)
                        }
                    )
                )

                Surface(
                    shape = groupedItemShape(1, 2),
                    color = CardDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                viewModel.onNfcLeido(manualNfcId)
                            },
                            enabled = manualNfcId.isNotBlank() && state !is ScanState.Buscando,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveWhite,
                                contentColor = CharcoalBase,
                                disabledContainerColor = CharcoalBase,
                                disabledContentColor = MediumGrey
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar etiqueta", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
