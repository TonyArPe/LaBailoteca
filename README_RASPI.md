# CONFIGURACIÓN DE TÚNEL PÚBLICO CON NGROK EN BANANA PI PARA BACKEND LOCAL

Aqui expongo un backend Spring Boot local desde un Banana Pi a través de internet usando Ngrok. Aunque inicialmente se pensó usar una Raspberry Pi, se descubrió que era una Banana Pi abriendo la cajetilla y viendo y analizando la placa comparandola con una placa de una Raspberry. Aquí se detallan los problemas encontrados y sus soluciones para lograr una app Android funcional desde cualquier red.

## CONTEXTO INICIAL

El objetivo era acceder al backend local desde fuera de la red doméstica (por ejemplo, usando datos móviles), especialmente para demostraciones. Para ello, se optó por un túnel público con Ngrok.

## OBJETIVO

Exponer el backend local (Java Spring Boot) que corre en un Banana Pi mediante un túnel seguro, accesible desde cualquier lugar usando una URL del tipo `https://...ngrok-free.app`.

## HARDWARE UTILIZADO

- Banana Pi (modelo no identificado al principio, creo que es `Banana Pi BPI-M1`)
- Tarjeta microSD con sistema pregrabado
- Cable MicroUBS para alimentación
- Conexión por Ethernet a router doméstico

## PROBLEMAS INICIALES

- **Confusión con el modelo:** Se pensaba que era una Raspberry Pi, pero el análisis físico de la placa revelaron que era una Banana Pi.
- **Sin pantalla ni teclado:** El dispositivo no mostraba señal por HDMI ni ofrecía forma gráfica de interactuar.
- **Dificultades de conexión:** Aunque la microSD estaba bien insertada y la luz roja encendida, no aparecía como dispositivo conocido en el router.
- **Sin acceso vía ping o SSH al principio.**

## FASE DE PRUEBA Y DIAGNÓSTICO

Se realizaron los siguientes intentos:

- Se grabó la SD con Raspberry Pi OS Lite (por si era compatible).
- Se agregó el archivo `ssh` y la configuración `wpa_supplicant.conf` con la configuracion de mi router domestico.
- Se monitorizó la red desde el router.
- Finalmente, se detectó un nuevo dispositivo `bananaapi` con IP local `192.168.2.107`.

## CONFIGURACIÓN EFECTIVA PASO A PASO

1. **Identificación del backend:**  
  Se confirmó que el backend Spring Boot corría en el puerto 8080 en el Banana Pi.

2. **Instalación de Ngrok:**  
  Desde el Banana Pi o el equipo principal donde corre el backend:
  ```bash
  choco install ngrok
  ```

  Y despues confirmamos con `y`

3. **Registro en Ngrok:**  
  - Crear una cuenta en [ngrok.com](https://ngrok.com)(Usada cuenta GitHub)
  - Acceder a la sección *Your Authtoken* desde el dashboard.
  - Copiar el token y ejecutar:
    ```bash
    ngrok config add-authtoken TU_AUTHTOKEN
    ```

4. **Lanzar el túnel:**  
  Una vez autenticado:
  ```bash
  ngrok http 8080
  ```
  Esto generará una salida como:
  ```
  Forwarding https://xxxx-xx-xx-xxx.ngrok-free.app -> http://localhost:8080
  ```
  Esta URL pública será la que debe usarse como `BASE_URL` en la app Android en la clase `NetworkModule`.

## VALIDACIÓN DE FUNCIONAMIENTO

- Se pudo acceder al backend mediante la URL pública generada por Ngrok desde datos móviles y otras redes.
- El backend respondió correctamente a las peticiones, validando tokens Firebase y sirviendo datos.

## CONCLUSIONES

- Aunque inicialmente se planificó el uso de una Raspberry Pi, el proceso fue adaptable al uso de un Banana Pi.
- El túnel público con Ngrok permite acceso seguro y temporal al backend desde cualquier lugar.

## RECURSOS ÚTILES

- [Ngrok Dashboard](https://dashboard.ngrok.com/)
- [Documentación oficial Ngrok](https://ngrok.com/docs)
- Ver logs de Ngrok:
  ```powershell
  type C:\Users\<TU_USUARIO>\ngrok.log
  ```
  > Cambia `<TU_USUARIO>` por tu nombre de usuario de Windows.

## ESTADO FINAL

El sistema está completamente funcional para exposición remota del backend. El Banana Pi inicia y se conecta automáticamente por red local, y Ngrok expone el backend por una URL pública utilizable desde cualquier red o red móvil.

> **Recomendación:** Usar un archivo `.env` o cambiar el `BASE_URL` mediante variable de entorno si el proyecto pasa a producción.

