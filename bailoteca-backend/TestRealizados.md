# TEST del Backend de La Bailoteca

Divididas por cada modulo y como distintos tipos de usuario: **ADMIN**, **PROFESOR**, **USUARIO**.

Mayoria de pruebas las realizare en Postman.

## 1. Autenticación y Seguridad

- **Login correcto** con correo y contraseña (Admin, Profesor, Usuario).
- **Login incorrecto** (correo inválido, contraseña errónea, usuario desactivado...).
- Revisar que el **token JWT** se genera correctamente.
- Probar peticiones a endpoints protegidos **sin token** → debe dar `401 Unauthorized`.
- Probar peticiones con **token válido**, pero a recursos que no le pertenecen → debe dar `403 Forbidden`.
- Comprobar que `/api/usuarios/me` funciona con cualquier usuario autenticado → No deberia funcionar con `INVITADO`
- Validar que `@PreAuthorize` y la lógica manual en los controladores coinciden.

## 2. Gestión de Usuarios

- `GET /api/usuarios` → Solo accesible para **ADMIN**.
- `GET /api/usuarios/{id}` → Admin puede ver a cualquiera. Usuario solo puede ver su propio perfil.
- `PUT /api/usuarios/{id}` → Modificar su propio perfil (nombre, dirección, etc.).
- `DELETE /api/usuarios/{id}` → Eliminar su propia cuenta. Admin puede eliminar a cualquiera.
- `POST /api/usuarios` → Registro público o por el admin.
    - Verificar que la contraseña se guarda encriptada.

## 3. Clases

- `GET /api/clases` → Acceso público.
- `GET /api/clases/{id}` → Ver una clase específica.
- `GET /api/clases/buscar?nombre=Zumba` → Comprobar búsquedas.
- `GET /api/clases/profesor/{id}` → Ver clases por profesor.
- `POST /api/clases` → Crear clase (solo **ADMIN** o **PROFESOR**).
- `PUT /api/clases/{id}` → Editar clase (solo si es el dueño o **ADMIN**).
- `DELETE /api/clases/{id}` → Eliminar clase (solo si es el dueño o **ADMIN**).
    - Verificar que el campo profesor se asigna automáticamente al crear.

## 4. Eventos

- `GET /api/eventos` → Ver todos los eventos.
- `GET /api/eventos/{id}` → Ver detalle.
- `POST /api/eventos` → Crear evento (**ADMIN** o **PROFESOR**).
- `PUT /api/eventos/{id}` → Editar evento (solo organizador o admin).
- `DELETE /api/eventos/{id}` → Eliminar evento (solo organizador o admin).
    - Verificar persistencia y cambios de estado (ACTIVO, CANCELADO...).

## 5. Inscripciones

- `GET /api/inscripciones` → Ver todas las inscripciones.
- `GET /api/inscripciones/usuario/{id}` → Ver inscripciones de un alumno.
- `GET /api/inscripciones/clase/{id}` → Ver inscripciones de una clase.
- `POST /api/inscripciones` → Inscribirse (usuario).
    - Si ya está inscrito → debe devolver error.
- `DELETE /api/inscripciones/{id}` → Cancelar inscripción.

## 6. Pagos (Eventos y Mensualidad)

### Pago de Eventos

- `GET /api/pagos-evento` → Lista general.
- `GET /usuario/{id}` → Ver pagos de un usuario.
- `GET /evento/{id}` → Ver pagos por evento.
- `POST` → Crear pago.
- `DELETE /{id}` → Eliminar (solo admin o quien lo creó).

### Pago de Mensualidad

- `GET /api/pagos-mensualidad`
- `GET /usuario/{id}` / `GET /mes/{mes}`
- `POST`
- `DELETE /{id}`

**ASEGURAR ROLES**

## 7. Notificaciones

- `POST /api/notificaciones` → Crear notificación manual.
- `GET /usuario/{id}` → Ver sus notificaciones.
- `GET /usuario/{id}/noleidas` → Filtrar no leídas.
- `GET /usuario/{id}/contador-noleidas` → Contador.
- `PUT /{id}/leida` → Marcar como leída.
- `PUT /usuario/{id}/marcar-todas-leidas`
- `DELETE /{id}`

Probar recepción por **WebSocket** en `notificaciones.html`.

## 8. Sistema de Chat

### Chat Privado

- Probar el **chat privado** entre dos usuarios (prueba cruzada: A escribe, B responde).
- Comprobar que el mensaje se guarda en base de datos.
- Asegurar que se ven en tiempo real en ambos navegadores.
- Verificar que no se permite enviar mensajes vacíos.
- Probar recarga de la página y reenvío.
- Probar que se respeta emisor y receptor.
    - Verificar que no hay envio doble.

