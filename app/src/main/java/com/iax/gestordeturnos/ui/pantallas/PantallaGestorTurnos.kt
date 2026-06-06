package com.iax.gestordeturnos.ui.pantallas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.iax.gestordeturnos.datos.TipoTurno
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.theme.NaranjaAccion
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * Pantalla principal de administración, control y configuración de las plantillas de turnos del usuario.
 * * Lista de forma fluida las reglas horarias almacenadas en SQLite mediante Room, permitiendo eliminarlas
 * de forma preventiva o redirigir los flujos interactivos hacia la edición individual de las mismas.
 * * Integra una consola avanzada de automatización masiva en primer plano ([DialogoGeneradorPatrones])
 * para inyectar cuadrantes y secuencias cíclicas calculadas en milisegundos sin bloquear el hilo UI de la app.
 *
 * @param viewModel Instancia del orquestador de datos [TurnosViewModel] que expone flujos de estado de Room.
 * @param onAtrasClick Callback lambda invocado al pulsar el botón físico o digital de retroceso de pantalla.
 * @param onEditarClick Callback lambda que transmite la entidad [TipoTurno] seleccionada hacia el formulario de modificación.
 * @param onCrearTurnoClick Callback lambda que reencauza al usuario hacia el formulario de alta de turnos vacíos.
 * * @author Lucas Merino Ortín
 * * @author Juan Pedro López García
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaGestorTurnos(
    viewModel: TurnosViewModel,
    onAtrasClick: () -> Unit,
    onEditarClick: (TipoTurno) -> Unit,
    onCrearTurnoClick: () -> Unit
) {
    // Colecta reactiva en tiempo real de la lista de tipos de turnos guardados en la persistencia local
    val tiposTurno by viewModel.tiposTurno.collectAsState()

    // Observa el flag multimedia de bloqueo para saber si la base de datos está inyectando un lote de patrones
    val cargandoMasivo by viewModel.cargandoMasivo.collectAsState()

    // Almacena de forma temporal la referencia del objeto de turno nominado para borrado físico en SQLite
    var turnoAEliminar by remember { mutableStateOf<TipoTurno?>(null) }

    // Conmuta la visibilidad de la consola modal de generación automática de patrones horizontales
    var mostrarGenerador by remember { mutableStateOf(false) }

    // Mecanismo de blindaje contra clics rápidos dobles accidentales que alteren el backstack de navegación
    var navegando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tipos de Turno",
                        color = Color(0xFF1E293B),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!navegando) {
                            navegando =
                                true // Congela de inmediato segundas intenciones de navegación
                            onAtrasClick()
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás") }
                },
                actions = {
                    IconButton(onClick = {
                        if (!navegando) {
                            navegando = true
                            onCrearTurnoClick() // Transbordo seguro al formulario de alta
                        }
                    }) { Icon(Icons.Filled.Add, contentDescription = "Crear", tint = AzulCobalto) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8FAFC))
            )
        },
        floatingActionButton = {
            // Botón flotante extendido con estilo Material Design 3 e iconografía multimedia automatizada
            ExtendedFloatingActionButton(
                onClick = { mostrarGenerador = true },
                containerColor = NaranjaAccion,
                contentColor = Color.White
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generar Patrón")
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Gestión de Horarios",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCobalto
            )
            Text("Configura y organiza tus turnos de trabajo", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))

            // LazyColumn optimiza el uso de memoria RAM reciclando las celdas no visibles de la lista
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(tiposTurno) { turno ->
                    ItemTipoTurno(
                        turno = turno,
                        onEliminarClick = { turnoAEliminar = turno },
                        onEditarClick = { onEditarClick(turno) }
                    )
                }
            }
        }

        // Despliegue superpuesto del asistente inteligente de generación de cuadrantes a gran escala
        if (mostrarGenerador) {
            DialogoGeneradorPatrones(
                viewModel = viewModel,
                tiposTurno = tiposTurno,
                cargando = cargandoMasivo,
                onCerrar = { mostrarGenerador = false },
                onGuardadoExitoso = {
                    mostrarGenerador = false
                    onAtrasClick() // Retorna al calendario raíz refrescado tras la persistencia transaccional
                }
            )
        }

        // Diálogo emergente de advertencia para prevenir la destrucción accidental de registros en Room
        turnoAEliminar?.let { turno ->
            AlertDialog(
                onDismissRequest = { turnoAEliminar = null },
                title = { Text("Eliminar turno", fontWeight = FontWeight.Bold) },
                text = { Text("¿Estás seguro de que quieres eliminar la plantilla '${turno.nombre}'? Esta acción no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = { viewModel.eliminarTipoTurno(turno); turnoAEliminar = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { turnoAEliminar = null }) {
                        Text(
                            "Cancelar",
                            color = Color.Gray
                        )
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

/**
 * Componente de diseño atómico que modela la tarjeta contenedora de cada tipo de turno.
 */
@Composable
fun ItemTipoTurno(turno: TipoTurno, onEliminarClick: () -> Unit, onEditarClick: () -> Unit) {
    val colorLinea = try {
        Color(turno.colorHex.toColorInt())
    } catch (_: Exception) {
        AzulCobalto
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // IntrinsicSize. Min ecualiza verticalmente la barra lateral cromática con la longitud del texto descriptivo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier
                .width(8.dp)
                .fillMaxHeight()
                .background(colorLinea))
            Column(modifier = Modifier
                .weight(1f)
                .padding(16.dp)) {
                Text(
                    turno.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("${turno.horaInicio} - ${turno.horaFin}", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onEditarClick) {
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = "Editar",
                    tint = Color.Gray
                )
            }
            IconButton(onClick = onEliminarClick) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFE53935)
                )
            }
        }
    }
}

