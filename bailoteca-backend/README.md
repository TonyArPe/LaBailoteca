# La Bailoteca — Backend (Spring Boot)

Este módulo contiene el backend de La Bailoteca, una API REST desarrollada en **Java 21** con **Spring Boot**, conectada a **PostgreSQL** y protegida con **JWT**. Expone todos los recursos necesarios para la gestión de una academia de baile: usuarios, clases, eventos, pagos, inscripciones y notificaciones.

---

## Tecnologías utilizadas

- Java 21 + Spring Boot 3
- Spring Security (JWT + BCrypt)
- Spring Data JPA
- PostgreSQL (en contenedor Docker)
- WebSockets + STOMP
- Firebase Admin SDK (validación de tokens móviles)
- Docker + docker-compose

---

## Estructura del proyecto

```
bailoteca-backend/
├── src/main/java/com/bailoteca/
│   ├── config/                # Configuraciones generales y seguridad
│   ├── controller/            # Controladores REST
│   ├── models/                # Entidades JPA (dominios)
│   ├── repository/            # Repositorios JPA
│   ├── service/               # Servicios y lógica de negocio
│   ├── security/              # JWT, filtros, UserDetailsService
├── src/main/resources/
│   ├── application.properties
│   └── static/                # Archivos HTML estáticos (WebSocket testing)
├── Dockerfile
├── docker-compose.yml
├── pom.xml
```

---

## Seguridad y Autenticación

La seguridad del backend se basa en:

- Autenticación JWT
- Cifrado de contraseñas con BCrypt
- Validación de token Firebase (para usuarios desde la app)
- Control de acceso por roles (`ADMIN`, `PROFESOR`, `USUARIO`, `INVITADO`)

### Protección de endpoints por rol

| Endpoint                      | Acceso permitido                            |
|------------------------------|---------------------------------------------|
| `GET /api/usuarios`          | Solo `ADMIN`                                |
| `GET /api/usuarios/{id}`     | `ADMIN` o el propio usuario                 |
| `DELETE /api/usuarios/{id}`  | `ADMIN` o el propio usuario                 |
| `GET /api/usuarios/me`       | Usuario autenticado                        |
| Otros CRUD (clases/eventos)  | `PROFESOR` o `ADMIN` según corresponda       |

### Clases clave
- `JwtAuthenticationFilter`: verifica el JWT en cada petición
- `JwtUtils`: generación y validación de tokens
- `CustomUserDetailsService`: carga usuarios desde la base de datos

---

## Datos iniciales y configuración

La clase `DataInitializer.java` crea usuarios, clases y horarios predefinidos al iniciar la app (solo si la tabla de usuarios está vacía).

### Docker

```bash
docker-compose up -d
```

Levanta:
- PostgreSQL (puerto 5432)
- pgAdmin (puerto 5050)

Credenciales pgAdmin:
- Email: `admin@bailoteca.com`
- Contraseña: `admin123`

---

## Endpoints documentados

Consulta los endpoints y modelos detallados en `docs/` o via Swagger si lo habilitas. Algunos ejemplos:

### Usuarios
- `POST /api/auth/login` → Login y obtención de token
- `GET /api/usuarios/me` → Perfil del usuario actual
- `GET /api/usuarios` → Lista de usuarios (solo admin)

## Módulo de Gestión de Usuarios

Este módulo permite gestionar los usuarios de la plataforma (administradores, profesores y alumnos). La lógica de acceso está controlada por roles y validación JWT.

### Acceso por rol

- `ADMIN:` puede ver, editar o eliminar cualquier usuario.
- `USUARIO:` solo puede acceder, modificar o eliminar su propio perfil.
- `INVITADO:` no tiene acceso a endpoints protegidos.

### Endpoints implementados

