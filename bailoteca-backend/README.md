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

### Clases y eventos
- `GET /api/clases`
- `GET /api/eventos`
- `POST /api/clases` (profesores)

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