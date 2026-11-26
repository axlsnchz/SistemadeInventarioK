package mx.edu.utez.sistemadeinventario.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.data.remote.RetrofitClient
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.CircleActionButton
import mx.edu.utez.sistemadeinventario.ui.components.DarkTextField
import mx.edu.utez.sistemadeinventario.ui.components.NfcWriteDialog
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.ScreenHeader
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.ErrorSurface
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceInput
import mx.edu.utez.sistemadeinventario.ui.theme.WarningSurface
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ProductoFormViewModel

@Composable
fun ProductoFormScreen(
    viewModel: ProductoFormViewModel,
    nfcManager: NfcManager,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var cameraPermissionMessage by remember { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { ok ->
        viewModel.onFotoResultado(ok)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPermissionMessage = null
            takePictureLauncher.launch(viewModel.prepararFoto())
        } else {
            cameraPermissionMessage = "Se necesita permiso de cámara para tomar la foto del producto."
        }
    }

    LaunchedEffect(state.guardado) {
        if (state.guardado) {
            onSaved()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.cancelarGrabado(nfcManager)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBase)
            .statusBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            ScreenHeader(
                title = if (state.esEdicion) "Editar producto" else "Nuevo producto",
                leading = {
                    CircleActionButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Regresar",
                        onClick = onBack
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (state.cargando) {
                Surface(
                    shape = BentoCardShape,
                    color = CardDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PastelCyan)
                    }
                }
            } else {

            // Tarjeta de fotografía (26.dp)
            SectionHeader("Fotografía y etiqueta")

            Surface(
                shape = BentoCardShape,
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val fotoModel: Any? = state.fotoUri ?: RetrofitClient.getImageUrl(state.fotoUrlActual)
                    if (fotoModel != null) {
                        AsyncImage(
                            model = fotoModel,
                            contentDescription = "Vista previa",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SurfaceInput)
                        )
                    } else {
                        PastelIconCircle(
                            icon = Icons.Rounded.CameraAlt,
                            backgroundColor = PastelPink,
                            size = 68.dp,
                            iconSize = 28.dp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (fotoModel != null) "Foto lista" else "Foto del producto",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = OffWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.fotoUri != null) "Nueva captura pendiente de subir"
                            else if (!state.fotoUrlActual.isNullOrBlank()) "Imagen actual guardada"
                            else "Opcional · usa la cámara",
                            fontSize = 12.sp,
                            color = MediumGrey
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    cameraPermissionMessage = null
                                    takePictureLauncher.launch(viewModel.prepararFoto())
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            enabled = !state.guardando,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ActiveWhite,
                                contentColor = CharcoalBase
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (fotoModel != null) "Cambiar foto" else "Tomar foto",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tarjeta de NFC (26.dp)
            Surface(
                shape = BentoCardShape,
                color = CardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PastelIconCircle(
                        icon = Icons.Rounded.Nfc,
                        backgroundColor = PastelCyan,
                        size = 48.dp,
                        iconSize = 24.dp
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Etiqueta NFC",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = OffWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.nfcId ?: "Sin etiqueta grabada",
                            fontSize = 12.sp,
                            color = if (state.nfcId != null) PastelCyan else MediumGrey
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { viewModel.grabarNfc(nfcManager) },
                        enabled = !state.guardando && nfcManager.isAvailable && nfcManager.isEnabled,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ActiveWhite,
                            contentColor = CharcoalBase,
                            disabledContainerColor = SurfaceInput,
                            disabledContentColor = MediumGrey
                        )
                    ) {
                        Text(
                            text = if (state.nfcId != null) "Regrabar" else "Grabar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Formulario agrupado (apilado con 2.dp, 24.dp exterior / 4.dp interior)
            SectionHeader("Datos del producto")

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DarkTextField(
                    value = state.nombre,
                    onValueChange = viewModel::onNombre,
                    label = "Nombre *",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(0, 6),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                DarkTextField(
                    value = state.sku,
                    onValueChange = viewModel::onSku,
                    label = "SKU",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(1, 6),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                DarkTextField(
                    value = state.categoria,
                    onValueChange = viewModel::onCategoria,
                    label = "Categoría",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(2, 6),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                DarkTextField(
                    value = state.precio,
                    onValueChange = viewModel::onPrecio,
                    label = "Precio *",
                    placeholder = "0.00",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(3, 6),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    )
                )
                DarkTextField(
                    value = state.stock,
                    onValueChange = viewModel::onStock,
                    label = "Stock actual *",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(4, 6),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )
                DarkTextField(
                    value = state.stockMinimo,
                    onValueChange = viewModel::onStockMinimo,
                    label = "Stock mínimo",
                    enabled = !state.guardando,
                    containerColor = CardDark,
                    shape = groupedItemShape(5, 6),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    )
                )
            }

            val errorText = state.error ?: cameraPermissionMessage
            if (!errorText.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = BentoCardShape,
                    color = ErrorSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ErrorOutline,
                            contentDescription = null,
                            tint = PastelPink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorText,
                            fontSize = 13.sp,
                            color = PastelPink
                        )
                    }
                }
            }

            if (!state.aviso.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = BentoCardShape,
                    color = WarningSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = PastelPeach,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.aviso!!,
                            fontSize = 13.sp,
                            color = PastelPeach
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.guardar() },
                enabled = !state.guardando && !state.grabandoNfc,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveWhite,
                    contentColor = CharcoalBase,
                    disabledContainerColor = CardDark,
                    disabledContentColor = MediumGrey
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (state.guardando) {
                    CircularProgressIndicator(
                        color = OffWhite,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = if (state.esEdicion) "Guardar cambios" else "Guardar producto",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (state.grabandoNfc) {
        NfcWriteDialog(
            onCancelar = { viewModel.cancelarGrabado(nfcManager) }
        )
    }
}
