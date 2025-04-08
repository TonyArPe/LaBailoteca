
# Estructura del Proyecto y Pasos para el Desarrollo de La Bailoteca - Backend

## Estructura de carpetas recomendada para Spring Boot:

com.bailoteca
│
├── controller         -> Controladores REST (exponen la API)
├── service            -> Servicios con la lógica de negocio
├── repository         -> Interfaces que extienden JpaRepository
├── model              -> Entidades JPA (Usuario, Clase, etc.)
├── dto                -> Clases DTO (opcional, para datos de entrada/salida)
├── config             -> Configuraciones de seguridad, CORS, etc.
└── BailotecaApplication.java -> Clase principal

---

## Pasos para el desarrollo del backend

### 1. Crear el proyecto Spring Boot
- Usar Spring Initializr
- Añadir dependencias: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Security (más adelante), Lombok

### 2. Configurar la conexión a PostgreSQL
- Editar `application.properties` o `application.yml`
- Añadir URL, username y password de tu BD local

### 3. Crear las entidades (model)
- `Usuario`, `Clase`, `Inscripcion`, `HorarioClase`, etc.
- Usar anotaciones `@Entity`, `@Id`, `@ManyToOne`, `@OneToMany`...

### 4. Crear los repositorios
- Crear interfaces que extiendan `JpaRepository<T, ID>`

### 5. Crear los servicios
- Lógica de negocio con `@Service`
- Inyección de repositorios con `@Autowired`

### 6. Crear los controladores
- `@RestController` para exponer los endpoints
- Uso de rutas como `/usuarios`, `/clases`, etc.

### 7. Probar la API con Postman
- Comprobar que se pueden crear, leer, actualizar y borrar entidades

### 8. Documentar el backend (opcional)
- Usar Swagger (springdoc-openapi) para documentación automática

---

## Siguientes pasos una vez todo funcione:
- Integrar Firebase Authentication
- Añadir subida de imágenes (multipart)
- Crear las notificaciones automáticas
- Configurar roles y permisos
- Desplegar con Docker

