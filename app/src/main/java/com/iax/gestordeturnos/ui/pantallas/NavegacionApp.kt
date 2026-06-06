package com.iax.gestordeturnos.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.viewmodels.AuthViewModel
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel

/**
 * Orquestador centralizado de la navegación y enrutamiento gráfico de la aplicación ([NavHost]).
 * * Cumple la función estructural de declarar de forma declarativa todos los destinos accesibles de la UI,
 * administrar la pila de retroceso histórica (*backstack*) y proveer el ciclo de vida de los ViewModels
 * globales a las pantallas secundarias de autenticación, alta, edición y visualización multimedia del calendario.
 *
 * Implementa mecánicas avanzadas de purga de historial (limpieza de transiciones) para asegurar flujos seguros
 * de cierre de sesión y flujos lineales de bienvenida a la plataforma.
 *
 * @param viewModel Instancia única de la arquitectura de datos [TurnosViewModel], inyectada para persistir el estado del calendario.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Composable
fun NavegacionApp(viewModel: TurnosViewModel) {
    // Inicializa y recuerda el controlador nativo de navegación gráfica para persistir su estado ante recomposiciones del sistema
    val controladorNavegacion = rememberNavController()

    // Resuelve y retiene la instancia de [AuthViewModel] vinculada jerárquicamente al ámbito de este nodo raíz
    val authViewModel: AuthViewModel = viewModel()

    // Estructura contenedora que hereda los márgenes y paddings seguros de sistema (ej. barras de estado)
    Scaffold { paddingValues ->
        // Componente contenedor multimedia que dibuja e intercambia las pantallas según el identificador de ruta activo
        NavHost(
            navController = controladorNavegacion,
            startDestination = "splash", // Define el punto de entrada lógico por defecto al encender el software
            modifier = Modifier.padding(paddingValues)
        ) {

            // ==========================================
            // --- Destino: Splash Screen ---
            // ==========================================
            composable("splash") {
                PantallaSplash(
                    onSplashTerminado = {
                        // Navega a la interfaz de login removiendo de forma definitiva el Splash de la pila multimedia,
                        // impidiendo que el usuario pueda retroceder físicamente a la animación inicial.
                        controladorNavegacion.navigate("login") {
                            popUpTo("splash") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // ==========================================
            // --- Destino: Autenticación / Login ---
            // ==========================================
            composable("login") {
                PantallaLogin(
                    authViewModel = authViewModel,
                    onViajeRegistro = {
                        controladorNavegacion.navigate("registro") // Desplazamiento lineal al formulario de alta
                    },
                    onLoginExitoso = {
                        // Consolida el ingreso al panel operativo central y extrae el Login de la pila de memoria del dispositivo
                        controladorNavegacion.navigate("calendario") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            // ==========================================
            // --- Destino: Registro de Usuario ---
            // ==========================================
            composable("registro") {
                // Estado mutable local para conmutar de forma reactiva el cuadro multimedia de confirmación
                var mostrarExito by remember { mutableStateOf(false) }

                PantallaRegistro(
                    authViewModel = authViewModel,
                    onViajeLogin = {
                        controladorNavegacion.popBackStack() // Retorna al paso inmediatamente anterior de la pila
                    },
                    onRegistroExitoso = {
                        mostrarExito =
                            true // Dispara el redibujado de la UI para superponer el diálogo modal
                    }
                )

                // Diálogo emergente de éxito de registro de cuenta
                if (mostrarExito) {
                    Dialog(onDismissRequest = { /* Bloquea intencionalmente el cierre accidental mediante pulsaciones exteriores */ }) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Iconografía multimedia informativa en color verde de éxito
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "¡Registro completado!",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tu cuenta se ha creado con éxito. Ahora puedes iniciar sesión con tus credenciales.",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(32.dp))

                                // Botón interactivo de redirección forzada
                                Button(
                                    onClick = {
                                        mostrarExito = false // Oculta el diálogo actual
                                        controladorNavegacion.navigate("login") {
                                            popUpTo("registro") {
                                                inclusive =
                                                    true // Purga la pantalla de registro para evitar duplicaciones lógicas
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Inicia Sesión",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ==========================================
            // --- Destino: Calendario Principal ---
            // ==========================================
            composable("calendario") {
                PantallaCalendario(
                    viewModel = viewModel,
                    onAgregarTurno = { },
                    onViajeExportar = { controladorNavegacion.navigate("exportar") },
                    onCrearNuevoTurnoViaje = { controladorNavegacion.navigate("crear_turno") },
                    onViajeGestorTurnos = { controladorNavegacion.navigate("gestor_turnos") },
                    onViajeAyuda = { controladorNavegacion.navigate("ayuda") },
                    onCerrarSesion = {
                        // Resetea los estados de autenticación y vacía de raíz la totalidad del árbol del backstack (ID cero)
                        authViewModel.cerrarSesion()
                        controladorNavegacion.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // ==========================================
            // --- Destino: Exportación de Turnos ---
            // ==========================================
            composable("exportar") {
                PantallaExportar(
                    viewModel = viewModel,
                    onVolverClick = { controladorNavegacion.popBackStack() }
                )
            }

            // ==========================================
            // --- Destino: Centro de Ayuda ---
            // ==========================================
            composable("ayuda") {
                PantallaAyuda(
                    onVolverClick = { controladorNavegacion.popBackStack() }
                )
            }

            // ==========================================
            // --- Destino: Listado / Gestión de Tipos de Turno ---
            // ==========================================
            composable("gestor_turnos") {
                PantallaGestorTurnos(
                    viewModel = viewModel,
                    onAtrasClick = { controladorNavegacion.popBackStack() },
                    onEditarClick = { turno ->
                        // Sincroniza síncronamente el objeto a editar en el almacén de datos global previo al viaje multimedia
                        viewModel.seleccionarTurnoParaEditar(turno)
                        controladorNavegacion.navigate("editar_turno")
                    },
                    onCrearTurnoClick = { controladorNavegacion.navigate("crear_turno") }
                )
            }

            // ==========================================
            // --- Destino: Alta de Turno ---
            // ==========================================
            composable("crear_turno") {
                PantallaCrearTurno(
                    onAtrasClick = { controladorNavegacion.popBackStack() },
                    onGuardarClick = { nombre, abrev, inicio, fin, color ->
                        // Delega al ViewModel común la inserción asíncrona de la nueva entidad y vuelve atrás
                        viewModel.guardarNuevoTurno(nombre, abrev, inicio, fin, color)
                        controladorNavegacion.popBackStack()
                    }
                )
            }

            // ==========================================
            // --- Destino: Edición de Turno ---
            // ==========================================
            composable("editar_turno") {
                PantallaEditarTurno(
                    viewModel = viewModel,
                    onAtrasClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
    }
}