package com.iax.gestordeturnos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.iax.gestordeturnos.datos.BaseDeDatosApp
import com.iax.gestordeturnos.datos.TurnosRepository
import com.iax.gestordeturnos.ui.pantallas.NavegacionApp
import com.iax.gestordeturnos.ui.theme.GestorDeTurnosTheme
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModelFactory

/**
 * Actividad principal y punto de entrada físico de la aplicación en el sistema operativo Android ([ComponentActivity]).
 * * Actúa como la clase Host perimetral encargada de inicializar el ciclo de vida del software,
 * configurar la inversión de control y construir el grafo jerárquico de dependencias del proyecto.
 * * Instancia de forma centralizada la conexión Singleton de la base de datos de Room ([BaseDeDatosApp]),
 * la inyecta de forma segura en el repositorio y provee un ciclo de vida unificado y persistente para el
 * [TurnosViewModel] a lo largo del árbol declarativo multimedia de Jetpack Compose.
 *
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
class MainActivity : ComponentActivity() {

    /**
     * Método base del ciclo de vida de la Activity ejecutado síncronamente al instanciar la app.
     * * Inicializa el andamiaje gráfico y el motor de dependencias previo al renderizado visual.
     * @param savedInstanceState Contenedor de tipo [Bundle] con estados residuales del sistema (no utilizado aquí).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Habilita la directiva multimedia inmersiva Edge-to-Edge nativa de Android.
        // Forza a la interfaz gráfica expandirse por debajo de las barras de estado del sistema
        // y de la barra de navegación física del terminal, maximizando el lienzo utilizable.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicialización y captura de la instancia única (Singleton) del motor de persistencia local Room.
        // Se pasa el ApplicationContext para asegurar que la conexión se desacople de la Activity y evite Memory Leaks.
        val baseDeDatos = BaseDeDatosApp.obtenerBaseDeDatos(applicationContext)

        // Inyección de dependencias manual: Construye el Repositorio inyectándole los canales de acceso DAO.
        val repository = TurnosRepository(baseDeDatos.turnosDao())

        // Instancia la fábrica personalizada intermedia requerida para inyectar repositorios en constructores ViewModel.
        val factory = TurnosViewModelFactory(repository)

        // Resuelve y retiene el ciclo de vida de la instancia de [TurnosViewModel] acoplándolo a la Activity.
        // Esto blinda los flujos reactivos de datos (StateFlow) impidiendo que se destruyan ante rotaciones de pantalla.
        val viewModel = ViewModelProvider(this, factory)[TurnosViewModel::class.java]

        // Inicializa el bloque de composición declarativo nativo que sustituye los antiguos layouts XML de Android
        setContent {
            // Inyecta el contenedor de tokens de estilo, tipografías y paletas de colores de la app
            GestorDeTurnosTheme {

                // Lanza el orquestador y enrutador central distribuyéndole el ViewModel unificado en cascada
                NavegacionApp(viewModel = viewModel)
            }
        }
    }
}