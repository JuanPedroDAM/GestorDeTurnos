package com.iax.gestordeturnos.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clase sellada ([Sealed Class]) que modela los estados finitos del flujo multimedia de autenticación.
 * * Define de forma unificada las situaciones en las que puede hallarse la pasarela de red de la aplicación.
 */
sealed class AuthState {
    /** Estado inerte o de reposo inicial en espera de interacciones del usuario. */
    object Idle : AuthState()

    /** Estado de carga asíncrona activo mientras la corrutina procesa peticiones I/O en segundo plano. */
    object Loading : AuthState()

    /** Estado de éxito que consolida la validación de credenciales en el servidor remoto. */
    object LoginSuccess : AuthState()

    /** Estado de éxito que consolida la creación física de la nueva cuenta en el servidor remoto. */
    object RegisterSuccess : AuthState()

    /** Estado que confirma el despacho atómico del correo de restablecimiento de contraseña. */
    object RecoveryEmailSent : AuthState()

    /** * Estado de error que encapsula el mensaje descriptivo del fallo de red.
     * @property message Cadena de texto legible devuelta por las excepciones del SDK.
     */
    data class Error(val message: String) : AuthState()
}

/**
 * Orquestador arquitectónico ([ViewModel]) encargado de centralizar y procesar la lógica de autenticación.
 * * Implementa el patrón de diseño MVVM sirviendo como puente no bloqueante entre el servicio remoto
 * de [FirebaseAuth] y las pantallas visuales de presentación de la app.
 * * Expone flujos de estado asíncronos y seguros ([StateFlow]) para notificar atómicamente a la UI sobre
 * transiciones de sesión, garantizando el blindaje de las credenciales ante recomposiciones del hardware.
 *
 * @author Lucas Merino Ortín
 * @author Juan Pedro López García
 */
class AuthViewModel(
    // Instancia central encapsulada del cliente de Firebase Authentication
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    // MutableStateFlow: Tubería de datos interna de escritura para actualizar los estados de sesión
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    /** * Flujo de lectura inmutable expuesto públicamente para la recolección reactiva desde las vistas Compose.
     * Protege el estado encapsulándolo mediante [asStateFlow] para impedir mutaciones externas.
     */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Bloque constructor: Verifica en caliente al instanciar la clase si el dispositivo móvil
        // conserva un token de sesión local válido del servidor remoto para gatillar un login automático.
        if (auth.currentUser != null) {
            _authState.value = AuthState.LoginSuccess
        }
    }

    /**
     * Despacha de forma asíncrona la verificación de credenciales de acceso contra los Web Services de Firebase.
     * * @param email Cadena de caracteres representativa del correo electrónico introducido.
     * @param pass Cadena de caracteres representativa de la clave de seguridad secreta.
     */
    fun login(email: String, pass: String) {
        // Validación local síncrona preliminar para optimizar ciclos de procesador y evitar peticiones de red vacías
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena todos los campos.")
            return
        }

        _authState.value =
            AuthState.Loading // Cambia reactivamente el estado para encender los cargadores en la UI

        // Invoca el canal de red asíncrono nativo del SDK
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.LoginSuccess // Consolida el acceso seguro
                } else {
                    // Extrae la descripción localizada del fallo (ej: contraseña incorrecta, red caída)
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Error al iniciar sesión"
                    )
                }
            }
    }

    /**
     * Despacha el alta o creación física de un nuevo perfil de usuario en la base de datos distribuida de Firebase.
     * * @param email Dirección de correo electrónico de registro.
     * @param pass Contraseña principal de seguridad elegida.
     * @param passConfirm Duplicado de confirmación estricta para la mitigación de erratas tipográficas.
     */
    fun registrar(email: String, pass: String, passConfirm: String) {
        if (email.isBlank() || pass.isBlank() || passConfirm.isBlank()) {
            _authState.value = AuthState.Error("Por favor, rellena todos los campos.")
            return
        }
        if (pass.length < 6) {
            _authState.value = AuthState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }
        if (pass != passConfirm) {
            _authState.value = AuthState.Error("Las contraseñas no coinciden.")
            return
        }

        _authState.value = AuthState.Loading

        // Despacha la intención asíncrona de creación de cuenta hacia el Web Service
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value =
                        AuthState.RegisterSuccess // Modifica el estado de fondo para gatillar el modal de éxito
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Error al registrarse"
                    )
                }
            }
    }

    /**
     * Envía una orden al servidor remoto para despachar un correo multimedia de restablecimiento de contraseña.
     * * @param email Cuenta de correo donde se inyectará el hipervínculo seguro de recuperación de credenciales.
     */
    fun recuperarContrasena(email: String) {
        if (email.isBlank()) {
            _authState.value = AuthState.Error("Por favor, introduce tu correo electrónico.")
            return
        }

        _authState.value = AuthState.Loading

        // Ejecuta la orden remota asíncrona de restablecimiento de Firebase
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.RecoveryEmailSent
                } else {
                    _authState.value = AuthState.Error(
                        task.exception?.localizedMessage ?: "Error al enviar el correo"
                    )
                }
            }
    }

    /**
     * Resetea el estado de autenticación regresándolo de forma segura al punto inerte inicial.
     * * Evita bucles infinitos de recomposición en las llamadas `LaunchedEffect` de la UI.
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    /**
     * Destruye de forma hermética el token de autenticación almacenado en el hardware del terminal móvil.
     * * Realiza la desconexión definitiva de la sesión y limpia el árbol del grafo de estados reactivos.
     */
    fun cerrarSesion() {
        auth.signOut() // Cierre físico nativo del SDK de Firebase
        _authState.value = AuthState.Idle
    }

    /**
     * Puente conector alternativo nominal integrado para mapear de forma segura los callbacks tipados de la UI.
     * * Redirige de forma unificada la acción hacia la implementación de recuperación canónica de la clase.
     * @param emailRecuperar Cuenta de correo electrónico de destino para la recuperación.
     */
    fun recuperarContrasenia(emailRecuperar: String) {
        recuperarContrasena(emailRecuperar) // Enlace relacional síncrono que subsana la duplicidad semántica de las vistas
    }
}