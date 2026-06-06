package com.iax.gestordeturnos.datos

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Interfaz de Acceso a Datos (DAO) para gestionar todas las operaciones CRUD y consultas
 * personalizadas de la aplicación en la base de datos local SQLite mediante Room.
 * * Contiene los mecanismos de interacción para usuarios, tipos de turnos, asignaciones de calendario
 * y notas globales. Todas las funciones de escritura/lectura única se ejecutan de manera suspendida,
 * mientras que las lecturas en tiempo real utilizan flujos reactivos ([Flow]).
 * @author Lucas Merino Ortín
 * @author Juan Pedro López Garcia
 */
@Dao
interface TurnosDao {

    // ==========================================
    // --- Operaciones de Usuarios ---
    // ==========================================

    /**
     * Inserta un nuevo usuario en la base de datos.
     * * @param usuario El objeto [Usuario] que se desea almacenar.
     * @throws Exception Si ocurre un error de restricción en la base de datos.
     * * Nota: Si el identificador único (ID) del usuario ya existe en la tabla,
     * se aplica la estrategia [OnConflictStrategy.REPLACE], reemplazando toda la fila antigua por la nueva.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario)

    /**
     * Recupera la información de un usuario específico basado en su identificador único.
     * * @param id El identificador único en formato [String] del usuario buscado.
     * @return Un objeto [Usuario] si se encuentra una coincidencia; `null` en caso contrario.
     */
    @Query("SELECT * FROM usuarios WHERE idUsuario = :id LIMIT 1")
    suspend fun obtenerUsuarioPorId(id: String): Usuario?

    // ==========================================
    // --- Operaciones de Tipos de Turno ---
    // ==========================================

    /**
     * Obtiene de forma reactiva todos los tipos de turno o plantillas que han sido creados por un usuario.
     * * @param idUsuario El identificador del usuario propietario de las plantillas de turnos.
     * @return Un flujo emitido mediante [Flow] que contiene una lista de objetos [TipoTurno].
     * Cada vez que cambien los datos en la tabla, el flujo emitirá una nueva lista actualizada automáticamente.
     */
    @Query("SELECT * FROM tipos_turno WHERE idUsuario = :idUsuario")
    fun obtenerTiposTurno(idUsuario: String): Flow<List<TipoTurno>>

    /**
     * Almacena una nueva plantilla o categoría de turno en la tabla correspondiente.
     * * @param tipoTurno Instancia de [TipoTurno] con la configuración (colores, horas, nombres) a guardar.
     */
    @Insert
    suspend fun insertarTipoTurno(tipoTurno: TipoTurno)

    /**
     * Actualiza los valores de una plantilla de turno que ya existe en la base de datos.
     * * @param tipoTurno La instancia de [TipoTurno] que contiene las modificaciones a sincronizar mediante su ID.
     */
    @Update
    suspend fun actualizarTipoTurno(tipoTurno: TipoTurno)

    /**
     * Elimina de forma definitiva una plantilla o categoría de turno de la base de datos.
     * * @param tipoTurno La instancia del objeto [TipoTurno] a ser borrada.
     */
    @androidx.room.Delete
    suspend fun eliminarTipoTurno(tipoTurno: TipoTurno)

    // ==========================================
    // --- Operaciones de Asignaciones (Calendario) ---
    // ==========================================

    /**
     * Inserta un conjunto masivo de asignaciones de turnos en el calendario.
     * * @param turnos Una [List] de objetos [AsignacionTurno] a insertar.
     * * Nota: En caso de colisión con un ID existente, se sobrescribirá la información antigua.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAsignaciones(turnos: List<AsignacionTurno>)

    /**
     * Elimina una asignación específica del calendario utilizando su clave primaria.
     * * @param id El número identificador único de la asignación de turno.
     */
    @Query("DELETE FROM asignaciones_turno WHERE idAsignacion = :id")
    suspend fun eliminarAsignacion(id: Int)

