# Copias de seguridad y recuperación de PostgreSQL

Estos procedimientos respaldan la base `gestion_estudiantil` sin cambiar tablas ni entidades. Los archivos custom de `pg_dump` se guardan en `backups/`, que Git ignora. El script conserva 30 días por defecto; se puede cambiar con `-RetentionDays`.

## Requisitos y contraseña

Se requiere PostgreSQL 16 con `pg_dump`, `pg_restore`, `createdb`, `dropdb` y `psql`. Los scripts buscan los ejecutables en `C:\Program Files\PostgreSQL\16\bin`; para otra instalación, define `POSTGRES_BIN` en la sesión. No guardes la contraseña en los scripts, el repositorio ni la línea de comandos. Para uso manual puedes suministrarla con `PGPASSWORD` en la sesión actual y borrarla al terminar; para tareas programadas, configura el archivo de contraseña de libpq del usuario de Windows con permisos restringidos.

```powershell
$env:PGPASSWORD = Read-Host 'Contraseña de PostgreSQL'
```

## Crear una copia

Desde la raíz del repositorio:

```powershell
.\scripts\backup-postgres.ps1
```

El resultado es un archivo `backups/gestion_estudiantil-<fecha>.backup`. Se puede cambiar destino, retención, base, host, puerto o usuario mediante parámetros. Ejemplo:

```powershell
.\scripts\backup-postgres.ps1 -BackupDirectory 'D:\copias\proyecto' -RetentionDays 45
```

Para automatizarlo, crea una tarea diaria en el Programador de tareas de Windows que ejecute `powershell.exe` con `-NoProfile -ExecutionPolicy Bypass -File "<ruta repo>\scripts\backup-postgres.ps1"`. Configura previamente la autenticación de PostgreSQL para esa misma cuenta de Windows; no pongas la contraseña como argumento de la tarea.

## Verificar recuperación sin tocar la base original

Este procedimiento crea una base temporal con nombre aleatorio, restaura allí la copia, comprueba las 18 tablas y los tres roles base, compara los conteos de las 18 tablas con la base origen y elimina la base temporal al finalizar. La base original `gestion_estudiantil` no se modifica. Los archivos de copia contienen datos personales: mantenlos en una ubicación con acceso restringido y cifrado en reposo.

```powershell
$backup = (Get-ChildItem .\backups\gestion_estudiantil-*.backup |
    Sort-Object LastWriteTime -Descending | Select-Object -First 1).FullName
.\scripts\verify-restore.ps1 -BackupPath $backup
```

La salida debe indicar `Restauración verificada`. Si el proceso falla, revisa `PGPASSWORD`, el servicio PostgreSQL y que el archivo esté completo. Si la base temporal no se puede eliminar automáticamente, el script informa su nombre para que se pueda limpiar luego de confirmar que no contiene nada necesario.

## Restaurar una copia a una base destino

Para recuperación, restaura primero en una base distinta de la original. Crea una base destino vacía con `createdb` y ejecuta:

```powershell
createdb -h 127.0.0.1 -p 5432 -U postgres gestion_estudiantil_recuperada
.\scripts\restore-postgres.ps1 `
  -BackupPath '.\backups\gestion_estudiantil-<fecha>.backup' `
  -TargetDatabase 'gestion_estudiantil_recuperada'
```

El script requiere escribir exactamente `RESTAURAR gestion_estudiantil_recuperada`; `pg_restore --clean` elimina objetos previos dentro de la base destino indicada. No apuntes a la base original durante una demostración. Después de restaurar, ejecuta la API contra la base recuperada en un entorno separado y valida perfiles, matrículas, solicitudes, historial y auditoría.

Al finalizar la operación manual, elimina la contraseña de la sesión:

```powershell
Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
```
