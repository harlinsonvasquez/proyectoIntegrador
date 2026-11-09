# AulaNexo — portal académico

Frontend React + Vite para el sistema de gestión estudiantil. Se conecta con la
API Spring Boot y usa Firebase Authentication para iniciar sesión. La API sigue
siendo responsable de autorizar los roles y validar cada operación.

## Desarrollo local

Esta carpeta contiene el proyecto Vite dentro del repositorio del backend.
Desde esta carpeta:

```powershell
Copy-Item .env.example .env.local
npm install
npm run dev
```

Configura en `.env.local` la Web API Key y los datos de la app web de Firebase
(`VITE_FIREBASE_*`) y la URL de Spring Boot (`VITE_API_URL`). La Web API Key se
usa como identificador público del cliente Firebase; no pongas aquí claves
privadas de Firebase Admin ni credenciales de PostgreSQL. `.env.local` está
ignorado por Git.

El usuario inicia sesión con Firebase. El frontend identifica su rol consultando
endpoints protegidos; se requiere correo verificado y un perfil local activo
asociado al correo. Si la cuenta no está verificada o la API no reconoce el rol,
se muestra el error recibido.

## Funcionalidades conectadas

- **Administrador:** catálogos (programas, planes, asignaturas, relaciones y
  prerrequisitos), periodos, ofertas, estudiantes, docentes, matrículas,
  actividades, perfiles sociodemográficos, roles y auditoría.
- **Docente:** búsqueda de estudiantes, consulta de su perfil, trayectoria,
  actividades y datos sociodemográficos; bandeja y transición de solicitudes;
  reportes de solicitudes, desempeño y participación.
- **Estudiante:** perfil, datos personales, trayectoria, perfil
  sociodemográfico, actividades, solicitudes de cancelación, historial y
  notificaciones.

Las cuentas nuevas se crean desde las pantallas administrativas mediante la
API. El alta crea la cuenta Firebase y el perfil PostgreSQL; el envío del correo
de verificación debe realizarse mediante el flujo configurado en Firebase. La
contraseña nunca se guarda en el navegador más allá del campo de alta ni en la
base local.

## API y CORS

En local, la API predeterminada es `http://localhost:8080`. El backend acepta
por defecto el origen `http://localhost:5173`. Para desplegar, configura en el
entorno del backend:

```text
CORS_ALLOWED_ORIGINS=https://tu-frontend.vercel.app
```

Puedes especificar más de un origen separados por comas. Usa los dominios
exactos que necesites. Configura Firebase Admin y PostgreSQL exclusivamente en
el entorno del backend. La base desplegada debe tener el esquema compatible: el
backend valida el esquema existente y no lo crea automáticamente.

## Despliegue en Vercel

El directorio raíz de Vercel es esta carpeta, que contiene `package.json`.
Configura las variables `VITE_API_URL`, `VITE_FIREBASE_API_KEY`,
`VITE_FIREBASE_AUTH_DOMAIN`, `VITE_FIREBASE_PROJECT_ID` y `VITE_FIREBASE_APP_ID`.
`vercel.json` agrega el fallback para las rutas del frontend.

## Validación

```powershell
npm run lint
npm run build
```
