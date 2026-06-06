package com.iax.gestordeturnos.ui.pantallas

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.google.firebase.auth.FirebaseAuth
import com.iax.gestordeturnos.datos.Nota
import com.iax.gestordeturnos.datos.TipoTurno
import com.iax.gestordeturnos.datos.TurnoAsignadoDetalle
import com.iax.gestordeturnos.ui.theme.AzulCobalto
import com.iax.gestordeturnos.ui.theme.NaranjaAccion
import com.iax.gestordeturnos.ui.viewmodels.TurnosViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.mutableIntStateOf
/**
 * Pantalla central y núcleo operativo de la aplicación.
 * * Maneja el lienzo mensual interactivo mediante la suscripción a flujos reactivos ([StateFlow])
 * provistos por el [TurnosViewModel]. Integra el andamiaje gráfico principal ([Scaffold])
 * coordinado con un menú de navegación lateral ([ModalNavigationDrawer]), diálogos de confirmación
 * y hojas inferiores modales para la asignación y parametrización ágil de jornadas laborales.
 *
 * @param viewModel Instancia del orquestador de estado que actúa como enlace con el repositorio.
 * @param onAgregarTurno Callback que transborda el ID del turno agendado hacia persistencia.
 * @param onViajeExportar Callback de navegación hacia la pantalla de exportación documental.
 * @param onCrearNuevoTurnoViaje Callback de redirección hacia el formulario de plantillas horarias.
 * @param onViajeGestorTurnos Callback de navegación hacia la pantalla de administración de turnos.
 * @param onCerrarSesion Callback encargado de invalidar el token de Firebase y limpiar la pila visual.
 * @param onViajeAyuda Callback de navegación hacia el centro de asistencia al usuario.
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCalendario(
    viewModel: TurnosViewModel,
    onAgregarTurno: (Int?) -> Unit,
    onViajeExportar: () -> Unit,
    onCrearNuevoTurnoViaje: () -> Unit,
    onViajeGestorTurnos: () -> Unit,
    onCerrarSesion: () -> Unit,
    onViajeAyuda: () -> Unit
) {
    // Captura síncrona de los estados del ciclo de vida expuestos por el ViewModel
    val anioMesActual by viewModel.anioMesActual.collectAsState()
    val diaSeleccionado by viewModel.diaSeleccionado.collectAsState()
    val tiposTurno by viewModel.tiposTurno.collectAsState()
    val asignacionesMes by viewModel.asignacionesMes.collectAsState()
    val notasMes by viewModel.notasMes.collectAsState()
    val proximosTurnos by viewModel.proximosTurnos.collectAsState()
    val notaDelDia by viewModel.notaDelDia.collectAsState()

    // Estados mutables locales para el gobierno de la visibilidad de componentes superpuestos
    var mostrarBottomSheet by remember { mutableStateOf(false) }
    var mostrarDialogoDetalles by remember { mutableStateOf(false) }
    var mostrarSelectorFecha by remember { mutableStateOf(false) }
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }

    // Controladores de estado para el cajón de navegación y lanzamiento de corrutinas visuales
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Extracción y formateo dinámico de los metadatos de sesión del usuario de Firebase
    val usuarioActual = FirebaseAuth.getInstance().currentUser
    val emailUsuario = usuarioActual?.email ?: "invitado@app.com"
    val nombreUsuario = emailUsuario.substringBefore("@").replaceFirstChar { it.uppercase() }
    val inicialUsuario = nombreUsuario.take(1).uppercase()

    // Formateador de texto localizado para la cabecera cronológica mensual
    val tituloMes = remember(anioMesActual) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
        anioMesActual.format(formatter).replaceFirstChar { it.uppercase() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFFF8FAFC),
                modifier = Modifier.width(300.dp)
            ) {
                // Cabecera del Drawer: Renderiza el avatar circular y credenciales del usuario
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(AzulCobalto),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = inicialUsuario,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = nombreUsuario,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                            Text(text = emailUsuario, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(16.dp))

                // Opción del menú: Redirección hacia el panel de administración de turnos
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null, tint = AzulCobalto) },
                    label = {
                        Column {
                            Text("Gestión de turnos", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Configura tus horarios", fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onViajeGestorTurnos()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.White)
                )

                // Opción del menú: Acceso al módulo multimedia de exportación
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null, tint = AzulCobalto) },
                    label = {
                        Column {
                            Text("Exportar calendario", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Exporta el calendario a PDF o JPG", fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onViajeExportar()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.White)
                )

                // Opción del menú: Acceso al canal de soporte y asistencia técnica
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.HeadsetMic, contentDescription = null, tint = AzulCobalto) },
                    label = {
                        Column {
                            Text("Ayuda y soporte", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("Centro de asistencia", fontSize = 10.sp, color = Color.Gray)
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onViajeAyuda()
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.White)
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(16.dp))

                // Opción destructiva del menú: Despliega el diálogo de confirmación de salida
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444)) },
                    label = {
                        Column {
                            Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                            Text("Salir de la aplicación", fontSize = 10.sp, color = Color(0xFFEF4444).copy(alpha = 0.7f))
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        mostrarDialogoCerrarSesion = true
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color(0xFFFEF2F2))
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Gestión de Turnos", color = AzulCobalto, fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) { Icon(Icons.Filled.Menu, contentDescription = "Menú", tint = AzulCobalto) }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.volverAHoy() }) {
                            Text("Hoy", color = AzulCobalto, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFFF5F7FA))
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarBottomSheet = true },
                    containerColor = NaranjaAccion,
                    contentColor = Color.White
                ) { Icon(Icons.Filled.Add, contentDescription = "Añadir turno") }
            },
            containerColor = Color(0xFFF5F7FA)
        ) { paddingValues ->
            // Contenedor principal scrollable encargado de estructurar el flujo vertical de la home
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Selector de mes/año e interruptor de salto temporal rápido
                NavegacionMes(
                    mesAnioStr = tituloMes,
                    onAnteriorClick = { viewModel.retrocederMes() },
                    onSiguienteClick = { viewModel.avanzarMes() },
                    onTituloClick = { mostrarSelectorFecha = true }
                )

                // Matriz cuadrangular dinámica encargada del renderizado del mes indexado
                CalendarioGridDinamico(
                    anioMesVisualizando = anioMesActual,
                    diaSeleccionado = diaSeleccionado,
                    asignaciones = asignacionesMes,
                    notas = notasMes,
                    onDiaSeleccionado = { viewModel.cambiarDiaSeleccionado(it) },
                    onDiaMantenido = {
                        viewModel.cambiarDiaSeleccionado(it)
                        viewModel.cargarDetallesDia(it)
                        mostrarDialogoDetalles = true
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Tarjetas inferiores con la colección ordenada de eventos laborales venideros
                ListaProximosTurnos(proximosTurnos)
            }

            // Inyección condicional de la hoja inferior para la asignación ágil de plantillas
            if (mostrarBottomSheet) {
                ModalAgregarTurno(
                    tiposTurno = tiposTurno,
                    onDismiss = { mostrarBottomSheet = false },
                    onTurnoSeleccionado = { idTurno ->
                        viewModel.asignarTurnoAlDia(idTurno, diaSeleccionado)
                        mostrarBottomSheet = false
                    },
                    onViajeACrearTurnoClick = {
                        mostrarBottomSheet = false
                        onCrearNuevoTurnoViaje()
                    }
                )
            }

            // Inyección de la vista detallada en pantalla completa para la gestión de notas y alarmas individuales
            if (mostrarDialogoDetalles) {
                DialogoDetallesDiaPantallaCompleta(
                    dia = diaSeleccionado,
                    anioMes = anioMesActual,
                    turnosDelDia = asignacionesMes[diaSeleccionado] ?: emptyList(),
                    notaGeneral = notaDelDia,
                    onEliminarAsignacion = { id -> viewModel.eliminarAsignacion(id) },
                    onCerrar = { mostrarDialogoDetalles = false },
                    onAbrirAgregarTurno = {
                        mostrarDialogoDetalles = false
                        mostrarBottomSheet = true
                    },
                    onActualizarNota = { id, nota -> viewModel.actualizarNotaTurno(id, nota) },
                    onActualizarAlerta = { id, alerta -> viewModel.actualizarAlertaTurno(id, alerta) },
                    onGuardarNotaGeneral = { viewModel.guardarNota(diaSeleccionado, it) },
                    onConfigurarAlertaCompleta = { idAsignacion, minutos, tipo, mensaje ->
                        viewModel.configurarAlertaCompleta(
                            idAsignacion = idAsignacion,
                            alerta = true,
                            minutos = minutos,
                            tipo = tipo,
                            mensaje = mensaje
                        )
                    }
                )
            }

            // Diálogo de salto numérico de anualidad y mensualidad mediante cuadrícula interactiva
            if (mostrarSelectorFecha) {
                DialogoSelectorMesAno(
                    anioInicial = anioMesActual.year,
                    mesInicial = anioMesActual.monthValue,
                    onDismiss = { mostrarSelectorFecha = false },
                    onConfirmar = { year, month ->
                        viewModel.setAnioMes(year, month)
                        mostrarSelectorFecha = false
                    }
                )
            }

            // Diálogo de confirmación e interrupción segura de la sesión remota de Firebase
            if (mostrarDialogoCerrarSesion) {
                Dialog(onDismissRequest = { mostrarDialogoCerrarSesion = false }) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFEBF1FF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = AzulCobalto,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "¿Estás seguro de que quieres cerrar sesión?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                color = Color.Black,
                                lineHeight = 26.sp
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = {
                                    mostrarDialogoCerrarSesion = false
                                    onCerrarSesion()
                                },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AzulCobalto),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { mostrarDialogoCerrarSesion = false },
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black),
                                shape = RoundedCornerShape(14.dp),
                                elevation = ButtonDefaults.buttonElevation(0.dp)
                            ) { Text("Cancelar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Diálogo flotante personalizado para la selección directa de mes y año.
 * * Permite al usuario saltar de forma ágil a fechas distantes a través de un paginador
 * numérico de anualidad y una rejilla indexada de meses abreviados.
 *
 * @param anioInicial Entero representativo del año que el calendario visualiza previamente.
 * @param mesInicial Entero del 1 al 12 del mes actualmente activo en la cabecera.
 * @param onDismiss Callback encargado de descartar la alerta del árbol visual.
 * @param onConfirmar Callback que propaga la anualidad y mensualidad seleccionadas hacia el ViewModel.
 */
