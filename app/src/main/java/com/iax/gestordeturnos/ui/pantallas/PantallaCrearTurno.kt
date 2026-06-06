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
import java.util.Calendar
import androidx.core.graphics.toColorInt

/**
 * Pantalla interactiva que proporciona un formulario estructurado para la creación y
 * parametrización de un nuevo modelo o plantilla de turno personalizado por el usuario.
 * * Permite la captura validada de metadatos críticos como el nombre descriptivo,
 * una abreviatura restringida en longitud, las franjas horarias de entrada y salida mediante
 * selectores multimedia nativos, y la asignación cromática en formato Hexadecimal.
 *
 * @param onAtrasClick Callback lambda que se ejecuta al pulsar el botón de retroceso en la barra superior.
 * @param onGuardarClick Callback lambda que transmite los 5 parámetros consolidados del nuevo turno hacia el ViewModel padre.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCrearTurno(
    onAtrasClick: () -> Unit,
    onGuardarClick: (String, String, String, String, String) -> Unit
) {
    // Captura el contexto de Android indispensable para instanciar diálogos clásicos del sistema (TimePickerDialog)
    val context = LocalContext.current

    // --- Variables de Estado local para la retención reactiva de las entradas del formulario ---
    var nombreTurno by remember { mutableStateOf("") }
    var abreviatura by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("08:00") }
    var horaFin by remember { mutableStateOf("15:00") }

    // Color hexadecimal por defecto asignado a la nueva plantilla (Naranja corporativo de inicio)
    var colorHexSeleccionado by remember { mutableStateOf("#FF9800") }

    // Control de conmutación reactiva para superponer el diálogo avanzado del ColorPicker HSV
    var mostrarSelectorColor by remember { mutableStateOf(false) }

    // Andamio estructural de Material 3 para la barra superior fija y el cuerpo del formulario
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Generar nuevo Turno",
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
        // Distribución vertical principal que aísla el cuerpo scrollable del botón inferior estático de guardado
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {

            // Columna intermedia scrollable que alberga los campos de entrada de datos
            Column(
                modifier = Modifier
                    .weight(1f) // Absorbe todo el espacio vertical disponible, empujando la barra de confirmación al extremo inferior
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()), // Blindaje multimedia que evita el truncado de campos por teclado o pantallas compactas
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Entrada textual: Nombre identificativo de la jornada laboral
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

                // Entrada textual restringida: Sigla o código corto representativo (Límite estricto de 3 caracteres)
                OutlinedTextField(
                    value = abreviatura,
                    onValueChange = {
                        if (it.length <= 3) abreviatura = it.uppercase()
                    }, // Forzado reactivo a mayúsculas y acotación de memoria
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

                // Bloque selector interactivo para la franja de entrada (Hora Inicio)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Hora Inicio",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    VisualizadorHoraNativoStyle(horaStr = horaInicio) {
                        val calendar = Calendar.getInstance()
                        // Invoca de forma síncrona el widget multimedia nativo de selección horaria de Android
                        TimePickerDialog(
                            context,
                            { _, hora, minuto ->
                                horaInicio = String.format("%02d:%02d", hora, minuto)
                            }, // Formatea a estructura segura "HH:mm"
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true // Fuerza el formato de reloj de 24 horas
                        ).show()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bloque selector interactivo para la franja de salida (Hora Fin)
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Hora Fin",
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

                // Sección interactiva multimedia de personalización cromática
                Text(
                    "Elige un color para el tipo de turno",
                    color = AzulCobalto,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Intenta parsear la estampa Hexadecimal a objeto Color; predetermina un gris de salvaguarda ante fallos de formato
                    val colorTurno = try {
                        Color(colorHexSeleccionado.toColorInt())
                    } catch (_: Exception) {
                        Color.LightGray
                    }

                    // Círculo físico multimedia indicador del color seleccionado activo
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(colorTurno)
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Enlace interactivo táctil para desplegar la paleta de colores HSV avanzada
                    Text(
                        "Personalizar",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { mostrarSelectorColor = true })
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Barra inferior fija de consolidación dotada de elevación y sombra multimedia Material 3
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        // Validación preliminar estricta de cadenas antes de disparar la persistencia de datos en Room
                        if (nombreTurno.isNotBlank() && abreviatura.isNotBlank()) {
                            onGuardarClick(
                                nombreTurno,
                                abreviatura,
                                horaInicio,
                                horaFin,
                                colorHexSeleccionado
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("GUARDAR TURNO", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Despliegue condicional síncrono del selector de color avanzado en la capa superior de la UI
        if (mostrarSelectorColor) {
            DialogoColorAvanzadoMejorado(
                colorInicial = colorHexSeleccionado,
                onColorSeleccionado = { hexFinal ->
                    colorHexSeleccionado = hexFinal // Actualiza reactivamente el estado local
                    mostrarSelectorColor = false // Cierra la ventana flotante
                },
                onCerrar = { mostrarSelectorColor = false }
            )
        }
    }
}