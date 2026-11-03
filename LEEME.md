# GRM PAY · App Android (APK)

App nativa ligera que abre el aplicativo PHP **GRM_PAY** dentro de la red FTTH:
`http://172.0.20.6/GRM_PAY/login.php` (editable desde la app).

## Qué incluye
- Pantalla inicial (Splash) con la marca GRM (paleta azul/cian) y acceso a "cambiar servidor".
- Después del Splash abre la pantalla de inicio de sesión del servidor: logo, título "USUARIO Y CLAVE", campos de usuario y clave y botón "ENTRAR".
- Las credenciales se validan en el servidor PHP; si son correctas se abre el dashboard y, si no, se muestra el error del sistema.
- WebView a pantalla completa con barra de progreso; la sesión se conserva mientras la app esté abierta.
- Pantalla de "Sin conexión" con **Reintentar** y **Configurar servidor** (la IP se cambia sin recompilar).
- Diálogos nativos para las confirmaciones de la página (registrar / anular pago).
- Exportar CSV (descarga a la carpeta Descargas) y subir el Excel de facturas (administrador).
- El botón Atrás navega dentro de la app; en el Resumen o el login cierra la app.
- Sin librerías de terceros. Android 5.0 o superior. Permite http (servidor local sin certificado).

## Cómo generar el APK
### Opción A · Android Studio (recomendada)
1. Instalar Android Studio.
2. File → Open → elegir esta carpeta (GRM_PAY_APK) y esperar a que termine "Gradle sync".
3. Build → Build Bundle(s) / APK(s) → **Build APK(s)**.
4. El archivo queda en `app/build/outputs/apk/debug/app-debug.apk`.

### Opción B · GitHub (sin instalar nada)
1. Subir esta carpeta a un repositorio de GitHub.
2. Pestaña **Actions → Compilar APK → Run workflow**.
3. Al terminar, descargar `GRM_PAY-apk` (contiene `app-debug.apk`).

## Instalar en el celular
1. Pasar el `.apk` al teléfono (WhatsApp, cable, Drive).
2. Abrirlo y permitir "instalar apps de esta fuente" cuando Android lo pida.
3. Conectar el celular al WiFi de la red FTTH y abrir **GRM PAY**.

## Cambiar la IP del servidor
- En la app: pantalla de inicio → tocar el texto "Servidor…", o en la pantalla de error → Configurar servidor.
- Para cambiar el valor de fábrica: `app/src/main/java/co/grmconexion/pay/Ajustes.java` → `URL_DEFECTO`.

## Notas
- El APK "debug" se instala sin problema para uso interno. Si lo quieres firmar con tu propia llave: Build → Generate Signed Bundle / APK.
- Para usar el sistema fuera de la red FTTH habría que publicarlo con HTTPS (dominio + certificado).
