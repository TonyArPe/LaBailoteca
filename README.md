# La Bailoteca — Proyecto Fullstack de Gestión de Academia de Baile

**La Bailoteca** es una aplicación fullstack desarrollada como Trabajo de Fin de Grado (TFG), destinada a la gestión integral de academias de baile. El sistema combina un backend robusto en Java con Spring Boot y una aplicación Android nativa en Kotlin con Jetpack Compose.

---

## Estructura del repositorio

```
LaBailoteca/
├── bailoteca-backend/     # Backend en Java (Spring Boot + PostgreSQL + Docker)
├── bailoteca-frontend/    # App Android (Kotlin + Jetpack Compose)
├── docs/                  # Documentación técnica y apuntes personales
└── README.md              # Este archivo: presentación general
```

---

## Tecnologías principales

### Backend
- Java 21
- Spring Boot
- PostgreSQL + Docker
- JWT Authentication
- Firebase (validación de usuarios móviles)
- Arquitectura modular por dominio

### App Android
- Kotlin + Jetpack Compose
- MVVM Architecture
- Navigation Compose
- Firebase Authentication
- Retrofit + JWT

---

## Funcionalidades destacadas

- Registro, login y seguridad basada en roles (`ADMIN`, `PROFESOR`, `USUARIO`, `INVITADO`).
- Gestión de clases, eventos, inscripciones y pagos.
- CRUD de usuarios, asignación de profesores, horarios y contenido.
- Notificaciones en tiempo real (WebSockets).
- App móvil funcional conectada al backend mediante JWT.

---

## Estado actual

- Backend funcional con seguridad y API REST completa
- App Android conectada a Firebase y al backend (login, usuarios)
- Módulo web pendiente de desarrollo (previsto en React + Vite)

---

## Autor

Desarrollado por **Antonio Manuel Aragón Pérez**, alumno de 2º DAM, como parte del Trabajo de Fin de Grado (2025).

---

## Documentación técnica

- [Backend](bailoteca-backend/README.md)
- [App Android](bailoteca-frontend/README.md)
- [docs/](docs/): diagramas, apuntes y decisiones técnicas

---

## Licencia

Proyecto con fines educativos. No destinado a producción de momento.

---