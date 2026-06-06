package com.iax.gestordeturnos

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.firebase.auth.FirebaseAuth
import com.iax.gestordeturnos.ui.viewmodels.AuthState
import com.iax.gestordeturnos.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Clase de pruebas unitarias encargada de validar las reglas de negocio del AuthViewModel.
 * Se utiliza Mockito para simular el comportamiento de FirebaseAuth sin depender de red o inicializaciones complejas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val firebaseAuth: FirebaseAuth = mock()
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun configurarEntorno() {
        Dispatchers.setMain(testDispatcher)
        // Configuramos el mock para que el constructor del ViewModel (que mira currentUser) no falle
        whenever(firebaseAuth.currentUser).thenReturn(null)
        
        authViewModel = AuthViewModel(firebaseAuth)
    }

    @After
    fun limpiarEntorno() {
        Dispatchers.resetMain()
    }

    @Test
    fun login_conCamposEnBlanco_debeMutarAEstadoDeErrorInmediatamente() {
        val emailVacio = ""
        val passwordVacia = "12345"

        authViewModel.login(emailVacio, passwordVacia)

        val estadoActual = authViewModel.authState.value

        assertTrue(estadoActual is AuthState.Error)
        assertEquals("Por favor, rellena todos los campos.", (estadoActual as AuthState.Error).message)
    }

    @Test
    fun registrar_conContraseniaCorta_debeGatillarErrorDeLongitud() {
        val emailValido = "lucas@juanpedro.com"
        val passCorta = "1234"
        val confirmacion = "1234"

        authViewModel.registrar(emailValido, passCorta, confirmacion)

        val estadoActual = authViewModel.authState.value

        assertTrue(estadoActual is AuthState.Error)
        assertEquals("La contraseña debe tener al menos 6 caracteres.", (estadoActual as AuthState.Error).message)
    }

    @Test
    fun registrar_conContraseniasDistintas_debeGatillarErrorDeCoincidencia() {
        val email = "soporte@TFG.com"
        val pass1 = "segura123"
        val pass2 = "diferente123"

        authViewModel.registrar(email, pass1, pass2)

        val estadoActual = authViewModel.authState.value

        assertTrue(estadoActual is AuthState.Error)
        assertEquals("Las contraseñas no coinciden.", (estadoActual as AuthState.Error).message)
    }
}
