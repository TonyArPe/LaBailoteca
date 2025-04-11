# Plantilla de Anteproyecto. 2º DAM-A
## Curso 24/25

### Nombre del Proyecto:
**La Bailoteca**  
*Gestión de Academia de Baile*

### **1.- Descripción del Proyecto.**
La Bailoteca es una aplicación diseñada para gestionar una academia de baile. Facilitará funciones como registro y autenticación de usuarios, gestión de cursos, administración de profesores, inscripción de alumnos y seguimiento de su progreso. Ofrecerá funcionalidades CRUD (Crear, Leer, Actualizar, Borrar) en todas sus secciones, con dos interfaces de usuario: una web y otra móvil, ambas conectadas a un backend.

### **2.- Estructura del Proyecto.**
- **Backend:**  
  Desarrollado en Java Spring Boot, gestionará la lógica del negocio, la comunicación con la base de datos, la seguridad (autenticación mediante Firebase) y expondrá una API RESTful para los frontends.
- **Frontend Web:**  
  Implementado en React con Vite, será la interfaz de usuario para la administración de la academia.
- **Aplicación Móvil:**  
  Desarrollada en Android con Kotlin, permitirá a los usuarios interactuar con la plataforma desde dispositivos móviles.
- **Base de Datos:**  
  PostgreSQL será el almacén de datos para usuarios, cursos, profesores, horarios, etc.
- **Sistema de Autenticación:**  
  Firebase gestionará la autenticación de usuarios de forma segura y escalable.

### **3.- Tecnologías Utilizadas.**
- **Backend:** Java Spring Boot
- **Frontend Web:** React con Vite
- **Aplicación Móvil:** Android (Kotlin)
- **Autenticación:** Firebase
- **Comunicación:** API RESTful
- **Otras:** Docker y docker-compose para la dockerización y despliegue del entorno.

### **4.- Base de Datos.**
- **Tipo:** Relacional.
- **SGBD Seleccionado:** PostgreSQL.
- **Ventajas para el Proyecto:**
  - **Integridad de datos:** Garantiza consistencia en la gestión de información mediante relaciones entre entidades.
  - **Compatibilidad y herramientas:** Compatible con frameworks de Java como Spring Boot y ofrece herramientas para administración y monitoreo.

### **5.- Casos de Uso, Descripción, Roles.**
![Diagrama de Casos de Uso Bailoteca](docs\DiagramaCasosDeUsoBailoteca.PNG)

### **6.- Despliegue y Control de Versiones.**
- **Despliegue:**  
- **Despliegue:**  
  La aplicación se dockerizará utilizando un archivo `docker-compose.yml`, lo que permitirá levantar todos los servicios necesarios (backend, base de datos, etc.) de forma sencilla.
- **Control de Versiones:**  
  Se utilizará Git para el control de versiones, especificando cada commit con un mensaje claro, trabajando con ramas y realizando commit y push de cada clase/módulo una vez testeado.

### **7.- Futuras Mejoras.**
- **Módulo de Gestión de Pagos:**  
  Actualmente todo el control de los pagos se realiza en efectivo y los mismos profesores llevan personalmente el control de los pagos de sus alumnos. Quizás se podría implementar un sistema de pago por Bizum y añadir el atributo `pagado` tipo Boolean.
- **Control de Analíticas:**  
  Desarrollar un panel de control (dashboard) para que tanto administradores como profesores puedan visualizar estadísticas relevantes, como número de inscripciones, asistencia a clases y tendencias de valoración.

### **8.- Integrantes Grupo.**
### Antonio Manuel Aragón Pérez

# COMPONENTES PRINCIPALES DEL PROYECTO
## Modelos de Datos y Endpoints

Esta sección detalla los modelos de datos principales y la propuesta inicial de endpoints que se utilizarán en la API RESTful, la cual será usada tanto por el frontend web (React Vite) como por la aplicación móvil (Android/Kotlin).

### Modelos de Datos

