package mx.edu.utez.sistemadeinventario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.PointOfSale
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.data.remote.dto.VentaResponse
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.CampoBusqueda
import mx.edu.utez.sistemadeinventario.ui.components.CarritoItemRow
import mx.edu.utez.sistemadeinventario.ui.components.CircleActionButton
import mx.edu.utez.sistemadeinventario.ui.components.ConfirmDialog
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.ScreenHeader
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.formatPrice
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelMint
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.theme.SurfaceInput
import mx.edu.utez.sistemadeinventario.ui.viewmodels.PosViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.UiState

@Composable
fun PosScreen(
    viewModel: PosViewModel,
    productosDisponibles: List<ProductoEntity>,
    nfcManager: NfcManager
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val total by viewModel.total.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val ventaState by viewModel.ventaState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showClearDialog by remember { mutableStateOf(false) }
    var showConfirmSaleDialog by remember { mutableStateOf(false) }
    var showPickerDialog by remember { mutableStateOf(false) }

    val totalUnidades = remember(items) { items.sumOf { it.cantidad } }
    val procesandoVenta = ventaState is UiState.Loading

    // Escucha etiquetas NFC mientras el POS está abierto
    LaunchedEffect(nfcManager) {
        nfcManager.nfcEvents.collect { texto ->
            viewModel.agregarPorNfc(texto)
        }
    }

    LaunchedEffect(mensaje) {
        if (!mensaje.isNullOrBlank()) {
            snackbarHostState.showSnackbar(mensaje!!)
            viewModel.consumirMensaje()
        }
    }

    LaunchedEffect(ventaState) {
        if (ventaState is UiState.Error) {
            snackbarHostState.showSnackbar((ventaState as UiState.Error).message)
            viewModel.reiniciarVenta()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBase)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 18.dp,
                bottom = 170.dp
            )
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = "Punto de venta",
                    subtitle = "Acerca etiquetas NFC o agrega productos",
                    actions = {
                        CircleActionButton(
                            icon = Icons.Rounded.Add,
                            contentDescription = "Agregar producto manual",
                            active = true,
                            onClick = { showPickerDialog = true }
                        )
                        if (items.isNotEmpty()) {
                            CircleActionButton(
                                icon = Icons.Rounded.DeleteSweep,
                                contentDescription = "Vaciar carrito",
                                onClick = { showClearDialog = true }
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Tarjeta Bento de Total
            item(key = "total_bento") {
                Surface(
                    shape = BentoCardShape,
                    color = CardDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PastelIconCircle(
                                icon = Icons.Rounded.PointOfSale,
                                backgroundColor = PastelMint,
                                size = 50.dp,
                                iconSize = 24.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Total a cobrar",
                                    fontSize = 13.sp,
                                    color = MediumGrey
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatPrice(total),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OffWhite
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = SurfaceInput
                        ) {
                            Text(
                                text = "$totalUnidades uds",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = OffWhite,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(22.dp))
            }

            item(key = "cart_section") {
                SectionHeader("Carrito (${items.size})")
            }

            if (items.isEmpty()) {
                item(key = "empty_cart") {
                    Surface(
                        shape = BentoCardShape,
                        color = CardDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PastelIconCircle(
                                icon = Icons.Rounded.Nfc,
                                backgroundColor = PastelCyan,
                                size = 56.dp,
                                iconSize = 28.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Carrito vacío",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = OffWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Escanea una etiqueta NFC para sumar artículos o toca + para elegir del catálogo.",
                                fontSize = 13.sp,
                                color = MediumGrey,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.productoId }
                ) { index, item ->
                    CarritoItemRow(
                        item = item,
                        shape = groupedItemShape(index, items.size),
                        onDecrement = { viewModel.cambiarCantidad(item.productoId, item.cantidad - 1) },
                        onIncrement = { viewModel.cambiarCantidad(item.productoId, item.cantidad + 1) },
                        onRemove = { viewModel.quitar(item.productoId) }
                    )
                    if (index < items.lastIndex) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        // Botón flotante de Confirmar Venta justo arriba de la píldora de navegación
        if (items.isNotEmpty()) {
            Button(
                onClick = { showConfirmSaleDialog = true },
                enabled = !procesandoVenta,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveWhite,
                    contentColor = CharcoalBase,
                    disabledContainerColor = CardDark,
                    disabledContentColor = MediumGrey
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, bottom = 92.dp)
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (procesandoVenta) {
                    CircularProgressIndicator(
                        color = OffWhite,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Confirmar venta · ${formatPrice(total)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 154.dp, start = 20.dp, end = 20.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = BentoCardShape,
                containerColor = CardDark,
                contentColor = OffWhite
            )
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Vaciar carrito",
            message = "¿Seguro que deseas quitar todos los productos del carrito?",
            confirmText = "Vaciar",
            icon = Icons.Rounded.DeleteSweep,
            iconTintBg = PastelPink,
            onConfirm = {
                showClearDialog = false
                viewModel.vaciar()
            },
            onDismiss = { showClearDialog = false }
        )
    }

    if (showConfirmSaleDialog) {
        ConfirmDialog(
            title = "Confirmar venta",
            message = "Se registrará la venta de $totalUnidades artículo(s) por ${formatPrice(total)} y se descontará el stock en el servidor.",
            confirmText = "Cobrar",
            icon = Icons.Rounded.PointOfSale,
            iconTintBg = PastelMint,
            onConfirm = {
                showConfirmSaleDialog = false
                viewModel.confirmarVenta()
            },
            onDismiss = { showConfirmSaleDialog = false }
        )
    }

    if (showPickerDialog) {
        ProductoPickerDialog(
            productos = productosDisponibles,
            onSelect = { producto ->
                viewModel.agregarProducto(producto)
                showPickerDialog = false
            },
            onDismiss = { showPickerDialog = false }
        )
    }

    if (ventaState is UiState.Success) {
        VentaExitosaDialog(
            venta = (ventaState as UiState.Success<VentaResponse>).data,
            onDismiss = { viewModel.reiniciarVenta() }
        )
    }
}

@Composable
private fun ProductoPickerDialog(
    productos: List<ProductoEntity>,
    onSelect: (ProductoEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var filtro by remember { mutableStateOf("") }
    val filtrados = remember(productos, filtro) {
        val q = filtro.trim()
        if (q.isEmpty()) productos
        else productos.filter {
            it.nombre.contains(q, ignoreCase = true) ||
                (it.sku?.contains(q, ignoreCase = true) == true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = BentoCardShape,
        containerColor = CardDark,
        title = {
            Text(
                text = "Agregar producto",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = OffWhite
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                CampoBusqueda(
                    query = filtro,
                    onQueryChange = { filtro = it },
                    placeholder = "Filtrar catálogo...",
                    containerColor = SurfaceInput
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (filtrados.isEmpty()) {
                    Text(
                        text = "No hay productos disponibles.",
                        fontSize = 14.sp,
                        color = MediumGrey,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        itemsIndexed(
                            items = filtrados,
                            key = { _, p -> p.id }
                        ) { index, producto ->
                            Surface(
                                shape = groupedItemShape(index, filtrados.size),
                                color = SurfaceInput,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(groupedItemShape(index, filtrados.size))
                                    .clickable(enabled = producto.stock > 0) {
                                        onSelect(producto)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = producto.nombre,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (producto.stock > 0) OffWhite else MediumGrey,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Stock: ${producto.stock}",
                                            fontSize = 12.sp,
                                            color = MediumGrey
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatPrice(producto.precio),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = PastelMint
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = OffWhite)
            }
        }
    )
}

@Composable
private fun VentaExitosaDialog(
    venta: VentaResponse,
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
                    icon = Icons.Rounded.CheckCircle,
                    backgroundColor = PastelMint,
                    size = 58.dp,
                    iconSize = 30.dp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Venta #${venta.id} registrada",
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!venta.vendedor.isNullOrBlank()) {
                    Text(
                        text = "Vendedor: ${venta.vendedor}",
                        fontSize = 13.sp,
                        color = MediumGrey
                    )
                }
                if (venta.items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        venta.items.forEachIndexed { index, item ->
                            Surface(
                                shape = groupedItemShape(index, venta.items.size),
                                color = SurfaceInput,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${item.cantidad}x ${item.producto}",
                                        fontSize = 13.sp,
                                        color = OffWhite,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatPrice(item.precioUnitario * item.cantidad),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OffWhite
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MediumGrey
                    )
                    Text(
                        text = formatPrice(venta.total),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PastelMint
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActiveWhite,
                    contentColor = CharcoalBase
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Listo", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
