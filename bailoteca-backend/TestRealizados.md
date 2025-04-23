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