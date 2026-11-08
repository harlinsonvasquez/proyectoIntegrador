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

Las cuentas nuevas de estudiantes y docentes se crean desde el CRUD administrativo
con Firebase Admin SDK y se enlazan con su usuario y perfil local en PostgreSQL.
La contraseña se envía únicamente a Firebase Authentication; no se almacena en
PostgreSQL. La cuenta queda con el correo sin verificar y el estudiante/docente
debe completar la verificación antes de autenticarse. No hay un endpoint para
crear administradores: el primer administrador debe aprovisionarse de forma
segura en Firebase y PostgreSQL.

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

### Documentación interactiva (Swagger UI)

Con la aplicación iniciada, abre `http://localhost:8080/swagger-ui/index.html`.
El documento OpenAPI en JSON está disponible en `http://localhost:8080/v3/api-docs`.
La interfaz y la especificación son públicas para consulta; para probar rutas
protegidas, pulsa **Authorize** y pega el token Bearer de Firebase Authentication.
La documentación agrupa las operaciones por responsabilidad e indica el objetivo
de cada endpoint, sus parámetros y los esquemas de solicitud/respuesta.

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

### CRUD administrativo

Todas las rutas siguientes requieren un token Firebase válido y un usuario
local activo con rol `ADMINISTRADOR`. Para crear cuentas, Firebase Admin SDK
debe estar configurado con credenciales de servicio. `DELETE` en programas,
planes, asignaturas, estudiantes y docentes es una baja lógica; no elimina el
historial ni la cuenta de Firebase.

| Recurso | Rutas administrativas |
|---|---|
| Usuarios | `GET /api/admin/usuarios` (consulta); `PATCH /api/admin/usuarios/{id}/rol` (cambio de rol con auditoría) |
| Programas | `GET, POST /api/admin/programas`; `GET, PUT, DELETE /api/admin/programas/{id}` |
| Planes de estudio | `GET, POST /api/admin/planes-estudio`; `GET, PUT, DELETE /api/admin/planes-estudio/{id}` |
| Asignaturas | `GET, POST /api/admin/asignaturas`; `GET, PUT, DELETE /api/admin/asignaturas/{id}` |
| Asignaturas del plan | `GET, POST /api/admin/plan-asignaturas`; `GET, PUT, DELETE /api/admin/plan-asignaturas/{id}` |
| Prerrequisitos | `GET, POST /api/admin/prerrequisitos`; `GET /api/admin/prerrequisitos/{planId}/{requisitoId}`; `DELETE` en esa misma ruta |
| Periodos | `GET, POST /api/admin/periodos`; `GET, PUT, DELETE /api/admin/periodos/{id}` |
| Ofertas | `GET, POST /api/admin/ofertas`; `GET, PUT, DELETE /api/admin/ofertas/{id}` |
| Estudiantes | `GET, POST /api/admin/estudiantes`; `GET, PUT, DELETE /api/admin/estudiantes/{id}` |
| Docentes | `GET, POST /api/admin/docentes`; `GET, PUT, DELETE /api/admin/docentes/{id}` |
| Matrículas | `GET, POST /api/admin/matriculas`; `GET, PUT /api/admin/matriculas/{id}` |
| Actividades | `GET /api/admin/actividades?estudianteId=`; `GET, POST /api/admin/actividades`; `GET, PUT, DELETE /api/admin/actividades/{id}` |
| Perfil sociodemográfico | `GET, PUT /api/admin/estudiantes/{id}/sociodemografico` |
| Auditoría | `GET /api/admin/auditoria?entidad=&registroId=&usuarioId=&page=&size=` (consulta paginada) |

Para registrar un estudiante, envía `POST /api/admin/estudiantes`:

```json
{
  "correo": "estudiante@ejemplo.edu",
  "contrasena": "UnaClaveSegura123",
  "nombre": "Ana",
  "apellido": "Pérez",
  "programaId": 1,
  "codigoEstudiantil": "EST-2025-001",
  "fechaIngreso": "2025-01-15"
}
```

Docente: `POST /api/admin/docentes` con `correo`, `contrasena`, `nombre`,
`apellido` y opcionalmente `codigoDocente`. Envía las contraseñas solo por HTTPS
y no las registres en logs ni clientes persistentes. Para modificar perfiles se
usan `PUT /api/admin/estudiantes/{id}` y `PUT /api/admin/docentes/{id}`; estos
datos no incluyen cambios de correo ni contraseña.

No se ofrece CRUD destructivo para `rol` (catálogo fijo), `auditoria_cambio`
(solo consulta), ni `outbox_evento` (proceso interno). Las solicitudes de
cancelación y su seguimiento se crean y avanzan por sus endpoints de flujo,
para conservar las reglas e historial. Las matrículas tampoco se borran desde
el CRUD: la cancelación debe tramitarse con una solicitud, y `PUT` no permite
reactivar una matrícula ya cancelada. El perfil sociodemográfico se consulta y
actualiza, pero no se elimina. Las restricciones de claves foráneas impiden
eliminar físicamente catálogos con registros dependientes.

El cambio de rol es una operación administrativa separada del CRUD de perfiles.
`PATCH /api/admin/usuarios/{id}/rol` acepta `{"rol":"DOCENTE"}` (y opcionalmente
`"codigoDocente"`); crea el perfil docente si falta y conserva el perfil de
estudiante y su historial. Para asignar `ESTUDIANTE`, el usuario debe tener ya
un perfil estudiantil; ese perfil se crea mediante el alta de estudiante. No se
permite cambiar el propio rol ni degradar al último administrador activo. Cada
cambio registra el rol anterior y el nuevo en `auditoria_cambio`. Los roles
admitidos son `ESTUDIANTE`, `DOCENTE` y `ADMINISTRADOR`.

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