#### Usuario
- **Atributos:**
  - `id`: Identificador único.
  - `nombre`: Nombre completo del usuario.
  - `email`: Correo electrónico.
  - `contraseña`: Contraseña (gestión de autenticación se realizará mediante Firebase).
  - `rol`: Rol asignado (ADMIN, PROFESOR, USUARIO, INVITADO).
  - `fotoPerfil`: URL o ruta de la imagen de perfil.
  - `otrosDatos`: Información adicional (opcional, como dirección, teléfono, etc.).

#### Clase
- **Atributos:**
  - `id`: Identificador único.
  - `nombre`: Nombre de la clase.
  - `descripción`: Descripción de la clase.
  - `horario`: Día y hora fija de la clase.
  - `profesorId`: Identificador del profesor asignado.
  - `videoPresentacion`: URL del video de presentación de la clase.
  - `calendario`: Datos visuales de la programación (para mostrar en el perfil del profesor).

#### Inscripción
- **Atributos:**
  - `id`: Identificador único.
  - `usuarioId`: Identificador del usuario inscrito.
  - `claseId`: Identificador de la clase.
  - `estado`: Estado de la inscripción (activo, inactivo, habilitado, deshabilitado).
  - `pago`: Indicador de pago (pagado/noPagado).

#### Evento
- **Atributos:**
  - `id`: Identificador único.
  - `claseId`: Identificador de la clase a la que pertenece.
  - `título`: Título del evento.
  - `descripción`: Descripción detallada.
  - `fecha`: Fecha y hora del evento.
  - `imagen`: URL o ruta de la imagen representativa.

#### Contenido Adicional
- **Atributos:**
  - `id`: Identificador único.
  - `claseId`: Identificador de la clase asociada.
  - `tipo`: Tipo de contenido (video, documento, etc.).
  - `url`: Ubicación del contenido.
  - `descripción`: Descripción o título del contenido.

### Endpoints Propuestos

#### Autenticación y Gestión de Usuarios

| Método | Endpoint                  | Descripción                                                         |
|--------|---------------------------|---------------------------------------------------------------------|
| POST   | `/api/auth/register`      | Registrar un nuevo usuario                                          |
| POST   | `/api/auth/login`         | Autenticación e inicio de sesión                                    |
| GET    | `/api/users`              | Obtener lista de usuarios (acceso restringido para administradores) |
| GET    | `/api/users/{id}`         | Obtener la información de un usuario específico                     |
| PUT    | `/api/users/{id}`         | Actualizar los datos del usuario (modificación de perfil)           |
| DELETE | `/api/users/{id}`         | Eliminar un usuario (solo para administradores)                      |

#### Clases

| Método | Endpoint                  | Descripción                                                           |
|--------|---------------------------|-----------------------------------------------------------------------|
| GET    | `/api/classes`            | Obtener la lista de clases disponibles                                |
| GET    | `/api/classes/{id}`       | Obtener detalles de una clase en particular                           |
| POST   | `/api/classes`            | Crear una nueva clase (acceso de administrador o profesor)            |
| PUT    | `/api/classes/{id}`       | Actualizar la información de una clase (acceso de administrador o profesor) |
| DELETE | `/api/classes/{id}`       | Eliminar una clase (solo administradores)                             |

#### Inscripciones

| Método | Endpoint                       | Descripción                                                                  |
|--------|--------------------------------|------------------------------------------------------------------------------|
| GET    | `/api/inscriptions`            | Obtener la lista de inscripciones (filtradas según rol)                      |
| POST   | `/api/inscriptions`            | Inscribirse en una clase                                                     |
| PUT    | `/api/inscriptions/{id}`       | Actualizar el estado de la inscripción (habilitar o deshabilitar participación)|
| DELETE | `/api/inscriptions/{id}`       | Cancelar una inscripción                                                     |

#### Eventos y Contenidos

| Método | Endpoint                      | Descripción                                                         |
|--------|-------------------------------|---------------------------------------------------------------------|
| GET    | `/api/events`                 | Obtener la lista de eventos                                         |
| GET    | `/api/events/{id}`            | Obtener detalles de un evento específico                            |
| POST   | `/api/events`                 | Crear un nuevo evento (función para profesores)                     |
| PUT    | `/api/events/{id}`            | Actualizar un evento (acceso para profesores)                       |
| DELETE | `/api/events/{id}`            | Eliminar un evento (acceso para profesores)                         |
| GET    | `/api/contents`               | Obtener los contenidos adicionales asociados a una clase            |
| POST   | `/api/contents`               | Subir un nuevo contenido (acceso para profesores)                   |
| PUT    | `/api/contents/{id}`          | Actualizar contenido (acceso para profesores)                       |
| DELETE | `/api/contents/{id}`          | Eliminar contenido (acceso para profesores)                         |