@Composable
fun DialogoSelectorMesAno(
    anioInicial: Int,
    mesInicial: Int,
    onDismiss: () -> Unit,
    onConfirmar: (Int, Int) -> Unit
) {
    var yearActual by remember { mutableIntStateOf(anioInicial) }
    var mesSeleccionado by remember { mutableIntStateOf(mesInicial) }
    val meses = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Cabecera del selector: Controles incrementales/decrementales de anualidad
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { yearActual-- }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = AzulCobalto) }
                    Text(text = yearActual.toString(), color = AzulCobalto, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { yearActual++ }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AzulCobalto) }
                }
                Spacer(modifier = Modifier.height(16.dp))
                // Rejilla de selección mensual en formato matricial de 3 columnas
                LazyVerticalGrid(columns = GridCells.Fixed(3), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(240.dp)) {
                    items(12) { index ->
                        val mesNum = index + 1
                        val isSelected = mesSeleccionado == mesNum
                        Box(
                            modifier = Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(8.dp)).background(if (isSelected) AzulCobalto else Color.White).border(1.dp, if (isSelected) Color.Transparent else Color(0xFFE2E8F0), RoundedCornerShape(8.dp)).clickable { mesSeleccionado = mesNum },
                            contentAlignment = Alignment.Center
                        ) { Text(text = meses[index], color = if (isSelected) Color.White else AzulCobalto, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onDismiss, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F9FF), contentColor = AzulCobalto), shape = RoundedCornerShape(50)) { Text("Cancelar", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    Button(onClick = { onConfirmar(yearActual, mesSeleccionado) }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccion), shape = RoundedCornerShape(50)) { Text("Aceptar", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                }
            }
        }
    }
}