### Chat grupal:
- que funcione con `/topic/clase/{id}`.

---

# TEST REALIZADOS
## BLOQUE 1: Autenticación y Usuarios

**(✔️COMPLETADO)**

Incluye:

- Login con token JWT

- Registro de nuevos usuarios

- Acceso denegado a usuarios desactivados

- Activación manual del usuario

- Confirmación de login tras activación

## BLOQUE 2: Perfil de Usuario y Autogestión

**(✔️COMPLETADO)**

Incluye:

- Modificación de datos personales por parte del propio usuario (teléfono, dirección, fecha de nacimiento, etc.).

- Endpoint /api/usuarios/me para obtener el perfil del usuario autenticado.

- Protección para evitar que un usuario edite a otro sin permisos.

- Edición del perfil por parte del administrador (con token de ADMIN).

- Borrado lógico del usuario (activo = false) mediante método DELETE.

- Confirmación de que el borrado se respeta en el login y en los listados.

- Intentos maliciosos de modificar otros usuarios son bloqueados con 403.

- Restricción en campos no modificables como dni, controlado por omisión en el PUT.

## BLOQUE 3: Gestión de Clases

**(✔️COMPLETADO)**

Incluye:

- Visualización de clases disponibles (GET /api/clases)

- Visualización de clases por ID (GET /api/clases/{id})

    - Se detectó un problema de recursividad con horarioClases → [SOLUCIONADO]

- Filtrado por ID de profesor (GET /api/clases/profesor/{id})

- Creación de clases por parte de profesores (POST /api/clases)

   - Verificado correctamente con token de profesor.

- Actualización de clases por su profesor asignado (PUT /api/clases/{id})

- Borrado lógico de clases (DELETE /api/clases/{id}):

    - No autorizado sin token (401)

    - No autorizado con rol USUARIO (403)

    - Permitido con token del profesor que creó la clase

## BLOQUE 3.1: Gestión de Clases Por Roles

**(✔️COMPLETADO)**

Incluye pruebas con:

  - `Admin:` acceso total a todas las clases (crear, editar, eliminar).

  - `Profesor:` acceso limitado solo a sus propias clases.

  - `Usuario:` solo puede visualizar clases.

**Pruebas realizadas:**

- Ver todas las clases

- Ver clase por ID

- Crear clase (solo profesor o admin)

- Editar clase (solo profesor dueño o admin)

- Eliminar clase (solo profesor dueño o admin)

- Los usuarios no pueden crear, editar ni eliminar clases

## BLOQUE 4: Gestión de Inscripciones

**(✔️COMPLETADO)**

### Incluye:

- Inscripción del usuario autenticado a una clase (`POST /api/inscripciones?claseId=...`)
- Visualización de inscripciones por usuario (`GET /api/inscripciones/usuario/{id}`)
- Visualización de inscripciones por clase (`GET /api/inscripciones/clase/{id}`)
- Listado global de inscripciones (`GET /api/inscripciones`) (solo para **ADMIN**)
- Prevención de inscripciones duplicadas (`409 CONFLICT`)
- Eliminación de inscripción (`DELETE /api/inscripciones/{id}`):
  - El usuario puede borrar su propia inscripción.
  - El profesor puede borrar inscripciones de su clase.
  - El administrador puede borrar cualquier inscripción.

### Acceso por rol

| Acción                          | ADMIN | PROFESOR                  | USUARIO                  |
|---------------------------------|-------|---------------------------|--------------------------|
| Ver todas las inscripciones     | ✅     | ❌                         | ❌                        |
| Ver inscripciones de un usuario | ✅     | ❌                         | ✅ si es el propio        |
| Ver inscripciones por clase     | ✅     | ✅ si es el profesor asignado | ❌                        |
| Inscribirse a una clase         | ❌     | ❌                         | ✅                        |
| Eliminar inscripción            | ✅     | ✅ si es el profesor de la clase | ✅ si es suya            |

### Seguridad aplicada

- Validación de **JWT** en todas las operaciones.
- Comprobación de duplicidad al inscribirse.
- Filtro de acceso por rol y propiedad (usuario autenticado).
- Protección contra acceso no autorizado (`401`, `403`, `409` correctamente devueltos).

### Pruebas realizadas

- ✅ Inscribirse correctamente como usuario autenticado.
- ✅ Visualizar inscripciones propias.
- ✅ Ver inscripciones por clase (como profesor dueño y admin).
- ✅ Ver todas las inscripciones (como admin).
- ✅ Evitar duplicados (`409 Conflict`).
- ✅ Eliminar inscripción desde los tres roles posibles (usuario, profesor, admin).
- ✅ Comprobaciones de seguridad sin token y con rol incorrecto (`403 / 401`).