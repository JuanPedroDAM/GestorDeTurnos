package com.iax.gestordeturnos.ui.pantallas

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import androidx.core.net.toUri
import androidx.compose.ui.draw.clip

/**
 * Pantalla informativa de Ayuda, Soporte Técnico y Preguntas Frecuentes (FAQ) de la aplicación.
 * * Proporciona un entorno interactivo multimedia de autoayuda mediante tarjetas expandibles animadas.
 * Adicionalmente, implementa un puente de comunicación directo con el hardware y software de terceros
 * del dispositivo del usuario al disparar un canal de mensajería asíncrona mediante un [Intent] implícito
 * de correo electrónico.
 *
 * @param onVolverClick Callback lambda que se ejecuta de forma síncrona al accionar el botón de retroceso de la barra superior.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAyuda(
    onVolverClick: () -> Unit
) {
    // Captura el Contexto actual de ejecución de Android de forma segura dentro del árbol de Compose
    // para habilitar el lanzamiento de actividades externas independientes de la aplicación.
    val context = LocalContext.current

    // Componente base de andamiaje Material Design 3 para pintar la barra superior y el fondo general
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Ayuda y Soporte",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8FAFC) // Tono grisáceo sutil para evitar fatiga visual
                )
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        // Contenedor secuencial que estructura los bloques informativos de soporte
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                // Inyecta un modificador multimedia de desplazamiento síncrono para asegurar
                // que el listado de preguntas no quede fuera de pantalla en terminales de baja resolución.
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Preguntas Frecuentes",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = AzulCobalto
            )
            Text(text = "Resuelve tus dudas rápidamente", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // ------------------------------------------
            // --- Lista de Preguntas Frecuentes (FAQ) ---
            // ------------------------------------------
            TarjetaFAQ(
                pregunta = "¿Cómo creo un nuevo tipo de turno?",
                respuesta = "Abre el menú lateral (arriba a la izquierda) y selecciona 'Gestión de turnos'. Allí podrás añadir nuevos turnos pulsando el botón '+' y eligiendo su nombre, horario y color."
            )
            Spacer(modifier = Modifier.height(12.dp))
            TarjetaFAQ(
                pregunta = "¿Cómo asigno un turno en el calendario?",
                respuesta = "En la pantalla principal del calendario, pulsa el día que quieras editar. Luego pulsa el botón flotante '+' naranja abajo a la derecha y selecciona el turno que deseas asignar."
            )
            Spacer(modifier = Modifier.height(12.dp))
            TarjetaFAQ(
                pregunta = "¿Cómo exporto mi cuadrante?",
                respuesta = "Abre el menú lateral y pulsa 'Exportar calendario'. Podrás elegir si quieres un PDF, una imagen JPG o un archivo para sincronizar con Google Calendar, además de elegir las fechas exactas."
            )
            Spacer(modifier = Modifier.height(12.dp))
            TarjetaFAQ(
                pregunta = "¿Están seguros mis datos?",
                respuesta = "¡Totalmente! Tus turnos se guardan de forma local y segura en tu dispositivo vinculados a tu cuenta, por lo que nadie más puede acceder a ellos."
            )

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(32.dp))

            // ------------------------------------------
            // --- Sección de Contacto por Correo ---
            // ------------------------------------------
            Text(
                text = "¿Sigues necesitando ayuda?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nuestro equipo de soporte técnico está disponible para ayudarte con cualquier problema.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón interactivo que inicializa la lógica multimedia de Intents externos
            Button(
                onClick = {
                    // Inicialización de un Intent implícito bajo la acción estándar ACTION_SENDTO.
                    // Garantiza el filtrado estricto del sistema operativo para abrir únicamente
                    // clientes de correo (como Gmail u Outlook) y omitir aplicaciones de mensajería de texto.
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data =
                            "mailto:soporte@gestordeturnos.com".toUri() // Esquema URI de correo corporativo
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Duda sobre Gestor de Turnos"
                        ) // Preconfigura el asunto
                    }
                    // Despierta el activity externo delegándolo al sistema operativo
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Contactar al Soporte",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Componente atómico y reutilizable que modela una tarjeta expansible de tipo acordeón para albergar las FAQ.
 * * Utiliza las API avanzadas de interpolación de fotogramas de Compose para animar dinámicamente
 * la transición y tamaño geométrico del contenedor al desplegar la respuesta.
 *
 * @param pregunta Cadena de texto informativa que describe la duda formulada por el usuario.
 * @param respuesta Cadena de texto con la instrucción detallada para subsanar la duda.
 */
@Composable
fun TarjetaFAQ(pregunta: String, respuesta: String) {
    // Flag de estado mutable local para conmutar reactivamente la visibilidad de la respuesta
    var expandido by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            // Invierte el estado booleano de expansión ante cualquier evento multimedia táctil del usuario
            .clickable { expandido = !expandido }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Cabecera estática de la tarjeta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pregunta,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                // Rotación lógica de la iconografía multimedia indicadora según el estado de expansión
                Icon(
                    imageVector = if (expandido) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = AzulCobalto
                )
            }

            // Bloque animado reactivo encargado de orquestar el redibujado de la caja a nivel gráfico
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)), // Despliegue elástico vertical en 300 ms
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300))   // Repliegue progresivo vertical en 300 ms
            ) {
                // Estructura oculta que emerge de forma fluida
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = respuesta,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 20.sp // Holgura tipográfica interlineal aumentada para optimizar la legibilidad
                    )
                }
            }
        }
    }
}