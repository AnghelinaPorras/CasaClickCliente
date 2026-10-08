# CasaClick Perú - Cliente

Proyecto Android independiente de Anghelina Porras Torres, caso 9, evaluación parcial Semana 8.

Abrir esta carpeta en Android Studio. Instalar SDK 37.1 si se solicita. Ejecutar la API compartida en puerto 3001 y usar `http://10.0.2.2:3001/` en Login del emulador. La URL es editable.

Cuenta: `cliente1@casaclick.pe` o `cliente2@casaclick.pe`; clave ficticia `CasaClick2026!`. Solo admite rol cliente. El primer ingreso es online.

Funciones: catálogo de 36 propiedades descargado a Room, filtros por operación/precio/habitaciones, contador por precio, galería, favoritas locales, agenda y cancelación de visitas propias, uso offline y sincronización con historial/errores/reintentos/duplicados.

Las visitas nuevas son PROVISIONAL hasta que el servidor las acepta. Un 409 no borra la solicitud; permite corregir horario o descartar el cambio.

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`. No subir `local.properties`, build ni cachés; ya están en `.gitignore`.

MVVM: ui -> viewmodel -> data/repository -> Room/Retrofit. `worker/SyncWorker.kt` procesa la cola con red disponible; cada escritura usa una transacción.

Pruebas: `PersistenciaSincronizacionTest` usa Room y un servidor simulado; `FlujoCompletoTest` requiere una API limpia con datos de seed y el argumento de instrumentación `apiUrl`. La documentación completa se entrega junto con este proyecto en `CasaClickPeru_Semana8`.
