# README.md - Despliegue completo de La Bailoteca en Banana Pi

Este documento explica paso a paso cómo restaurar y desplegar completamente el sistema **La Bailoteca** en un entorno real utilizando una Banana Pi M1. Incluye desde la configuración base del hardware, instalación de dependencias, despliegue del backend con tunelado seguro, configuración del frontend Android con Firebase, hasta detalles de sesión, seguridad y resolución de errores frecuentes.

> **IMPORTANTE:** Este README está diseñado como guía maestra para restablecer el proyecto en cualquier momento desde cero, sin dejar cabos sueltos.

---

## Índice

- [README.md - Despliegue completo de La Bailoteca en Banana Pi](#readmemd---despliegue-completo-de-la-bailoteca-en-banana-pi)
  - [Índice](#índice)
  - [1. Requisitos previos](#1-requisitos-previos)
    - [Hardware:](#hardware)
    - [Software:](#software)
  - [2. Flasheo de la imagen del sistema operativo](#2-flasheo-de-la-imagen-del-sistema-operativo)
  - [3. Primera conexión y configuración por SSH](#3-primera-conexión-y-configuración-por-ssh)
  - [4. Instalación de Java, Git, Maven, Ngrok y configuración de entorno](#4-instalación-de-java-git-maven-ngrok-y-configuración-de-entorno)
  - [5. Clonación del proyecto y estructura general](#5-clonación-del-proyecto-y-estructura-general)
  - [6. Configuración del backend de Spring Boot](#6-configuración-del-backend-de-spring-boot)
    - [Editar `application.properties`](#editar-applicationproperties)
    - [Crear base de datos en PostgreSQL](#crear-base-de-datos-en-postgresql)
    - [Ejecutar](#ejecutar)
  - [7. Ejecución del backend y publicación con Ngrok](#7-ejecución-del-backend-y-publicación-con-ngrok)
  - [8. Configuración del frontend Android (Jetpack Compose + Firebase)](#8-configuración-del-frontend-android-jetpack-compose--firebase)
  - [9. Manejo de sesiones, tokens y persistencia](#9-manejo-de-sesiones-tokens-y-persistencia)
  - [10. Errores frecuentes y solución de problemas](#10-errores-frecuentes-y-solución-de-problemas)
  - [11. Notas adicionales y recomendaciones](#11-notas-adicionales-y-recomendaciones)

---

## 1. Requisitos previos

### Hardware:

* Banana Pi M1 (o similar)
* Tarjeta microSD mínima de 16GB clase 10
* Cable Ethernet
* Fuente de alimentación estable
* PC anfitrión con Windows 10 o superior

### Software:

* [Etcher](https://www.balena.io/etcher/) o Raspberry Pi Imager
* Imagen Debian/Ubuntu para Banana Pi M1 (revisar compatibilidad con headless boot y SSH)
* Acceso a [Ngrok](https://ngrok.com/) (plan gratuito)

---

## 2. Flasheo de la imagen del sistema operativo

1. **Descargar imagen recomendada**: Utilizar una versión headless estable (por ejemplo: `Armbian 22.05 Bananapi Ubuntu 20.04 Focal`).
2. **Grabar en SD con Etcher**:

   * Seleccionar imagen `.img` o `.iso`
   * Elegir unidad SD
   * Clic en "Flash"
3. **Habilitar SSH automáticamente**:

   * Montar partición `boot` tras flasheo
   * Crear archivo vacío llamado `ssh` (sin extensión)
   * (Opcional) crear archivo `user-data` para configuración cloud-init

---

## 3. Primera conexión y configuración por SSH

1. **Insertar SD en Banana Pi y conectar por Ethernet**
2. **Detectar IP local desde router (Tenda, FritzBox, etc)**
3. **Conectarse vía SSH desde el PC:**

```bash
ssh root@192.168.2.107
```

4. **Actualizar paquetes básicos:**

```bash
apt update && apt upgrade -y
```

5. **Cambiar contraseña de root si lo deseas:**

```bash
passwd
```

---

## 4. Instalación de Java, Git, Maven, Ngrok y configuración de entorno

```bash
# Java 17 OpenJDK
apt install openjdk-17-jdk -y

# Git y Maven
apt install git maven -y

# Ngrok
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-arm.zip
unzip ngrok-stable-linux-arm.zip -d /usr/local/bin
chmod +x /usr/local/bin/ngrok

# Autenticación con token de Ngrok
ngrok config add-authtoken TU_TOKEN_PERSONAL
```

> Si tienes el error de múltiples agentes activos de Ngrok, ejecuta:

```bash
pkill -f ngrok
```

---

## 5. Clonación del proyecto y estructura general

```bash
# Crear carpeta de trabajo
mkdir /opt/bailoteca && cd /opt/bailoteca

# Clonar desde GitHub (o copiar por SCP)
git clone https://github.com/tuusuario/bailoteca.git .

# Estructura del proyecto:
- bailoteca-backend/     --> Spring Boot
- bailotecaappfrontend/  --> Android (Kotlin Compose)
- README_RASPI.md        --> Guía de despliegue
```

---

## 6. Configuración del backend de Spring Boot

### Editar `application.properties`

```properties
server.address=0.0.0.0
server.port=8080

spring.datasource.username=bailo_admin
spring.datasource.password=superseguro123
spring.datasource.url=jdbc:postgresql://localhost:5432/bailoteca_db
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

jwt.secret=my-super-secret-key-for-jwt-signing-labailoteca123456789
jwt.expiration=86400000
```

### Crear base de datos en PostgreSQL

```bash
sudo -u postgres psql
CREATE DATABASE bailoteca_db;
CREATE USER bailo_admin WITH ENCRYPTED PASSWORD 'superseguro123';
GRANT ALL PRIVILEGES ON DATABASE bailoteca_db TO bailo_admin;
```

### Ejecutar

```bash
cd bailoteca-backend
./mvnw clean spring-boot:run
```

Verás:

```
Tomcat started on port 8080
FIREBASE --> Inicializado correctamente
```

---

## 7. Ejecución del backend y publicación con Ngrok

```bash
ngrok http 8080
```

Verás una salida como:

```
Forwarding: https://723f-84-122-0-141.ngrok-free.app -> http://localhost:8080
```

**Copia esta URL para usarla en el frontend (Firebase Remote Config).**

> ⚠️ Si ves `502 Bad Gateway`, asegúrate de que el backend esté arrancado y que estás usando la URL correcta.

---

## 8. Configuración del frontend Android (Jetpack Compose + Firebase)

1. **Android Studio + Hilt + Jetpack Compose**

2. **Firebase Console**:

   * Añadir app Android
   * Descargar `google-services.json`
   * Activar autenticación por email/password
   * Activar Remote Config y añadir clave `BASE_URL`

3. **SplashScreen.kt:**

   * Ejecuta `fetchAndActivate()` antes de navegar
   * Bloquea la navegación hasta obtener la URL dinámica

4. **SesionViewModel y SesionManager:**

   * Sincroniza sesión desde DataStore
   * Renueva el token de Firebase si cambia
   * Carga automáticamente inscripciones del usuario autenticado

5. **Errores comunes evitados:**

   * Token de otro usuario mezclado (403)
   * Usuario persistido diferente al de Firebase
   * URLs antiguas tras reinicio de la app (solucionado en `SplashScreen`)

---

## 9. Manejo de sesiones, tokens y persistencia

* Se usa Firebase para la autenticación
* El token JWT se extrae del `FirebaseUser`
* La clase `SesionManagerSingleton` guarda el `token`, `usuario` y `expiración`
* Se usa `TokenPreferences` y `UsuarioPreferences` vía DataStore
* Al iniciar sesión:

  1. Se limpia la sesión previa (`cerrarSesion()`)
  2. Se guarda el nuevo token
  3. Se recupera el usuario con `/me`
  4. Se cargan las inscripciones (`GET /inscripciones/usuario/{id}`)

---

## 10. Errores frecuentes y solución de problemas

| Error                                      | Causa común                                | Solución                                                                    |
| ------------------------------------------ | ------------------------------------------ | --------------------------------------------------------------------------- |
| `403 Forbidden al consultar inscripciones` | Token válido pero ID de usuario incorrecto | Cerrar sesión previa antes de iniciar nueva                                 |
| `No se encuentra editProfile`              | Falta en la `NavGraph`                     | Añadir la ruta `editProfile` en `NavHost`                                   |
| `Bad Gateway (502)` en Ngrok               | Backend no arrancado o crash               | Asegurarse de que `Spring Boot` se ejecuta en puerto 8080                   |
| `Token Firebase nulo`                      | Usuario no autenticado aún                 | Comprobar con `FirebaseAuth.getInstance().currentUser` antes de pedir token |

---

## 11. Notas adicionales y recomendaciones

* **Siempre cerrar sesión previa antes de un login nuevo.**
* **Configura Firebase Remote Config correctamente para actualizar la `BASE_URL`.**
* **Evita que `HomeScreen` vuelva a cargar sesión después de login con un nuevo usuario.**
* **Usa `.filterNotNull().first` en Flows para evitar estados inválidos.**
* **Verifica que el ID de usuario que pasas en endpoints sea el mismo que el que está logueado.**
* **Nunca mezcles tokens de Firebase con usuarios diferentes.**

---
