# Guía del frontend

## Qué contiene

El frontend está en `proyectoIntegradorFront/proyectoIntegradorFront`. Es una aplicación de una sola página construida con React 19 y Vite 8. Usa Firebase Authentication para iniciar sesión y Spring Boot como API para datos y autorización. No utiliza un router externo: `App.jsx` mantiene el usuario, rol y sección activa en estado React y renderiza la vista correspondiente.

## Estructura principal

| Archivo | Responsabilidad |
|---|---|
| `src/main.jsx` | Punto de entrada React y montaje de la aplicación. |
| `src/App.jsx` | Login, sesión, navegación por rol, paneles y vistas funcionales. Incluye las pantallas administrativas, estudiante, docente, reportes y notificaciones. |
| `src/api.js` | Cliente HTTP común: URL configurable, token Bearer, JSON y manejo de errores HTTP/red. |
| `src/resources.js` | Configuración declarativa de recursos CRUD: rutas API, campos, columnas, opciones de select y navegación por rol. |
| `src/firebase.js` | Inicialización del cliente Firebase y Firebase Auth mediante variables `VITE_FIREBASE_*`. |
| `src/index.css`, `src/App.css` | Estilos globales y de interfaz; `public/` y `src/assets/` contienen íconos e imágenes. |
| `vite.config.js` | Configuración de Vite. |
| `vercel.json` | Reescritura/fallback para servir la SPA en Vercel. |

## Experiencia por rol

- **Estudiante:** inicio/resumen, perfil y datos personales, trayectoria académica, perfil sociodemográfico, actividades, solicitudes de cancelación e historial, notificaciones.
- **Docente:** búsqueda de estudiantes y consulta de sus datos académicos/sociodemográficos y actividades, bandeja para revisar solicitudes, reportes de solicitudes, desempeño y participación.
- **Administrador:** catálogos académicos (programas, planes, materias y relaciones curriculares, prerrequisitos, periodos, ofertas), usuarios y roles, estudiantes/docentes, matrículas, actividades, perfiles sociodemográficos y auditoría.

Los formularios CRUD reutilizan la metadata de `resources.js`; la UI carga las opciones de selects desde sus endpoints (`/api/admin/...`). Operaciones especializadas, como bandeja docente, login, búsqueda y reportes, tienen lógica propia dentro de `App.jsx`.

## Inicio de sesión y llamadas a la API

1. El usuario inicia sesión con correo y contraseña mediante Firebase Authentication.
2. `onAuthStateChanged` conserva/recupera la sesión; se exige correo verificado.
3. El frontend obtiene el ID token de Firebase y se lo entrega al cliente `api` como `Authorization: Bearer ...`.
4. El backend valida el token, busca el correo local y determina el rol. La interfaz descubre el rol consultando recursos protegidos y muestra la navegación correspondiente.
5. `api.js` usa `VITE_API_URL`, o `http://localhost:8080` si no está definida. Convierte cuerpos JSON, maneja respuestas `204` y normaliza errores en `ApiError`.

El control de acceso real está en el backend; ocultar una opción en la interfaz no sustituye sus permisos. Si hay errores de red/CORS, revisa URL de API, origen permitido en backend y token. La sesión de Firebase y el perfil/rol local deben coincidir.

## Ejecución local y configuración

Desde la carpeta `proyectoIntegradorFront/proyectoIntegradorFront`:

```powershell
npm install
npm run dev
```

Vite sirve normalmente en `http://localhost:5173`. Variables esperadas en `.env.local`:

```text
VITE_API_URL=http://localhost:8080
VITE_FIREBASE_API_KEY=...
VITE_FIREBASE_AUTH_DOMAIN=...
VITE_FIREBASE_PROJECT_ID=...
VITE_FIREBASE_APP_ID=...
```

Los valores de la app web de Firebase son configuración pública del cliente; nunca pongas ahí una cuenta de servicio de Firebase Admin ni credenciales de PostgreSQL. El backend necesita CORS para permitir el origen del frontend. En Vercel, el directorio raíz del proyecto debe ser `proyectoIntegradorFront/proyectoIntegradorFront` y deben configurarse esas mismas variables para el entorno de despliegue. `vercel.json` permite el fallback de las rutas SPA.

## Notas para mantenimiento

- El README propio menciona `.env.example`, pero ese archivo no aparece en el inventario actual del directorio; crea `.env.local` con las variables indicadas o agrega una plantilla sin secretos.
- Los scripts disponibles son `npm run dev`, `npm run build`, `npm run lint` y `npm run preview`.
- `App.jsx` concentra muchas vistas y lógica de presentación/API; separar componentes y servicios por dominio facilitaría cambios y mantenimiento, aunque el patrón metadata-driven de `resources.js` reduce duplicación en CRUD.
- La aplicación usa estado local de React para selección de sección y datos; no se observa una capa de rutas dedicada ni una biblioteca de estado global.
- Las llamadas de administración presuponen respuestas compatibles con la configuración de columnas/campos en `resources.js`; si cambia un DTO del backend, revisa el recurso correspondiente y las vistas especializadas.

