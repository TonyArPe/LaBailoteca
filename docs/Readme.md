# Plantilla de Anteproyecto. 2º DAM-A
## Curso 24/25

### Nombre del Proyecto:
**La Bailoteca**  
*Gestión de Academia de Baile*

### **1.- Descripción del Proyecto.**
La Bailoteca es una aplicación diseñada para gestionar una academia de baile. La app facilitará todas las funciones necesarias para la administración de la academia, incluyendo el registro y autenticación de usuarios, la gestión de cursos, la administración de profesores, la inscripción de alumnos y el seguimiento de su progreso. La aplicación ofrecerá funcionalidades CRUD (Crear, Leer, Actualizar, Borrar) en todas sus secciones, proporcionando dos interfaces de usuario: una web y otra móvil, ambas conectadas a un backend.

### **2.- Estructura del Proyecto.**
- **Backend:**  
  Desarrollado en Java Spring Boot, este módulo se encargará de la lógica del negocio, la comunicación con la base de datos, la seguridad (integrando autenticación mediante Firebase) y la exposición de una API RESTful para el uso por los frontends.
- **Frontend Web:**  
  Implementado en React con Vite, proporcionará la interfaz de usuario para la administración y gestión de la academia.
- **Aplicación Móvil:**  
  Desarrollada en Android utilizando Kotlin, para dispositivos móviles, permitiendo a los usuarios interactuar con la plataforma a través de un app nativa.
- **Base de Datos:**  
  Realizada con PostgreSQL almacén de datos que se utilizará para guardar la información de usuarios, cursos, profesores, horarios, etc.
- **Sistema de Autenticación:**  
  Se integrará Firebase para gestionar la autenticación de usuarios de forma segura y escalable.

### **3.- Tecnologías Utilizadas.**
- **Backend:** Java Spring Boot
- **Frontend Web:** React con Vite
- **Aplicación Móvil:** Android (Kotlin)
- **Autenticación:** Firebase
- **Comunicación:** API RESTful
- **Otras:**  
  Se utilizará Docker y docker-compose para la dockerización y despliegue del entorno de la aplicación.

### **4.- Base de Datos.**
- **Tipo:** Relacional.
- **SGBD Seleccionado:** PostgreSQL.
- **Ventajas para el Proyecto:**
  - **Integridad de datos:** Permite definir relaciones (por ejemplo, entre usuarios, clases y profesores) que garantizan la consistencia en la gestión de información.
  - **Compatibilidad y herramientas:** Amplia compatibilidad con frameworks de Java (como Spring Boot) y disponibilidad de herramientas para administración y monitoreo.

### **5.- Casos de Uso, Descripción, Roles.**
![Diagrama de Casos de Uso Bailoteca](docs\DiagramaCasosDeUsoBailoteca.PNG)

### **6.- Despliegue y Control de Versiones.**
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