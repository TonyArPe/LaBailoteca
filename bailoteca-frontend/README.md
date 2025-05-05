# La Bailoteca App — Android Frontend

Aplicación móvil desarrollada en **Kotlin** con **Jetpack Compose**, como interfaz para los usuarios del sistema La Bailoteca. Conectada al backend Spring Boot mediante Retrofit y protegida por Firebase Authentication y JWT.

---

## Tecnologías utilizadas

- Kotlin + Jetpack Compose
- Navigation Compose
- Material 3 (UI moderna)
- MVVM (en proceso)
- Retrofit + Gson
- Firebase Authentication

---

## Estructura del proyecto

```
bailoteca-frontend/
├── data            # Modelos y ViewModels
├── navigation      # Gestor de rutas
├── ui
│   ├── components  # Elementos reutilizables (botones, inputs)
│   ├── screens     # Pantallas (Login, Home...)
│   └── theme       # Colores, tipografía, formas
├── utils           # Constantes y helpers
└── MainActivity.kt # Punto de entrada
```

---

## Funcionalidades implementadas

### Login con Firebase

- Registro e inicio de sesión con email y contraseña
- Obtención del token JWT desde Firebase
- Envío del token al backend Spring Boot

### Consulta de usuarios

- Petición protegida al backend para obtener datos reales
- Validación del token en backend
- Renderizado en pantalla con Compose

### Navegación declarativa

- `NavController` para moverse entre pantallas
- Rutas seguras usando `sealed class Screens`

```kotlin
sealed class Screens(val route: String) {
    object Login : Screens("login")
    object Home : Screens("home")
    object Usuarios : Screens("usuarios")
}
```

---

## Diagrama de flujo de autenticación

```mermaid
sequenceDiagram
    participant User
    participant AndroidApp
    participant FirebaseAuth
    participant SpringBackend

    User->>AndroidApp: Email + Password
    AndroidApp->>FirebaseAuth: Sign in
    FirebaseAuth-->>AndroidApp: idToken
    AndroidApp->>SpringBackend: Authorization: Bearer idToken
    SpringBackend->>FirebaseAuth: Validar token
    FirebaseAuth-->>SpringBackend: OK
    SpringBackend-->>AndroidApp: Datos del usuario
```

---

## Conexión con backend (Retrofit)

```kotlin
Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8080/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
```

Se utilizan endpoints como:
- `GET /usuarios/me`
- `GET /usuarios`

---

## Seguridad

- El token de Firebase se envía en cada petición protegida
- El backend valida el token y devuelve datos solo si es válido
- Se usa `Authorization: Bearer <idToken>`

### Inscripción de usuarios a clases

El backend expone un endpoint:

- **POST** `/api/inscripciones?claseId={id}`: Permite registrar la inscripción del usuario autenticado en una clase.
- También permite obtener inscripciones por usuario o por clase.

El modelo **Inscripcion** relaciona:

- **Usuario**: Información del usuario inscrito.
- **Clase**: Detalles de la clase.
- **Fecha**: Representada como `LocalDate`.
- **Estado**: Representado por `EstadoInscripcion`.

En el frontend (Jetpack Compose):

- Se accede a las inscripciones del usuario para controlar el estado de la UI (botón "Inscribirme").
- Si el usuario ya está inscrito:
  - Se oculta el botón.
  - Se muestra el mensaje: **"Ya estás inscrito"**.
- Desde la pantalla de detalle, se permite cancelar la inscripción.

Se corrigieron problemas de mapeo asegurando que los modelos de Kotlin reflejen correctamente la estructura anidada del backend:

- **usuario**: Representado por el modelo `Usuario`.
- **clase**: Representada por el modelo `Clase`.

---

# NAVIGATION DRAWER
## Scaffold
El **Scaffold** es un componente de alto nivel que ofrece una estructura básica para pantallas con elementos típicos como:

- TopAppBar (barra superior)

- BottomBar (barra inferior)

- FloatingActionButton (botón flotante)

- DrawerContent (panel lateral o Navigation Drawer)

- Content (el contenido principal)

Es el equivalente moderno al clásico DrawerLayout + AppBarLayout de Android XML, pero diseñado para Compose.

## Próximos pasos

- Arquitectura MVVM completa (con ViewModel y Repository)
- Pantalla de inscripción en clases y eventos
- Integración de notificaciones (mensajes / avisos)
- Mejora visual con temas personalizados

---

## Capturas de pantalla

*(Se añadirán más adelante)*

---

## Autor
**Antonio Manuel Aragón Pérez** — 2º DAM, Curso 24/25

---