#### Funcionalidades Adicionales

**Notificaciones:**

| Método | Endpoint                      | Descripción                                                         |
|--------|-------------------------------|---------------------------------------------------------------------|
| GET    | `/api/notifications`          | Obtener notificaciones para el usuario                              |
| POST   | `/api/notifications`          | Crear una notificación (generada por el sistema o por acciones de profesores) |

**Chat Interno:**

| Método | Endpoint                   | Descripción                                                         |
|--------|----------------------------|---------------------------------------------------------------------|
| GET    | `/api/chat`                | Obtener las conversaciones de chat (por usuario o clase)             |
| POST   | `/api/chat`                | Enviar un mensaje en el chat                                         |

## CREACION DEL PROYECTO(BACKEND)

### Usar la extensión de Spring Boot en VSCode

  Abre VSCode

  Instala la extensión **Spring Initializr Java Support**

  Pulsa `Ctrl + Shift + P` y escribimos:

`Spring Initializr: Generate a Maven Project`

  Group: `com.bailoteca`

  Artifact: `bailoteca-backend`

  Lenguaje: `Java`

  Java version: `17`

  Dependencias: `Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok`

## Tecnologías utilizadas

- **Lenguaje**: Java 17
- **Framework**: Spring Boot
- **Build Tool**: Maven
- **Base de datos**: PostgreSQL
- **ORM**: Spring Data JPA
- **Dependencias principales**:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `postgresql`
  - `lombok`
  - `spring-boot-devtools`

---

## Estructura del repositorio

- `master` → rama principal de producción
- `dev` → rama de desarrollo general
- `backend` → desarrollo del backend Spring Boot
- `frontend` → desarrollo web con React
- `app` → desarrollo de la app móvil en Kotlin

---

## Estructura base del proyecto Spring Boot

```
bailoteca-backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── bailoteca/
│       │           └── BailotecaApplication.java
│       └── resources/
│           ├── application.properties
│           └── static/
├── .gitignore
├── pom.xml
```

## Configuración del entorno de desarrollo

### Contenedores Docker

Se utiliza Docker para gestionar PostgreSQL y pgAdmin:

- **PostgreSQL**: Base de datos principal.
- **pgAdmin**: Interfaz web para administrar la base de datos.

#### Variables del entorno (.env)

```env
POSTGRES_DB=bailoteca_db
POSTGRES_USER=bailo_admin
POSTGRES_PASSWORD=superseguro123
PGADMIN_DEFAULT_EMAIL=admin@bailoteca.com
PGADMIN_DEFAULT_PASSWORD=admin123
```
### Puertos utilizados

