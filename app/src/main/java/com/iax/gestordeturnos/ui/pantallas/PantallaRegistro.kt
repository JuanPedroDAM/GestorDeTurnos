package com.iax.gestordeturnos.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.theme.NaranjaAccion
import com.iax.gestordeturnos.ui.viewmodels.AuthState
import com.iax.gestordeturnos.ui.viewmodels.AuthViewModel

/**
 * Pantalla interactiva que proporciona la interfaz y el formulario estructurado para el registro
 * de nuevos usuarios en el sistema.
 * * Captura y valida de forma reactiva las entradas textuales del correo electrónico, la contraseña
 * y su respectiva confirmación de seguridad, empleando enmascaramiento multimedia visual para salvaguardar
 * los caracteres en la memoria caché de la UI.
 * * Utiliza un bloque de ciclo de vida seguro ([LaunchedEffect]) que intercepta de forma asíncrona
 * las respuestas de red emitidas por el [AuthViewModel], disparando banners flotantes informativos (Toasts)
 * o desencadenando de forma síncrona el diálogo modal de confirmación en la capa superior de navegación.
 *
 * @param authViewModel Instancia del orquestador arquitectónico [AuthViewModel] encargado de procesar la lógica de alta.
 * @param onViajeLogin Callback lambda ejecutado al presionar el botón inferior de descarte para regresar al Login.
 * @param onRegistroExitoso Callback lambda gatillado síncronamente cuando el servidor consolida la nueva cuenta en la base de datos remota.
 * * @author Lucas Merino Ortín
 * * @author Juan Pedro López García
 */
@Composable
fun PantallaRegistro(
    authViewModel: AuthViewModel,
    onViajeLogin: () -> Unit,
    onRegistroExitoso: () -> Unit
) {
    // --- Variables de Estado local para la retención reactiva de las entradas de texto ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    // Extrae el contexto nativo de Android seguro para la invocación de Toasts informativos
    val context = LocalContext.current

    // Recolecta reactivamente el flujo de estado asíncrono (StateFlow) de la autenticación remota
    val authState by authViewModel.authState.collectAsState()

    // Manejador multimedia de efectos secundarios. Monitoriza de forma aislada el flujo de red
    // y se activa síncronamente cuando el puntero del objeto 'authState' muta su valor.
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.RegisterSuccess -> {
                onRegistroExitoso() // Dispara el modal de éxito declarado en el orquestador de rutas general
                authViewModel.resetState() // Pone a cero el estado del ViewModel para evitar ejecuciones cíclicas en bucle
            }

            is AuthState.Error -> {
                // Desenvuelve e imprime la descripción exacta del fallo de red devuelto por el servidor
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG)
                    .show()
                authViewModel.resetState()
            }

            else -> {}
        }
    }

    // Contenedor base de andamiaje con fondo blanco absoluto para evitar fatiga visual
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        // Distribución lineal vertical principal que organiza el banner superior y los campos de entrada
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- 1. BANNER DE CABECERA DECORATIVO INTERACTIVO ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp) // Altura estática proporcionada por las guías estéticas de diseño corporativo
                    .background(AzulCobalto) // Fondo con el token de color principal de la app
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Registro",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Crea tu cuenta para empezar a organizar tu tiempo.",
                    color = Color.White.copy(alpha = 0.8f), // Aplica canal alfa de transparencia sutil al texto
                    fontSize = 16.sp
                )
            }

            // --- 2. CUERPO CENTRAL DEL FORMULARIO DE ALTA ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Entrada de texto: Dirección de correo electrónico
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Email,
                            contentDescription = null,
                            tint = NaranjaAccion
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Adapta el teclado virtual inyectando la tecla '@'
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaranjaAccion,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Entrada de texto: Contraseña primaria con enmascaramiento de caracteres
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = NaranjaAccion
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(), // Transforma visualmente el texto plano en puntos opacos de seguridad
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Esto oculta sugerencias automáticas del corrector
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaranjaAccion,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Entrada de texto: Duplicado de confirmación estricta de contraseña
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text("Confirmar contraseña") },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = NaranjaAccion
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaranjaAccion,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                // --- 3. BOTÓN DE ACCIÓN CENTRAL: CREAR CUENTA ---
                Button(
                    onClick = { authViewModel.registrar(email, password, passwordConfirm) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccion), // Botón sólido con el color de contraste secundario
                    shape = RoundedCornerShape(16.dp),
                    enabled = authState != AuthState.Loading // Inhabilita interacciones concurrentes de forma elástica si corre una corrutina I/O
                ) {
                    if (authState == AuthState.Loading) {
                        // Renderiza el indicador de carga multimedia animado en el centro geométrico del botón
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text("Crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0)) // Línea de separación perimetral estética
                Spacer(modifier = Modifier.height(24.dp))

                // --- 4. BOTÓN DE ACCIÓN SECUNDARIO: RETORNO AL LOGIN ---
                OutlinedButton(
                    onClick = onViajeLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NaranjaAccion),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        NaranjaAccion
                    ), // Contorno perimetral definido de contraste cromático
                    shape = RoundedCornerShape(16.dp),
                    enabled = authState != AuthState.Loading
                ) {
                    Text("Inicia Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}