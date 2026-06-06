package com.iax.gestordeturnos.ui.pantallas

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Pantalla interactiva que orquesta el sistema multimedia de exportación y compartición de cuadrantes.
 * * Permite al usuario seleccionar el formato final de salida (ej. texto formateado para mensajería instantánea)
 * y acotar de forma precisa el rango temporal mediante filtros preconfigurados (Mensual, Anual) o un
 * rango libre parametrizado mediante selectores gráficos de fecha nativos ([DatePickerDialog]).
 *
 * Sincroniza las consultas por intervalos de tiempo hacia el repositorio local en milisegundos (Epoch)
 * y despierta el menú de compartición (*Share sheet*) del sistema operativo Android.
 *
 * @param viewModel Instancia del orquestador arquitectónico [TurnosViewModel] encargado de procesar la generación de cadenas.
 * @param onVolverClick Callback lambda que se ejecuta al pulsar el botón de retroceso en la barra superior.
 * * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaExportar(
    viewModel: TurnosViewModel,
    onVolverClick: () -> Unit
) {
    // Extrae el contexto nativo de Android seguro para lanzar la actividad compartida y los diálogos del sistema
    val context = LocalContext.current

    // Ámbito de corrutina acoplado al ciclo de vida visual para el lanzamiento de tareas asíncronas de base de datos
    val coroutineScope = rememberCoroutineScope()

    // --- Estados locales reactivos de control de formato y rangos ---
    var formatoSeleccionado by remember { mutableStateOf("TEXTO") }
    var periodoSeleccionado by remember { mutableStateOf("Mes") }

    // Inicializa la instancia cronológica en caché para calcular la estampa temporal por defecto de la sesión
    val hoy = Calendar.getInstance()
    val anioActualStr = hoy.get(Calendar.YEAR).toString()
    val mesesNombres = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val mesActualStr = mesesNombres[hoy.get(Calendar.MONTH)]
    val fechaHoyFormat = String.format(
        "%02d/%02d/%04d",
        hoy.get(Calendar.DAY_OF_MONTH),
        hoy.get(Calendar.MONTH) + 1,
        hoy.get(Calendar.YEAR)
    )

    // Estados mutables para el control de los listados desplegables (Dropdowns)
    var anioSeleccionado by remember { mutableStateOf(anioActualStr) }
    var mesSeleccionado by remember { mutableStateOf(mesActualStr) }
    var anioExpandio by remember { mutableStateOf(false) }
    var mesExpandio by remember { mutableStateOf(false) }

    // Almacena de forma visible las cadenas formateadas para el rango libre personalizado
    var fechaInicio by remember { mutableStateOf(fechaHoyFormat) }
    var fechaFin by remember { mutableStateOf(fechaHoyFormat) }

    // Control de carga (Loading helper) para bloquear clics y renderizar animaciones mientras corre la corrutina I/O
    var exportando by remember { mutableStateOf(false) }

    // Expresión lambda encapsulada para instanciar el widget multimedia nativo de selección de fecha
    val abrirDatePicker = { esInicio: Boolean ->
        val calendar = Calendar.getInstance()
        val dialog = DatePickerDialog(
            context,
            { _, year, month, day ->
                // Sincroniza la selección gráfica con el formato estándar requerido por el parseador lógico
                val fechaFormat = String.format("%02d/%02d/%04d", day, month + 1, year)
                if (esInicio) fechaInicio = fechaFormat else fechaFin = fechaFormat
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // Fuerza el inicio visual del calendario en el Lunes según el estándar europeo corporativo
        dialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        dialog.show()
    }

    // Andamio estructural de Material Design 3
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Exportar Calendario",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(
                        0xFFF8FAFC
                    )
                )
            )
        },
        bottomBar = {
            // Contenedor inferior fijo para albergar de forma estable el botón de acción principal de compartición
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = {
                        exportando = true // Lanza la animación visual del indicador circular
                        coroutineScope.launch {
                            try {
                                val sdfParse = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                var dInicio: Date? = null
                                lateinit var dFin: Date

                                // Algoritmo cronológico de normalización y empaquetado por bloques temporales
                                when (periodoSeleccionado) {
                                    "Personalizado" -> {
                                        if (fechaInicio.isNotEmpty() && fechaFin.isNotEmpty()) {
                                            dInicio = sdfParse.parse(fechaInicio)
                                            dFin = sdfParse.parse(fechaFin)
                                        }
                                    }

                                    "Mes" -> {
                                        val mesIndex = mesesNombres.indexOf(mesSeleccionado)
                                        val cal = Calendar.getInstance()
                                        cal.set(Calendar.YEAR, anioSeleccionado.toInt())
                                        cal.set(Calendar.MONTH, mesIndex)
                                        cal.set(Calendar.DAY_OF_MONTH, 1)
                                        // Aseguramos estrictamente que el inicio comience al microsegundo cero del día
                                        cal.set(Calendar.HOUR_OF_DAY, 0)
                                        cal.set(Calendar.MINUTE, 0)
                                        cal.set(Calendar.SECOND, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        dInicio = cal.time

                                        // Calcula dinámicamente la última jornada del mes (ej. día 28, 30 o 31 de forma adaptativa)
                                        cal.set(
                                            Calendar.DAY_OF_MONTH,
                                            cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                                        )
                                        dFin = cal.time
                                    }

                                    "Año" -> {
                                        val cal = Calendar.getInstance()
                                        cal.set(Calendar.YEAR, anioSeleccionado.toInt())
                                        cal.set(Calendar.DAY_OF_YEAR, 1)
                                        cal.set(Calendar.HOUR_OF_DAY, 0)
                                        cal.set(Calendar.MINUTE, 0)
                                        cal.set(Calendar.SECOND, 0)
                                        cal.set(Calendar.MILLISECOND, 0)
                                        dInicio = cal.time

                                        cal.set(Calendar.MONTH, Calendar.DECEMBER)
                                        cal.set(Calendar.DAY_OF_MONTH, 31)
                                        dFin = cal.time
                                    }
                                }

                                // --- Validaciones de Integridad de Datos e Intervención en el Hilo UI ---
                                if (dInicio != null) {
                                    if (dFin.before(dInicio)) {
                                        Toast.makeText(
                                            context,
                                            "❌ La fecha de fin no puede ser anterior al inicio.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        exportando = false
                                        return@launch // Interrumpe de forma limpia el hilo sin provocar crashes
                                    }

                                    // Normalización matemática a milisegundos (Epoch Epoch-Unix Timestamp UTC)
                                    val calInicio = Calendar.getInstance().apply {
                                        time = dInicio!!
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    val inicioMillis = calInicio.timeInMillis

                                    val calFin = Calendar.getInstance().apply {
                                        time = dFin
                                        set(Calendar.HOUR_OF_DAY, 23)
                                        set(Calendar.MINUTE, 59)
                                        set(Calendar.SECOND, 59)
                                        set(Calendar.MILLISECOND, 999)
                                    }
                                    val finMillis = calFin.timeInMillis

                                    // Invoca la rutina de fondo asíncrona del ViewModel encargada de procesar el String
                                    val textoExportacion =
                                        viewModel.generarTextoExportacion(inicioMillis, finMillis)

                                    if (textoExportacion == null) {
                                        Toast.makeText(
                                            context,
                                            "⚠️ No hay turnos asignados en este período.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        // Inicialización y configuración multimedia de un Intent implícito bajo la acción ACTION_SEND.
                                        // Despierta de forma nativa la pasarela compartida de Android permitiendo inyectar el texto
                                        // directamente en aplicaciones como WhatsApp, Telegram, Notas o Clientes de Correo.
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, textoExportacion)
                                            type =
                                                "text/plain" // MimeType plano universal compatible
                                        }
                                        val shareIntent =
                                            Intent.createChooser(sendIntent, "Compartir Turnos")
                                        context.startActivity(shareIntent)

                                        onVolverClick() // Retorna de forma limpia al calendario raíz
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "❌ Faltan fechas por seleccionar.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(
                                    context,
                                    "❌ Error al generar la exportación.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            exportando = false // Restaura el estado original del botón
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !exportando // Previene ataques de clics repetidos en ráfaga (Debounce nativo)
                ) {
                    if (exportando) {
                        // Renderiza el componente circular multimedia animado que informa del procesamiento asíncrono
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            "Compartir Cuadrante",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        // Contenedor principal con modificador de scroll para garantizar adaptabilidad
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Formato de Exportación",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta que agrupa las opciones estéticas y de formato multimedia
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column {
                    // Opción: Cadena de texto plana compatible con mensajería instantánea
                    OpcionFormatoCobalto(
                        icono = {
                            Icon(
                                Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                        },
                        fondoIcono = Color(0xFFECFDF5),
                        titulo = "Mensaje de Texto",
                        subtitulo = "Ideal para enviar por WhatsApp o Email",
                        seleccionado = formatoSeleccionado == "TEXTO",
                        onClick = { formatoSeleccionado = "TEXTO" }
                    )

                    HorizontalDivider(
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Opción: Renderizado estructurado en PDF (Bloqueado con Toast informativo)
                    OpcionFormatoCobalto(
                        icono = {
                            Icon(
                                Icons.Filled.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },
                        fondoIcono = Color(0xFFF1F5F9),
                        titulo = "Documento PDF",
                        subtitulo = "Próximamente en futuras versiones",
                        seleccionado = formatoSeleccionado == "PDF",
                        onClick = {
                            Toast.makeText(
                                context,
                                "Función en desarrollo para v2.0",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Período de Tiempo",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Selector horizontal de pestañas tipo Segmented Button maquetado de forma flexible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Año", "Mes", "Personalizado").forEach { opcion ->
                    val isSelected = periodoSeleccionado == opcion
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFE2E8F0) else Color.Transparent)
                            .clickable { periodoSeleccionado = opcion },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opcion,
                            color = if (isSelected) Color.Black else Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Panel contenedor adaptativo que muta su estructura de campos según la pestaña activa
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Renderizado Condicional del Dropdown de Selección Anual
                    if (periodoSeleccionado == "Año" || periodoSeleccionado == "Mes") {
                        Text("Año", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = anioExpandio,
                            onExpandedChange = { anioExpandio = !anioExpandio }
                        ) {
                            OutlinedTextField(
                                value = anioSeleccionado, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = anioExpandio) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFFE2E8F0),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = anioExpandio,
                                onDismissRequest = { anioExpandio = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                // Genera dinámicamente un rango de 7 años alrededor del actual para evitar llamadas rígidas duras
                                val listaAniosDinamica =
                                    ((hoy.get(Calendar.YEAR) - 2)..(hoy.get(Calendar.YEAR) + 5)).map { it.toString() }
                                listaAniosDinamica.forEach { anio ->
                                    DropdownMenuItem(
                                        text = { Text(anio, color = Color.Black) },
                                        onClick = { anioSeleccionado = anio; anioExpandio = false }
                                    )
                                }
                            }
                        }
                    }

                    // Renderizado Condicional del Dropdown de Selección Mensual
                    if (periodoSeleccionado == "Mes") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Mes", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = mesExpandio,
                            onExpandedChange = { mesExpandio = !mesExpandio }
                        ) {
                            OutlinedTextField(
                                value = mesSeleccionado, onValueChange = {}, readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mesExpandio) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFFE2E8F0),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = mesExpandio,
                                onDismissRequest = { mesExpandio = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                mesesNombres.forEach { mes ->
                                    DropdownMenuItem(
                                        text = { Text(mes, color = Color.Black) },
                                        onClick = { mesSeleccionado = mes; mesExpandio = false }
                                    )
                                }
                            }
                        }
                    }

                    // Renderizado Condicional de las cajas táctiles de entrada libre (DatePicker Pasarela)
                    if (periodoSeleccionado == "Personalizado") {
                        Text("Fecha de Inicio", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { abrirDatePicker(true) }
                        ) {
                            OutlinedTextField(
                                value = fechaInicio,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false, // Desactiva el teclado nativo forzando el clic del contenedor superior
                                placeholder = { Text("Seleccionar fecha", color = Color.Gray) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE2E8F0),
                                    disabledTextColor = Color.Black,
                                    disabledContainerColor = Color(0xFFF8FAFC),
                                    disabledTrailingIconColor = Color.Gray
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Fecha de Fin", color = Color.Gray, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { abrirDatePicker(false) }
                        ) {
                            OutlinedTextField(
                                value = fechaFin,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                placeholder = { Text("Seleccionar fecha", color = Color.Gray) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = Color(0xFFE2E8F0),
                                    disabledTextColor = Color.Black,
                                    disabledContainerColor = Color(0xFFF8FAFC),
                                    disabledTrailingIconColor = Color.Gray
                                )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * Fila interactiva personalizada con estilo corporativo que renderiza las opciones de formato
 * inyectando un componente de selección circular de tipo [RadioButton].
 */
@Composable
fun OpcionFormatoCobalto(
    icono: @Composable () -> Unit,
    fondoIcono: Color,
    titulo: String,
    subtitulo: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(fondoIcono, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) { icono() }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = Color.Black,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            if (subtitulo.isNotEmpty()) Text(
                text = subtitulo,
                color = Color.Gray,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        RadioButton(
            selected = seleccionado,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AzulCobalto,
                unselectedColor = Color.Gray
            )
        )
    }
}