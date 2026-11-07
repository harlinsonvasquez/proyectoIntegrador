# proyectoIntegrador

Backend Spring Boot con PostgreSQL. Los mapeos JPA se encuentran en
`infrastructure/persistence/jpa/entity` y corresponden al esquema existente
`gestion_estudiantil`.

## Conexión local

Configura estas variables de entorno antes de iniciar la aplicación:

| Variable | Requerida | Valor de ejemplo |
|---|---:|---|
| `DB_HOST` | No | `127.0.0.1` (predeterminado) |
| `DB_PORT` | No | `5432` (predeterminado) |
| `DB_NAME` | No | `gestion_estudiantil` (predeterminado) |
| `DB_USERNAME` | No | `postgres` (predeterminado) |
| `DB_PASSWORD` | No | `postgres` (predeterminado) |

En PowerShell, por ejemplo:

```powershell
.\mvnw.cmd spring-boot:run
```

La conexión local predeterminada usa `127.0.0.1:5432` y las credenciales
`postgres` / `postgres` y la base de datos `gestion_estudiantil`. Si el nombre de
la base o las credenciales cambian, puedes sobrescribirlos con `DB_NAME`,
`DB_USERNAME` y `DB_PASSWORD`.

La aplicación apunta a PostgreSQL y al esquema `gestion_estudiantil`. Hibernate
usa `ddl-auto=validate`: valida el mapeo al iniciar, pero no crea ni altera
tablas. Flyway está desactivado porque la base ya existe y no se proporcionaron
migraciones versionadas para adoptarla.

## Autenticación y Firebase Realtime Database

La API recibe un token de Firebase Authentication como `Authorization: Bearer <token>`.
El backend verifica el token y busca el correo en `gestion_estudiantil.usuario`;
el rol y el estado activo se toman de PostgreSQL. Por eso debe existir un usuario
local con el mismo correo de Firebase y un perfil de estudiante o docente según
corresponda.

Actualmente no hay un endpoint público para registrar usuarios. La creación de
la cuenta de autenticación se realiza en Firebase y el usuario/perfil local debe
ser provisionado en PostgreSQL por el proceso administrativo correspondiente;
esta API solo autentica cuentas existentes y permite actualizar los datos
personales del estudiante autenticado.

La configuración predeterminada ya apunta al proyecto `base-datos-8eff6` y a su
Realtime Database. Para iniciar con Firebase habilitado en PowerShell:

```powershell
$env:FIREBASE_ENABLED = "true"
$env:FIREBASE_PROJECT_ID = "base-datos-8eff6"
$env:FIREBASE_DATABASE_URL = "https://base-datos-8eff6-default-rtdb.firebaseio.com/"
$env:FIREBASE_CREDENTIALS_PATH = "$env:USERPROFILE\.config\firebase\firebase_key.json"
```

La ruta local predeterminada de la cuenta de servicio es
`%USERPROFILE%\.config\firebase\firebase_key.json`; se puede sobrescribir con
`FIREBASE_CREDENTIALS_PATH`. El archivo no se guarda en el repositorio. Para
ejecutar sin Firebase, establece `FIREBASE_ENABLED=false`; los endpoints
protegidos responderán como no autenticados.

Las notificaciones se guardan en el nodo permitido
`estudiantes/{codigoEstudiantil}/notificaciones/{eventoId}`. Cada registro incluye
el tipo de evento, el ID de la solicitud, su estado, el mensaje, la fecha y el
indicador `leida`. Las tablas relacionales siguen siendo la fuente oficial para
solicitudes, historial y reportes; los nodos existentes `cursos` y `matriculas`
no se duplican ni se modifican con este flujo.

## Endpoints implementados

Todos requieren autenticación Firebase. Las rutas `/docente` y los reportes
validan el rol docente o administrador; las rutas `/me` operan sobre el usuario
autenticado.

| Método | Ruta | Uso |
|---|---|---|
| `GET` | `/api/estudiantes/me` | Consultar perfil propio |
| `PUT` | `/api/estudiantes/me/datos-personales` | Actualizar nombre y apellido propios |
| `GET` | `/api/estudiantes/me/trayectoria` | Ver materias del plan, estado e información de prerrequisitos |
| `GET` | `/api/estudiantes/{id}/perfil` | Consulta docente de perfil |
| `GET` | `/api/estudiantes/{id}/trayectoria` | Consulta docente de trayectoria |
| `GET` | `/api/docente/estudiantes?q=&programaId=&estado=` | Buscar estudiantes por datos básicos |
| `GET` | `/api/estudiantes/me/actividades` | Consultar actividades propias |
| `POST` | `/api/estudiantes/me/actividades` | Registrar actividad integral |
| `GET` / `PUT` | `/api/estudiantes/me/sociodemografico` | Consultar o actualizar perfil sociodemográfico propio |
| `GET` | `/api/estudiantes/{id}/sociodemografico` | Consulta docente autorizada de datos sociodemográficos |
| `POST` | `/api/solicitudes` | Crear solicitud para una matrícula propia |
| `GET` | `/api/solicitudes/mias` | Consultar solicitudes propias |
| `GET` | `/api/solicitudes/{id}` | Consultar solicitud propia o caso autorizado |
| `GET` | `/api/docente/solicitudes?estado=PENDIENTE` | Consultar bandeja docente |
| `PATCH` | `/api/docente/solicitudes/{id}` | Avanzar el estado y registrar observación |
| `GET` | `/api/reportes/solicitudes?programaId=&periodoId=&estado=` | Resumen filtrable de solicitudes |
| `GET` | `/api/reportes/desempeno?programaId=` | Indicadores agregados de notas y estados académicos |
| `GET` | `/api/reportes/participacion?programaId=` | Conteo por categoría de actividad integral |
| `GET` | `/api/notificaciones` | Consultar las últimas 50 notificaciones propias (Realtime Database activo) |
| `PATCH` | `/api/notificaciones/{id}/leida` | Marcar una notificación propia como leída |

Al aprobar una cancelación, el servicio actualiza la matrícula, registra el
seguimiento y la auditoría en la misma operación y agrega un evento a
`outbox_evento`. Con Firebase activo, el proceso programado publica ese evento
en `estudiantes/{codigoEstudiantil}/notificaciones/{eventoId}` en Realtime Database y lo marca procesado. La disponibilidad de materias
se calcula con el plan activo y prerrequisitos aprobados; aún no valida cupos ni
fechas institucionales de cancelación porque esas reglas no están en el enunciado.

## Pruebas

```powershell
.\mvnw.cmd test
```

Las pruebas que cargan el contexto completo requieren una base accesible con
esas variables configuradas y el esquema compatible.
