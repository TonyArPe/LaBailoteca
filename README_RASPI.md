# Raspberry Pi como Túnel Remoto para Backend

Este documento explica cómo configurar una Raspberry Pi para que funcione como túnel público hacia un backend local usando `ngrok`. El objetivo es que, al encender la Raspberry en casa, se conecte sola a la red Wi-Fi, habilite SSH y exponga tu backend automáticamente, sin teclado ni monitor.

---

## Requisitos

### Hardware
- Raspberry Pi (modelo 3, 4 o superior)
- Tarjeta microSD (mínimo 8GB)
- Fuente de alimentación (5V, 2.5A o superior)
- Cable microUSB o USB-C para alimentación

### Software necesario
- [Raspberry Pi Imager](https://www.raspberrypi.com/software/)
- [ngrok](https://ngrok.com/)
- (Opcional) Notepad++ para editar archivos sin errores de codificación

---

## PASO 1: Grabar Raspberry Pi OS en la microSD

1. Abre **Raspberry Pi Imager**.
2. Selecciona:
    - `Raspberry Pi OS Lite (32-bit)`
    - Las versiones de 64-bit muchas veces no crean correctamente
      las particiones para Windows
3. Pulsa `Ctrl + Shift + X` para abrir la configuración avanzada.
4. Configura:
    - Nombre de host: `raspberrypi`
    - Habilitar SSH (con contraseña)
    - Usuario: `pi` / Contraseña: la que prefieras
    - Configura Wi-Fi:
      - SSID: Tu red de casa
      - Contraseña: Tu contraseña Wi-Fi
      - País: `ES`
    - Omitir asistente de inicio
    - Guardar ajustes personalizados
5. Escribe la imagen en la SD.

---

## PASO 2: Preparar archivos en la partición `boot`

Una vez grabada la imagen, reinserta la tarjeta SD en el PC. Aparecerá una partición llamada `boot` (o `bootfs`).

### Archivos que debes colocar ahí:

#### `ssh` (activa el servidor SSH)
- Crea un archivo vacío llamado `ssh` (sin extensión).
- Desde Notepad++ o Bloc de notas:
  - Guardar como: `ssh`
  - Tipo: Todos los archivos
  - Codificación: ANSI

#### `wpa_supplicant.conf` (conexión a Wi-Fi)
Guarda el siguiente contenido como `wpa_supplicant.conf` en la raíz de `boot`:

```conf
country=ES
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

network={
     ssid="NombreDeTuWiFi"
     psk="ContraseñaDeTuWiFi"
}
```

#### `firstboot.sh` (script para instalar ngrok y lanzar túnel)
Guarda el siguiente script como `firstboot.sh` en la raíz de la SD:

```bash
#!/bin/bash
cd /home/pi

# Instalar ngrok
wget https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-arm.zip
unzip ngrok-stable-linux-arm.zip
chmod +x ngrok
sudo mv ngrok /usr/local/bin

# Autenticar ngrok
ngrok config add-authtoken TU_TOKEN_NGROK

# Lanzar túnel
ngrok http 8080 > /home/pi/ngrok.log &
```

- Reemplaza `TU_TOKEN_NGROK` por tu token personal de [ngrok](https://dashboard.ngrok.com/get-started/setup).
- Codificación: ANSI o UTF-8 sin BOM
- Fin de línea: LF (Unix)

---

## PASO 3: Encender la Raspberry en casa

1. Inserta la tarjeta SD en la Raspberry Pi.
2. Conéctala a la corriente (microUSB o USB-C).
3. Espera 1-2 minutos.

**¿Qué ocurre automáticamente?**
- Se conecta a la red Wi-Fi configurada.
- Activa el servicio SSH.
- Si configuras el script en `rc.local`, ejecuta el túnel automáticamente.

---

## PASO 4 (opcional): Acceder por SSH desde tu PC

Desde otro PC conectado a la misma red:

```sh
ssh pi@raspberrypi.local
# o con la IP local
ssh pi@192.168.1.XXX
```

Si es la primera vez, ejecuta:

```sh
sudo mv /boot/firstboot.sh /home/pi/
sudo chmod +x /home/pi/firstboot.sh
sudo bash /home/pi/firstboot.sh
```

---

## PASO 5 (opcional): Automatizar la ejecución al arrancar

Para que el túnel se inicie automáticamente cada vez que arranques la Raspberry:

```sh
sudo nano /etc/rc.local
```

Antes de `exit 0`, añade:

```sh
bash /home/pi/firstboot.sh
```

Guarda (Ctrl+O), luego (Ctrl+X).

---

## Resultado

Una vez ejecutado el script, obtendrás algo como:

```
Forwarding https://2ff7-80-xx-xxx-101.ngrok.io → http://localhost:8080
```

Ese será el enlace público que debes poner como `BASE_URL` en tu app móvil para conectarte a tu backend local de casa desde cualquier lugar del mundo.

---

## Validaciones desde CMD (Windows)

Puedes comprobar los archivos en la SD desde CMD:

```cmd
D:
dir
type ssh
type wpa_supplicant.conf
type firstboot.sh
```