    /**
     * Ejecuta una inserción masiva de asignaciones de turnos de manera atómica bajo una transacción única de SQLite.
     * * @param turnos Una [List] de objetos [AsignacionTurno] que representan las asignaciones que se quieren guardar simultáneamente.
     * * La anotación [@Transaction] garantiza la atomicidad de la operación: si alguna inserción de la lista falla,
     * se realiza un rollback automático y ninguna de las asignaciones se consolida en la persistencia local.
     */
    @Transaction
    suspend fun transaccionAsignacionMasiva(turnos: List<AsignacionTurno>) {
        insertarAsignaciones(turnos) // Ejecución del lote bajo el paraguas de una transacción segura
    }

    /**
     * Realiza una consulta relacional (INNER JOIN) compleja para obtener el histórico detallado de turnos
     * asignados a un usuario dentro de un rango cronológico establecido.
     * * Combina los datos de vinculación de la tabla 'asignaciones_turno' con las propiedades estéticas y
     * temporales de la tabla 'tipos_turno'.
     * * @param usuarioId El identificador del usuario activo actual.
     * @param inicio Fecha límite inferior del rango de búsqueda, expresada en milisegundos (timestamp Epoch).
     * @param fin Fecha límite superior del rango de búsqueda, expresada en milisegundos (timestamp Epoch).
     * @return Un flujo asíncrono [Flow] que contiene la lista de detalles combinados bajo el objeto de proyección [TurnoAsignadoDetalle].
     */
    @Query(
        """
        SELECT a.idAsignacion, a.fecha, a.nota, a.tieneAlerta, t.idTipoTurno, t.nombre, t.abreviatura, t.colorHex, t.horaInicio, t.horaFin 
        FROM asignaciones_turno a 
        INNER JOIN tipos_turno t ON a.idTipoTurno = t.idTipoTurno 
        WHERE a.idUsuario = :usuarioId AND a.fecha >= :inicio AND a.fecha <= :fin
    """
    )
    fun obtenerAsignacionesConDetalle(
        usuarioId: String,
        inicio: Long,
        fin: Long
    ): Flow<List<TurnoAsignadoDetalle>>

    /**
     * Actualiza el campo de observaciones o anotaciones de un turno específico asignado previamente.
     * * @param idAsignacion Clave primaria del turno asignado que se modificará.
     * @param nota Cadena de texto que representa el nuevo comentario asociado al día.
     */
    @Query("UPDATE asignaciones_turno SET nota = :nota WHERE idAsignacion = :idAsignacion")
    suspend fun actualizarNotaTurno(idAsignacion: Int, nota: String)

    /**
     * Modifica rápidamente el estado de habilitación/deshabilitación de la alerta para un turno del calendario.
     * * @param idAsignacion Clave primaria del turno asignado que se desea modificar.
     * @param alerta Booleano que define si el turno activará o no un recordatorio visual/sonoro (`true` para encender, `false` para apagar).
     */
    @Query("UPDATE asignaciones_turno SET tieneAlerta = :alerta WHERE idAsignacion = :idAsignacion")
    suspend fun actualizarAlertaTurno(idAsignacion: Int, alerta: Boolean)

    /**
     * Permite la parametrización completa y detallada de un sistema de alarma o recordatorio asociado a un turno.
     * * @param idAsignacion Identificador único del turno al cual se le asocian los metadatos de alerta.
     * @param alerta Booleano que indica si el recordatorio está activo.
     * @param minutos Margen de anticipación temporal de la alerta con respecto al inicio del turno, medido en minutos.
     * @param tipo El tipo de alerta multimedia que se reproducirá (p. ej., "SONIDO", "VIBRACION", "SILENCIOSO").
     * @param mensaje Mensaje o texto personalizado que se mostrará en los canales de notificación del sistema operativo.
     */
    @Query(
        """
        UPDATE asignaciones_turno 
        SET tieneAlerta = :alerta, 
            minutosAntesAlerta = :minutos, 
            tipoAlertaConfigurada = :tipo, 
            mensajeAlerta = :mensaje 
        WHERE idAsignacion = :idAsignacion
    """
    )
    suspend fun configurarAlertaCompleta(
        idAsignacion: Int,
        alerta: Boolean,
        minutos: Int,
        tipo: String,
        mensaje: String,
    )