/**
 * Componente horizontal encargado del control y paginación cronológica mensual.
 *
 * @param mesAnioStr Cadena de texto formateada con el mes y año visible en la interfaz (ej.: "mayo 2026").
 * @param onAnteriorClick Callback gatillado al pulsar la flecha izquierda para retroceder un mes.
 * @param onSiguienteClick Callback gatillado al pulsar la flecha derecha para avanzar un mes.
 * @param onTituloClick Callback desencadenado al pulsar el texto central para desplegar el selector rápido.
 */
@Composable
fun NavegacionMes(mesAnioStr: String, onAnteriorClick: () -> Unit, onSiguienteClick: () -> Unit, onTituloClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onAnteriorClick) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Anterior", tint = AzulCobalto) }
        Text(text = mesAnioStr, color = AzulCobalto, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onTituloClick() })
        IconButton(onClick = onSiguienteClick) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente", tint = AzulCobalto) }
    }
}

/**
 * Rejilla adaptativa mensual que modela la cuadrícula de días del calendario laboral.
 * * Calcula matemáticamente el número de días del mes y los huecos iniciales requeridos según el día de la semana
 * del primer día del mes para alinear la matriz según los estándares ISO.
 *
 * @param anioMesVisualizando Dimensión temporal de tipo [YearMonth] que define las fronteras numéricas del mes.
 * @param diaSeleccionado Entero que representa el día natural que se encuentra resaltado en la UI.
 * @param asignaciones Mapa indexado por día de mes que almacena los detalles horariales y estéticos de las jornadas asignadas.
 * @param notas Mapa indexado por día de mes que retiene las observaciones y citas ajenas a los turnos.
 * @param onDiaSeleccionado Callback invocado al efectuar un tap corto sobre una celda válida.
 * @param onDiaMantenido Callback gatillado al mantener pulsada la celda para forzar la lectura del recordatorio e iniciar el modal extendido.
 */
