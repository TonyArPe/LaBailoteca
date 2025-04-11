# La Bailoteca — Proyecto Fullstack de Gestión de Academia de Baile

**La Bailoteca** es una aplicación fullstack desarrollada como Trabajo de Fin de Grado, destinada a la gestión integral de academias de baile. Combina un backend robusto en Java con Spring Boot y un frontend móvil en Android usando Kotlin y Jetpack Compose.

---

## Estructura del proyecto

```
LaBailoteca/
├── bailoteca-backend/   → Backend en Java (Spring Boot + PostgreSQL + Docker)
├── bailoteca-frontend/  → App Android nativa (Kotlin + Jetpack Compose)
├── docs/                → Documentación técnica y apuntes personales
└── README.md            → Documentación general del proyecto
```

---

## Tecnologías utilizadas

### Backend
- Java 21
- Spring Boot
- PostgreSQL
- Docker
- WebSockets (STOMP)
- Firebase Authentication (validación de usuarios móviles)
- Arquitectura modular por dominios

### Frontend móvil (Android)
- Kotlin
- Jetpack Compose
- MVVM Architecture
- Navigation Compose
- Material 3
- Firebase Authentication

---

## Funcionalidades principales (en desarrollo)

- Autenticación de usuarios mediante Firebase.
- Gestión de clases, eventos, profesores y horarios.
- Sistema de inscripción y pagos (mensualidad o evento).
- Notificaciones en tiempo real (WebSockets).
- Panel de administración y gestión de usuarios.
- App móvil con navegación, registro y consulta de clases/eventos.

---

## Instalación y ejecución

### Backend
```bash
cd bailoteca-backend
docker-compose up
```

Asegúrate de tener Docker instalado para levantar PostgreSQL automáticamente.

### Frontend Android
1. Abre `bailoteca-frontend/` con Android Studio.
2. Compila y ejecuta en un emulador o dispositivo real.

---

## Documentación

Puedes encontrar apuntes personales, decisiones técnicas y explicaciones paso a paso en la carpeta `/docs`, incluyendo:

- Apuntes de frontend Android.
- Documentación técnica del backend.
- Estructura de base de datos.
- Justificación de arquitectura.

---

## Autor

Desarrollado por **Antonio Manuel Aragón Pérez**, alumno de 2º DAM, como parte del TFG.

---

## Licencia

Este proyecto es de uso académico y no está destinado a producción. Derechos reservados © 2025.

---