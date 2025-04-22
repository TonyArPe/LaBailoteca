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


### Pagos
- `/api/pagos-evento`
- `/api/pagos-mensualidad`

### Notificaciones
- `/api/notificaciones` + WebSocket STOMP con HTML de prueba incluido

---

## En desarrollo / mejoras futuras

- Sistema de roles más granular para profesores y alumnos
- Panel de control estadístico (asistencias, inscripciones)
- Endpoint para edición de perfil con seguridad reforzada

---

## Autor
**Antonio Manuel Aragón Pérez** — 2º DAM (Curso 2024/2025)

---