@Composable
fun CalendarioGridDinamico(
    anioMesVisualizando: YearMonth,
    diaSeleccionado: Int,
    asignaciones: Map<Int, List<TurnoAsignadoDetalle>>,
    notas: Map<Int, Nota>,
    onDiaSeleccionado: (Int) -> Unit,
    onDiaMantenido: (Int) -> Unit
) {
    val diasEnElMes = anioMesVisualizando.lengthOfMonth()
    val primerDiaDeLaSemana = anioMesVisualizando.atDay(1).dayOfWeek.value
    // Calcula los desfases iniciales de la cuadrícula convirtiendo el estándar dominical a lunes (1=lunes, 7=domingo)
    val huecosIniciales = primerDiaDeLaSemana - 1
    val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")

    Card(modifier = Modifier.padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Renderizado de las etiquetas informativas superiores de la semana
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                diasSemana.forEach { dia -> Text(text = dia, color = Color.Gray, fontSize = 14.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center) }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
            // Rejilla de renderizado espacial rígida de 7 columnas fijas
            LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.heightIn(max = 1000.dp)) {
                // Generación inerte de las celdas de relleno del mes anterior
                items(huecosIniciales) { CeldaDia(dia = "", esMesActual = false, seleccionado = false, turnos = emptyList(), tieneNota = false, onClick = {}, onLongClick = {}) }
                // Inyección dinámica de las celdas naturales del mes en curso
                items(diasEnElMes) { index ->
                    val dia = index + 1
                    CeldaDia(dia = dia.toString(), esMesActual = true, seleccionado = dia == diaSeleccionado, turnos = asignaciones[dia] ?: emptyList(), tieneNota = notas.containsKey(dia), onClick = { onDiaSeleccionado(dia) }, onLongClick = { onDiaMantenido(dia) })
                }
            }
        }
    }
}

/**
 * Representación visual atómica y táctil de un día individual de la cuadrícula mensual ([CeldaDia]).
 * * Implementa detección avanzada de gestos táctiles mediante [pointerInput] separando el clic corto
 * de la pulsación prolongada. Renderiza indicadores circulares de notas y un máximo de 3 barras compactas de turno.
 *
 * @param dia Cadena textual con el número ordinal del día.
 * @param esMesActual Flag que define si la celda pertenece al mes activo o es un hueco de desborde.
 * @param seleccionado Estado booleano que añade un reborde de realce si el día se encuentra enfocado por el usuario.
 * @param turnos Colección de proyecciones planas de turnos agendados para este día específico.
 * @param tieneNota Flag que indica si la celda debe pintar un indicador circular de observación diaria.
 * @param onClick Acción de actualización síncrona de foco al pulsar.
 * @param onLongClick Acción multimedia de apertura de detalles al sostener la celda.
 */