| Servicio   | URL de acceso                  | Puerto |
|------------|--------------------------------|--------|
| Backend    | [http://localhost:8080](http://localhost:8080) | 8080   |
| API REST   | [http://localhost:8080/api/usuarios](http://localhost:8080/api/usuarios) | -      |
| pgAdmin    | [http://localhost:5050](http://localhost:5050) | 5050   |

### Acceso a pgAdmin

1. Abre [http://localhost:5050](http://localhost:5050).
2. Inicia sesión con las siguientes credenciales:
  - **Correo:** `admin@bailoteca.com`
  - **Contraseña:** `admin123`
3. Añade un nuevo servidor con la siguiente configuración:
  - **Nombre:** `PostgreSQL Bailoteca`
  - **Host:** `db`
  - **Usuario:** `bailo_admin`
  - **Contraseña:** `superseguro123`


# Documentación de Pagos en La Bailoteca

## Objetivo

Registrar y consultar los pagos realizados por los usuarios de forma informativa, ya que los pagos se efectúan en efectivo y en persona. Este sistema permite a profesores y administradores llevar un control claro de:

- Participación en eventos.
- Pagos mensuales de clases.
- Usuarios que han abonado y cuándo lo hicieron.

## Estructura de Entidades

### PagoEvento

Pagos asociados a un evento.

| Campo      | Tipo     | Descripción                          |
|------------|----------|--------------------------------------|
| `id`       | `Long`   | Identificador único del pago         |
| `usuario`  | `Usuario`| Usuario que realizó el pago          |
| `evento`   | `Evento` | Evento al que corresponde el pago    |
| `fechaPago`| `Date`   | Fecha en la que se realizó el pago   |
| `cantidad` | `double` | Cantidad abonada en efectivo         |
| `pagado`   | `boolean`| Indica si el pago fue efectuado correctamente |

### PagoMensualidad

Pagos correspondientes a la mensualidad de clases.

| Campo      | Tipo     | Descripción                          |
|------------|----------|--------------------------------------|
| `id`       | `Long`   | Identificador único del pago         |
| `usuario`  | `Usuario`| Usuario que realizó el pago          |
| `mes`      | `String` | Mes de la mensualidad pagada         |
| `fechaPago`| `Date`   | Fecha en la que se realizó el pago   |
| `cantidad` | `double` | Cantidad abonada                     |
| `pagado`   | `boolean`| Estado del pago                      |

## Endpoints Disponibles

### `/api/pagos-evento`

| Método | Ruta                                | Descripción                          |
|--------|-------------------------------------|--------------------------------------|
| `GET`  | `/api/pagos-evento`                | Lista todos los pagos de eventos     |
| `GET`  | `/api/pagos-evento/usuario/{usuarioId}` | Lista pagos de un usuario            |
| `GET`  | `/api/pagos-evento/evento/{eventoId}` | Lista pagos por evento               |
| `POST` | `/api/pagos-evento`                | Crear nuevo registro de pago         |
| `DELETE`| `/api/pagos-evento/{id}`          | Eliminar un pago por su ID           |

### `/api/pagos-mensualidad`

| Método | Ruta                                | Descripción                          |
|--------|-------------------------------------|--------------------------------------|
| `GET`  | `/api/pagos-mensualidad`           | Lista todos los pagos mensuales      |
| `GET`  | `/api/pagos-mensualidad/usuario/{usuarioId}` | Lista pagos por usuario              |
| `GET`  | `/api/pagos-mensualidad/mes/{mes}` | Lista pagos por mes |
| `POST` | `/api/pagos-mensualidad`           | Crear nuevo pago mensual             |
| `DELETE`| `/api/pagos-mensualidad/{id}`     | Eliminar pago mensual por ID         |

## Notificaciones en Tiempo Real

Hemos implementado un sistema de notificaciones en tiempo real usando WebSockets + STOMP, pensado para informar a los usuarios registrados sobre eventos relevantes (por ejemplo, la creación de un nuevo evento).
### Funcionalidades disponibles

  - **Crear una notificación:** `(POST /api/notificaciones)`

  - **Obtener notificaciones de un usuario:** `(GET /api/notificaciones/usuario/{id})`

  - **Obtener notificaciones no leídas:** `(GET /api/notificaciones/usuario/{id}/noleidas)`

  - **Contar las notificaciones no leídas:** `(GET /api/notificaciones/usuario/{id}/contador-noleidas)`

  - **Marcar una notificación como leída:** `(PUT /api/notificaciones/{id}/leida)`

  - **Marcar todas como leídas:** `(PUT /api/notificaciones/usuario/{id}/marcar-todas-leidas)`

  - **Eliminar una notificación:** `(DELETE /api/notificaciones/{id})`

### Visualización en Tiempo Real

Creamos un HTML de prueba para visualizar las notificaciones entrantes:

  - Coloca el archivo notificaciones.html en src/main/resources/static/.

  - Accede a `http://localhost:8080/notificaciones.html`

  - Las notificaciones llegarán en tiempo real mediante WebSocket.

### Tecnología usada

  - WebSocket (STOMP)

  - Spring WebSocket

  - SockJS + StompJS en el frontend

## Inserción Automática de Datos Iniciales

Mediante la clase DataInitializer.java, se insertan automáticamente los datos
que se quieran insertar automaticamente.