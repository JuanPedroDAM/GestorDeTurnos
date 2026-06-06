package com.iax.gestordeturnos.ui.pantallas

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.componentes.DialogoColorAvanzadoMejorado
import com.iax.gestordeturnos.ui.componentes.VisualizadorHoraNativoStyle
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel
import java.util.Calendar
import androidx.core.graphics.toColorInt

/**
 * Pantalla interactiva que proporciona un formulario validado para la modificación o edición
 * profunda de una plantilla de turno preexistente seleccionada por el usuario.
 * * Utiliza mecánicas avanzadas de sincronización de flujos reactivos mediante [LaunchedEffect] para
 * inyectar asíncronamente los metadatos almacenados en Room dentro del estado mutable de los campos de la UI.
 * Integra validaciones de longitud tipográfica y selectores multimedia nativos cronológicos y cromáticos.
 *
 * @param viewModel Instancia del orquestador arquitectónico [TurnosViewModel] que expone el flujo del turno en edición.
 * @param onAtrasClick Callback lambda que se ejecuta al pulsar el botón de retroceso en la barra de herramientas superior.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaEditarTurno(
    viewModel: TurnosViewModel,
    onAtrasClick: () -> Unit
) {
    // Captura el Context nativo de Android indispensable para la invocación de diálogos clásicos del sistema
    val context = LocalContext.current

    // Recolecta reactivamente el flujo de estado (StateFlow) que guarda el turno mapeado para modificar
    val turnoAEditar by viewModel.turnoEnEdicion.collectAsState()

    // --- Variables de Estado local para el almacenamiento reactivo de las entradas del formulario ---
    var nombreTurno by remember { mutableStateOf("") }
    var abreviatura by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("08:00") }
    var horaFin by remember { mutableStateOf("16:00") }

    // Almacena de forma temporal la cadena Hexadecimal representativa del color de la plantilla
    var colorHexSeleccionado by remember { mutableStateOf("#FF9800") }

    // Flag mutable que conmuta la superposición del visor cromático HSV avanzado
    var mostrarSelectorColor by remember { mutableStateOf(false) }

    // Disparador de ciclo de vida seguro. Se ejecuta de forma síncrona en un hilo asíncrono secundario
    // cuando la referencia del objeto 'turnoAEditar' muta o se inyecta por primera vez en memoria.
    LaunchedEffect(turnoAEditar) {
        turnoAEditar?.let { turno ->
            nombreTurno = turno.nombre
            abreviatura = turno.abreviatura
            horaInicio = turno.horaInicio
            horaFin = turno.horaFin
            colorHexSeleccionado = turno.colorHex
        }
    }

    // Andamio estructural Material Design 3 para la maquetación perimetral de la pantalla
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Editar Turno",
                        color = AzulCobalto,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = AzulCobalto
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        // Contenedor secuencial que divide el cuerpo dinámico del pie de página fijo de confirmación
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // Columna con modificador de desplazamiento para evitar desbordamientos visuales por el teclado
            Column(
                modifier = Modifier
                    .weight(1f) // Absorbe la holgura vertical disponible empujando la barra de confirmación a la base
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Entrada de texto: Nombre o descripción identificativa del turno
                OutlinedTextField(
                    value = nombreTurno,
                    onValueChange = { nombreTurno = it },
                    label = { Text("Nombre del Turno") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulCobalto,
                        focusedLabelColor = AzulCobalto
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Entrada de texto acotada: Sigla o código visual corto (Límite condicional de 3 caracteres)
                OutlinedTextField(
                    value = abreviatura,
                    onValueChange = {
                        if (it.length <= 3) abreviatura = it.uppercase()
                    }, // Forzado reactivo a mayúsculas en tiempo de pulsación
                    label = { Text("Abrev") },
                    modifier = Modifier.width(150.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AzulCobalto,
                        focusedLabelColor = AzulCobalto
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(24.dp))

                // Bloque interactivo para el ajuste de la franja de entrada (Hora Inicio)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hora Inicio",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VisualizadorHoraNativoStyle(horaStr = horaInicio) {
                        val calendar = Calendar.getInstance()
                        // Instancia el widget multimedia nativo clásico para una captura temporal guiada y libre de errores
                        TimePickerDialog(
                            context,
                            { _, hora, minuto ->
                                horaInicio = String.format(
                                    "%02d:%02d",
                                    hora,
                                    minuto
                                ) // Formatea a estructura estándar "HH:mm"
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true // Configura el formato a reloj de 24 horas
                        ).show()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bloque interactivo para el ajuste de la franja de salida (Hora Fin)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Hora Fin",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VisualizadorHoraNativoStyle(horaStr = horaFin) {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            context,
                            { _, hora, minuto ->
                                horaFin = String.format("%02d:%02d", hora, minuto)
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(32.dp))

                // Sección interactiva multimedia para la actualización cromática
                Text(
                    text = "Elige un color para el tipo de turno",
                    color = AzulCobalto,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Intenta deserializar de forma segura la cadena Hex a un objeto Color funcional de Compose
                    val colorTurno = try {
                        Color(colorHexSeleccionado.toColorInt())
                    } catch (_: Exception) {
                        Color.LightGray
                    }
                    // Círculo físico multimedia indicador del color actual de la plantilla
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(colorTurno)
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Texto táctil que abre la paleta de colores HSV avanzada
                    Text(
                        text = "Personalizar",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { mostrarSelectorColor = true })
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Pie de página rígido con elevaciones tonales y sombras multimedia Material 3
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        // Desenvuelve de forma segura el objeto en edición y valida que los textos no estén en blanco
                        turnoAEditar?.let { turnoOriginal ->
                            if (nombreTurno.isNotBlank() && abreviatura.isNotBlank()) {
                                // Despacha la consolidación de cambios asíncronos hacia SQLite mediante el repositorio
                                viewModel.guardarEdicionTurno(
                                    idTipoTurno = turnoOriginal.idTipoTurno,
                                    nombre = nombreTurno,
                                    abreviatura = abreviatura,
                                    horaInicio = horaInicio,
                                    horaFin = horaFin,
                                    colorHex = colorHexSeleccionado
                                )
                                onAtrasClick() // Retorna de forma síncrona a la consola de gestión previa
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "GUARDAR CAMBIOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Renderizado condicional del diálogo selector de espectro cromático en la capa flotante de la UI
        if (mostrarSelectorColor) {
            DialogoColorAvanzadoMejorado(
                colorInicial = colorHexSeleccionado,
                onColorSeleccionado = { hexFinal ->
                    colorHexSeleccionado = hexFinal // Sincroniza síncronamente el estado local
                    mostrarSelectorColor = false // Remueve la ventana modal flotante
                },
                onCerrar = { mostrarSelectorColor = false }
            )
        }
    }
}