@Composable
fun CeldaDia(dia: String, esMesActual: Boolean, seleccionado: Boolean, turnos: List<TurnoAsignadoDetalle>, tieneNota: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val modifier = Modifier
        .aspectRatio(0.6f)
        .padding(2.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(if (esMesActual) Color.Transparent else Color(0xFFE2E8F0).copy(alpha = 0.5f))
        .then(if (seleccionado) Modifier.border(2.dp, NaranjaAccion, RoundedCornerShape(8.dp)) else Modifier)
        .pointerInput(Unit) {
            // El detector táctil intercepta de forma aislada las intenciones gestuales del usuario
            if (esMesActual) { detectTapGestures(onTap = { onClick() }, onLongPress = { onLongClick() }) }
        }
    Box(contentAlignment = Alignment.TopCenter, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 2.dp)) {
            Text(text = dia, color = if (esMesActual) AzulCobalto else Color.Gray, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp, modifier = Modifier.padding(bottom = 2.dp))
            // Punto de alta visibilidad indicativo de la existencia de recordatorios generales ajenos a la jornada laboral
            if (tieneNota) { Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(NaranjaAccion)) }
            // Mapeado lineal y escalado de las barras cromáticas de las jornadas asignadas
            turnos.take(3).forEach { turno ->
                val colorTurno = try { Color(turno.colorHex.toColorInt()) } catch (_: Exception) { AzulCobalto }
                Box(modifier = Modifier.padding(top = 2.dp).fillMaxWidth(0.85f).height(12.dp).background(colorTurno, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                    Text(text = turno.abreviatura.take(3), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, style = androidx.compose.ui.text.TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)))
                }
            }
        }
    }
}

/**
 * Hoja de diálogo deslizable inferior para la asignación inmediata de plantillas horarias.
 *
 * @param tiposTurno Colección maestra de modelos de turnos preconfigurados por el usuario en base de datos.
 * @param onDismiss Callback invocado para cerrar y replegar la hoja inferior.
 * @param onTurnoSeleccionado Callback que transmite la clave del tipo de turno escogido para su inserción física.
 * @param onViajeACrearTurnoClick Callback de salto directo hacia el formulario de plantillas horarias.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalAgregarTurno(tiposTurno: List<TipoTurno>, onDismiss: () -> Unit, onTurnoSeleccionado: (Int) -> Unit, onViajeACrearTurnoClick: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).padding(bottom = 32.dp)) {
            Text("Agregar Turno", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AzulCobalto)
            Text("Selecciona una plantilla para asignar a este día", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            if (tiposTurno.isEmpty()) { Text("Aún no tienes turnos creados.", color = Color.Gray) }
            else {
                // Listado scrollable vertical optimizado para la selección fluida de la plantilla
                LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                    items(tiposTurno.size) { index ->
                        val turno = tiposTurno[index]
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onTurnoSeleccionado(turno.idTipoTurno) }.padding(vertical = 12.dp)) {
                            val colorTurno = try { Color(turno.colorHex.toColorInt()) } catch (_: Exception) { AzulCobalto }
                            Box(modifier = Modifier.size(24.dp).background(colorTurno, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(turno.nombre, fontWeight = FontWeight.Bold)
                                Text("${turno.horaInicio} - ${turno.horaFin}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            OutlinedButton(onClick = onViajeACrearTurnoClick, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Crear Nuevo Tipo de Turno")
            }
        }
    }
}

/**
 * Contenedor multimedia inferior destinado a listar los eventos laborales más cercanos cronológicamente.
 *
 * @param proximosTurnos Colección inmutable ordenada de proyecciones [TurnoAsignadoDetalle] recopiladas de Room.
 */
@Composable
fun ListaProximosTurnos(proximosTurnos: List<TurnoAsignadoDetalle>) {
    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), color = Color.White) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Próximos Turnos", color = AzulCobalto, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(16.dp))
            if (proximosTurnos.isEmpty()) { Text("No tienes turnos próximos asignados.", color = Color.Gray) }
            else { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { proximosTurnos.forEach { TarjetaTurnoReal(it) } } }
        }
    }
}

/**
 * Tarjeta informativa individual representativa de un turno laboral venidero ([TarjetaTurnoReal]).
 * * Formatea la marca temporal Epoch de la base de datos a un formato comprensible en castellano
 * e inyecta la paleta de color hexadecimal de forma nativa en los canvas gráficos de la UI.
 *
 * @param turno Instancia DTO con la información unificada procedente del INNER JOIN relacional de Room.
 */
