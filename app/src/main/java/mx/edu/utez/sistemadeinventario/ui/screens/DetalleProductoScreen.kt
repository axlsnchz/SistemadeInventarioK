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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Nfc
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import mx.edu.utez.sistemadeinventario.data.local.entity.ProductoEntity
import mx.edu.utez.sistemadeinventario.data.remote.RetrofitClient
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.CircleActionButton
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.ScreenHeader
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.formatPrice
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.components.stockPastelColor
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelMint
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.viewmodels.DetalleProductoViewModel

@Composable
fun DetalleProductoScreen(
    viewModel: DetalleProductoViewModel,
    onBack: () -> Unit,
    onEditarClick: (Long) -> Unit,
    onAgregarAlCarrito: (ProductoEntity) -> Unit
) {
    val producto by viewModel.producto.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error!!)
        }
    }

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
                .padding(horizontal = 20.dp, vertical = 18.dp)
                .navigationBarsPadding()
        ) {
            ScreenHeader(
                title = "Detalle",
                leading = {
                    CircleActionButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Regresar",
                        onClick = onBack
                    )
                },
                actions = {
                    CircleActionButton(
                        icon = Icons.Rounded.Refresh,
                        contentDescription = "Recargar",
                        onClick = { viewModel.recargar() }
                    )
                    if (isAdmin && producto != null) {
                        CircleActionButton(
                            icon = Icons.Rounded.Edit,
                            contentDescription = "Editar producto",
                            active = true,
                            onClick = { onEditarClick(producto!!.id) }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            val item = producto
            if (item == null) {
                Surface(
                    shape = BentoCardShape,
                    color = CardDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(color = PastelCyan)
                        } else {
                            Text(
                                text = error ?: "No se encontró el producto",
                                color = MediumGrey,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else {
                // Foto principal
                val imageUrl = RetrofitClient.getImageUrl(item.fotoUrl)
                Surface(
                    shape = BentoCardShape,
                    color = CardDark,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.nombre,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(BentoCardShape)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PastelIconCircle(
                                icon = Icons.Rounded.Inventory2,
                                backgroundColor = PastelCyan,
                                size = 64.dp,
                                iconSize = 32.dp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sin fotografía",
                                fontSize = 13.sp,
                                color = MediumGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = item.nombre,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bento Grid: Precio y Stock
                val stockColor = stockPastelColor(item.stock, item.stockMinimo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = BentoCardShape,
                        color = CardDark
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            PastelIconCircle(
                                icon = Icons.Rounded.Sell,
                                backgroundColor = PastelMint,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = formatPrice(item.precio),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OffWhite
                            )
                            Text(
                                text = "Precio unitario",
                                fontSize = 12.sp,
                                color = MediumGrey
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = BentoCardShape,
                        color = CardDark
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            PastelIconCircle(
                                icon = Icons.Rounded.Inventory2,
                                backgroundColor = stockColor,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "${item.stock} uds",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OffWhite
                            )
                            Text(
                                text = "Mínimo: ${item.stockMinimo}",
                                fontSize = 12.sp,
                                color = MediumGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                SectionHeader("Especificaciones")

                val infoRows = listOf(
                    Triple("SKU", item.sku?.takeIf { it.isNotBlank() } ?: "Sin SKU", Icons.Rounded.QrCode2 to PastelPeach),
                    Triple("Categoría", item.categoria?.takeIf { it.isNotBlank() } ?: "Sin categoría", Icons.Rounded.Category to PastelPink),
                    Triple("Etiqueta NFC", item.nfcId?.takeIf { it.isNotBlank() } ?: "Sin etiqueta asignada", Icons.Rounded.Nfc to PastelCyan)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    infoRows.forEachIndexed { index, (label, value, iconPair) ->
                        InfoGroupedRow(
                            label = label,
                            value = value,
                            icon = iconPair.first,
                            pastelColor = iconPair.second,
                            index = index,
                            count = infoRows.size
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        onAgregarAlCarrito(item)
                        scope.launch {
                            snackbarHostState.showSnackbar("Agregado al punto de venta")
                        }
                    },
                    enabled = item.stock > 0,
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
                    Icon(
                        imageVector = Icons.Rounded.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (item.stock > 0) "Agregar al carrito" else "Sin existencias",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(20.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = BentoCardShape,
                containerColor = CardDark,
                contentColor = OffWhite
            )
        }
    }
}

@Composable
private fun InfoGroupedRow(
    label: String,
    value: String,
    icon: ImageVector,
    pastelColor: Color,
    index: Int,
    count: Int
) {
    Surface(
        shape = groupedItemShape(index, count),
        color = CardDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PastelIconCircle(
                icon = icon,
                backgroundColor = pastelColor,
                size = 38.dp,
                iconSize = 19.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MediumGrey
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = value,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = OffWhite
                )
            }
        }
    }
}
