package com.iax.gestordeturnos.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import kotlinx.coroutines.delay

/**
 * Pantalla de bienvenida efímera (Splash Screen) de la aplicación.
 * * Actúa como el punto de entrada gráfico inicial al abrir el software. Proporciona una transición
 * visual corporativa limpia y fluida haciendo uso de una rutina de temporización asíncrona suspendida
 * ([delay]) integrada en el ciclo de vida seguro de Compose ([LaunchedEffect]).
 * * Tras completarse el margen temporal, delega al enrutador el transbordo inmediato hacia los flujos de login.
 *
 * @param onSplashTerminado Callback lambda ejecutado síncronamente tras expirar el contador para saltar de destino.
 * * @author Lucas Merino Ortín
 * * @author Juan Pedro López García
 */
@Composable
fun PantallaSplash(
    onSplashTerminado: () -> Unit
) {
    // Gancho de ciclo de vida seguro (Side-Effect). Al pasarle 'Unit' como clave, garantizamos
    // que la corrutina de fondo se ejecute estrictamente una única vez al instanciar la pantalla
    // en memoria, evitando que rotaciones de pantalla o recomposiciones reinicien el temporizador.
    LaunchedEffect(Unit) {
        delay(2000) // Temporizador de fondo multimedia que suspende el hilo durante 2000 ms (2 segundos)
        onSplashTerminado() // Ejecuta la lambda de salto gráfico delegada al NavHost central
    }

    // Contenedor principal que abarca la totalidad del lienzo multimedia con el color base corporativo
    Column(
        modifier = Modifier
            .fillMaxSize() // Expande el contenedor ocupando el 100% del ancho y alto físico del terminal
            .background(AzulCobalto), // Aplica el tinte del token cromático de la app de forma homogénea
        horizontalAlignment = Alignment.CenterHorizontally, // Centra los subcomponentes de forma horizontal
        verticalArrangement = Arrangement.Center // Alinea los subcomponentes exactamente en el centro geométrico vertical
    ) {
        // Iconografía principal descriptiva que opera como logotipo temporal de la plataforma
        Icon(
            imageVector = Icons.Filled.DateRange,
            contentDescription = "Logo de la App",
            tint = Color.White,
            modifier = Modifier.size(100.dp) // Escala de píxeles optimizada para destacar en el lienzo central
        )

        Spacer(modifier = Modifier.height(24.dp)) // Espaciador multimedia simétrico de separación

        // Elemento textual de cabecera: Título oficial del TFG
        Text(
            text = "Gestor de Turnos",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Elemento textual de pie: Eslogan corporativo estilizado mediante transparencia alfa
        Text(
            text = "Organiza tu tiempo",
            color = Color.White.copy(alpha = 0.8f), // Inyecta un canal de opacidad del 80% para denotar jerarquía visual
            fontSize = 16.sp
        )
    }
}