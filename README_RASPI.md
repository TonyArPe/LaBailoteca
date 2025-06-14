# CONFIGURACIÓN DE TÚNEL PÚBLICO CON NGROK EN BANANA PI PARA BACKEND LOCAL

Aquí se documenta cómo exponer un backend Spring Boot local desde un **Banana Pi** a través de internet usando **Ngrok**. Desde el principio se trabajó con un Banana Pi. Para confirmar el modelo miramos la placa de la cual se compone abriendo la cajetilla que la contiene. A continuación, se detalla la solución técnica y profesional para permitir una URL pública siempre actualizada, ideal para pruebas móviles o acceso remoto.

## CONTEXTO INICIAL

El objetivo era acceder al backend local desde fuera de la red doméstica (por ejemplo, usando datos móviles), especialmente para demostraciones. Para ello, se optó por un túnel público con Ngrok.

## OBJETIVO

Exponer el backend local (Java Spring Boot) que corre en un Banana Pi mediante un túnel seguro, accesible desde cualquier lugar usando una URL del tipo `https://...ngrok-free.app`, y permitir que esta URL se actualice automáticamente en los clientes que la consumen.

## HARDWARE UTILIZADO

* Banana Pi (modelo confirmado como `Banana Pi BPI-M1` tras inspección física de la placa)
* Tarjeta microSD con sistema pregrabado con adaptador SD
* Cable MicroUSB para alimentación
* Conexión por Ethernet a router doméstico

## PROBLEMAS INICIALES

* **Sin interfaz gráfica:** El dispositivo no mostraba señal HDMI y no se podía usar directamente.
* **Dificultades de conexión:** La red no detectaba inicialmente el dispositivo.
* **Sin acceso vía ping o SSH inicialmente.**

## FASE DE PRUEBA Y DIAGNÓSTICO

* Se grabó la SD con Raspberry Pi OS Lite.
* Se habilitó SSH y Wi-Fi por configuración previa en la SD.
* Se monitoreó el router hasta que apareció un nuevo host (`bananaapi`) con IP local `192.168.2.107`.

## CONFIGURACIÓN EFECTIVA PASO A PASO

1. **Verificar el backend:**
   El backend Spring Boot se ejecuta correctamente en el puerto `8080`.

2. **Instalación de Ngrok:**

   ```bash
   choco install ngrok
   ```

3. **Autenticación en Ngrok:**

   ```bash
   ngrok config add-authtoken TU_AUTHTOKEN
   ```

4. **Lanzar túnel Ngrok:**

   ```bash
   ngrok http 8080
   ```

   Esto genera una URL pública como:

   ```
   Forwarding https://xxxx-xx-xx-xxx.ngrok-free.app -> http://localhost:8080
   ```

5. **Automatización de la URL (profesional):**

   Para evitar tener que cambiar manualmente la URL cada vez, se propone el siguiente sistema automatizado:

   * Ngrok expone su configuración en tiempo real en `http://localhost:4040/api/tunnels`.
   * Un script en Python puede consultar esta URL y guardar la `BASE_URL` en un archivo `base_url.txt`.

   **Script Python recomendado:**

   ```python
   import requests

   def obtener_url_ngrok():
       try:
           res = requests.get("http://127.0.0.1:4040/api/tunnels")
           tunnels = res.json()["tunnels"]
           for t in tunnels:
               if t["proto"] == "https":
                   return t["public_url"]
       except Exception as e:
           print("Error obteniendo la URL de Ngrok:", e)

   if __name__ == "__main__":
       url = obtener_url_ngrok()
       if url:
           with open("base_url.txt", "w") as f:
               f.write(url)
           print("URL actual de Ngrok guardada:", url)
   ```

   **Automatizar con cron:**

   ```bash
   crontab -e
   @reboot /usr/bin/python3 /home/banana/actualizar_base_url.py
   ```

## VALIDACIÓN DE FUNCIONAMIENTO

* Acceso al backend desde redes externas vía la URL Ngrok.
* Validación de endpoints, autenticación con Firebase, y persistencia funcional.
* URL dinámica correctamente gestionada sin intervención manual.

## CONCLUSIONES

* El Banana Pi ha demostrado ser un entorno robusto para backend expuesto públicamente con Ngrok.
* Automatizar la sincronización de la URL mejora enormemente la experiencia de desarrollo y demostración.
* Esta solución es escalable y aplicable a otros dispositivos y microservicios locales.

## RECURSOS ÚTILES

* [Ngrok Dashboard](https://dashboard.ngrok.com/)
* [Documentación oficial Ngrok](https://ngrok.com/docs)
* Logs Ngrok:

  ```powershell
  type C:\Users\<TU_USUARIO>\ngrok.log
  ```

> ⚠️ Recomendación: usar `.env`, `base_url.txt` o `Firebase Remote Config` en clientes Android para consumo automático de la URL sin hardcodear en código fuente.