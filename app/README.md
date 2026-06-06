# Module app

# 📅 Gestor de Turnos - Documentación Técnica de TFG

### Autores
* **Lucas Merino Ortín**
* **Juan Pedro López García**

---

## 🚀 1. Descripción General del Proyecto
Este proyecto consiste en un **Gestor de Turnos Laborales Reactivo e Inmersivo** desarrollado para dispositivos Android utilizando **Kotlin** y **Jetpack Compose** bajo la arquitectura de diseño **MVVM** (Model-View-ViewModel) y principios de **Clean Architecture**.

La aplicación permite a trabajadores por turnos (sanitarios, seguridad, fábricas) configurar sus plantillas horarias cuadradas, automatizar la inyección masiva de cuadrantes periódicos en lote y exportar calendarios multimedia mediante pasarelas nativas del sistema operativo.

---

## 🛠️ 2. Entorno y Requisitos de Desarrollo
* **IDE Oficial:** Android Studio (Ladybug / Koala o superior)
* **SDK Compilación (compileSdk):** 35 (Android 15)
* **SDK Mínimo (minSdk):** 26 (Android 8.0) -> *Garantiza soporte nativo a la API java.time sin des-azucarado.*
* **Herramientas de Compilación:** Kotlin DSL (`.gradle.kts`) y KSP (Kotlin Symbol Processing).

### Stack Tecnológico Central
1. **Jetpack Compose (UI Layer):** Maquetación 100% declarativa basada en Material Design 3.
2. **Jetpack Room (Data Layer):** Base de datos relacional SQLite local con transacciones atómicas.
3. **Firebase Authentication (Network Layer):** Web Services asíncronos para el control de sesiones en la nube.
4. **Kotlin Coroutines & Flow:** Gestión asíncrona de subprocesos paralelos orientada al rendimiento del hilo UI.

---

## 📐 3. Arquitectura del Proyecto y Flujo Multimedia
El software está estructurado bajo los principios de desacoplamiento de Clean Architecture, dividiéndose en tres capas herméticas:



1. **Capa de Datos (Data Layer):** * `BaseDeDatosApp`: Singleton que administra el pool de conexiones de SQLite.
    * `TurnosDao`: Define los mapeos contractuales de las consultas SQL (`@Insert`, `@Transaction`).
    * `TurnosRepository`: Actúa como la **Única Fuente de la Verdad**, abstrayendo el origen de los datos de las vistas.
2. **Capa de Lógica de Negocio (Domain/ViewModel Layer):**
    * `TurnosViewModel`: Mantiene los estados reactivos mutables en búferes protegidos (`StateFlow`) y computa algoritmos matemáticos como el operador módulo (`%`) para el generador cíclico de patrones.
    * `AuthViewModel`: Controla la máquina de estados finitos (`Sealed Class`) de la pasarela de red con Firebase.
3. **Capa de Presentation Visual (UI Layer):**
    * `NavegacionApp`: Orquestador de enrutamiento basado en Compose Navigation encargado de purgar la pila multimedia (`backstack`) en cierres de sesión de forma segura.
    * `PantallaCalendario`: Interfaz operativa central dotada de cuadrículas dinámicas indexadas por hilos secundarios.

---

## 📡 4. Documentación de la API de Persistencia Local (Room DAOs)
Las interacciones de lectura operan mediante flujos calientes reactivos (`Flow`), notificando los cambios en SQLite instantáneamente a la UI a 60-120 FPS.

### Operaciones Críticas del DAO
* `insertarUsuario(usuario: Usuario)`: Inserta credenciales locales aplicando `OnConflictStrategy.REPLACE`.
* `transaccionAsignacionMasiva(turnos: List<AsignacionTurno>)`: Garantiza atomicidad estructural mediante la anotación `@Transaction`. Si falla una sola fila del patrón, SQLite ejecuta un rollback completo impidiendo la corrupción de la base de datos.
* `obtenerAsignacionesConDetalle(usuarioId: String, inicio: Long, fin: Long)`: Realiza un `INNER JOIN` relacional complejo unificando las tablas de asignaciones y estilos cromáticos, proyectando el resultado sobre el DTO plano `TurnoAsignadoDetalle`.
