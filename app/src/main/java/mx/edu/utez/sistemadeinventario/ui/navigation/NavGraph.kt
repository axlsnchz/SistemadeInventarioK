package mx.edu.utez.sistemadeinventario.ui.navigation

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utez.sistemadeinventario.InventarioApplication
import mx.edu.utez.sistemadeinventario.data.nfc.NfcManager
import mx.edu.utez.sistemadeinventario.ui.components.FloatingPillNavBar
import mx.edu.utez.sistemadeinventario.ui.screens.DetalleProductoScreen
import mx.edu.utez.sistemadeinventario.ui.screens.InventarioScreen
import mx.edu.utez.sistemadeinventario.ui.screens.LoginScreen
import mx.edu.utez.sistemadeinventario.ui.screens.PosScreen
import mx.edu.utez.sistemadeinventario.ui.screens.ProductoFormScreen
import mx.edu.utez.sistemadeinventario.ui.screens.ScannerScreen
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.viewmodels.AuthViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.DetalleProductoViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.InventarioViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.PosViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ProductoFormViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ScannerViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.ViewModelFactory

object NavRoutes {
    const val LOGIN = "login"
    const val INVENTARIO = "inventario"
    const val SCANNER = "scanner"
    const val POS = "pos"
    const val DETALLE = "detalle/{id}"
    const val FORMULARIO = "formulario?id={id}"

    fun detalle(id: Long) = "detalle/$id"
    fun formulario(id: Long = -1L) = "formulario?id=$id"
}

@Composable
fun AppNavGraph(
    nfcManager: NfcManager
) {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val container = (app as InventarioApplication).appContainer
    val factory = remember(app) { ViewModelFactory(app) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val posViewModel: PosViewModel = viewModel(factory = factory)

    val session by authViewModel.session.collectAsStateWithLifecycle()
    val cartItems by posViewModel.items.collectAsStateWithLifecycle()
    val cartCount = remember(cartItems) { cartItems.sumOf { it.cantidad } }

    // Aviso cuando el servidor responde 401 (token vencido)
    LaunchedEffect(authViewModel) {
        authViewModel.sessionExpired.collect {
            Toast.makeText(
                context,
                "Tu sesión expiró, inicia sesión de nuevo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Espera a que DataStore cargue antes de decidir la pantalla inicial
    val currentSession = session
    if (currentSession == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CharcoalBase),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PastelCyan)
        }
        return
    }

    val navController = rememberNavController()
    val startDestination = remember {
        if (currentSession.isLoggedIn) NavRoutes.INVENTARIO else NavRoutes.LOGIN
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // Si la sesión pasa a cerrada (logout o 401), regresar a login limpiando el historial
    LaunchedEffect(currentSession.isLoggedIn) {
        val activeRoute = navController.currentDestination?.route
        if (!currentSession.isLoggedIn && activeRoute != null && activeRoute != NavRoutes.LOGIN) {
            navController.navigate(NavRoutes.LOGIN) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val showBottomBar = currentRoute in setOf(
        NavRoutes.INVENTARIO,
        NavRoutes.SCANNER,
        NavRoutes.POS
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBase)
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = {
                        navController.navigate(NavRoutes.INVENTARIO) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(NavRoutes.INVENTARIO) {
                val inventarioViewModel: InventarioViewModel = viewModel(factory = factory)
                InventarioScreen(
                    viewModel = inventarioViewModel,
                    username = currentSession.username,
                    rol = currentSession.rol,
                    onProductoClick = { id ->
                        navController.navigate(NavRoutes.detalle(id))
                    },
                    onNuevoProductoClick = {
                        if (currentSession.isAdmin) {
                            navController.navigate(NavRoutes.formulario())
                        }
                    },
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }

            composable(NavRoutes.SCANNER) {
                val scannerViewModel: ScannerViewModel = viewModel(factory = factory)
                ScannerScreen(
                    viewModel = scannerViewModel,
                    nfcManager = nfcManager,
                    onProductoEncontrado = { id ->
                        navController.navigate(NavRoutes.detalle(id))
                    }
                )
            }

            composable(NavRoutes.POS) {
                val productosCatalogo by remember(container) {
                    container.productoRepository.observarProductos()
                }.collectAsStateWithLifecycle(initialValue = emptyList())

                PosScreen(
                    viewModel = posViewModel,
                    productosDisponibles = productosCatalogo,
                    nfcManager = nfcManager
                )
            }

            composable(
                route = NavRoutes.DETALLE,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: -1L
                if (id <= 0L) {
                    LaunchedEffect(Unit) { navController.popBackStack() }
                } else {
                    val detalleFactory = remember(app, id) { ViewModelFactory(app, id) }
                    val detalleViewModel: DetalleProductoViewModel = viewModel(
                        key = "detalle_$id",
                        factory = detalleFactory
                    )
                    DetalleProductoScreen(
                        viewModel = detalleViewModel,
                        onBack = { navController.popBackStack() },
                        onEditarClick = { prodId ->
                            if (currentSession.isAdmin) {
                                navController.navigate(NavRoutes.formulario(prodId))
                            }
                        },
                        onAgregarAlCarrito = { producto ->
                            posViewModel.agregarProducto(producto)
                        }
                    )
                }
            }

            composable(
                route = NavRoutes.FORMULARIO,
                arguments = listOf(
                    navArgument("id") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { entry ->
                // Protección de ruta: solo ADMIN puede crear/editar productos
                if (!currentSession.isAdmin) {
                    LaunchedEffect(Unit) {
                        Toast.makeText(context, "No tienes permiso", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                } else {
                    val id = entry.arguments?.getLong("id") ?: -1L
                    val formFactory = remember(app, id) { ViewModelFactory(app, id) }
                    val formViewModel: ProductoFormViewModel = viewModel(
                        key = "form_$id",
                        factory = formFactory
                    )
                    ProductoFormScreen(
                        viewModel = formViewModel,
                        nfcManager = nfcManager,
                        onBack = { navController.popBackStack() },
                        onSaved = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showBottomBar) {
            FloatingPillNavBar(
                currentRoute = currentRoute,
                cartCount = cartCount,
                inventarioRoute = NavRoutes.INVENTARIO,
                scannerRoute = NavRoutes.SCANNER,
                posRoute = NavRoutes.POS,
                onNavigate = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(NavRoutes.INVENTARIO) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
