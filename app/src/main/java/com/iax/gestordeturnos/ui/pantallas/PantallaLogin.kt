package com.iax.gestordeturnos.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.theme.NaranjaAccion
import com.iax.gestordeturnos.ui.viewmodels.AuthState
import com.iax.gestordeturnos.ui.viewmodels.AuthViewModel

/**
 * Pantalla interactiva que proporciona la interfaz de usuario para el inicio de sesión (Login)
 * y la autenticación de usuarios.
 * * Maneja de forma segura las credenciales de correo electrónico y contraseña empleando filtros
 * de entrada de teclado y enmascaramiento multimedia visual para salvaguardar la privacidad de los datos.
 * * Utiliza un gancho de ciclo de vida seguro ([LaunchedEffect]) para monitorizar de manera asíncrona
 * el estado de autenticación de red remoto expuesto por el [AuthViewModel], despachando alertas instantáneas
 * mediante Toasts o transbordos limpios en la navegación gráfica.
 *
 * @param authViewModel Instancia del orquestador de lógica [AuthViewModel] encargado de coordinar llamadas con Firebase.
 * @param onViajeRegistro Callback lambda invocado al pulsar el botón de redirección hacia el formulario de alta.
 * @param onLoginExitoso Callback lambda ejecutado de forma síncrona cuando las credenciales son validadas con éxito en el servidor.
 * * @author Lucas Merino Ortín
 * * @author Juan Pedro López García
 */
@Composable
fun PantallaLogin(
    authViewModel: AuthViewModel,
    onViajeRegistro: () -> Unit,
    onLoginExitoso: () -> Unit
) {
    // --- Variables de Estado local para la retención reactiva de las entradas de texto ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Extrae el contexto nativo de Android seguro para la visualización de notificaciones flotantes (Toasts)
    val context = LocalContext.current

    // Controla la superposición en pantalla de la ventana modal de restablecimiento de contraseña
    var mostrarRecuperar by remember { mutableStateOf(false) }

    // Recolecta reactivamente el flujo de estado asíncrono (StateFlow) de la autenticación remota
    val authState by authViewModel.authState.collectAsState()

    // Manejador multimedia de efectos secundarios. Reacciona de forma atómica y aislada
    // en un hilo seguro cada vez que el valor de la variable 'authState' es mutado desde la red.
    LaunchedEffect(authState) {
        if (authState is AuthState.LoginSuccess) {
            onLoginExitoso() // Despacha la redirección inmediata hacia el núcleo operativo de la app
        }

        when (authState) {
            is AuthState.RecoveryEmailSent -> {
                Toast.makeText(context, "✅ Correo enviado. Revisa tu bandeja.", Toast.LENGTH_LONG)
                    .show()
                authViewModel.resetState() // Resetea el puntero de estado del ViewModel para evitar ejecuciones en bucle
                mostrarRecuperar = false // Cierra de forma síncrona la ventana flotante
            }

            is AuthState.Error -> {
                // Extrae e imprime el banner textual con la descripción del fallo de red devuelto por el servidor
                Toast.makeText(context, (authState as AuthState.Error).message, Toast.LENGTH_LONG)
                    .show()
                authViewModel.resetState()
            }

            else -> {}
        }
    }

    // Andamio estructural básico con fondo corporativo blanco limpio
    Scaffold(containerColor = Color.White) { paddingValues ->
        // Distribución secuencial que centra simétricamente los campos del formulario en el lienzo táctil
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // --- 1. CABECERA INFORMATIVA ---
            Text(
                text = "Gestor de Turnos",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Inicia sesión para continuar",
                color = Color.Gray,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // --- 2. CAMPOS DE ENTRADA TEXTUAL ---

            // Entrada de datos: Dirección de correo electrónico
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        tint = AzulCobalto
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), // Fuerza el teclado del móvil a mostrar el símbolo '@'
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulCobalto,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = AzulCobalto,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Entrada de datos restringida: Contraseña con enmascaramiento multimedia visual
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                leadingIcon = {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AzulCobalto
                    )
                },
                visualTransformation = PasswordVisualTransformation(), // Enmascara los caracteres transformándolos en puntos de seguridad
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Configura el teclado para ocultar diccionarios de sugerencias
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AzulCobalto,
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    focusedLabelColor = AzulCobalto,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            // --- 3. ENLACE INTERACTIVO: RESTABLECIMIENTO DE CREDENCIALES ---
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Olvidé mi contraseña",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.End) // Desplaza el elemento hacia el extremo derecho de la caja
                    .clickable {
                        mostrarRecuperar = true
                    } // Habilita la interactividad táctil e inyecta la retroalimentación de onda expansiva
                    .padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- 4. BOTÓN DE ACCIÓN PRINCIPAL: ENTRAR ---
            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                shape = RoundedCornerShape(16.dp),
                enabled = authState != AuthState.Loading // Bloquea de forma elástica las pulsaciones concurrentes si está cargando
            ) {
                if (authState == AuthState.Loading) {
                    // Renderiza la animación multimedia circular mientras la petición corre asíncronamente en segundo plano
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0)) // Divisor visual perimetral de bloques
            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. BOTÓN DE ACCIÓN SECUNDARIO: REGISTRARME (SÓLIDO NARANJA ACCIÓN) ---
            Button(
                onClick = onViajeRegistro,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NaranjaAccion,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = authState != AuthState.Loading
            ) {
                if (authState == AuthState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Registrarme", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // --- INTERFAZ FLOTANTE DIÁLOGO: RECUPERAR CONTRASEÑA ---
    if (mostrarRecuperar) {
        var emailRecuperar by remember { mutableStateOf("") }
        Dialog(onDismissRequest = {
            if (authState != AuthState.Loading) mostrarRecuperar =
                false // Impide el descarte accidental si la petición de red está corriendo
        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = AzulCobalto,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Recuperar Contraseña",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Introduce tu correo y te enviaremos un enlace.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Entrada textual del correo de restablecimiento remoto
                    OutlinedTextField(
                        value = emailRecuperar,
                        onValueChange = { emailRecuperar = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AzulCobalto,
                            unfocusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón de despacho de correo de recuperación
                    Button(
                        onClick = { authViewModel.recuperarContrasenia(emailRecuperar) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                        shape = RoundedCornerShape(12.dp),
                        enabled = authState != AuthState.Loading
                    ) {
                        if (authState == AuthState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text("Enviar correo", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    TextButton(
                        onClick = { mostrarRecuperar = false },
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = authState != AuthState.Loading
                    ) { Text("Cancelar", color = Color.Gray) }
                }
            }
        }
    }
}