    /**
     * Busca en la base de datos y recupera la entidad cruda de asignación de turno mediante su ID de registro.
     * * @param idAsignacion Clave primaria del turno mapeado en el calendario.
     * @return El objeto puro [AsignacionTurno] si existe en los registros; `null` si no se localiza.
     */
    @Query("SELECT * FROM asignaciones_turno WHERE idAsignacion = :idAsignacion LIMIT 1")
    suspend fun obtenerAsignacionPorId(idAsignacion: Int): AsignacionTurno?

    // ==========================================
    // --- Operaciones de Notas Globales ---
    // ==========================================

    /**
     * Registra o actualiza una nota de carácter global asociada a una fecha concreta en la agenda del usuario.
     * * @param nota Instancia de la entidad [Nota] que contiene el texto de recordatorio diario del usuario.
     * * Aplica [OnConflictStrategy.REPLACE], de forma que si el usuario ya redactó una nota para dicho día,
     * se sobrescribirá el registro existente de forma limpia.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNota(nota: Nota): Long

    /**
     * Consulta si existe alguna anotación general para un usuario en un día determinado del calendario.
     * * @param idUsuario Identificador único del usuario dueño de las notas.
     * @param fechaMillis Timestamp representativo del día a consultar, expresado en milisegundos.
     * @return La coincidencia mapeada en un objeto [Nota], o `null` si la fecha no tiene anotaciones guardadas.
     */
    @Query("SELECT * FROM notas WHERE idUsuario = :idUsuario AND fecha = :fechaMillis LIMIT 1")
    suspend fun obtenerNotaPorFecha(idUsuario: String, fechaMillis: Long): Nota?

    /**
     * Elimina una nota específica de la base de datos.
     */
    @androidx.room.Delete
    suspend fun eliminarNota(nota: Nota)

    /**
     * Recupera todas las notas generales de un usuario dentro de un rango de fechas.
     */
    @Query("SELECT * FROM notas WHERE idUsuario = :idUsuario AND fecha >= :inicio AND fecha <= :fin")
    fun obtenerNotasPorRango(idUsuario: String, inicio: Long, fin: Long): Flow<List<Nota>>
}

/**
 * Gestor principal de la base de datos relacional orientada a objetos de la aplicación.
 * * Se encarga de exponer los canales de acceso (DAO) y de instanciar de forma centralizada la conexión
 * nativa hacia el motor SQLite embebido en Android a través de Room.
 */
@Database(
    entities = [Usuario::class, TipoTurno::class, AsignacionTurno::class, Nota::class],
    version = 5,
    exportSchema = false
)
abstract class BaseDeDatosApp : RoomDatabase() {

    /**
     * Proporciona acceso directo al DAO que manipula la persistencia del gestor de turnos.
     * * @return La implementación generada por Room de [TurnosDao].
     */
    abstract fun turnosDao(): TurnosDao

    companion object {
        // La anotación @Volatile asegura que cualquier cambio en esta variable sea visible inmediatamente
        // para todos los hilos de ejecución de la CPU, evitando lecturas obsoletas en caché de hilos.
        @Volatile
        private var INSTANCE: BaseDeDatosApp? = null

        /**
         * Obtiene la instancia única (Patrón Singleton) de la base de datos de la aplicación.
         * * Garantiza que no se abran de forma concurrente múltiples conexiones a la base de datos de SQLite,
         * lo cual consumiría recursos de memoria y causaría bloqueos de base de datos.
         * * @param context El [Context] de la aplicación (se extrae el ApplicationContext para evitar fugas de memoria).
         * @return La instancia única y centralizada de [BaseDeDatosApp].
         */
        fun obtenerBaseDeDatos(context: Context): BaseDeDatosApp {
            // Si la instancia ya existe (no es nula), se retorna directamente de forma ultrarápida.
            return INSTANCE ?: synchronized(this) {
                // Bloque sincronizado: previene que dos hilos paralelos ejecuten la inicialización a la vez.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BaseDeDatosApp::class.java,
                    "turnos_db"
                )
                    // Estrategia de migración destructiva (Útil en desarrollo).
                    // Limpia las tablas viejas y genera la nueva estructura si se incrementa el número de versión (version = 4).
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // Retorna la instancia recién construida
                instance
            }
        }
    }
}