@SuppressLint("UseKtx")
@Composable
fun TarjetaTurnoReal(turno: TurnoAsignadoDetalle) {
    val colorTurno = try { Color(turno.colorHex.toColorInt()) } catch (_: Exception) { AzulCobalto }
    val sdf = SimpleDateFormat("EEEE, d 'de' MMM", Locale("es", "ES"))
    val fechaStr = sdf.format(Date(turno.fecha)).replaceFirstChar { it.uppercase() }
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F7FA), RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(48.dp).background(colorTurno.copy(alpha = 0.2f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Filled.WorkOutline, contentDescription = null, tint = colorTurno) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(turno.nombre, color = AzulCobalto, fontWeight = FontWeight.Medium)
            Text(fechaStr, color = Color.Gray, fontSize = 12.sp)
        }
        Text(text = "${turno.horaInicio}-${turno.horaFin}", color = colorTurno, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Diálogo interactivo en pantalla completa enfocado a la edición y desglose minucioso de un día de la agenda.
 * * Administra de forma centralizada la inserción de comentarios globales de fecha, conmutación reactiva de
 * interruptores de alarmas de segundo plano de Android, apertura de paneles secundarios de configuración y borrado físico de ítems.
 *
 * @param dia Entero del día seleccionado objeto de desglose.
 * @param anioMes Anualidad y mes cronológico de referencia.
 * @param turnosDelDia Lista completa de turnos agendados mapeados en esta fecha natural concreta.
 * @param notaGeneral Instancia de tipo [Nota] recuperada de SQLite vinculada de forma exclusiva a este día.
 * @param onEliminarAsignacion Callback que propaga el identificador numérico de registro a borrar hacia Room.
 * @param onCerrar Acción de cierre y destrucción de este diálogo del árbol de composición.
 * @param onAbrirAgregarTurno Callback que conmuta el estado de visibilidad para transbordar al usuario hacia el modal de plantillas.
 * @param onActualizarNota Callback que actualiza el string del comentario asignado a la jornada de fondo.
 * @param onActualizarAlerta Callback binario que activa/desactiva la bandera de escucha del receptor multimedia de alarmas.
 * @param onConfigurarAlertaCompleta Callback que transmite la configuración de minutos de antelación, canal y mensaje hacia persistencia.
 * @param onGuardarNotaGeneral Callback que consolida o sobrescribe la observación diaria global en SQLite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoDetallesDiaPantallaCompleta(
    dia: Int,
    anioMes: YearMonth,
    turnosDelDia: List<TurnoAsignadoDetalle>,
    notaGeneral: Nota?,
    onEliminarAsignacion: (Int) -> Unit,
    onCerrar: () -> Unit,
    onAbrirAgregarTurno: () -> Unit,
    onActualizarNota: (Int, String) -> Unit,
    onActualizarAlerta: (Int, Boolean) -> Unit,
    onConfigurarAlertaCompleta: (Int, Int, String, String) -> Unit,
    onGuardarNotaGeneral: (String) -> Unit
) {
    val fechaDate = anioMes.atDay(dia)
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es", "ES"))
    val tituloFecha = fechaDate.format(formatter).replaceFirstChar { it.uppercase() }
    // Retenedores temporales mutables para transbordar objetos lógicos hacia diálogos confirmacionales secundarios
    var turnoAEliminar by remember { mutableStateOf<TurnoAsignadoDetalle?>(null) }
    var turnoParaAlerta by remember { mutableStateOf<TurnoAsignadoDetalle?>(null) }

    Dialog(onDismissRequest = onCerrar, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(tituloFecha, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                    navigationIcon = { IconButton(onClick = onCerrar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = Color.White) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AzulCobalto)
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAbrirAgregarTurno, containerColor = NaranjaAccion, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Filled.Add, contentDescription = "Añadir turno", modifier = Modifier.size(28.dp)) }
            },
            containerColor = Color(0xFFF3F4F6)
        ) { paddingValues ->
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Tarjeta de entrada de texto reactiva para recordatorios genéricos externos al trabajo
                item { TarjetaNotaGeneral(nota = notaGeneral, onGuardar = onGuardarNotaGeneral) }
                item { Text("Turnos del día", style = androidx.compose.ui.text.TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AzulCobalto), modifier = Modifier.padding(vertical = 8.dp)) }
                if (turnosDelDia.isEmpty()) { item { Text("No hay turnos asignados para este día.", color = Color.Gray, modifier = Modifier.padding(16.dp)) } }
                else { items(turnosDelDia) { TarjetaTurnoDetalleExpandido(it, { turnoAEliminar = it }, { turnoParaAlerta = it }, onActualizarNota, onActualizarAlerta) } }
            }
            // Alerta confirmacional destructiva intermedia para el borrado físico de la jornada laboral agendada
            turnoAEliminar?.let { turno ->
                AlertDialog(
                    onDismissRequest = { turnoAEliminar = null },
                    title = { Text("Eliminar Turno", fontWeight = FontWeight.Bold) },
                    text = { Text("¿Estás seguro de que quieres eliminar el '${turno.nombre}' de este día? Esta acción no se puede deshacer.", color = Color.Black) },
                    confirmButton = { Button(onClick = { onEliminarAsignacion(turno.idAsignacion); turnoAEliminar = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) { Text("Eliminar", color = Color.White) } },
                    dismissButton = { TextButton(onClick = { turnoAEliminar = null }) { Text("Cancelar", color = Color.Gray) } },
                    containerColor = Color.White
                )
            }
        }
    }
    // Despliega condicionalmente el panel exhaustivo de parametrización de notificaciones push de Android
    turnoParaAlerta?.let { turno -> DialogoConfigurarAlerta(onCerrar = { turnoParaAlerta = null }, onGuardar = { minutos, tipo, mensaje -> onConfigurarAlertaCompleta(turno.idAsignacion, minutos, tipo, mensaje); turnoParaAlerta = null; onActualizarAlerta(turno.idAsignacion, true) }) }
}

