const idField = (name, label, optionsPath, optionLabel, options = {}) => ({
  name, label, type: 'select', optionsPath, optionLabel, typeValue: 'number', ...options,
})
const numberField = (name, label, options = {}) => ({ name, label, type: 'number', ...options })
const textField = (name, label, options = {}) => ({ name, label, ...options })
const dateField = (name, label, options = {}) => ({ name, label, type: 'date', ...options })
const boolField = (name, label, defaultValue = true, options = {}) => ({ name, label, type: 'boolean', default: defaultValue, ...options })
const enumField = (name, label, enumName) => ({ name, label, type: 'enum', enum: enumName })
const standardColumns = (fields) => fields.slice(0, 5).map((field) => ({ key: field.name, label: field.label }))
const adminResource = (key, title, description, fields, options = {}) => ({
  id: key,
  label: title,
  icon: options.icon || 'books',
  resource: {
    title, singular: options.singular || title.replace(/s$/, ''),
    groupLabel: options.groupLabel || 'ADMINISTRACIÓN · ACADÉMICO',
    description, listPath: options.listPath || `/api/admin/${key}`,
    createPath: options.create === false ? null : options.createPath || `/api/admin/${key}`,
    updatePath: options.update === false ? null : options.updatePath || ((row) => `/api/admin/${key}/${row.id}`),
    deletePath: options.delete === false ? null : options.deletePath || ((row) => `/api/admin/${key}/${row.id}`),
    detailPath: options.detail === false ? null : options.detailPath || ((row) => `/api/admin/${key}/${row.id}`),
    fields, columns: options.columns || standardColumns(fields),
    updateFields: options.updateFields,
    createdMessage: options.createdMessage,
    rowKey: options.rowKey,
    createLabel: options.createLabel,
    deleteConfirmation: options.deleteConfirmation,
    deleteSuccess: options.deleteSuccess,
  },
})

const programFields = [textField('codigo', 'Código'), textField('nombre', 'Nombre'), boolField('activo')]
const planFields = [
  idField('programaId', 'Programa', '/api/admin/programas', (item) => `${item.codigo} · ${item.nombre}`),
  textField('codigo', 'Código'), textField('nombre', 'Nombre'), textField('version', 'Versión'),
  dateField('fechaInicio', 'Fecha de inicio', { required: false }),
  dateField('fechaFin', 'Fecha de finalización', { required: false }), boolField('activo'),
]
const subjectFields = [
  textField('codigo', 'Código'), textField('nombre', 'Nombre'),
  numberField('creditos', 'Créditos', { min: 1 }), boolField('activo'),
]
const planSubjectFields = [
  idField('planEstudioId', 'Plan de estudio', '/api/admin/planes-estudio', (item) => `${item.codigo} · ${item.nombre}`),
  idField('asignaturaId', 'Asignatura', '/api/admin/asignaturas', (item) => `${item.codigo} · ${item.nombre}`),
  numberField('semestreSugerido', 'Semestre sugerido', { min: 1, required: false }),
  boolField('obligatoria', 'Obligatoria', true, {
    trueLabel: 'Sí · Obligatoria',
    falseLabel: 'No · Electiva / opcional',
    help: 'Elige “No” para registrar una asignatura electiva u opcional.',
  }),
]
const prerequisiteFields = [
  idField('planAsignaturaId', 'Asignatura del plan', '/api/admin/plan-asignaturas', (item) => `${item.asignaturaCodigo} · ${item.asignaturaNombre} (${item.planCodigo})`),
  idField('prerrequisitoPlanAsignaturaId', 'Prerrequisito del plan', '/api/admin/plan-asignaturas', (item) => `${item.asignaturaCodigo} · ${item.asignaturaNombre} (${item.planCodigo})`),
]
const periodFields = [
  textField('codigo', 'Código'), textField('nombre', 'Nombre'),
  dateField('fechaInicio', 'Fecha de inicio'), dateField('fechaFin', 'Fecha de finalización'),
]
const offerFields = [
  idField('planAsignaturaId', 'Asignatura del plan', '/api/admin/plan-asignaturas', (item) => `${item.asignaturaCodigo} · ${item.asignaturaNombre} (${item.planCodigo})`),
  idField('periodoId', 'Periodo', '/api/admin/periodos', (item) => `${item.codigo} · ${item.nombre}`),
  textField('grupo', 'Grupo'), numberField('cupo', 'Cupo', { min: 1, required: false }),
  idField('docenteId', 'Docente (opcional)', '/api/admin/docentes', (item) => `${item.nombre} ${item.apellido}`, { required: false }),
]
const activityFields = [
  idField('estudianteId', 'Estudiante', '/api/admin/estudiantes', (item) => `${item.codigoEstudiantil} · ${item.nombre} ${item.apellido}`),
  enumField('categoria', 'Categoría', 'CategoriaActividad'), textField('nombre', 'Nombre'),
  textField('descripcion', 'Descripción', { type: 'textarea', required: false }),
  textField('organizacion', 'Organización', { required: false }),
  dateField('fechaInicio', 'Fecha de inicio', { required: false }),
  dateField('fechaFin', 'Fecha de finalización', { required: false }),
  textField('evidenciaUrl', 'URL de evidencia', { type: 'url', required: false }),
]
const matriculaFields = [
  idField('estudianteId', 'Estudiante', '/api/admin/estudiantes', (item) => `${item.codigoEstudiantil} · ${item.nombre} ${item.apellido}`),
  idField('ofertaAsignaturaId', 'Oferta académica', '/api/admin/ofertas', (item) => `${item.asignaturaCodigo} · ${item.asignaturaNombre} · ${item.codigoPeriodo} · Grupo ${item.grupo}`),
  enumField('estado', 'Estado de matrícula', 'EstadoMatricula'),
  numberField('notaFinal', 'Nota final', { min: 0, max: 5, step: 0.01, required: false }),
]

