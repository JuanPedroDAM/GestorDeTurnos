package com.iax.gestordeturnos.datos

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa a un usuario registrado en el sistema.
 * * Mapea directamente con la tabla "usuarios" dentro de la base de datos local SQLite.
 * Almacena las credenciales esenciales para la sesión local y el identificador de sincronización.
 *
 * @property idUsuario Clave primaria única en formato [String] que identifica de forma unívoca al usuario.
 * @property nombre Nombre completo, alias o apodo que se mostrará en las pantallas de la interfaz de usuario.
 * @property email Dirección de correo electrónico asociada al perfil de usuario para autenticación o recuperación.
 * @property contrasena Cadena de caracteres que almacena la contraseña de acceso.
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey val idUsuario: String, // Clave primaria manual (comúnmente un UUID de Firebase o ID de servidor)
    val nombre: String,
    val email: String,
    val contrasena: String
)

/**
 * Entidad de Room que define un modelo, categoría o plantilla de turno configurado de forma personalizada por el usuario.
 * * Mapea con la tabla "tipos_turno". Permite reutilizar esquemas horarios estables (ej.: "Mañana", "Noche", "Guardia").
 *
 * @property idTipoTurno Clave primaria numérica autogenerada automáticamente por el motor de Room/SQLite.
 * @property idUsuario Clave foránea conceptual que vincula este tipo de turno con el [Usuario] creador.
 * @property nombre Nombre descriptivo u oficial asignado a la franja del turno (ej.: "Turno Central", "Partido").
 * @property abreviatura Código corto de texto o sigla visual utilizado para representar el turno en cuadrículas estrechas (ej.: "M", "T", "N").
 * @property horaInicio Instante de entrada al turno guardado en formato de texto de 24 horas (ej: "08:00").
 * @property horaFin Instante de salida o finalización del turno guardado en formato de texto de 24 horas (ej: "15:00").
 * @property colorHex Código de color en formato Hexadecimal con prefijo hash (ej.: "#FF0000") utilizado para renderizar la tarjeta en la interfaz.
 */
@Entity(tableName = "tipos_turno")
data class TipoTurno(
    @PrimaryKey(autoGenerate = true) val idTipoTurno: Int = 0, // Autoincremento gestionado por SQLite
    val idUsuario: String,
    val nombre: String,
    val abreviatura: String,
    val horaInicio: String,
    val horaFin: String,
    val colorHex: String
)

/**
 * Entidad de Room que representa la instanciación u ocupación de un [TipoTurno] específico en una fecha concreta de la agenda.
 * * Mapea con la tabla "asignaciones_turno". Actúa como el núcleo de datos para el renderizado del calendario mensual.
 * Incluye metadatos multimedia extendidos para la parametrización de alarmas nativas.
 *
 * @property idAsignacion Clave primaria numérica y autoincremental única para cada día asignado.
 * @property idUsuario Vínculo con el [Usuario] propietario del evento en su calendario privado.
 * @property idTipoTurno Identificador de referencia que apunta al objeto [TipoTurno] que se está aplicando en este día.
 * @property fecha Estampa de tiempo cronológica calculada en milisegundos desde la época Unix (Epoch UTC Timestamp) que define el día del turno.
 * @property nota Comentario o anotación textual específica añadida por el usuario únicamente para este día asignado (ej.: "Cambio con Carlos").
 * @property tieneAlerta Interruptor booleano para determinar si este turno activará el subsistema multimedia de alarmas de Android.
 * @property minutosAntesAlerta Margen de antelación en minutos antes de la [TipoTurno.horaInicio] para detonar el disparo de la alerta de fondo.
 * @property tipoAlertaConfigurada Categoría de comportamiento multimedia que se instanciará para la notificación (ej.: "Sonido", "Vibración", "Silencioso").
 * @property mensajeAlerta Texto personalizado que el broadcast receiver inyectará dentro de la notificación multimedia push al despertar.
 */
@Entity(tableName = "asignaciones_turno")
data class AsignacionTurno(
    @PrimaryKey(autoGenerate = true) val idAsignacion: Int = 0, // Identificador interno correlativo
    val idUsuario: String,
    val idTipoTurno: Int,
    val fecha: Long, // Almacenado como Long para optimizar búsquedas numéricas indexadas de rangos
    val nota: String = "",
    val tieneAlerta: Boolean = false,
    val minutosAntesAlerta: Int = 15,
    val tipoAlertaConfigurada: String = "Sonido",
    val mensajeAlerta: String = ""
)

/**
 * Entidad de Room que representa un recordatorio o nota de texto global anclada a un día específico.
 * * Mapea con la tabla "notas". A diferencia de [AsignacionTurno], este objeto modela eventos externos a la jornada
 * laboral diaria (ej.: "Cumpleaños", "Cita Médica").
 *
 * @property idNota Clave primaria numérica autoincrementable.
 * @property idUsuario Identificador del [Usuario] que redactó el recordatorio global.
 * @property fecha Timestamp en milisegundos que indica el día del calendario donde se renderizará el indicador de la nota.
 * @property contenido Cuerpo del mensaje de texto o descripción detallada del recordatorio diario.
 * @property tipoAlerta Configuración o canal de notificación multimedia asignado para la alerta de esta nota global.
 */
@Entity(
    tableName = "notas",
    indices = [Index(value = ["idUsuario", "fecha"], unique = true)]
)
data class Nota(
    @PrimaryKey(autoGenerate = true) val idNota: Int = 0, // Clave única autogenerada
    val idUsuario: String,
    val fecha: Long,
    val contenido: String,
    val tipoAlerta: String
)

/**
 * Objeto de Transferencia de Datos (DTO) o Proyección de persistencia no anotada como entidad.
 * * Se utiliza exclusivamente en la capa de UI y Repositorios para recibir el resultado aplanado de consultas complejas
 * tipo `INNER JOIN` (realizadas en [TurnosDao.obtenerAsignacionesConDetalle]). Evita la sobrecarga de memoria que implicaría
 * realizar múltiples consultas anidadas o manejar objetos relacionales pesados.
 *
 * @property idAsignacion ID de la asignación del calendario correspondiente.
 * @property fecha Día en milisegundos del evento.
 * @property nota Nota personal e individual del día.
 * @property tieneAlerta Flag del estado de activación del servicio multimedia de alarmas.
 * @property idTipoTurno ID del tipo de turno original para posibles operaciones de edición profunda.
 * @property nombre Nombre legible de la plantilla del turno (ej.: "Mañana", "Noche").
 * @property abreviatura Código o sigla de impresión visual.
 * @property colorHex Color en formato String hexadecimal para pintado dinámico del canvas/gráficos en UI.
 * @property horaInicio Texto de la hora de entrada al puesto laboral.
 * @property horaFin Texto de la hora de salida del puesto laboral.
 */
data class TurnoAsignadoDetalle(
    val idAsignacion: Int,
    val fecha: Long,
    val nota: String,
    val tieneAlerta: Boolean,
    val idTipoTurno: Int,
    val nombre: String,
    val abreviatura: String,
    val colorHex: String,
    val horaInicio: String,
    val horaFin: String
)