/**
 * Bloque de entrada de texto interactivo destinado a la redacción persistente de observaciones generales diarias.
 * * Propaga de manera instantánea el texto al ViewModel mediante lambdas de transbordo en cada pulsación.
 *
 * @param nota Instancia del modelo de datos de tipo [Nota] recuperada de la base de datos de Room.
 * @param onGuardar Callback encargado de persistir la cadena de texto en SQLite en tiempo real.
 */
@Composable
fun TarjetaNotaGeneral(nota: Nota?, onGuardar: (String) -> Unit) {
    var textoNota by remember(nota) { mutableStateOf(nota?.contenido ?: "") }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Nota General del Día", color = AzulCobalto, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = textoNota,
                onValueChange = { textoNota = it; onGuardar(it) },
                placeholder = { Text("Escribe algo importante para hoy...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0), focusedBorderColor = AzulCobalto, unfocusedContainerColor = Color(0xFFF8FAFC), focusedContainerColor = Color(0xFFF8FAFC))
            )
        }
    }
}

/**
 * Tarjeta expandida interactiva para el desglose pormenorizado de una jornada calendarizada ([TarjetaTurnoDetalleExpandido]).
 * * Incorpora un interruptor reactivo ([Switch]) para enganchar/desenganchar el servicio de alarmas y un
 * cuadro de texto para notas aclaratorias internas relativas a la asignación de este día natural específico.
 *
 * @param turno Instancia DTO con la información unificada procedente del INNER JOIN relacional de Room.
 * @param onEliminarClick Acción gatillada al pulsar el botón destructivo de remoción de turno.
 * @param onAlertaClick Acción multimedia de apertura del panel de configuración de alertas pre-turno.
 * @param onActualizarNota Lambda ejecutada al alterar las anotaciones específicas de la jornada agendada.
 * @param onActualizarAlerta Lambda encargada de modificar el flag booleano de activación de la alerta.
 */
