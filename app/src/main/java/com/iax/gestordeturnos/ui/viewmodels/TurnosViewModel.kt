package com.iax.gestordeturnos.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.iax.gestordeturnos.datos.AsignacionTurno
import com.iax.gestordeturnos.datos.Nota
import com.iax.gestordeturnos.datos.TipoTurno
import com.iax.gestordeturnos.datos.TurnoAsignadoDetalle
import com.iax.gestordeturnos.datos.TurnosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Núcleo arquitectónico de control lógico de negocio y gestión de estados reactivos del calendario ([ViewModel]).
 * * Implementa el patrón de diseño MVVM encapsulando de forma hermética el acceso a la persistencia local
 * administrada por el [TurnosRepository]. Expone tuberías de estado inmutables optimizadas ([StateFlow])
 * para alimentar los layouts multimedia de Jetpack Compose, garantizando que el árbol visual conserve
 * de manera simétrica sus datos ante rotaciones de pantalla o interrupciones de llamadas telefónicas.
 *
 * Administra escuchas activas sobre los servicios de red de [FirebaseAuth] para conmutar síncronamente
 * el identificador de usuario activo y recalcular automáticamente el mapa indexado de cuadrantes.
 *
 * @property repository Instancia inyectada del mediador de persistencia [TurnosRepository] que actúa como fuente de la verdad.
 * * @author Lucas Merino Ortín
 * * @author Juan Pedro López García
 */
class TurnosViewModel(private val repository: TurnosRepository) : ViewModel() {

    // Tubería interna mutable que almacena la clave UID del usuario autenticado en Firebase
    private val _usuarioId =
        MutableStateFlow(FirebaseAuth.getInstance().currentUser?.uid ?: "desconectado")

    // Estados internos mutables encargados de regir las dimensiones cronológicas visibles del calendario
    private val _anioMesActual = MutableStateFlow<YearMonth>(YearMonth.now())

    /** Flujo de lectura público e inmutable representativo del mes y año que el usuario visualiza actualmente. */
    val anioMesActual: StateFlow<YearMonth> = _anioMesActual.asStateFlow()

    private val _diaSeleccionado = MutableStateFlow(LocalDate.now().dayOfMonth)

    /** Flujo de lectura público representativo del día numérico que se halla marcado en la interfaz táctil. */
    val diaSeleccionado: StateFlow<Int> = _diaSeleccionado.asStateFlow()

    private val _cargandoMasivo = MutableStateFlow(false)

    /** Estado binario que conmuta el bloqueo de la UI y enciende cargadores multimedia durante inserciones atómicas complejas. */
    val cargandoMasivo: StateFlow<Boolean> = _cargandoMasivo.asStateFlow()

    private val _turnoEnEdicion = MutableStateFlow<TipoTurno?>(null)

    /** Almacena el objeto [TipoTurno] transbordado hacia el formulario de modificación. */
    val turnoEnEdicion: StateFlow<TipoTurno?> = _turnoEnEdicion.asStateFlow()

    private val _notaDelDia = MutableStateFlow<Nota?>(null)

    /** Almacena la observación o recordatorio global mapeado para el día activo de la agenda. */
    val notaDelDia: StateFlow<Nota?> = _notaDelDia.asStateFlow()