export const enums = {
  EstadoSolicitud: ['PENDIENTE', 'EN_REVISION', 'APROBADA', 'RECHAZADA'],
  EstadoMatricula: ['PENDIENTE', 'MATRICULADA', 'CURSANDO', 'APROBADA', 'REPROBADA', 'CANCELADA'],
  EstadoEstudiante: ['ACTIVO', 'INACTIVO', 'GRADUADO', 'RETIRADO'],
  CategoriaActividad: ['CONGRESO', 'SEMILLERO', 'PROYECTO_INVESTIGACION', 'PUBLICACION', 'DEPORTE', 'OTRA'],
  RolNombre: ['ESTUDIANTE', 'DOCENTE', 'ADMINISTRADOR'],
}

const programas = adminResource('programas', 'Programas', 'Organiza los programas académicos. La baja es lógica para preservar el historial.', programFields, {
  icon: 'academic', singular: 'programa', deleteSuccess: 'Programa desactivado; se conserva el historial.',
})
const planes = adminResource('planes-estudio', 'Planes de estudio', 'Define las versiones curriculares asociadas a un programa.', planFields, {
  icon: 'books', singular: 'plan de estudio', deleteSuccess: 'Plan desactivado; se conserva el historial.',
  columns: [
    { key: 'codigo', label: 'Código' }, { key: 'nombre', label: 'Plan de estudio' },
    { key: 'programaCodigo', label: 'Código programa' }, { key: 'programaNombre', label: 'Programa' },
    { key: 'version', label: 'Versión' }, { key: 'activo', label: 'Estado', render: (row) => row.activo ? 'Activo' : 'Inactivo' },
  ],
})
const asignaturas = adminResource('asignaturas', 'Asignaturas', 'Administra el catálogo de asignaturas. La baja es lógica.', subjectFields, {
  icon: 'books', singular: 'asignatura', deleteSuccess: 'Asignatura desactivada; se conserva el historial.',
})
const planAsignaturas = adminResource('plan-asignaturas', 'Asignaturas del plan', 'Asocia asignaturas a planes y define semestre sugerido y obligatoriedad.', planSubjectFields, {
  icon: 'academic', singular: 'asignatura del plan',
  columns: [
    { key: 'planCodigo', label: 'Código del plan' },
    { key: 'planNombre', label: 'Plan de estudio' },
    { key: 'asignaturaCodigo', label: 'Código de asignatura' },
    { key: 'asignaturaNombre', label: 'Asignatura' },
    { key: 'semestreSugerido', label: 'Semestre sugerido' },
    { key: 'obligatoria', label: 'Obligatoria', render: (row) => row.obligatoria ? 'Sí' : 'No' },
  ],
})
const prerrequisitos = adminResource('prerrequisitos', 'Prerrequisitos', 'Define dependencias curriculares entre asignaturas del plan.', prerequisiteFields, {
  icon: 'academic', singular: 'prerrequisito',
  listPath: '/api/admin/prerrequisitos',
  detailPath: (row) => `/api/admin/prerrequisitos/${row.planAsignaturaId}/${row.prerrequisitoPlanAsignaturaId}`,
  update: false,
  deletePath: (row) => `/api/admin/prerrequisitos/${row.planAsignaturaId}/${row.prerrequisitoPlanAsignaturaId}`,
  rowKey: (row) => `${row.planAsignaturaId}-${row.prerrequisitoPlanAsignaturaId}`,
  columns: [
    { key: 'asignaturaCodigo', label: 'Código asignatura' },
    { key: 'asignaturaNombre', label: 'Asignatura' },
    { key: 'prerrequisitoCodigo', label: 'Código prerrequisito' },
    { key: 'prerrequisitoNombre', label: 'Prerrequisito' },
  ],
})
const periodos = adminResource('periodos', 'Periodos académicos', 'Configura periodos y sus fechas de inicio y finalización.', periodFields, {
  icon: 'calendar', singular: 'periodo',
})
const ofertas = adminResource('ofertas', 'Ofertas académicas', 'Programa asignaturas por periodo, grupo, cupo y docente.', offerFields, {
  icon: 'calendar', singular: 'oferta',
  columns: [
    { key: 'asignaturaCodigo', label: 'Código' },
    { key: 'asignaturaNombre', label: 'Asignatura' },
    { key: 'codigoPeriodo', label: 'Código periodo' },
    { key: 'periodo', label: 'Periodo' },
    { key: 'grupo', label: 'Grupo' },
    { key: 'cupo', label: 'Cupo' },
    { key: 'nombreDocente', label: 'Docente', render: (row) => row.nombreDocente ? `${row.nombreDocente} ${row.apellidoDocente}` : 'Sin asignar' },
  ],
})
const estudiantes = adminResource('estudiantes', 'Estudiantes', 'Crea cuentas en Firebase Authentication y sus perfiles locales. El correo se verifica desde Firebase.', [
  textField('correo', 'Correo electrónico', { type: 'email' }),
  textField('contrasena', 'Contraseña inicial', { type: 'password', help: 'Se envía a Firebase Authentication y no se guarda en PostgreSQL.' }),
  textField('nombre', 'Nombre'), textField('apellido', 'Apellido'),
  idField('programaId', 'Programa', '/api/admin/programas', (item) => `${item.codigo} · ${item.nombre}`),
  textField('codigoEstudiantil', 'Código estudiantil'),
  dateField('fechaIngreso', 'Fecha de ingreso', { required: false }),
], {
  icon: 'people', singular: 'estudiante', createLabel: 'Registrar estudiante',
  createdMessage: 'Estudiante creado en Firebase y PostgreSQL. Envía el correo de verificación desde Firebase antes de su primer inicio de sesión.',
  updateFields: [
    textField('nombre', 'Nombre'), textField('apellido', 'Apellido'),
    idField('programaId', 'Programa', '/api/admin/programas', (item) => `${item.codigo} · ${item.nombre}`),
    textField('codigoEstudiantil', 'Código estudiantil'),
    dateField('fechaIngreso', 'Fecha de ingreso', { required: false }),
    enumField('estado', 'Estado', 'EstadoEstudiante'),
  ],
  columns: [{ key: 'codigoEstudiantil', label: 'Código' }, { key: 'nombre', label: 'Nombre' }, { key: 'apellido', label: 'Apellido' }, { key: 'correo', label: 'Correo' }, { key: 'estado', label: 'Estado' }],
  deleteSuccess: 'Estudiante inactivado; su historial se conserva.',
})
const docentes = adminResource('docentes', 'Docentes', 'Crea cuentas en Firebase Authentication y perfiles docentes locales.', [
  textField('correo', 'Correo electrónico', { type: 'email' }),
  textField('contrasena', 'Contraseña inicial', { type: 'password', help: 'Se envía a Firebase Authentication y no se guarda en PostgreSQL.' }),
  textField('nombre', 'Nombre'), textField('apellido', 'Apellido'),
  textField('codigoDocente', 'Código docente', { required: false }),
], {
  icon: 'people', singular: 'docente', createLabel: 'Registrar docente',
  createdMessage: 'Docente creado en Firebase y PostgreSQL. Envía el correo de verificación desde Firebase antes de su primer inicio de sesión.',
  updateFields: [
    textField('nombre', 'Nombre'), textField('apellido', 'Apellido'),
    textField('codigoDocente', 'Código docente', { required: false }), boolField('activo'),
  ],
  columns: [{ key: 'codigoDocente', label: 'Código' }, { key: 'nombre', label: 'Nombre' }, { key: 'apellido', label: 'Apellido' }, { key: 'correo', label: 'Correo' }, { key: 'activo', label: 'Estado' }],
  deleteSuccess: 'Docente inactivado; su historial se conserva.',
})
const matriculas = adminResource('matriculas', 'Matrículas', 'Registra y actualiza matrículas. Las cancelaciones deben tramitarse por solicitudes.', matriculaFields, {
  icon: 'academic', singular: 'matrícula', delete: false,
  columns: [
    { key: 'codigoEstudiantil', label: 'Código estudiante' },
    { key: 'nombreEstudiante', label: 'Estudiante', render: (row) => `${row.nombreEstudiante || ''} ${row.apellidoEstudiante || ''}`.trim() || 'Sin nombre' },
    { key: 'asignaturaCodigo', label: 'Código asignatura' },
    { key: 'asignaturaNombre', label: 'Asignatura' },
    { key: 'codigoPeriodo', label: 'Periodo' },
    { key: 'grupo', label: 'Grupo' },
    { key: 'estado', label: 'Estado de matrícula' },
    { key: 'notaFinal', label: 'Nota final' },
  ],
})
const actividades = {
  id: 'actividades-admin', label: 'Actividades estudiantiles', icon: 'activity',
  resource: {
    title: 'Actividades estudiantiles', singular: 'actividad', groupLabel: 'ADMINISTRACIÓN · ESTUDIANTES',
    description: 'Consulta y administra actividades integrales asociadas a estudiantes.',
    listPath: '/api/admin/actividades', createPath: '/api/admin/actividades',
    detailPath: (row) => `/api/admin/actividades/${row.id}`,
    updatePath: (row) => `/api/admin/actividades/${row.id}`,
    deletePath: (row) => `/api/admin/actividades/${row.id}`,
    fields: activityFields,
    columns: [{ key: 'id', label: 'ID' }, { key: 'codigoEstudiantil', label: 'Código estudiante' }, { key: 'nombreEstudiante', label: 'Estudiante', render: (row) => `${row.nombreEstudiante || ''} ${row.apellidoEstudiante || ''}`.trim() || 'Sin nombre' }, { key: 'categoria', label: 'Categoría' }, { key: 'nombre', label: 'Actividad' }, { key: 'fechaInicio', label: 'Inicio' }],
  },
}
const actividadSelf = {
  id: 'actividades', label: 'Mis actividades', icon: 'activity',
}
const profileAdmin = {
  id: 'perfil-admin',
  label: 'Perfiles sociodemográficos',
  icon: 'people',
}