@Composable
fun TarjetaTurnoDetalleExpandido(turno: TurnoAsignadoDetalle, onEliminarClick: () -> Unit, onAlertaClick: () -> Unit, onActualizarNota: (Int, String) -> Unit, onActualizarAlerta: (Int, Boolean) -> Unit) {
    val colorTurno = try { Color(turno.colorHex.toColorInt()) } catch (_: Exception) { AzulCobalto }
    var alertaActivada by remember(turno.tieneAlerta) { mutableStateOf(turno.tieneAlerta) }
    var textoNota by remember(turno.nota) { mutableStateOf(turno.nota) }
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Franja cromática izquierda indicativa del color corporativo de la plantilla horaria
            Box(modifier = Modifier.width(8.dp).fillMaxHeight().background(colorTurno))
            Column(modifier = Modifier.padding(16.dp).weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(turno.nombre, color = AzulCobalto, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Alerta", color = AzulCobalto, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = alertaActivada, onCheckedChange = { alertaActivada = it; onActualizarAlerta(turno.idAsignacion, it); if (it) onAlertaClick() }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = NaranjaAccion))
                    }
                }
                Text("${turno.horaInicio} - ${turno.horaFin}", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = textoNota, onValueChange = { textoNota = it; onActualizarNota(turno.idAsignacion, it) }, placeholder = { Text("Añadir una nota...", color = Color.LightGray) }, modifier = Modifier.fillMaxWidth().height(100.dp), shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFE2E8F0), focusedBorderColor = AzulCobalto, unfocusedContainerColor = Color(0xFFF8FAFC), focusedContainerColor = Color(0xFFF8FAFC)))
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEliminarClick) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color(0xFFE53935), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Eliminar Turno", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Diálogo flotante avanzado multimedia para la configuración y parametrización de alarmas nativas pre-turno.
 * * Integra un menú desplegable expuesto ([ExposedDropdownMenuBox]) para la selección matemática de la antelación
 * en minutos y una fila táctil exclusiva para alternar los canales de notificación (Sonido, Vibración o Silencio).
 *
 * @param onCerrar Acción de descarte del diálogo flotante de configuración.
 * @param onGuardar Callback que transmite el entero de minutos calculados, string del canal y cuerpo del mensaje.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoConfigurarAlerta(onCerrar: () -> Unit, onGuardar: (Int, String, String) -> Unit) {
    var expandedAviso by remember { mutableStateOf(false) }
    var avisoSeleccionado by remember { mutableStateOf("15 minutos antes") }
    val opcionesAviso = listOf("Al momento", "5 minutos antes", "15 minutos antes", "30 minutos antes", "1 hora antes", "2 horas antes")
    // Mapeado explícito que traduce las etiquetas de texto de la UI a enteros de minutos consumibles por la alarma
    val mappingMinutos = mapOf("Al momento" to 0, "5 minutos antes" to 5, "15 minutos antes" to 15, "30 minutos antes" to 30, "1 hora antes" to 60, "2 horas antes" to 120)
    var tipoAlerta by remember { mutableStateOf("Sonido") }
    var mensaje by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onCerrar) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Configurar Alerta", color = AzulCobalto, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(24.dp))
                Text("Avisarme", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = expandedAviso, onExpandedChange = { expandedAviso = !expandedAviso }) {
                    OutlinedTextField(value = avisoSeleccionado, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAviso) }, modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = expandedAviso, onDismissRequest = { expandedAviso = false }, modifier = Modifier.background(Color.White)) {
                        opcionesAviso.forEach { opcion -> DropdownMenuItem(text = { Text(opcion) }, onClick = { avisoSeleccionado = opcion; expandedAviso = false }) }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Tipo de alerta", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                // Fila de selección táctil exclusiva de la tipología multimedia del aviso push
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BotonTipoAlerta("Sonido", Icons.Filled.Notifications, tipoAlerta == "Sonido") { tipoAlerta = "Sonido" }
                    BotonTipoAlerta("Vibración", Icons.Filled.PhoneAndroid, tipoAlerta == "Vibración") { tipoAlerta = "Vibración" }
                    BotonTipoAlerta("Silencio", Icons.Filled.NotificationsOff, tipoAlerta == "Silencio") { tipoAlerta = "Silencio" }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Mensaje (opcional)", fontSize = 14.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = mensaje, onValueChange = { mensaje = it }, placeholder = { Text("Escribe un mensaje...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                Spacer(modifier = Modifier.height(32.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(onClick = onCerrar, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black), shape = RoundedCornerShape(12.dp)) { Text("Cancelar", fontWeight = FontWeight.Bold) }
                    Button(onClick = { val minutos = mappingMinutos[avisoSeleccionado] ?: 0; onGuardar(minutos, tipoAlerta, mensaje) }, modifier = Modifier.weight(1f).height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = NaranjaAccion), shape = RoundedCornerShape(12.dp)) { Text("Guardar", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

/**
 * Botón con comportamiento de selector de ratio atómico e individual para la tipología de alerta ([BotonTipoAlerta]).
 * * Modifica de forma reactiva y elástica el color de sus bordes e iconos según su estado enfocado.
 *
 * @param texto Etiqueta textual indicativa del canal multimedia (ej.: "Sonido").
 * @param icono Vector de tipo [ImageVector] oficial de Material Design a renderizar en el centro del canvas.
 * @param seleccionado Estado booleano que añade un reborde de color y peso si la opción es la activa.
 * @param onClick Acción de conmutación lógica al efectuar tap corto sobre el Box.
 */
@Composable
fun RowScope.BotonTipoAlerta(texto: String, icono: androidx.compose.ui.graphics.vector.ImageVector, seleccionado: Boolean, onClick: () -> Unit) {
    val colorBorde = if (seleccionado) NaranjaAccion else Color(0xFFE2E8F0)
    val colorTexto = if (seleccionado) AzulCobalto else Color.Gray
    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp).clip(RoundedCornerShape(12.dp)).border(1.dp, colorBorde, RoundedCornerShape(12.dp)).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icono, contentDescription = null, tint = if (seleccionado) NaranjaAccion else Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = texto, color = colorTexto, fontSize = 12.sp, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal)
        }
    }
}