    init {
        // Bloque constructor: Registra un listener asíncrono en el hardware de Firebase.
        // Reacciona de forma inmediata si la sesión expira o si el usuario efectúa un logout,
        // actualizando el UID para purgar en cascada los datos confidenciales expuestos en las pantallas.
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            _usuarioId.value = auth.currentUser?.uid ?: "desconectado"
        }
    }

    // ==========================================
    // --- Rutinas de Sincronización Temporal ---
    // ==========================================

    /** Actualiza de manera síncrona el entero del día seleccionado en la matriz mensual. */
    fun cambiarDiaSeleccionado(dia: Int) {
        _diaSeleccionado.value = dia
    }

    /** Configura un salto explícito masivo de anualidad y mensualidad. */
    fun setAnioMes(year: Int, month: Int) {
        _anioMesActual.value = YearMonth.of(year, month)
    }

    /** Desplaza el paginador de la cabecera un mes hacia adelante. */
    fun avanzarMes() {
        _anioMesActual.value = _anioMesActual.value.plusMonths(1)
    }

    /** Desplaza el paginador de la cabecera un mes hacia atrás. */
    fun retrocederMes() {
        _anioMesActual.value = _anioMesActual.value.minusMonths(1)
    }

    /** Resetea instantáneamente los punteros cronológicos devolviéndolos al día de hoy en tiempo real. */
    fun volverAHoy() {
        _anioMesActual.value = YearMonth.now()
        _diaSeleccionado.value = LocalDate.now().dayOfMonth
    }

    // ==========================================
    // --- Tuberías Reactivas Avanzadas (Flow) ---
    // ==========================================

    /**
     * Flujo reactivo caliente de lectura que expone la colección de plantillas horarias creadas por el usuario.
     * * Emplea [flatMapLatest] para interrumpir y purgar consultas previas si el UID del usuario cambia de golpe.
     * Mantiene los datos vivos en la RAM mediante [stateIn] preservando un búfer de seguridad elástico de 5 segundos.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val tiposTurno: StateFlow<List<TipoTurno>> = _usuarioId.flatMapLatest { uid ->
        repository.obtenerTiposTurno(uid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Módulo lógico de cálculo matricial que unifica, mapea y agrupa los turnos mensuales asignados.
     * * Combina de forma asíncrona las variaciones del mes seleccionado y la identidad del usuario.
     * Calcula las fronteras numéricas en milisegundos (Epoch) de inicio y fin de mes para optimizar la consulta SQL.
     * Transforma la lista plana devuelta por Room agrupándola en un [Map] indexado por el número de día del mes,
     * sirviendo la información lista para su pintado inmediato a FPS nativos en la cuadrícula de la UI.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val asignacionesMes: StateFlow<Map<Int, List<TurnoAsignadoDetalle>>> =
        combine(_anioMesActual, _usuarioId) { yearMonth, uid ->
            Pair(yearMonth, uid)
        }.flatMapLatest { (yearMonth, uid) ->
            val zonaUTC = ZoneId.of("UTC")
            val inicioLong =
                yearMonth.atDay(1).atStartOfDay(zonaUTC).toInstant().toEpochMilli()
            val finLong = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zonaUTC)
                .toInstant().toEpochMilli()
            repository.obtenerAsignacionesConDetalle(uid, inicioLong, finLong)
        }.map { lista ->
            val zonaUTC = ZoneId.of("UTC")
            lista.groupBy { detalle ->
                val fechaLocal =
                    Instant.ofEpochMilli(detalle.fecha).atZone(zonaUTC).toLocalDate()
                fechaLocal.dayOfMonth // Agrupa los elementos coleccionados usando la clave numérica de su día natural
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Tubería reactiva que recupera y agrupa las notas generales del mes visualizado.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val notasMes: StateFlow<Map<Int, Nota>> =
        combine(_anioMesActual, _usuarioId) { yearMonth, uid ->
            Pair(yearMonth, uid)
        }.flatMapLatest { (yearMonth, uid) ->
            val zonaUTC = ZoneId.of("UTC")
            val inicioLong =
                yearMonth.atDay(1).atStartOfDay(zonaUTC).toInstant().toEpochMilli()
            val finLong = yearMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zonaUTC)
                .toInstant().toEpochMilli()
            // Necesitamos un método en el repositorio para obtener todas las notas de un rango
            repository.obtenerNotasPorRango(uid, inicioLong, finLong)
        }.map { lista ->
            val zonaUTC = ZoneId.of("UTC")
            lista.associateBy { nota ->
                val fechaLocal =
                    Instant.ofEpochMilli(nota.fecha).atZone(zonaUTC).toLocalDate()
                fechaLocal.dayOfMonth
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Filtro reactivo secuencial encargado de recopilar los 5 eventos laborales inmediatamente venideros.
     * * Extrae la marca temporal del día presente a las 00:00 y proyecta un límite de búsqueda anual.
     * Ordena cronológicamente la colección y extrae un límite estricto de hasta 5 ítems para la zona de bienvenida.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val proximosTurnos: StateFlow<List<TurnoAsignadoDetalle>> =
        combine(_anioMesActual, _usuarioId) { yearMonth, uid ->
            Pair(yearMonth, uid)
        }.flatMapLatest { (_, uid) ->
            val hoyMillis =
                LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val finLong = Instant.now().plusMillis(31536000000L)
                .toEpochMilli() // Añade un margen temporal de un año en milisegundos
            repository.obtenerAsignacionesConDetalle(uid, hoyMillis, finLong)
        }.map { lista -> lista.sortedBy { it.fecha }.take(5) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // --- Operaciones de Escritura Asíncrona ---
    // ==========================================

    /** Almacena una nueva plantilla de turno de forma paralela en el pool de hilos de entrada/salida ([Dispatchers.IO]). */
    fun guardarNuevoTurno(
        nombre: String,
        abreviatura: String,
        horaInicio: String,
        horaFin: String,
        colorHex: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val nuevoTurno = TipoTurno(
                idUsuario = _usuarioId.value,
                nombre = nombre,
                abreviatura = abreviatura,
                horaInicio = horaInicio,
                horaFin = horaFin,
                colorHex = colorHex
            )
            repository.insertarTipoTurno(nuevoTurno)
        }
    }

    /** Prepara y retiene en la RAM la referencia del tipo de turno a modificar. */
    fun seleccionarTurnoParaEditar(turno: TipoTurno) {
        _turnoEnEdicion.value = turno
    }

    /** Consolida los cambios de edición sobre una plantilla horaria en SQLite de forma asíncrona. */
    fun guardarEdicionTurno(
        idTipoTurno: Int,
        nombre: String,
        abreviatura: String,
        horaInicio: String,
        horaFin: String,
        colorHex: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val turnoEditado = TipoTurno(
                idTipoTurno = idTipoTurno,
                idUsuario = _usuarioId.value,
                nombre = nombre,
                abreviatura = abreviatura,
                horaInicio = horaInicio,
                horaFin = horaFin,
                colorHex = colorHex
            )
            repository.actualizarTipoTurno(turnoEditado)
            _turnoEnEdicion.value =
                null // Libera la referencia de edición una vez completado el guardado
        }
    }

    /** Remueve de manera permanente un modelo de turno de los registros físicos locales. */
    fun eliminarTipoTurno(turno: TipoTurno) {
        viewModelScope.launch(Dispatchers.IO) { repository.eliminarTipoTurno(turno) }
    }

    /** Asigna una ocurrencia de turno a un día específico calculando su marca temporal síncronamente. */
    fun asignarTurnoAlDia(idTipoTurno: Int, dia: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val fecha = _anioMesActual.value.atDay(dia)
            val fechaMillis = fecha.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            val nuevaAsignacion = AsignacionTurno(
                idUsuario = _usuarioId.value,
                idTipoTurno = idTipoTurno,
                fecha = fechaMillis
            )
            repository.guardarAsignacionesMasivas(listOf(nuevaAsignacion))
        }
    }

    /** Borra de la agenda un turno asignado utilizando su ID único correlativo de registro. */
    fun eliminarAsignacion(idAsignacion: Int) {
        viewModelScope.launch(Dispatchers.IO) { repository.eliminarAsignacion(idAsignacion) }
    }

    /** Sobrescribe los comentarios o anotaciones de un día agendado de forma paralela. */
    fun actualizarNotaTurno(idAsignacion: Int, nota: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.actualizarNotaTurno(idAsignacion, nota)
        }
    }

    /** Modifica de forma rápida el estado booleano de activación de la alarma nativa. */
    fun actualizarAlertaTurno(idAsignacion: Int, alerta: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.actualizarAlertaTurno(idAsignacion, alerta)
        }
    }

    /** Configura exhaustivamente las propiedades y el cuerpo multimedia de una alarma pre-turno. */
    fun configurarAlertaCompleta(
        idAsignacion: Int,
        alerta: Boolean,
        minutos: Int,
        tipo: String,
        mensaje: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.configurarAlertaCompleta(idAsignacion, alerta, minutos, tipo, mensaje)
        }
    }

    /**
     * Algoritmo avanzado de automatización masiva para la inyección periódica secuencial de cuadrantes.
     * * Despliega un bucle lineal iterativo desde la fecha de inicio hasta la de fin, calculando de forma matemática
     * el residuo del índice numérico (`% secuencia.size`) para expandir de forma cíclica e infinita el patrón táctil
     * creado por el usuario (ej. Mañana, Tarde, Noche, Libre). Despacha el lote en un entorno de transacción única de Room.
     */
    fun generarPatronTurnos(
        fechaInicio: LocalDate,
        fechaFin: LocalDate,
        secuencia: List<TipoTurno?>
    ) {
        if (secuencia.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            _cargandoMasivo.value =
                true // Activa de forma reactiva el indicador circular de carga en la UI
            val listaNuevosTurnos = mutableListOf<AsignacionTurno>()
            var fechaBucle = fechaInicio
            var diaIndice = 0

            // Zona horaria UTC para asegurar que el timestamp represente exactamente el día seleccionado
            val zonaUTC = ZoneId.of("UTC")

            while (!fechaBucle.isAfter(fechaFin)) {
                val turnoActual = secuencia[diaIndice % secuencia.size]
                if (turnoActual != null) {
                    val fechaMillis = fechaBucle.atStartOfDay(zonaUTC).toInstant().toEpochMilli()
                    listaNuevosTurnos.add(
                        AsignacionTurno(
                            idUsuario = _usuarioId.value,
                            idTipoTurno = turnoActual.idTipoTurno,
                            fecha = fechaMillis
                        )
                    )
                }
                fechaBucle =
                    fechaBucle.plusDays(1) // Avanza linealmente la fecha de control del bucle
                diaIndice++
            }
            repository.guardarAsignacionesMasivas(listaNuevosTurnos) // Inyección del lote masivo bajo transacción atómica
            _cargandoMasivo.value = false // Libera el bloqueo de la interfaz de usuario
        }
    }

    /** Extrae de forma asíncrona observaciones globales ancladas a un día partiendo de su marca de tiempo. */
    fun cargarDetallesDia(dia: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val fecha = _anioMesActual.value.atDay(dia)
            val fechaMillis = fecha.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            val nota = repository.obtenerNotaPorFecha(_usuarioId.value, fechaMillis)
            _notaDelDia.value = nota
        }
    }

    /** Registra o actualiza un recordatorio general diario resolviendo colisiones de ID mediante Room. */
    fun guardarNota(dia: Int, contenido: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fecha = _anioMesActual.value.atDay(dia)
            val zonaUTC = ZoneId.of("UTC")
            val fechaMillis = fecha.atStartOfDay(zonaUTC).toInstant().toEpochMilli()
            
            // Buscamos la nota actual en la base de datos para asegurar consistencia
            val notaExistente = repository.obtenerNotaPorFecha(_usuarioId.value, fechaMillis)

            if (contenido.isBlank()) {
                // Si el contenido está vacío, eliminamos la nota si existe
                notaExistente?.let { repository.eliminarNota(it) }
                _notaDelDia.value = null
            } else {
                val nuevaNota = Nota(
                    idNota = notaExistente?.idNota ?: 0,
                    idUsuario = _usuarioId.value,
                    fecha = fechaMillis,
                    contenido = contenido,
                    tipoAlerta = "Ninguna"
                )
                val idGenerado = repository.guardarNota(nuevaNota)
                // Actualizamos el estado con el ID correcto (el existente o el nuevo autogenerado)
                _notaDelDia.value = nuevaNota.copy(idNota = idGenerado.toInt())
            }
        }
    }

    /**
     * Recupera y formatea mediante un hilo asíncrono suspendido la cadena ordenada de texto descriptiva del cuadrante.
     * * Es consumido por el subsistema multimedia de reportes de la app previa llamada de Intents compartidos.
     * @return Una cadena de texto estructurada legible tipo [String], o `null` si el intervalo carece de registros.
     */
    suspend fun generarTextoExportacion(inicioMillis: Long, finMillis: Long): String? {
        val uid = _usuarioId.value

        // El método .first() extrae el primer lote atómico emitido por la tubería de datos y detiene la escucha activa
        val turnos = repository.obtenerAsignacionesConDetalle(uid, inicioMillis, finMillis).first()

        if (turnos.isEmpty()) return null

        val sb = StringBuilder()
        sb.append("📅 MI CUADRANTE DE TURNOS 📅\n\n")

        val sdfPrint = java.text.SimpleDateFormat("EEEE, dd/MM/yyyy", java.util.Locale("es", "ES"))

        // Ordenación cronológica en memoria previa al formateo de cadenas de texto
        turnos.sortedBy { it.fecha }.forEach { turno ->
            val fechaFormateada =
                sdfPrint.format(java.util.Date(turno.fecha)).replaceFirstChar { it.uppercase() }
            sb.append("🔸 $fechaFormateada\n")
            sb.append("   ${turno.nombre} (${turno.horaInicio} - ${turno.horaFin})\n\n")
        }
        return sb.toString()
    }
}

/**
 * Fábrica de instanciación personalizada ([ViewModelProvider.Factory]) requerida por la arquitectura de Android Jetpack.
 * * Inyecta de forma limpia y parametrizada la dependencia exclusiva del [TurnosRepository] dentro del
 * constructor primario del [TurnosViewModel], aislando su ciclo de vida de la destrucción de Activities.
 */
class TurnosViewModelFactory(private val repository: TurnosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TurnosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TurnosViewModel(repository) as T
        }
        throw IllegalArgumentException("Clase ViewModel desconocida")
    }
}