const userItem = { id: 'usuarios', label: 'Usuarios y roles', icon: 'people' }
const auditItem = { id: 'auditoria', label: 'Auditoría', icon: 'reports' }
export const adminGroups = [
  { label: 'ACADÉMICO', items: [programas, planes, asignaturas, planAsignaturas, prerrequisitos, periodos, ofertas, matriculas] },
  { label: 'COMUNIDAD', items: [estudiantes, docentes, actividades, profileAdmin] },
  { label: 'SISTEMA', items: [userItem, auditItem] },
]

export const studentNav = [
  { label: 'MI VIDA ACADÉMICA', items: [
    { id: 'perfil', label: 'Mi perfil', icon: 'people' },
    { id: 'trayectoria', label: 'Mi trayectoria', icon: 'academic' },
    { id: 'historial-academico', label: 'Historial académico', icon: 'books' },
    { id: 'ofertas-academicas', label: 'Ofertas disponibles', icon: 'calendar' },
    { id: 'sociodemografico', label: 'Perfil sociodemográfico', icon: 'settings' },
  ] },
  { label: 'MI ACTIVIDAD', items: [
    { id: 'actividades', label: 'Mis actividades', icon: 'activity' },
    { id: 'solicitudes', label: 'Solicitudes', icon: 'requests' },
    { id: 'notificaciones', label: 'Notificaciones', icon: 'bell' },
  ] },
]

export const teacherNav = [
  { label: 'ACOMPAÑAMIENTO', items: [
    { id: 'asignaturas-docente', label: 'Mis asignaturas', icon: 'books' },
    { id: 'estudiantes-docente', label: 'Estudiantes', icon: 'people' },
    { id: 'solicitudes-docente', label: 'Solicitudes', icon: 'requests' },
  ] },
  { label: 'INDICADORES', items: [
    { id: 'reporte-solicitudes', label: 'Solicitudes', icon: 'reports' },
    { id: 'reporte-academico', label: 'Desempeño académico', icon: 'academic' },
    { id: 'reporte-participacion', label: 'Participación', icon: 'activity' },
  ] },
]

export { actividadSelf }
