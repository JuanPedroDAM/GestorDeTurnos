package com.iax.gestordeturnos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.theme.NaranjaAccion
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import androidx.core.graphics.toColorInt

/**
 * Cuadro de diálogo modal de pantalla completa que permite al usuario seleccionar un color
 * personalizado de forma multimedia interactiva mediante una rueda espectral basada en el modelo HSV.
 * * Este componente es utilizado por el sistema para la personalización de las etiquetas visuales
 * de los tipos de turno (ej. asignar un color hexadecimal único a las guardias médicas o turnos de noche).
 * Implementa un mecanismo tolerante a fallos para el parseo cromático y controles reactivos de confirmación.
 *
 * @param colorInicial Cadena de texto que representa el color de partida en formato Hexadecimal (ej.: "#FF5733").
 * @param onColorSeleccionado Callback lambda que se dispara al confirmar la selección ("OK"), devolviendo el nuevo código Hex.
 * @param onCerrar Callback lambda que se ejecuta al cancelar la acción o descartar el diálogo modal de la pantalla.
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Composable
fun DialogoColorAvanzadoMejorado(
    colorInicial: String,
    onColorSeleccionado: (String) -> Unit,
    onCerrar: () -> Unit
) {
    // Intenta deserializar de forma segura la cadena Hexadecimal a un objeto Color nativo de Compose.
    // Captura cualquier excepción de formato (p. ej. si viene vacío o sin el prefijo '#') para evitar un crash,
    // predeterminando el color blanco como salvaguarda.
    val colorObjetoInicial = try {
        Color(colorInicial.toColorInt())
    } catch (_: Exception) {
        Color.White
    }

    // Inicializa y recuerda el controlador de estado provisto por la librería externa para manipular el canvas del Picker
    val controller = rememberColorPickerController()

    // Estado mutable local que almacena la cadena del color seleccionado dinámicamente antes de su confirmación final
    var hexTemporal by remember { mutableStateOf(colorInicial) }

    // Componente nativo de Compose para superponer una ventana flotante sobre el contenido actual de la pantalla
    Dialog(
        onDismissRequest = onCerrar,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Fuerza al diálogo a expandirse a lo ancho sin márgenes nativos
    ) {
        // Estructura contenedora Material Design 3 que organiza de forma limpia la barra superior, barra inferior y cuerpo
        Scaffold(
            topBar = {
                // Fila superior que actúa como cabecera o Toolbar del diálogo modal
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Selector de Color Avanzado",
                        color = AzulCobalto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            },
            bottomBar = {
                // Fila inferior de acciones que contiene los botones de descarte o consolidación de datos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onCerrar) {
                        Text("CANCELAR", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = { onColorSeleccionado(hexTemporal) }, // Retorna el color definitivo acumulado localmente
                        colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccion)
                    ) {
                        Text("OK")
                    }
                }
            },
            containerColor = Color.White
        ) { paddingValues ->
            // Columna principal que aloja el espacio de renderizado interactivo y los indicadores cromáticos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()), // Añade soporte multimedia de Scroll para evitar desbordamientos en pantallas compactas
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Contenedor del área interactiva táctil del selector de espectro HSV
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.LightGray)
                ) {
                    // Instanciación del elemento gráfico de la librería externa.
                    // Gestiona eventos táctiles de arrastre (drag) en tiempo real en un hilo de renderizado rápido.
                    HsvColorPicker(
                        modifier = Modifier.fillMaxSize(),
                        controller = controller,
                        initialColor = colorObjetoInicial,
                        onColorChanged = { colorEnvelope ->
                            // El 'colorEnvelope' provee la información matemática de la selección.
                            // Se extrae el código hexadecimal puro (ARGB) y se le concatena el prefijo requerido por el backend/Room
                            val hex = "#${colorEnvelope.hexCode}"
                            hexTemporal =
                                hex // Actualiza de forma reactiva el estado para redibujar los textos asociados
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Fila informativa para la visualización del código de salida y la muestra física del color
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Muestra el texto dinámico del String Hexadecimal actual (ej.: #FFFFFFFF)
                    Text(
                        text = hexTemporal,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = AzulCobalto,
                        modifier = Modifier.weight(1f) // Expande el texto ocupando todo el ancho sobrante
                    )
                    Spacer(modifier = Modifier.width(16.dp))

                    // Círculo multimedia de previsualización que lee reactivamente el color activo del controlador gráfico
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(controller.selectedColor.value) // Vinculación directa con la mutación cromática de la paleta
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}