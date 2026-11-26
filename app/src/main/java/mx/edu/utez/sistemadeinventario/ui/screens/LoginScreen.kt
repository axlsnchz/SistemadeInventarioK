package mx.edu.utez.sistemadeinventario.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mx.edu.utez.sistemadeinventario.R
import mx.edu.utez.sistemadeinventario.ui.components.BentoCardShape
import mx.edu.utez.sistemadeinventario.ui.components.DarkTextField
import mx.edu.utez.sistemadeinventario.ui.components.PastelIconCircle
import mx.edu.utez.sistemadeinventario.ui.components.SectionHeader
import mx.edu.utez.sistemadeinventario.ui.components.groupedItemShape
import mx.edu.utez.sistemadeinventario.ui.theme.ActiveWhite
import mx.edu.utez.sistemadeinventario.ui.theme.CardDark
import mx.edu.utez.sistemadeinventario.ui.theme.CharcoalBase
import mx.edu.utez.sistemadeinventario.ui.theme.ErrorSurface
import mx.edu.utez.sistemadeinventario.ui.theme.MediumGrey
import mx.edu.utez.sistemadeinventario.ui.theme.OffWhite
import mx.edu.utez.sistemadeinventario.ui.theme.PastelCyan
import mx.edu.utez.sistemadeinventario.ui.theme.PastelPink
import mx.edu.utez.sistemadeinventario.ui.viewmodels.AuthViewModel
import mx.edu.utez.sistemadeinventario.ui.viewmodels.UiState

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading = loginState is UiState.Loading

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) {
            viewModel.resetLoginState()
            onLoginSuccess()
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
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Encabezado grande arriba a la izquierda (26.sp, Medium)
            Image(
                painter = painterResource(id = R.drawable.logo_stox),
                contentDescription = "Stox",
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = (-24).dp, y = 18.dp)
            )

            Text(
                text = "Stox",
                fontSize = 26.sp,
                fontWeight = FontWeight.Medium,
                color = OffWhite
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Inicia sesión para gestionar inventario y ventas",
                fontSize = 14.sp,
                color = MediumGrey
            )

            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader("Credenciales")

            // Inputs apilados con 2.dp de separación y esquinas 24.dp / 4.dp
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                DarkTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = "Usuario",
                    placeholder = "admin o vendedor",
                    enabled = !isLoading,
                    containerColor = CardDark,
                    shape = groupedItemShape(0, 2),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                DarkTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    enabled = !isLoading,
                    containerColor = CardDark,
                    shape = groupedItemShape(1, 2),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login(username, password)
                        }
                    )
                )
            }

            if (loginState is UiState.Error) {
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
                            text = (loginState as UiState.Error).message,
                            fontSize = 13.sp,
                            color = PastelPink
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.login(username, password)
                },
                enabled = !isLoading,
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
                if (isLoading) {
                    CircularProgressIndicator(
                        color = OffWhite,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Iniciar sesión",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
