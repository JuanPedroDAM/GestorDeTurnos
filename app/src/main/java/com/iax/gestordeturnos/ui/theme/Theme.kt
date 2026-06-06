package com.iax.gestordeturnos.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView

// Esquema de emparejamiento cromático basado en los estándares estrictos de Material Design 3
private val EsquemaColores = lightColorScheme(
    primary = AzulCobalto,
    secondary = NaranjaAccion,
    background = FondoClaro,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = TextoOscuro,
    onSurface = TextoOscuro
)

/**
 * Envoltorio del Tema de Diseño Global ([MaterialTheme]) de la aplicación "Gestor de Turnos".
 * * Inyecta reactivamente el esquema corporativo de colores y tipografías a lo largo de la jerarquía de vistas.
 * * Implementa un mecanismo multimedia avanzado de efectos secundarios ([SideEffect]) para interceptar
 * la ventana física del hardware del dispositivo móvil, forzando un diseño inmersivo *Edge-to-Edge* con barras
 * de estado completamente transparentes de forma nativa.
 *
 * @param content Callback composable jerárquico que heredará los estilos declarados.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Composable
fun GestorDeTurnosTheme(
    content: @Composable () -> Unit
) {
    // Captura la referencia física del árbol de renderizado visual de Compose activo en el hardware
    val view = LocalView.current

    // Condición de seguridad: El bloque de alteración de barras del sistema operativo solo debe correr
    // en un dispositivo físico o emulador real, omitiéndose dentro del editor visual de layouts (EditMode)
    if (!view.isInEditMode) {
        // SideEffect garantiza que la comunicación con las API de la ventana de Android ocurra
        // de forma síncrona únicamente tras completarse con éxito la recomposición gráfica
        SideEffect {
            // Realiza un casteo seguro del contexto de la vista hacia la ComponentActivity host de Android
            val activity = view.context as ComponentActivity

            // Invoca la configuración multimedia nativa de pantalla sin bordes (Inmersiva)
            activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT, // Forza transparencia absoluta en la barra superior de estado
                    android.graphics.Color.TRANSPARENT  // Forza transparencia absoluta en la barra inferior de navegación de Android
                )
            )
        }
    }

    // Instanciación y distribución multimedia de tokens del motor de Material 3
    MaterialTheme(
        colorScheme = EsquemaColores,
        typography = Tipografia, // Enlaza de forma unificada el token tipográfico declarado en Type.kt
        content = content
    )
}