| Método | Ruta                  | Descripción                                   | Acceso            |
|--------|-----------------------|-----------------------------------------------|-------------------|
| GET    | `/api/usuarios`       | Obtener todos los usuarios                    | Solo `ADMIN`      |
| GET    | `/api/usuarios/{id}`  | Obtener un usuario específico                 | `ADMIN` o propietario |
| GET    | `/api/usuarios/me`    | Obtener el perfil del usuario autenticado     | Cualquier usuario |
| POST   | `/api/usuarios`       | Crear un nuevo usuario (registro o admin)     | Público / `ADMIN` |
| PUT    | `/api/usuarios/{id}`  | Editar un usuario (nombre, teléfono, etc.)    | `ADMIN` o propietario |
| DELETE | `/api/usuarios/{id}`  | Eliminar un usuario                           | `ADMIN` o propietario |

### Seguridad aplicada

- Todos los endpoints protegidos validan el token JWT.
- El sistema distingue si el usuario es `ADMIN` o si está accediendo a su propio perfil.
- Las contraseñas se almacenan de forma segura con BCrypt.
- Se usan anotaciones `@PreAuthorize` y comprobaciones manuales con el `SecurityContext`.

### Lógica de protección personalizada

Ejemplo de protección en el controlador:

```java
if (actual.getRol().name().equals("ADMIN") || actual.getId().equals(id)) {
    // Permitir acción
} else {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

Esto permite que un usuario acceda solo a su propio perfil, y que el administrador pueda gestionar cualquier usuario.
### Notas adicionales

  - El campo fechaRegistro se asigna automáticamente al crear un usuario.

  - Las contraseñas se codifican en el UsuarioController usando passwordEncoder.encode(...) antes de guardarse.

  - Se proporciona el endpoint /api/usuarios/me para que cualquier usuario pueda consultar fácilmente su información actual.

### Clases y eventos
- `GET /api/clases`
- `GET /api/eventos`
- `POST /api/clases` (profesores)

## Módulo de Gestión de Clases

Este módulo permite a los usuarios autenticados interactuar con las clases ofrecidas por la academia.

### Acceso por rol

- `ADMIN`: puede ver, crear, editar y eliminar cualquier clase.
- `PROFESOR`: puede crear clases propias, y editar/eliminar solo las suyas.
- `USUARIO`: solo puede consultar clases.
- `INVITADO`: acceso opcional para consultar clases públicas.

### Endpoints implementados

| Método | Ruta                              | Descripción                                         | Acceso     |
|--------|-----------------------------------|-----------------------------------------------------|------------|
| GET    | `/api/clases`                     | Lista todas las clases disponibles                  | Público     |
| GET    | `/api/clases/{id}`                | Obtener una clase específica por ID                 | Público     |
| GET    | `/api/clases/profesor/{id}`       | Obtener clases asociadas a un profesor              | Público     |
| GET    | `/api/clases/buscar?nombre=xxx`   | Buscar clases por nombre parcial o completo         | Público     |
| POST   | `/api/clases`                     | Crear una nueva clase                               | ADMIN, PROFESOR |
| PUT    | `/api/clases/{id}`                | Editar una clase (si eres el profesor o admin)      | ADMIN, PROFESOR |
| DELETE | `/api/clases/{id}`                | Eliminar una clase (si eres el profesor o admin)    | ADMIN, PROFESOR |

### Seguridad aplicada

- Todos los endpoints POST/PUT/DELETE validan el token JWT.
- Se usa el método `getUsuarioAutenticado()` para validar si el usuario es el dueño de la clase o tiene permisos de administración.
- El campo `profesor` se asigna automáticamente desde el usuario autenticado en el momento de crear la clase.

### Notas adicionales

- Se utiliza `findByNombreContainingIgnoreCase(...)` para búsquedas más intuitivas.
- Se pueden añadir más filtros (por fecha, nivel, etc.) en el futuro.
Inscripciones

    GET /api/inscripciones/usuario/{usuarioId} → Lista de inscripciones de un usuario

    GET /api/inscripciones/clase/{claseId} → Lista de inscripciones a una clase

    POST /api/inscripciones?claseId=... → Inscribirse en una clase

    DELETE /api/inscripciones/{id} → Cancelar inscripción (según permisos)

## Módulo de Gestión de Inscripciones

Este módulo permite a los usuarios registrarse en clases y consultar sus inscripciones, así como permitir a profesores y administradores gestionar los alumnos inscritos.

### Acceso por rol

- `ADMIN:` puede consultar y eliminar cualquier inscripción.
- `PROFESOR:` puede consultar y eliminar inscripciones de sus propias clases.
- `USUARIO:` puede consultar y cancelar sus propias inscripciones.
- `INVITADO:` no tiene acceso a este módulo.

### Endpoints implementados

| Método | Ruta                              | Descripción                                   | Acceso                     |
|--------|-----------------------------------|-----------------------------------------------|----------------------------|
| GET    | `/api/inscripciones`             | Obtener todas las inscripciones               | Solo `ADMIN`               |
| GET    | `/api/inscripciones/usuario/{id}`| Obtener inscripciones de un usuario           | `ADMIN` o el mismo         |
| GET    | `/api/inscripciones/clase/{id}`  | Obtener inscripciones a una clase             | `ADMIN` o profesor dueño   |
| POST   | `/api/inscripciones?claseId=...` | Inscribir al usuario autenticado en una clase | Cualquier `USUARIO`        |
| DELETE | `/api/inscripciones/{id}`        | Cancelar una inscripción                      | `ADMIN`, profesor o usuario implicado |

### Seguridad aplicada

- Todos los endpoints validan el token JWT para asegurar que el usuario esté autenticado.
- Se comprueba el rol del usuario para permitir solo acciones autorizadas.
- El campo `usuario` en una inscripción siempre se asigna automáticamente a partir del JWT, evitando que un usuario pueda inscribir a otro.

### Lógica de protección personalizada

Ejemplo de lógica en el controlador:

```java
if (
    actual.getId().equals(inscripcion.getUsuario().getId()) ||
    actual.getId().equals(inscripcion.getClase().getProfesor().getId()) ||
    actual.getRol().name().equals("ADMIN")
) {
    // Permitir acción
} else {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```
Esto asegura que solo el usuario implicado, su profesor o un administrador puedan eliminar una inscripción.

### Notas adicionales
- No se permite crear inscripciones duplicadas a una misma clase.
- El campo estado de la inscripción se establece automáticamente como ACTIVO.
- El sistema podría ampliarse en el futuro para gestionar listas de espera, inscripciones pendientes o aprobaciones manuales.

## Módulo de Gestión de Eventos

Este módulo permite a los profesores y administradores gestionar los eventos públicos de la academia. Los usuarios e invitados pueden consultar eventos, pero solo el personal autorizado puede crearlos, modificarlos o eliminarlos.

### Acceso por rol

- `ADMIN:` puede ver, crear, editar o eliminar cualquier evento.
- `PROFESOR:` puede crear y modificar sus propios eventos.
- `USUARIO:` solo puede consultar eventos.
- `INVITADO:` puede consultar eventos activos.

### Endpoints implementados

| Método | Ruta                  | Descripción                                   | Acceso            |
|--------|-----------------------|-----------------------------------------------|-------------------|
| GET    | `/api/eventos`        | Lista todos los eventos registrados           | Público           |
| GET    | `/api/eventos/{id}`   | Obtiene un evento específico                  | Público           |
| POST   | `/api/eventos`        | Crea un nuevo evento                          | `ADMIN`, `PROFESOR` |
| PUT    | `/api/eventos/{id}`   | Edita un evento (solo el organizador o un admin) | `ADMIN`, `PROFESOR` |
| DELETE | `/api/eventos/{id}`   | Elimina un evento (solo el organizador o un admin) | `ADMIN`, `PROFESOR` |

### Seguridad aplicada

- Los métodos `POST`, `PUT` y `DELETE` están protegidos mediante lógica personalizada.
- El organizador del evento se guarda automáticamente desde el usuario autenticado al crear un nuevo evento.
- Solo el organizador o un administrador puede modificar o eliminar un evento.
- La validación del token JWT y recuperación del usuario se realiza mediante `SecurityContextHolder`.

### Notas adicionales

- Los eventos se crean en estado `ACTIVO` por defecto.
- Se recomienda añadir filtros opcionales por fecha o estado (`ACTIVO`, `CANCELADO`, `POSPUESTO`) en el futuro.


## Módulo de Gestión de Pagos

Este módulo permite llevar un control de los pagos realizados por los usuarios en la academia. Los pagos pueden ser por:

- Participación en eventos puntuales.
- Pagos mensuales por clases regulares.

Los registros de pago se realizan de forma manual por parte de administradores o profesores, ya que los pagos se efectúan en persona y en efectivo.

### Acceso por rol

- `ADMIN:` puede ver, crear y eliminar cualquier pago (eventos o mensualidades).
- `PROFESOR:` puede registrar y consultar pagos de sus alumnos.
- `USUARIO:` no tiene acceso a los endpoints de pagos (por seguridad y privacidad).

### Endpoints implementados

#### 🔹 `/api/pagos-evento`

| Método | Ruta                          | Descripción                          | Acceso            |
|--------|-------------------------------|--------------------------------------|-------------------|
| GET    | `/api/pagos-evento`           | Obtener todos los pagos por evento   | `ADMIN`, `PROFESOR` |
| GET    | `/api/pagos-evento/usuario/{id}` | Obtener pagos por usuario            | `ADMIN`, `PROFESOR` |
| GET    | `/api/pagos-evento/evento/{id}`  | Obtener pagos por evento             | `ADMIN`, `PROFESOR` |
| POST   | `/api/pagos-evento`           | Registrar un nuevo pago de evento    | `ADMIN`, `PROFESOR` |
| DELETE | `/api/pagos-evento/{id}`      | Eliminar un pago (si eres el dueño o admin) | `ADMIN`, propietario |

#### 🔹 `/api/pagos-mensualidad`

| Método | Ruta                              | Descripción                          | Acceso            |
|--------|-----------------------------------|--------------------------------------|-------------------|
| GET    | `/api/pagos-mensualidad`         | Obtener todos los pagos mensuales    | `ADMIN`, `PROFESOR` |
| GET    | `/api/pagos-mensualidad/usuario/{id}` | Obtener pagos mensuales de un usuario | `ADMIN`, `PROFESOR` |
| GET    | `/api/pagos-mensualidad/mes/{mes}` | Obtener pagos registrados para un mes | `ADMIN`, `PROFESOR` |
| POST   | `/api/pagos-mensualidad`         | Registrar nuevo pago mensual         | `ADMIN`, `PROFESOR` |
| DELETE | `/api/pagos-mensualidad/{id}`    | Eliminar un pago mensual             | `ADMIN`           |

### Seguridad aplicada

- Los endpoints están protegidos mediante autenticación JWT.
- Las acciones están limitadas según el rol del usuario autenticado.
- Para eliminar un pago, se comprueba que el usuario sea `ADMIN` o el mismo que realizó el pago (`pago.getUsuario().getId()`).
- No se permite a los usuarios normales modificar, crear ni borrar pagos.

### Notas adicionales

- Cada pago se relaciona con el usuario que lo realiza mediante `@ManyToOne`.
- En `PagoEvento`, también se vincula con un evento.
- En `PagoMensualidad`, se guarda el mes y la fecha del pago como referencia.
- Se puede extender este módulo en el futuro para añadir filtros por fecha o generación de reportes.

## Módulo de Notificaciones

Este módulo permite enviar, recibir y gestionar notificaciones en tiempo real entre los usuarios de la plataforma, utilizando WebSockets con STOMP, además de exponer endpoints REST para su gestión.

### Acceso por rol

- `ADMIN` y `PROFESOR:` pueden generar notificaciones para sus alumnos u otros usuarios.
- `USUARIO:` puede recibir notificaciones, marcarlas como leídas o eliminarlas.
- `INVITADO:` no tiene acceso a este módulo.

### Endpoints implementados

| Método | Ruta                                              | Descripción                                   | Acceso            |
|--------|---------------------------------------------------|-----------------------------------------------|-------------------|
| GET    | `/api/notificaciones/usuario/{usuarioId}`         | Obtener todas las notificaciones del usuario  | Usuario autenticado |
| GET    | `/api/notificaciones/usuario/{usuarioId}/noleidas`| Obtener solo notificaciones no leídas         | Usuario autenticado |
| GET    | `/api/notificaciones/usuario/{usuarioId}/contador-noleidas` | Contar notificaciones no leídas              | Usuario autenticado |
| POST   | `/api/notificaciones`                             | Crear y enviar una nueva notificación         | `ADMIN`, `PROFESOR` |
| PUT    | `/api/notificaciones/{id}/leida`                  | Marcar una notificación como leída            | Usuario autenticado |
| PUT    | `/api/notificaciones/usuario/{usuarioId}/marcar-todas-leidas` | Marcar todas como leídas                     | Usuario autenticado |
| DELETE | `/api/notificaciones/{id}`                        | Eliminar una notificación                     | Usuario autenticado |

### WebSocket en tiempo real

Se ha configurado un canal de WebSocket para enviar notificaciones instantáneamente a los usuarios conectados:

| Destino STOMP          | Descripción                                      |
|-------------------------|--------------------------------------------------|
| `/app/notificar`        | Endpoint para enviar notificaciones desde el cliente |
| `/topic/notificaciones` | Canal público donde todos los suscriptores recibirán nuevas notificaciones |

#### Estructura del WebSocket:

Cliente → `/app/notificar` → [Servidor] → `/topic/notificaciones` → Clientes suscritos

### Seguridad aplicada

- El envío de notificaciones vía REST o WebSocket requiere autenticación.
- Se verifica que el receptor exista antes de guardar una notificación.
- Solo el usuario receptor puede consultar, marcar o eliminar sus notificaciones.
- El sistema registra automáticamente la fecha de envío y marca la notificación como no leída por defecto.

### Clases clave

- **`NotificacionService:`** lógica de negocio y validación de acceso.
- **`NotificacionController:`** endpoints REST para gestión de notificaciones.
- **`NotificacionWebSocketController:`** maneja las notificaciones enviadas vía WebSocket.
- **`WebSocketConfig:`** configuración de STOMP y canal `/ws`.

### Notas adicionales

- Las notificaciones pueden tener distintos tipos (`TipoNotificacion` enum).
- Se almacena quién envía la notificación (emisor) y quién la recibe (receptor).
- Los mensajes no leídos pueden ser contados y marcados como leídos en lote.
- Para facilitar pruebas, se incluye un HTML simple en `static/notificaciones.html` para visualizar las notificaciones entrantes por WebSocket.

## Módulo de Chat Interno

Este módulo permite a profesores y alumnos comunicarse mediante un sistema de mensajería tipo WhatsApp. Soporta chats privados (entre un profesor y un alumno de su clase) y chats grupales (todos los alumnos inscritos en una clase + el profesor).

### Acceso por rol

- `PROFESOR:` puede iniciar chats con alumnos inscritos en sus clases, y participar en chats grupales.
- `USUARIO:` puede escribir a su profesor si está inscrito en una clase.
- `ADMIN:` acceso general si se habilita en el futuro (no obligatorio actualmente).

### Estructura de entidades

- **`Chat:`** representa una conversación. Puede ser privada (usuario1 y usuario2) o grupal (asociada a una clase).
- **`Mensaje:`** representa un mensaje enviado. Tiene contenido, `fechaEnvio`, emisor, receptor (si es privado), o clase (si es grupal).

### Endpoints implementados

| Método | Ruta                     | Descripción                              |
|--------|---------------------------|------------------------------------------|
| GET    | `/api/mensajes/chat/{id}` | Obtener mensajes de un chat privado      |
| GET    | `/api/mensajes/clase/{id}`| Obtener mensajes del chat grupal de una clase |
| POST   | `/api/mensajes`           | Crear un nuevo mensaje (privado o grupal)|

- Todos los mensajes se envían también en tiempo real mediante WebSocket.

### WebSocket

#### Suscripciones desde el frontend:

- **Grupo:** `subscribe("/topic/clase/{id}")`  
  (los alumnos y el profesor de la clase)
- **Privado:** `subscribe("/topic/chat/{id}")`  
  (solo entre el profesor y alumno)

#### Envío de mensajes:

- **Endpoint:** `send("/app/mensaje", mensaje)`  
  El servidor guarda y reenvía el mensaje a todos los suscriptores del chat.

### Seguridad aplicada

- La lógica de autorización (que el emisor y receptor estén en la misma clase, o que el usuario esté inscrito) puede agregarse fácilmente en el `MensajeService` para mayor control.
- El sistema usa tokens JWT para autenticar al usuario.
## Módulo de Chat en Tiempo Real

Este módulo permite la comunicación en tiempo real entre usuarios registrados de la academia, implementando un sistema de mensajería tipo WhatsApp con WebSocket y STOMP.

### Objetivo

Facilitar la comunicación privada entre profesores y alumnos inscritos en sus clases, así como permitir chats grupales entre el profesor y todos los alumnos de una clase. Todo el sistema funciona en tiempo real y los mensajes se almacenan en la base de datos.

### Tecnologías empleadas

- **WebSocket + STOMP:** comunicación bidireccional en tiempo real.
- **SockJS:** compatibilidad con navegadores que no soportan WebSocket nativo.
- **Spring WebSocket (SimpMessagingTemplate):** envío y recepción de mensajes desde el backend.
- **HTML y JS:** cliente de prueba para simular chats privados y grupales.
- **Spring Boot y JPA:** para la persistencia de mensajes.

### Estructura del módulo

#### Entidades

**`Mensaje:`** Representa un mensaje en el sistema. Puede ser privado o grupal.

| Campo       | Tipo            | Descripción                                |
|-------------|-----------------|--------------------------------------------|
| `id`        | `Long`          | Identificador del mensaje                  |
| `contenido` | `String`        | Texto del mensaje                          |
| `fechaEnvio`| `LocalDateTime` | Fecha y hora de envío                      |
| `emisor`    | `Usuario`       | Usuario que envía el mensaje               |
| `receptor`  | `Usuario`       | Usuario receptor (solo en chats privados)  |
| `clase`     | `Clase`         | Clase asociada (solo en chats grupales)    |
| `esGrupal`  | `boolean`       | Define si el mensaje es privado o grupal   |

#### Repositorio

**`MensajeRepo:`** Contiene métodos personalizados:

```java
List<Mensaje> findByClaseIdOrderByFechaEnvioAsc(Long claseId);

List<Mensaje> findByEmisorIdAndReceptorIdOrReceptorIdAndEmisorIdOrderByFechaEnvioAsc(
    Long emisorId, Long receptorId, Long receptorId2, Long emisorId2);
```


### MensajeService
- Guarda el mensaje en la base de datos.
- Envía el mensaje al canal correspondiente usando SimpMessagingTemplate.
- Verifica que el emisor y receptor existan.
- Envío a dos canales privados:
    - `user/{receptorId}/queue/chat/{emisorId}`
    - `user/{emisorId}/queue/chat/{receptorId}`

**Envio para un chat privado:**
```java
messagingTemplate.convertAndSend("/user/" + receptorId + "/queue/chat/" + emisorId, mensaje);
messagingTemplate.convertAndSend("/user/" + emisorId + "/queue/chat/" + receptorId, mensaje);
```

**Envío para chat grupal:**
```java
messagingTemplate.convertAndSend("/topic/clase/" + mensaje.getClase().getId(), mensaje);
```

### MensajeWebSocketController
```java
@MessageMapping("/mensaje")
public void recibirMensaje(Mensaje mensaje) {
    mensajeService.enviarMensaje(mensaje);
}
```

## Frontend de prueba (HTML estático)
### chat-privado.html:

- HTML simple con campos para IDs de emisor y receptor y caja de mensajes.
- Se conecta a /ws y se suscribe a ambos canales (queue/chat/X).
- Muestra los mensajes recibidos en pantalla.
- Simula el comportamiento de WhatsApp sin autenticación.
**Suscripciones:**
```java
stompClient.subscribe("/user/" + emisorId + "/queue/chat/" + receptorId, callback);
stompClient.subscribe("/user/" + receptorId + "/queue/chat/" + emisorId, callback);
```

## Seguridad y acceso
WebSocket expuesto por el endpoint `/ws.`
El broker **STOMP** está configurado en `WebSocketConfig:`
  - `setUserDestinationPrefix("/user");`
  - `enableSimpleBroker("/topic", "/queue");`
  - `setApplicationDestinationPrefixes("/app");`


En SecurityConfig, se permite acceso público a los archivos HTML y al endpoint /ws:
```java
.requestMatchers("/", "/chat-privado.html", "/chat-grupal.html", "/ws/**").permitAll();
```

## Estado actual del módulo
- Funcionalidad: operativa.
- Se pueden enviar y recibir mensajes correctamente.
- Los mensajes se guardan correctamente en la base de datos.
### Posibles mejoras:
- Integrar WebSocket con el sistema de autenticación real.
- Mostrar mensajes históricos al cargar el chat.
- Validar que no se puedan enviar mensajes vacíos (ya implementado).
- Añadir timestamps, leer/recibido y mejoras visuales.
### Notas técnicas
- El chat se comporta como WhatsApp en cuanto a lógica:
- Cada conversación tiene su canal.
- Las conversaciones privadas solo las ven emisor y receptor.
- Las grupales son visibles para todos los inscritos en una clase.
- El módulo está aislado y fácilmente integrable con la interfaz real de la aplicación móvil/web en el futuro.


## Módulo de Chat Grupal (clases)

### Diferencias del chat privado

- Está orientado a conversaciones grupales entre un profesor y todos los alumnos inscritos en una clase concreta.
- Utiliza un solo canal compartido para la clase:  
  `/topic/clase/{id}`
- Cualquier mensaje enviado se emite a todos los suscriptores del canal de esa clase.
- No tiene campo receptor, sino que el mensaje está vinculado a una **Clase**.

### Estructura de datos (reutiliza `Mensaje`)

- Se establece `esGrupal = true`.
- El campo `clase` contiene el identificador de la clase (relación `ManyToOne`).

Ejemplo de mensaje grupal:

```json
{
  "contenido": "¡Hola a todos!",
  "esGrupal": true,
  "emisor": {
    "id": 3,
    "nombre": "Profesor Tomás"
  },
  "clase": {
    "id": 7
  },
  "fechaEnvio": "2025-04-22T17:00:00"
}
```

### Canal de comunicación WebSocket

- **Destino para recibir mensajes grupales:**  
  `/topic/clase/{id}`

- **En el frontend (JS/HTML), al entrar en un chat de clase:**

```javascript
stompClient.subscribe("/topic/clase/" + claseId, (msg) => {
  const mensaje = JSON.parse(msg.body);
  // Renderizar en pantalla...
});
```

- **Mensaje enviado desde el frontend:**

```javascript
stompClient.send("/app/mensaje", {}, JSON.stringify(mensaje));
```

Envío de mensajes grupales:

```java
if (mensaje.isEsGrupal()) {
    messagingTemplate.convertAndSend("/topic/clase/" + mensaje.getClase().getId(), mensaje);
}
```

### Notas técnicas

- El chat grupal es ideal para avisos generales, debates o coordinación entre grupos.
- Funciona de forma transparente para el usuario final: solo tiene que entrar en una clase para participar.
- Todos los mensajes se almacenan con su `claseId`, por lo que es posible consultar el historial si se desea.

### Estado actual

- **Funcionalidad completa y operativa.**
- Se reciben los mensajes en tiempo real correctamente.
- Está listo para integrarse con la interfaz real de alumno y profesor.

---

## En desarrollo / mejoras futuras

- Panel de control estadístico (asistencias, inscripciones)

---

## Autor
**Antonio Manuel Aragón Pérez** — 2º DAM (Curso 2024/2025)

---