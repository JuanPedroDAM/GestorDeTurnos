package com.iax.gestordeturnos


import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.iax.gestordeturnos.datos.AsignacionTurno
import com.iax.gestordeturnos.datos.BaseDeDatosApp
import com.iax.gestordeturnos.datos.TipoTurno
import com.iax.gestordeturnos.datos.TurnosDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Clase de pruebas de integración adaptada para la simulación nativa de SQLite en la JVM.
 * * Utiliza el ejecutor de Robolectric ([RobolectricTestRunner]) para mockear el entorno de hardware
 * de Android, solucionando los fallos de inicialización del driver 'setWriteAheadLoggingEnabled'.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@RunWith(RobolectricTestRunner::class) // Indica a JUnit que use el entorno simulado de Robolectric
class BaseDatosIntegracionTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var baseDeDatos: BaseDeDatosApp
    private lateinit var turnosDao: TurnosDao

    /**
     * Inicializador del entorno. Extrae un contexto simulado de aplicación real
     * para permitir que Room inicialice el motor DDL de SQLite de forma transparente.
     */
    @Before
    fun crearBaseDeDatos() {
        // Extrae el contexto real emulado por Robolectric en lugar de un Mock inerte
        val context = ApplicationProvider.getApplicationContext<Context>()

        baseDeDatos = Room.inMemoryDatabaseBuilder(context, BaseDeDatosApp::class.java)
            .allowMainThreadQueries()
            .build()

        turnosDao = baseDeDatos.turnosDao()
    }

    @After
    fun cerrarBaseDeDatos() {
        baseDeDatos.close()
    }

    @Test
    fun insertarTipoTurno_debeGuardarseCorrectamente_ySerLeidoEnFlow() = runTest {
        val tipoTurnoPrueba = TipoTurno(
            idTipoTurno = 1,
            idUsuario = "user_test_123",
            nombre = "Mañana",
            abreviatura = "M",
            horaInicio = "08:00",
            horaFin = "15:00",
            colorHex = "#0D47A1"
        )

        turnosDao.insertarTipoTurno(tipoTurnoPrueba)

        val listaResultados = turnosDao.obtenerTiposTurno("user_test_123").first()

        assertEquals(1, listaResultados.size)
        assertEquals("Mañana", listaResultados[0].nombre)
        assertEquals("#0D47A1", listaResultados[0].colorHex)
    }

    @Test
    fun transaccionMasiva_debeSobrescribir_anteConflictosDeID() = runTest {
        val asignacion1 = AsignacionTurno(idAsignacion = 5, idUsuario = "uid", idTipoTurno = 1, fecha = 1000L, nota = "Nota Vieja")
        val asignacion2 = AsignacionTurno(idAsignacion = 5, idUsuario = "uid", idTipoTurno = 1, fecha = 1000L, nota = "Nota Nueva")

        turnosDao.transaccionAsignacionMasiva(listOf(asignacion1))
        turnosDao.transaccionAsignacionMasiva(listOf(asignacion2))

        val resultado = turnosDao.obtenerAsignacionPorId(5)

        assertTrue(resultado != null)
        assertEquals("Nota Nueva", resultado?.nota)
    }
}