package com.iax.gestordeturnos.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.theme.AzulCobalto

/**
 * Componente de UI reutilizable y de diseño atómico que renderiza una franja horaria estructurada
 * dentro de un contenedor estilizado de bordes redondeados y aspecto limpio.
 * Actúa visualmente como un campo de texto informativo o de lectura, diseñado específicamente para
 * sustituir las cajas de entrada de texto clásicas por un disparador interactivo multimedia (como un selector
 * de hora nativo de Android, [android.app.TimePickerDialog]). Su uso principal se centra en los formularios
 * de creación y edición de las plantillas horarias de los tipos de turno.
 *
 * @param horaStr Cadena de texto formateada que representa la hora a mostrar en la tarjeta (ej.: "08:00", "22:15").
 * @param onClick Expresión lambda o callback que se ejecuta inmediatamente cuando el usuario pulsa sobre la superficie táctil de la caja.
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Composable
fun VisualizadorHoraNativoStyle(
    horaStr: String,
    onClick: () -> Unit
) {
    // Contenedor Box que define el área física del componente y gestiona su maquetación interna
    Box(
        modifier = Modifier
            .fillMaxWidth() // Expande el elemento horizontalmente para acoplarse al ancho de su contenedor padre
            .height(56.dp) // Define una altura estándar idéntica a la de los componentes TextField de Material Design 3
            .clip(RoundedCornerShape(12.dp)) // Recorta la superficie y los efectos visuales (como el ripple) en esquinas redondeadas de 12dp
            .background(Color(0xFFF8FAFC)) // Aplica un tinte de fondo gris neutro muy suave para denotar un área de lectura/selección
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)) // Dibuja un contorno perimetral sutil que define los límites de la caja
            .clickable { onClick() } // Inyecta el comportamiento interactivo y añade retroalimentación multimedia táctil por defecto del SO
            .padding(horizontal = 16.dp), // Establece márgenes internos a los lados para separar el texto de los bordes laterales
        contentAlignment = Alignment.CenterStart // Alinea los elementos internos verticalmente en el centro y pegados al inicio izquierdo
    ) {
        // Elemento textual que imprime la hora renderizada en pantalla
        Text(
            text = horaStr,
            fontSize = 18.sp, // Tamaño de fuente optimizado para lecturas rápidas en formularios de cuadrícula
            fontWeight = FontWeight.SemiBold, // Peso de tipografía seminegrita para destacar el valor temporal del fondo
            color = AzulCobalto // Aplica el token de color corporativo de la aplicación
        )
    }
}