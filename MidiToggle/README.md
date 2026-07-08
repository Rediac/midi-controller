# MIDI Toggle

App Android de un solo botón que envía un mensaje **MIDI Control Change** por USB cada vez que se toca:

| Parámetro | Valor |
|---|---|
| Canal | 1 |
| CC | 2 |
| Valor ON | 127 |
| Valor OFF | 0 |

> Nota: como es un botón **toggle**, se asumió que en el estado OFF envía valor `0` (comportamiento estándar de un toggle MIDI). Si en realidad querés que siempre mande 127 sin importar el estado, avisame y lo ajusto en una línea (`sendCc(...)` en `MainActivity.kt`).

Usa la API nativa `android.media.midi` (no requiere librerías externas), así que basta con conectar un dispositivo MIDI por USB OTG (por ejemplo tu MG-300 MKII) y la app lo detecta automáticamente al abrirse.

## Estructura del proyecto

```
MidiToggle/
├── .github/workflows/build.yml   ← compila el APK automáticamente
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/rediac/miditoggle/MainActivity.kt
│       └── res/
│           ├── values/strings.xml
│           ├── values/themes.xml
│           └── drawable/ic_launcher.xml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── .gitignore
```

## Cómo subirlo a GitHub (vía web, sin drag-and-drop)

El drag-and-drop de GitHub falla con carpetas anidadas y archivos ocultos como `.github/`. La forma que sí funciona:

1. Creá un repo nuevo vacío en GitHub (sin README).
2. Entrá al repo → **Add file → Create new file**.
3. En el campo de nombre, escribí la **ruta completa**, por ejemplo:
   `app/src/main/java/com/rediac/miditoggle/MainActivity.kt`
   GitHub crea las carpetas intermedias solo con escribir las barras `/`.
4. Pegá el contenido de ese archivo y hacé commit.
5. Repetí para cada archivo del ZIP, **incluyendo** `.github/workflows/build.yml` (el punto inicial no es problema si escribís la ruta completa así).

Alternativa más rápida: subí el ZIP a un repo vacío usando **Add file → Upload files**, arrastrando el ZIP completo (no la carpeta descomprimida). GitHub no descomprime ZIPs automáticamente, así que esta opción solo sirve si preferís subir los archivos descomprimidos igual — en ese caso conviene el método de "Create new file" con ruta completa de arriba para no perder `.github/`.

## Cómo obtener el APK

1. Una vez subidos todos los archivos a la rama `main`, andá a la pestaña **Actions** del repo.
2. El workflow "Build APK" corre solo. Esperá a que termine (ícono verde ✓).
3. Entrá al run finalizado → sección **Artifacts** → descargá `app-debug`.
4. Descomprimí el `.zip` descargado: adentro está el `app-debug.apk` listo para instalar en tu tablet.

## Cambiar canal / CC / valores

Todo está centralizado arriba de `MainActivity.kt`:

```kotlin
private val midiChannel = 0   // 0 = Canal 1
private val ccNumber = 2      // CC 2
private val onValue = 127
private val offValue = 0
```
