package mx.edu.utez.sistemadeinventario.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.CampoBusqueda
import mx.edu.utez.sistemadeinventario.ui.components.CircleActionButton
import mx.edu.utez.sistemadeinventario.ui.components.ConfirmDialog
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.ProductoCard
import mx.edu.utez.sistemadeinventario.ui.components.ScreenHeader
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPeach
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.viewmodels.InventarioViewModel

@Composable
fun InventarioScreen(
    viewModel: InventarioViewModel,
    username: String?,
    rol: String?,
    onProductoClick: (Long) -> Unit,
    onNuevoProductoClick: () -> Unit,
    onLogout: () -> Unit
) {
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val stockBajoCount = remember(productos) {
        productos.count { it.stock <= it.stockMinimo }
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error!!)
            viewModel.consumirError()
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
                bottom = 120.dp
            )
        ) {
            item(key = "header") {
                val subtitle = listOfNotNull(
                    username?.takeIf { it.isNotBlank() },
                    rol?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")

                ScreenHeader(
                    title = "Inventario",
                    subtitle = subtitle.ifEmpty { null },
                    actions = {
                        if (isRefreshing) {
                            Box(
                                modifier = Modifier.size(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = PastelCyan,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            CircleActionButton(
                                icon = Icons.Rounded.Refresh,
                                contentDescription = "Actualizar inventario",
                                onClick = { viewModel.refresh() }
                            )
                        }
                        CircleActionButton(
                            icon = Icons.AutoMirrored.Rounded.Logout,
                            contentDescription = "Cerrar sesión",
                            onClick = { showLogoutDialog = true }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bento Grid resumen
            item(key = "bento_summary") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BentoMetricCard(
                        title = "Productos",
                        value = productos.size.toString(),
                        icon = Icons.Rounded.Inventory2,
                        pastelColor = PastelCyan,
                        modifier = Modifier.weight(1f)
                    )
                    BentoMetricCard(
                        title = "Stock bajo",
                        value = stockBajoCount.toString(),
                        icon = Icons.Rounded.WarningAmber,
                        pastelColor = if (stockBajoCount > 0) PastelPeach else PastelCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item(key = "search") {
                CampoBusqueda(
                    query = query,
                    onQueryChange = viewModel::onQueryChange
                )
                Spacer(modifier = Modifier.height(22.dp))
            }

            item(key = "section_title") {
                SectionHeader(
                    if (query.isBlank()) "Catálogo (${productos.size})"
                    else "Resultados (${productos.size})"
                )
            }

            if (productos.isEmpty()) {
                item(key = "empty") {
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
                                icon = Icons.Rounded.Inventory2,
                                backgroundColor = PastelCyan,
                                size = 54.dp,
                                iconSize = 26.dp
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (query.isBlank()) "No hay productos registrados"
                                else "Sin resultados para \"$query\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = OffWhite,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isAdmin && query.isBlank()) {
                                    "Toca el botón + para agregar el primer producto"
                                } else {
                                    "Toca actualizar para sincronizar con el servidor"
                                },
                                fontSize = 13.sp,
                                color = MediumGrey,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = productos,
                    key = { _, item -> item.id }
                ) { index, producto ->
                    ProductoCard(
                        producto = producto,
                        shape = groupedItemShape(index, productos.size),
                        onClick = { onProductoClick(producto.id) }
                    )
                    if (index < productos.lastIndex) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        // FAB circular solo visible para ADMIN, posicionado encima de la píldora inferior
        if (isAdmin) {
            FloatingActionButton(
                onClick = onNuevoProductoClick,
                shape = CircleShape,
                containerColor = ActiveWhite,
                contentColor = CharcoalBase,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 92.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Nuevo producto"
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 86.dp, start = 20.dp, end = 20.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = BentoCardShape,
                containerColor = CardDark,
                contentColor = OffWhite
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "Cerrar sesión",
            message = "Se cerrará tu sesión actual y se limpiará la caché local de productos y carrito.",
            confirmText = "Salir",
            icon = Icons.AutoMirrored.Rounded.Logout,
            iconTintBg = PastelPink,
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun BentoMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    pastelColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = BentoCardShape,
        color = CardDark
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            PastelIconCircle(
                icon = icon,
                backgroundColor = pastelColor,
                size = 40.dp,
                iconSize = 20.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = OffWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = MediumGrey
            )
        }
    }
}
