package com.iax.gestordeturnos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Configuración del motor tipográfico y hojas de estilo de texto ([Typography]) oficiales del sistema.
 * * Estandariza las dimensiones, espaciados e interlineados para garantizar una consistencia visual simétrica.
 *
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
val Tipografia = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, // Hace uso de la fuente predeterminada adaptativa del sistema (ej. Roboto / Inter)
        fontWeight = FontWeight.Normal, // Peso tipográfico regular de lectura
        fontSize = 16.sp, // Dimensión física de la fuente optimizada para pantallas táctiles de alta densidad
        lineHeight = 24.sp, // Holgura o interlineado vertical aumentado para prevenir fatiga en lecturas extensas
        letterSpacing = 0.5.sp // Espaciado horizontal milimétrico entre caracteres
    )
)