/**
 * Consola modal avanzada que implementa un generador secuencial periódico de cuadrantes laborales.
 * * Permite encadenar dinámicamente eslabones cronológicos y expandirlos cíclicamente a lo largo
 * de un intervalo determinado mediante interpolación de milisegundos e inserción masiva en SQLite.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DialogoGeneradorPatrones(
    viewModel: TurnosViewModel,
    tiposTurno: List<TipoTurno>,
    cargando: Boolean,
    onCerrar: () -> Unit,
    onGuardadoExitoso: () -> Unit
) {
    var fechaInicioMillis by remember { mutableStateOf<Long?>(null) }
    var fechaFinMillis by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    // SnapshotStateList: Colección elástica reactiva de Compose diseñada para escuchar inserciones,
    // reordenaciones y borrados de eslabones táctiles en tiempo de renderizado continuo.
    val secuencia = remember { mutableStateListOf<TipoTurno?>() }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Pasarela de instanciación síncrona hacia el DatePicker tradicional del sistema operativo
    val abrirDatePicker = { esInicio: Boolean ->
        val calendar = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        val currentMillis = if (esInicio) fechaInicioMillis else fechaFinMillis
        if (currentMillis != null) calendar.timeInMillis = currentMillis

        val dialog = android.app.DatePickerDialog(
            context,
            { _, year, month, day ->
                val selected = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                selected.set(year, month, day, 0, 0, 0)
                selected.set(java.util.Calendar.MILLISECOND, 0)
                if (esInicio) fechaInicioMillis = selected.timeInMillis else fechaFinMillis =
                    selected.timeInMillis
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.firstDayOfWeek = java.util.Calendar.MONDAY
        dialog.show()
    }

    Dialog(
        onDismissRequest = { if (!cargando) onCerrar() },
        properties = DialogProperties(usePlatformDefaultWidth = false) // Dialog expandido a pantalla completa para el manejo adaptativo de rejillas
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Generador de Patrones",
                            color = AzulCobalto,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onCerrar) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (cargando) {
                    // Bloquea visualmente la consola mediante un cargador circular multimedia mientras corre la corrutina I/O
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator(color = NaranjaAccion) }
                } else {
                    Text(
                        "Período de Generación",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { abrirDatePicker(true) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (fechaInicioMillis != null) dateFormatter.format(
                                    Date(
                                        fechaInicioMillis!!
                                    )
                                ) else "Fecha Inicio", color = AzulCobalto
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        OutlinedButton(
                            onClick = { abrirDatePicker(false) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (fechaFinMillis != null) dateFormatter.format(
                                    Date(
                                        fechaFinMillis!!
                                    )
                                ) else "Fecha Fin", color = AzulCobalto
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Construir Patrón",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // FlowRow: Distribuidor inteligente de Material 3 que organiza de forma adaptativa los Chips en rejilla,
                    // saltando de línea automáticamente si detecta déficit de píxeles horizontales por hardware.
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tiposTurno.forEach { turno ->
                            val colorTurno = try {
                                Color(turno.colorHex.toColorInt())
                            } catch (_: Exception) {
                                AzulCobalto
                            }
                            ElevatedFilterChip(
                                selected = false,
                                onClick = { secuencia.add(turno) }, // Inserta de forma reactiva el eslabón cromático al pool
                                label = { Text(turno.nombre) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(colorTurno)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Añadir",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        // Chip selector multimedia dedicado para modelar descansos u ocurrencias de días libres (Nulos lógicos)
                        ElevatedFilterChip(
                            selected = false,
                            onClick = { secuencia.add(null) },
                            label = { Text("Día Libre") },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = "Añadir",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "SECUENCIA ACTUAL",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp),
                        fontSize = 12.sp
                    )

                    if (secuencia.isEmpty()) {
                        Text(
                            "Toca los botones de arriba para construir tu patrón.",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    } else {
                        // Renderizado dinámico secuencial de la lista elástica mutada por el usuario
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // CORRECCIÓN DE LA CAPTURA image_e46470.png:
                            // Forzamos la firma contractual estricta de Kotlin para la desestructuración indexada: (index, element)
                            secuencia.forEachIndexed { index, turno ->
                                val color = if (turno == null) Color.Gray else try {
                                    Color(turno.colorHex.toColorInt())
                                } catch (_: Exception) {
                                    AzulCobalto
                                }
                                val nombre = turno?.nombre ?: "Día Libre"
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Día ${index + 1}",
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(60.dp)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        nombre,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    // Remueve quirúrgicamente de la colección el elemento basándose estrictamente en su entero indexador primitivo
                                    IconButton(
                                        onClick = { secuencia.removeAt(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Quitar",
                                            tint = Color(0xFFE53935)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Cláusula perimetral de seguridad binaria: Exige fechas definidas y al menos un eslabón secuencial asignado
                    val puedeGuardar =
                        fechaInicioMillis != null && fechaFinMillis != null && secuencia.isNotEmpty()
                    Button(
                        enabled = puedeGuardar,
                        onClick = {
                            if (fechaInicioMillis == null || fechaFinMillis == null) {
                                Toast.makeText(context, "❌ Selecciona fechas.", Toast.LENGTH_SHORT)
                                    .show()
                                return@Button
                            }
                            if (fechaFinMillis!! < fechaInicioMillis!!) {
                                Toast.makeText(
                                    context,
                                    "❌ Fecha fin incorrecta.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            // Conversión a objetos modernos inmutables LocalDate mapeados bajo la zona temporal UTC estándar
                            val fechaInicioLD =
                                Instant.ofEpochMilli(fechaInicioMillis!!).atZone(ZoneId.of("UTC"))
                                    .toLocalDate()
                            val fechaFinLD =
                                Instant.ofEpochMilli(fechaFinMillis!!).atZone(ZoneId.of("UTC"))
                                    .toLocalDate()

                            // Despacha el lote estructurado hacia la Lógica de Negocio/ViewModel para procesar la transacción SQL
                            viewModel.generarPatronTurnos(fechaInicioLD, fechaFinLD, secuencia)
                            Toast.makeText(
                                context,
                                "✅ Patrón generado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.volverAHoy()
                            onGuardadoExitoso()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Generar Patrón", fontWeight = FontWeight.Bold, color = Color.White) }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}