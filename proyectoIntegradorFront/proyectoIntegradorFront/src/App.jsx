import { useCallback, useEffect, useMemo, useState } from 'react'
import {
  browserLocalPersistence,
  onAuthStateChanged,
  setPersistence,
  signInWithEmailAndPassword,
  signOut,
} from 'firebase/auth'
import { auth, firebaseConfigured } from './firebase'
import { api, ApiError } from './api'
import { adminGroups, enums, studentNav, teacherNav } from './resources'
import './App.css'

const iconMap = {
  dashboard: '◫', academic: '▤', people: '♙', calendar: '◷', books: '▣',
  activity: '✳', requests: '↗', reports: '▥', bell: '♧', settings: '⚙',
  search: '⌕', arrow: '↗', menu: '☰', logout: '⇥', plus: '+', close: '×',
}

function Icon({ name }) {
  return <span aria-hidden="true" className="icon">{iconMap[name] || '•'}</span>
}

function errorMessage(error) {
  if (error instanceof ApiError) {
    if (error.status === 401) return 'Sesión no válida o correo sin verificar. Inicia sesión de nuevo y verifica la cuenta en Firebase.'
    if (error.status === 403) return 'Tu rol no tiene permiso para esta operación.'
    if (error.status === 404) return error.message || 'No se encontró el recurso solicitado.'
    return error.message || `La API respondió con estado ${error.status}.`
  }
  if (error?.code === 'auth/user-disabled') return 'La cuenta está deshabilitada en Firebase Authentication. Contacta al administrador para habilitarla.'
  if (error?.code === 'auth/invalid-credential') return 'Correo o contraseña incorrectos.'
  if (error?.code === 'auth/too-many-requests') return 'Demasiados intentos. Espera un momento e inténtalo de nuevo.'
  if (error?.code === 'auth/network-request-failed') return 'No se pudo conectar con Firebase. Revisa tu conexión.'
  return error?.message || 'Ocurrió un error inesperado.'
}

async function identifyRole() {
  const checks = [
    ['ESTUDIANTE', '/api/estudiantes/me'],
    ['ADMINISTRADOR', '/api/admin/usuarios'],
    ['DOCENTE', '/api/docente/solicitudes'],
  ]
  for (const [role, path] of checks) {
    try {
      await api.get(path)
      return role
    } catch (error) {
      if (error.status === 401) throw error
      if (![403, 404].includes(error.status)) throw error
    }
  }
  throw new Error('La cuenta no tiene un perfil activo reconocido en la aplicación.')
}

function Login({ onLogin, loading, message, onMessage }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await setPersistence(auth, browserLocalPersistence)
      await signInWithEmailAndPassword(auth, email.trim(), password)
      await onLogin()
    } catch (error) {
      onMessage(errorMessage(error))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-shell">
      <section className="login-story">
        <div className="brand brand-light"><span className="brand-mark">A</span><span>Aula<span className="brand-accent">Nexo</span></span></div>
        <div className="story-content">
          <div className="eyebrow eyebrow-light"><span className="eyebrow-dot" /> PLATAFORMA ACADÉMICA</div>
          <h1>Tu camino<br />universitario,<br /><em>en un solo lugar.</em></h1>
          <p>Un espacio para conectar tu aprendizaje, acompañar tu progreso y dar forma a cada logro.</p>
        </div>
        <div className="story-footer"><span>APRENDER · CRECER · AVANZAR</span><span>01 — 04</span></div>
        <div className="story-orbit orbit-one" /><div className="story-orbit orbit-two" />
      </section>
      <section className="login-panel">
        <div className="login-top-note">BIENVENIDO DE NUEVO <span>✦</span></div>
        <div className="login-form-wrap">
          <div className="login-heading">
            <div className="mini-icon">✳</div>
            <h2>Inicia sesión</h2>
            <p>Ingresa con la cuenta institucional registrada en la plataforma.</p>
          </div>
          {!firebaseConfigured && <div className="notice notice-warning">Configura las variables de Firebase en <code>.env.local</code> para habilitar el inicio de sesión.</div>}
          {message && <div className="notice notice-error" role="alert">{message}</div>}
          <form onSubmit={submit} className="form-stack">
            <label>Correo electrónico
              <input type="email" autoComplete="username" placeholder="nombre@institucion.edu.co" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </label>
            <label>Contraseña
              <input type="password" autoComplete="current-password" placeholder="Ingresa tu contraseña" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </label>
            <button className="button button-primary button-wide" disabled={!firebaseConfigured || submitting || loading}>
              {submitting || loading ? <><span className="spinner" /> Validando acceso…</> : <>Entrar a mi espacio <span>→</span></>}
            </button>
          </form>
          <div className="login-security"><span className="security-lock">⌑</span><span>Acceso seguro con Firebase Authentication</span></div>
        </div>
        <div className="login-help">¿Necesitas ayuda? Contacta a soporte académico.</div>
      </section>
    </main>
  )
}

function Sidebar({ role, active, setActive, user, onLogout, open, onClose }) {
  const nav = role === 'ADMINISTRADOR' ? adminGroups : role === 'DOCENTE' ? teacherNav : studentNav
  const title = role === 'ADMINISTRADOR' ? 'Administración' : role === 'DOCENTE' ? 'Espacio docente' : 'Mi espacio'
  return (
    <>
      {open && <button className="sidebar-backdrop" onClick={onClose} aria-label="Cerrar menú" />}
      <aside className={`sidebar ${open ? 'sidebar-open' : ''}`}>
        <div className="brand"><span className="brand-mark">A</span><span>Aula<span className="brand-accent">Nexo</span></span></div>
        <div className="nav-section-label">{title}</div>
        <nav className="side-nav">
          <button className={`nav-item ${active === 'inicio' ? 'active' : ''}`} onClick={() => { setActive('inicio'); onClose() }}>
            <Icon name="dashboard" /><span>Resumen</span>
          </button>
          {nav.map((group) => (
            <div className="nav-group" key={group.label}>
              <div className="nav-group-label">{group.label}</div>
              {group.items.map((item) => (
                <button key={item.id} className={`nav-item ${active === item.id ? 'active' : ''}`}
                  onClick={() => { setActive(item.id); onClose() }}>
                  <Icon name={item.icon} /><span>{item.label}</span>
                </button>
              ))}
            </div>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="support-card"><div className="support-spark">✦</div><strong>¿Necesitas ayuda?</strong><span>Estamos aquí para acompañarte.</span></div>
          <div className="user-card">
            <div className="avatar">{(user?.displayName || user?.email || 'U').charAt(0).toUpperCase()}</div>
            <div className="user-meta"><strong>{user?.displayName || user?.email?.split('@')[0]}</strong><span>{roleLabel(role)}</span></div>
            <button className="icon-button logout-button" onClick={onLogout} aria-label="Cerrar sesión"><Icon name="logout" /></button>
          </div>
        </div>
      </aside>
    </>
  )
}

function roleLabel(role) {
  return ({ ADMINISTRADOR: 'Administrador', DOCENTE: 'Docente', ESTUDIANTE: 'Estudiante' })[role] || role
}

function Topbar({ activeItem, onMenu, user }) {
  return (
    <header className="topbar">
      <button className="mobile-menu icon-button" onClick={onMenu} aria-label="Abrir menú"><Icon name="menu" /></button>
      <div className="breadcrumb"><span>Portal académico</span><span className="breadcrumb-sep">/</span><strong>{activeItem?.label || 'Resumen'}</strong></div>
      <div className="topbar-actions"><div className="topbar-date">{new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium' }).format(new Date())}</div><div className="topbar-avatar">{(user?.displayName || user?.email || 'U').charAt(0).toUpperCase()}</div></div>
    </header>
  )
}

function Dashboard({ role, user, setActive }) {
  const [stats, setStats] = useState([])
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    let alive = true
    async function load() {
      const endpoints = role === 'ADMINISTRADOR'
        ? [['Programas', '/api/admin/programas'], ['Estudiantes', '/api/admin/estudiantes'], ['Docentes', '/api/admin/docentes'], ['Solicitudes', '/api/admin/auditoria?size=1']]
        : role === 'DOCENTE'
          ? [['Solicitudes pendientes', '/api/docente/solicitudes?estado=PENDIENTE'], ['Estudiantes', '/api/docente/estudiantes'], ['Programas', '/api/admin/programas']]
          : [['Mi trayectoria', '/api/estudiantes/me/trayectoria'], ['Mis actividades', '/api/estudiantes/me/actividades'], ['Mis solicitudes', '/api/solicitudes/mias']]
      const results = await Promise.all(endpoints.map(async ([label, path]) => {
        try {
          const value = await api.get(path)
          const count = Array.isArray(value) ? value.length : value?.totalElements ?? value?.content?.length ?? 0
          return { label, count, ok: true }
        } catch {
          return { label, count: '—', ok: false }
        }
      }))
      if (alive) { setStats(results); setLoading(false) }
    }
    load()
    return () => { alive = false }
  }, [role])
  const roleHeading = role === 'ADMINISTRADOR' ? 'Administra tu comunidad académica.' : role === 'DOCENTE' ? 'Acompaña cada proceso de aprendizaje.' : 'Cada logro te acerca a tu meta.'
  return (
    <div className="page-content">
      <section className="welcome-banner">
        <div className="welcome-copy">
          <div className="eyebrow eyebrow-light"><span className="eyebrow-dot" /> TU CAMPUS DIGITAL</div>
          <h1>{roleHeading}</h1>
          <p>Hola, {user?.displayName?.split(' ')[0] || user?.email?.split('@')[0]}. Aquí tienes una vista general de tu actividad.</p>
          <button className="button button-white" onClick={() => setActive(role === 'ADMINISTRADOR' ? 'estudiantes' : role === 'DOCENTE' ? 'solicitudes-docente' : 'trayectoria')}>Explorar mi espacio <span>→</span></button>
        </div>
        <div className="welcome-art" aria-hidden="true"><div className="art-sun" /><div className="art-book"><span /><span /><span /></div><div className="art-leaf leaf-a" /><div className="art-leaf leaf-b" /><div className="art-dot dot-a" /><div className="art-dot dot-b" /></div>
      </section>
      <div className="section-heading"><div><span className="eyebrow">EN UN VISTAZO</span><h2>Tu resumen</h2></div><span className="muted small">Datos actualizados desde la plataforma</span></div>
      <div className="stats-grid">
        {(loading ? Array.from({ length: role === 'DOCENTE' ? 3 : 4 }, () => ({ label: 'Cargando', count: '…', icon: '▤' })) : stats).map((stat, index) => (
          <article className={`stat-card stat-${index}`} key={stat.label}>
            <div className="stat-top"><span className="stat-icon">{['▤', '♙', '✳', '↗'][index % 4]}</span><span className="stat-period">AHORA</span></div>
            <strong className="stat-value">{stat.count}</strong><span className="stat-label">{stat.label}</span>
          </article>
        ))}
      </div>
      <div className="dashboard-lower">
        <section className="panel welcome-note"><div className="panel-heading"><div><span className="eyebrow">UN ESPACIO PARA TI</span><h3>Aprender es avanzar</h3></div><span className="note-icon">✦</span></div><p>Consulta tu información, gestiona tus procesos académicos y mantén todo tu camino universitario organizado desde un mismo lugar.</p><div className="note-link" onClick={() => setActive(role === 'ADMINISTRADOR' ? 'programas' : role === 'DOCENTE' ? 'estudiantes-docente' : 'actividades')}>Ir a mis herramientas <span>→</span></div></section>
        <section className="panel quick-links"><div className="panel-heading"><div><span className="eyebrow">ACCESOS DIRECTOS</span><h3>¿Qué necesitas hacer?</h3></div></div><QuickLink icon="books" text={role === 'ADMINISTRADOR' ? 'Gestionar catálogos' : role === 'DOCENTE' ? 'Buscar estudiantes' : 'Ver mi trayectoria'} onClick={() => setActive(role === 'ADMINISTRADOR' ? 'programas' : role === 'DOCENTE' ? 'estudiantes-docente' : 'trayectoria')} /><QuickLink icon="requests" text={role === 'ADMINISTRADOR' ? 'Revisar trazabilidad' : 'Consultar solicitudes'} onClick={() => setActive(role === 'ADMINISTRADOR' ? 'auditoria' : role === 'DOCENTE' ? 'solicitudes-docente' : 'solicitudes')} /></section>
      </div>
      <div className="page-footnote">AulaNexo <span>·</span> Gestión académica con propósito</div>
    </div>
  )
}

function QuickLink({ icon, text, onClick }) {
  return <button className="quick-link" onClick={onClick}><span className="quick-link-icon"><Icon name={icon} /></span><span>{text}</span><b>→</b></button>
}

const fieldTypes = {
  date: 'date', number: 'number', email: 'email', password: 'password', url: 'url', text: 'text',
}

function FieldInput({ field, value, onChange, options }) {
  const common = { id: field.name, name: field.name, value: value ?? '', required: field.required !== false, onChange: (event) => onChange(field.name, event.target.value) }
  if (field.type === 'select') {
    return <select {...common}><option value="">Selecciona…</option>{(options || field.options || []).map((option) => <option key={String(option.value)} value={option.value}>{option.label}</option>)}</select>
  }
  if (field.type === 'boolean') return <select {...common}><option value="true">Sí / Activo</option><option value="false">No / Inactivo</option></select>
  if (field.type === 'textarea') return <textarea {...common} rows={3} placeholder={field.placeholder || ''} />
  if (field.type === 'enum') return <select {...common}><option value="">Selecciona…</option>{(enums[field.enum] || []).map((item) => <option key={item} value={item}>{item.replaceAll('_', ' ')}</option>)}</select>
  return <input {...common} type={fieldTypes[field.type] || 'text'} min={field.min} max={field.max} step={field.step} placeholder={field.placeholder || ''} />
}

function ResourcePage({ resource, onNotice }) {
  const [rows, setRows] = useState([])
  const [optionsByName, setOptionsByName] = useState({})
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')
  const [modal, setModal] = useState(null)
  const [busy, setBusy] = useState(false)

  const load = useCallback(async () => {
    try {
      const result = await api.get(resource.listPath)
      setRows(Array.isArray(result) ? result : result?.content || [])
    } catch (err) {
      setError(errorMessage(err))
    } finally {
      setLoading(false)
    }
  }, [resource.listPath])

  useEffect(() => { void Promise.resolve().then(load) }, [load])
  useEffect(() => {
    let alive = true
    const optionFields = resource.fields.filter((field) => field.optionsPath)
    Promise.all(optionFields.map(async (field) => {
      try {
        const values = await api.get(field.optionsPath)
        const items = (Array.isArray(values) ? values : values?.content || []).map((item) => ({
          value: item[field.optionValue || 'id'],
          label: field.optionLabel ? field.optionLabel(item) : `${item[field.optionValue || 'id']} · ${item.nombre || item.codigo || item.correo || item.id}`,
        }))
        return [field.name, items]
      } catch {
        return [field.name, []]
      }
    })).then((entries) => { if (alive) setOptionsByName(Object.fromEntries(entries)) })
    return () => { alive = false }
  }, [resource.fields])

  const filtered = useMemo(() => {
    const needle = query.trim().toLocaleLowerCase()
    if (!needle) return rows
    return rows.filter((row) => Object.values(row).some((value) => String(value ?? '').toLocaleLowerCase().includes(needle)))
  }, [rows, query])
  const formFields = modal?.mode === 'edit' && resource.updateFields ? resource.updateFields : resource.fields
  const columns = resource.columns || resource.fields.filter((field) => !['password'].includes(field.type)).slice(0, 5).map((field) => ({ key: field.name, label: field.label }))

  function startCreate() {
    setModal({ mode: 'create', values: Object.fromEntries(resource.fields.map((field) => [field.name, field.default ?? ''])) })
  }
  async function startEdit(row) {
    if (!resource.detailPath) {
      const fields = resource.updateFields || resource.fields
      setModal({ mode: 'edit', row, values: Object.fromEntries(fields.map((field) => [field.name, row[field.name] ?? field.default ?? ''])) })
      return
    }
    try {
      const detailPath = resource.detailPath(row)
      const detail = await api.get(detailPath)
      const fields = resource.updateFields || resource.fields
      setModal({ mode: 'edit', row: detail, values: Object.fromEntries(fields.map((field) => [field.name, detail[field.name] ?? field.default ?? ''])) })
    } catch (err) { onNotice(errorMessage(err), 'error') }
  }
  async function submit(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const payload = buildPayload({ fields: formFields }, modal.values)
      if (modal.mode === 'create') await api.post(resource.createPath, payload)
      else await api.put(resource.updatePath(modal.row), payload)
      setModal(null)
      onNotice(resource.createdMessage && modal.mode === 'create' ? resource.createdMessage : modal.mode === 'create' ? 'Registro creado correctamente.' : 'Cambios guardados correctamente.', 'success')
      await load()
    } catch (err) { onNotice(errorMessage(err), 'error') }
    finally { setBusy(false) }
  }
  async function remove(row) {
    const message = resource.deleteConfirmation || `¿Confirmas eliminar o desactivar este registro?`
    if (!window.confirm(message)) return
    try {
      await api.delete(resource.deletePath(row))
      onNotice(resource.deleteSuccess || 'Operación completada.', 'success')
      await load()
    } catch (err) { onNotice(errorMessage(err), 'error') }
  }
  async function details(row) {
    if (!resource.detailPath) { setModal({ mode: 'view', row, values: row }); return }
    try {
      const detail = await api.get(resource.detailPath(row))
      setModal({ mode: 'view', row: detail, values: detail })
    } catch (err) { onNotice(errorMessage(err), 'error') }
  }

  return (
    <div className="page-content">
      <div className="section-heading page-title-row"><div><span className="eyebrow">{resource.groupLabel}</span><h2>{resource.title}</h2><p>{resource.description}</p></div>
        {resource.createPath && <button className="button button-primary" onClick={startCreate}><Icon name="plus" /> {resource.createLabel || 'Nuevo registro'}</button>}</div>
      {error && <div className="notice notice-error">{error}<button className="text-button" onClick={load}>Reintentar</button></div>}
      <section className="panel table-panel">
        <div className="table-toolbar"><div><strong>{resource.tableTitle || `Registros de ${resource.title.toLocaleLowerCase()}`}</strong><span>{rows.length} en total</span></div><label className="searchbox"><Icon name="search" /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar en la tabla…" /></label></div>
        <div className="table-scroll"><table><thead><tr>{columns.map((column) => <th key={column.key}>{column.label}</th>)}<th className="actions-head">Acciones</th></tr></thead>
          <tbody>{loading ? <tr><td colSpan={columns.length + 1} className="table-empty">Cargando información…</td></tr>
            : filtered.length === 0 ? <tr><td colSpan={columns.length + 1} className="table-empty">{error ? 'No fue posible obtener los datos.' : 'No hay registros para mostrar.'}</td></tr>
              : filtered.map((row, index) => <tr key={resource.rowKey ? resource.rowKey(row) : row.id ?? index}>
                {columns.map((column) => <td key={column.key}>{column.render ? column.render(row) : displayValue(row[column.key])}</td>)}
                <td><div className="row-actions"><button title="Ver detalle" onClick={() => details(row)}>Ver</button>{resource.updatePath && <button title="Editar" onClick={() => startEdit(row)}>Editar</button>}{resource.deletePath && <button className="danger-text" title="Eliminar o desactivar" onClick={() => remove(row)}>Baja</button>}</div></td>
              </tr>)}
          </tbody></table></div>
        <div className="table-foot"><span>Mostrando {filtered.length} de {rows.length} resultados</span><button className="text-button" onClick={load}>Actualizar lista ↻</button></div>
      </section>
      {modal && <Modal title={modal.mode === 'create' ? resource.createLabel || `Nuevo ${resource.singular}` : modal.mode === 'edit' ? `Editar ${resource.singular}` : `Detalle: ${resource.singular}`} onClose={() => setModal(null)}>
        {modal.mode === 'view' ? <dl className="detail-list">{Object.entries(modal.values || {}).map(([key, value]) => <div key={key}><dt>{humanize(key)}</dt><dd>{displayValue(value)}</dd></div>)}</dl>
          : <form className="modal-form" onSubmit={submit}><div className="form-grid">{formFields.map((field) => <label key={field.name} className={field.type === 'textarea' ? 'field-wide' : ''}>{field.label}{field.help && <small>{field.help}</small>}<FieldInput field={field} value={modal.values[field.name]} options={optionsByName[field.name]} onChange={(name, value) => setModal((current) => ({ ...current, values: { ...current.values, [name]: value } }))} /></label>)}</div>
            <div className="modal-actions"><button type="button" className="button button-soft" onClick={() => setModal(null)}>Cancelar</button><button className="button button-primary" disabled={busy}>{busy ? 'Guardando…' : 'Guardar cambios'}</button></div>
          </form>}
      </Modal>}
    </div>
  )
}

function buildPayload(resource, values) {
  const payload = {}
  for (const field of resource.fields) {
    const value = values[field.name]
    if (value === '' || value === undefined) {
      if (field.required === false) payload[field.name] = null
      continue
    }
    if (field.type === 'number' || field.typeValue === 'number') payload[field.name] = Number(value)
    else if (field.type === 'boolean') payload[field.name] = value === true || value === 'true'
    else payload[field.name] = value
  }
  return payload
}

function displayValue(value) {
  if (value === null || value === undefined || value === '') return <span className="muted">—</span>
  if (typeof value === 'boolean') return <span className={`status-pill ${value ? 'status-positive' : 'status-neutral'}`}>{value ? 'Activo' : 'Inactivo'}</span>
  if (typeof value === 'object') return JSON.stringify(value)
  const valueString = String(value)
  if (valueString.length > 74) return `${valueString.slice(0, 71)}…`
  return valueString.replaceAll('_', ' ')
}
function humanize(value) { return value.replace(/([A-Z])/g, ' $1').replaceAll('_', ' ').replace(/^./, (letter) => letter.toUpperCase()) }

function Modal({ title, children, onClose }) {
  useEffect(() => {
    const handleKey = (event) => { if (event.key === 'Escape') onClose() }
    window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [onClose])
  return <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}><section className="modal-card" role="dialog" aria-modal="true" aria-label={title}><header className="modal-header"><div><span className="eyebrow">AULANEXO · GESTIÓN</span><h3>{title}</h3></div><button className="icon-button" onClick={onClose} aria-label="Cerrar"><Icon name="close" /></button></header>{children}</section></div>
}

function StudentSpace({ page, onNotice }) {
  if (page === 'trayectoria') return <SimpleDataPage title="Mi trayectoria académica" group="MI ESPACIO" description="Consulta el avance en tu plan de estudios, tus matrículas y los requisitos de cada asignatura." path="/api/estudiantes/me/trayectoria" onNotice={onNotice} />
  if (page === 'perfil') return <SimpleDataPage title="Mi perfil" group="MI ESPACIO" description="Información del perfil de estudiante asociado a tu cuenta." path="/api/estudiantes/me" onNotice={onNotice} />
  if (page === 'sociodemografico') return <SimpleDataPage title="Perfil sociodemográfico" group="MI ESPACIO" description="Mantén actualizada tu información de caracterización." path="/api/estudiantes/me/sociodemografico" editPath="/api/estudiantes/me/sociodemografico" fields={socioFields} onNotice={onNotice} />
  if (page === 'actividades') return <ResourcePage resource={studentActivities} onNotice={onNotice} />
  if (page === 'solicitudes') return <StudentRequests onNotice={onNotice} />
  if (page === 'notificaciones') return <Notifications onNotice={onNotice} />
  return null
}

function SimpleDataPage({ title, group, description, path, editPath, fields = [], onNotice }) {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)
  const [values, setValues] = useState({})
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const load = useCallback(async () => {
    try { const result = await api.get(path); setData(result); setValues({ ...result }) }
    catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [path])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  async function save(event) {
    event.preventDefault(); setBusy(true)
    try {
      const payload = buildPayload({ fields }, values)
      const result = await api.put(editPath, payload)
      setData(result); setValues({ ...result }); setEditing(false)
      onNotice('Información actualizada correctamente.', 'success')
    } catch (err) { onNotice(errorMessage(err), 'error') }
    finally { setBusy(false) }
  }
  return <div className="page-content"><div className="section-heading page-title-row"><div><span className="eyebrow">{group}</span><h2>{title}</h2><p>{description}</p></div>{editPath && <button className="button button-primary" onClick={() => setEditing((current) => !current)}>{editing ? 'Cancelar' : 'Editar información'}</button>}</div>
    {error && <div className="notice notice-error">{error}<button className="text-button" onClick={load}>Reintentar</button></div>}
    <section className="panel data-panel">{loading ? <div className="loading-line">Cargando datos…</div> : editing ? <form onSubmit={save} className="form-grid">{fields.map((field) => <label key={field.name} className={field.type === 'textarea' ? 'field-wide' : ''}>{field.label}<FieldInput field={field} value={values[field.name]} onChange={(name, value) => setValues((old) => ({ ...old, [name]: value }))} /></label>)}<div className="field-wide modal-actions"><button className="button button-primary" disabled={busy}>{busy ? 'Guardando…' : 'Guardar'}</button></div></form> : <dl className="detail-list">{Object.entries(data || {}).map(([key, value]) => <div key={key}><dt>{humanize(key)}</dt><dd>{displayValue(value)}</dd></div>)}</dl>}</section>
  </div>
}

function StudentRequests({ onNotice }) {
  const resource = {
    ...simpleListResource,
    title: 'Mis solicitudes', singular: 'solicitud', groupLabel: 'MI ESPACIO',
    description: 'Consulta el estado y seguimiento de las solicitudes de cancelación que has creado.',
    listPath: '/api/solicitudes/mias', createPath: '/api/solicitudes',
    fields: [{ name: 'matriculaId', label: 'Matrícula', type: 'number' }, { name: 'motivo', label: 'Motivo de cancelación', type: 'textarea' }],
    columns: [{ key: 'id', label: 'Solicitud' }, { key: 'asignatura', label: 'Asignatura' }, { key: 'periodo', label: 'Periodo' }, { key: 'estado', label: 'Estado', render: (row) => <span className={`status-pill ${statusClass(row.estado)}`}>{row.estado?.replaceAll('_', ' ')}</span> }, { key: 'fechaCreacion', label: 'Fecha' }],
    detailPath: (row) => `/api/solicitudes/${row.id}`,
  }
  return <ResourcePage resource={resource} onNotice={onNotice} />
}

function Notifications({ onNotice }) {
  const [rows, setRows] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const load = useCallback(async () => {
    try { setRows(await api.get('/api/notificaciones')) }
    catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  async function markRead(id) {
    try { await api.patch(`/api/notificaciones/${encodeURIComponent(id)}/leida`); await load(); onNotice('Notificación marcada como leída.', 'success') }
    catch (err) { onNotice(errorMessage(err), 'error') }
  }
  return <div className="page-content"><div className="section-heading page-title-row"><div><span className="eyebrow">MI ESPACIO</span><h2>Notificaciones</h2><p>Actualizaciones sobre tus solicitudes y actividad académica.</p></div><button className="button button-soft" onClick={load}>Actualizar ↻</button></div>{error && <div className="notice notice-error">{error}</div>}<div className="notification-list">{loading ? <section className="panel table-empty">Cargando notificaciones…</section> : rows.length ? rows.map((row) => <article className={`notification-card ${row.leida ? '' : 'unread'}`} key={row.id}><div className="notification-symbol"><Icon name="bell" /></div><div className="notification-body"><div className="notification-title">{row.tipo?.replaceAll('_', ' ') || 'Actualización'} <span className={`status-pill ${statusClass(row.estado)}`}>{row.estado}</span></div><p>{row.mensaje}</p><small>{row.fecha || ''}</small></div>{!row.leida && <button className="text-button" onClick={() => markRead(row.id)}>Marcar leída</button>}</article>) : <section className="panel table-empty">Aún no tienes notificaciones.</section>}</div></div>
}

function TeacherSpace({ page, onNotice }) {
  if (page === 'solicitudes-docente') return <TeacherRequests onNotice={onNotice} />
  if (page === 'estudiantes-docente') return <TeacherStudents onNotice={onNotice} />
  if (page === 'reporte-solicitudes') return <ReportPage title="Reporte de solicitudes" path="/api/reportes/solicitudes" filters={reportRequestFilters} onNotice={onNotice} />
  if (page === 'reporte-academico') return <ReportPage title="Desempeño académico" path="/api/reportes/desempeno" filters={programFilter} onNotice={onNotice} />
  if (page === 'reporte-participacion') return <ReportPage title="Participación estudiantil" path="/api/reportes/participacion" filters={programFilter} onNotice={onNotice} />
  return null
}

function TeacherRequests({ onNotice }) {
  const [rows, setRows] = useState([])
  const [state, setState] = useState('PENDIENTE')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null)
  const [nextState, setNextState] = useState('EN_REVISION')
  const [observation, setObservation] = useState('')
  const load = useCallback(async () => {
    try { setRows(await api.get(`/api/docente/solicitudes?estado=${state}`)) }
    catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [state])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  async function submitUpdate(event) {
    event.preventDefault()
    try {
      await api.patch(`/api/docente/solicitudes/${selected.id}`, { estado: nextState, observacion: observation })
      setSelected(null); setObservation('')
      onNotice('Estado de solicitud actualizado y seguimiento registrado.', 'success')
      await load()
    } catch (err) { onNotice(errorMessage(err), 'error') }
  }
  const transitionOptions = state === 'PENDIENTE' ? ['EN_REVISION'] : state === 'EN_REVISION' ? ['APROBADA', 'RECHAZADA'] : []
  return <div className="page-content"><div className="section-heading page-title-row"><div><span className="eyebrow">ESPACIO DOCENTE</span><h2>Bandeja de solicitudes</h2><p>Revisa cada caso, registra observaciones y avanza el flujo de atención.</p></div><select className="filter-select" value={state} onChange={(e) => setState(e.target.value)}>{enums.EstadoSolicitud.map((item) => <option key={item}>{item}</option>)}</select></div>
    {error && <div className="notice notice-error">{error}</div>}<section className="panel table-panel"><div className="table-toolbar"><div><strong>Solicitudes {state.toLowerCase().replaceAll('_', ' ')}</strong><span>{rows.length} casos</span></div><button className="text-button" onClick={load}>Actualizar ↻</button></div><div className="table-scroll"><table><thead><tr><th>ID</th><th>Asignatura</th><th>Periodo</th><th>Motivo</th><th>Creada</th><th>Acciones</th></tr></thead><tbody>{loading ? <tr><td colSpan="6" className="table-empty">Cargando solicitudes…</td></tr> : rows.length === 0 ? <tr><td colSpan="6" className="table-empty">No hay solicitudes en este estado.</td></tr> : rows.map((row) => <tr key={row.id}><td>#{row.id}</td><td>{row.asignatura}</td><td>{row.periodo}</td><td>{displayValue(row.motivo)}</td><td>{displayValue(row.fechaCreacion)}</td><td><button className="table-action" onClick={() => { setSelected(row); setNextState(state === 'PENDIENTE' ? 'EN_REVISION' : 'APROBADA') }}>Revisar →</button></td></tr>)}</tbody></table></div></section>
    {selected && <Modal title={`Solicitud #${selected.id}`} onClose={() => setSelected(null)}><div className="request-detail"><p><strong>Motivo:</strong> {selected.motivo}</p><p><strong>Asignatura:</strong> {selected.asignatura} · {selected.periodo}</p><h4>Seguimiento</h4>{(selected.seguimientos || []).map((item) => <div className="timeline-item" key={item.id}><span className="timeline-dot" /><div><strong>{item.estado?.replaceAll('_', ' ')}</strong><p>{item.observacion}</p><small>{item.responsable} · {item.fecha}</small></div></div>)}</div><form onSubmit={submitUpdate} className="form-stack"><label>Nuevo estado<select value={nextState} onChange={(e) => setNextState(e.target.value)}>{transitionOptions.map((item) => <option key={item}>{item}</option>)}</select></label><label>Observación<textarea rows="3" maxLength="2000" value={observation} onChange={(e) => setObservation(e.target.value)} /></label><div className="modal-actions"><button type="button" className="button button-soft" onClick={() => setSelected(null)}>Cerrar</button><button className="button button-primary">Guardar decisión</button></div></form></Modal>}
  </div>
}

function TeacherStudents({ onNotice }) {
  const [query, setQuery] = useState('')
  const [programaId, setProgramaId] = useState('')
  const [estado, setEstado] = useState('')
  const [programs, setPrograms] = useState([])
  const [rows, setRows] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [selected, setSelected] = useState(null)
  useEffect(() => { api.get('/api/admin/programas').then(setPrograms).catch((err) => setError(errorMessage(err))) }, [])
  const search = useCallback(async (event) => {
    if (event) { event.preventDefault(); setLoading(true); setError('') }
    const params = new URLSearchParams()
    if (query.trim()) params.set('q', query.trim())
    if (programaId) params.set('programaId', programaId)
    if (estado) params.set('estado', estado)
    try { setRows(await api.get(`/api/docente/estudiantes?${params}`)) }
    catch (err) { setError(errorMessage(err)) }
    finally { setLoading(false) }
  }, [estado, programaId, query])
  useEffect(() => { void Promise.resolve().then(search) }, [search])
  async function openProfile(row) {
    try {
      const [perfil, trayectoria, actividades, social] = await Promise.all([
        api.get(`/api/estudiantes/${row.id}/perfil`),
        api.get(`/api/estudiantes/${row.id}/trayectoria`),
        api.get(`/api/docente/estudiantes/${row.id}/actividades`),
        api.get(`/api/estudiantes/${row.id}/sociodemografico`),
      ])
      setSelected({ perfil, trayectoria, actividades, social })
    } catch (err) { onNotice(errorMessage(err), 'error') }
  }
  return <div className="page-content"><div className="section-heading"><div><span className="eyebrow">ESPACIO DOCENTE</span><h2>Estudiantes</h2><p>Encuentra estudiantes y consulta su información académica.</p></div></div><form className="filter-panel panel" onSubmit={search}><label>Buscar estudiante<input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Nombre o código" /></label><label>Programa<select value={programaId} onChange={(e) => setProgramaId(e.target.value)}><option value="">Todos los programas</option>{programs.map((program) => <option key={program.id} value={program.id}>{program.nombre}</option>)}</select></label><label>Estado<select value={estado} onChange={(e) => setEstado(e.target.value)}><option value="">Todos</option>{enums.EstadoEstudiante.map((item) => <option key={item}>{item}</option>)}</select></label><button className="button button-primary">Buscar estudiantes</button></form>{error && <div className="notice notice-error">{error}</div>}<StudentTable rows={rows} loading={loading} onSelect={openProfile} />{selected && <Modal title={`Ficha de ${selected.perfil.nombre} ${selected.perfil.apellido}`} onClose={() => setSelected(null)}><div className="profile-tabs"><h4>Perfil académico</h4><dl className="detail-list">{Object.entries(selected.perfil).map(([key, value]) => <div key={key}><dt>{humanize(key)}</dt><dd>{displayValue(value)}</dd></div>)}</dl><h4>Perfil sociodemográfico</h4><dl className="detail-list">{Object.entries(selected.social || {}).map(([key, value]) => <div key={key}><dt>{humanize(key)}</dt><dd>{displayValue(value)}</dd></div>)}</dl><h4>Trayectoria ({selected.trayectoria.length})</h4><div className="compact-list">{selected.trayectoria.map((item) => <div key={item.planAsignaturaId}><span>{item.nombre}</span><span className={`status-pill ${statusClass(item.estado)}`}>{item.estado}</span></div>)}</div><h4>Actividades ({selected.actividades.length})</h4><div className="compact-list">{selected.actividades.map((item, index) => <div key={item.id || index}><span>{item.nombre}</span><span>{item.categoria}</span></div>)}</div></div></Modal>}</div>
}

function StudentTable({ rows, loading, onSelect }) {
  return <section className="panel table-panel"><div className="table-scroll"><table><thead><tr><th>Código</th><th>Estudiante</th><th>Programa</th><th>Estado</th><th></th></tr></thead><tbody>{loading ? <tr><td colSpan="5" className="table-empty">Buscando…</td></tr> : rows.length === 0 ? <tr><td colSpan="5" className="table-empty">No hay estudiantes para mostrar.</td></tr> : rows.map((row) => <tr key={row.id}><td>{row.codigoEstudiantil}</td><td>{row.nombre} {row.apellido}</td><td>{row.programa}</td><td><span className={`status-pill ${statusClass(row.estado)}`}>{row.estado}</span></td><td><button className="table-action" onClick={() => onSelect(row)}>Ver ficha →</button></td></tr>)}</tbody></table></div></section>
}

function ReportPage({ title, path, filters, onNotice }) {
  const [values, setValues] = useState({})
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [filterOptions, setFilterOptions] = useState({})
  const [filterError, setFilterError] = useState('')
  useEffect(() => {
    const optionFields = filters.filter((field) => field.optionsPath)
    if (!optionFields.length) return
    Promise.all(optionFields.map(async (field) => [field.name, await api.get(field.optionsPath)]))
      .then((entries) => setFilterOptions(Object.fromEntries(entries)))
      .catch((error) => setFilterError(errorMessage(error)))
  }, [filters])
  async function run(event) {
    event.preventDefault(); setLoading(true)
    const params = new URLSearchParams()
    Object.entries(values).forEach(([key, value]) => { if (value !== '' && value != null) params.set(key, value) })
    try { setResult(await api.get(`${path}?${params}`)) }
    catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setLoading(false) }
  }
  return <div className="page-content"><div className="section-heading"><div><span className="eyebrow">REPORTES</span><h2>{title}</h2><p>Consulta indicadores agregados con filtros opcionales.</p></div></div>{filterError && <div className="notice notice-error">{filterError}</div>}<form className="panel filter-panel" onSubmit={run}>{filters.map((field) => <label key={field.name}>{field.label}{field.type === 'enum' ? <select value={values[field.name] || ''} onChange={(e) => setValues({ ...values, [field.name]: e.target.value })}><option value="">Todos</option>{enums[field.enum].map((item) => <option key={item}>{item}</option>)}</select> : field.optionsPath ? <select value={values[field.name] || ''} onChange={(e) => setValues({ ...values, [field.name]: e.target.value })}><option value="">Todos</option>{(filterOptions[field.name] || []).map((option) => <option value={option.id} key={option.id}>{option.nombre}</option>)}</select> : <input type="number" value={values[field.name] || ''} onChange={(e) => setValues({ ...values, [field.name]: e.target.value })} />}</label>)}<button className="button button-primary" disabled={loading}>{loading ? 'Generando…' : 'Generar reporte'}</button></form>{result && <section className="panel report-result"><div className="panel-heading"><div><span className="eyebrow">RESULTADO</span><h3>{title}</h3></div><button className="text-button" onClick={() => navigator.clipboard?.writeText(JSON.stringify(result, null, 2))}>Copiar JSON</button></div><JsonView value={result} /></section>}</div>
}

function JsonView({ value }) {
  if (Array.isArray(value)) return <div className="json-array">{value.length ? value.map((item, index) => <article className="json-record" key={index}><JsonView value={item} /></article>) : <p className="muted">Sin datos para los filtros seleccionados.</p>}</div>
  if (value && typeof value === 'object') return <dl className="detail-list">{Object.entries(value).map(([key, item]) => <div key={key}><dt>{humanize(key)}</dt><dd>{item && typeof item === 'object' ? <JsonView value={item} /> : displayValue(item)}</dd></div>)}</dl>
  return <span>{displayValue(value)}</span>
}

const simpleListResource = {
  rowKey: (row) => row.id, columns: [], fields: [], listPath: '',
}
const socioFields = [
  { name: 'municipioResidencia', label: 'Municipio de residencia', required: false },
  { name: 'estrato', label: 'Estrato', type: 'number', min: 1, max: 6, required: false },
  { name: 'situacionLaboral', label: 'Situación laboral', required: false },
  { name: 'requiereApoyo', label: '¿Requiere apoyo?', type: 'boolean' },
  { name: 'observacion', label: 'Observación', type: 'textarea', required: false },
]
const reportRequestFilters = [
  { name: 'estado', label: 'Estado de solicitud', type: 'enum', enum: 'EstadoSolicitud' },
  { name: 'programaId', label: 'Programa', optionsPath: '/api/admin/programas' },
  { name: 'periodoId', label: 'Periodo', optionsPath: '/api/admin/periodos' },
]
const programFilter = [{ name: 'programaId', label: 'Programa', optionsPath: '/api/admin/programas' }]
const studentActivities = {
  title: 'Mis actividades', singular: 'actividad', groupLabel: 'MI ESPACIO',
  description: 'Registra y consulta tus actividades integrales. Los datos se asocian automáticamente a tu usuario.',
  listPath: '/api/estudiantes/me/actividades', createPath: '/api/estudiantes/me/actividades',
  createLabel: 'Registrar actividad',
  fields: [
    { name: 'categoria', label: 'Categoría', type: 'enum', enum: 'CategoriaActividad' },
    { name: 'nombre', label: 'Nombre de la actividad' },
    { name: 'descripcion', label: 'Descripción', type: 'textarea', required: false },
    { name: 'organizacion', label: 'Organización', required: false },
    { name: 'fechaInicio', label: 'Fecha de inicio', type: 'date', required: false },
    { name: 'fechaFin', label: 'Fecha de finalización', type: 'date', required: false },
    { name: 'evidenciaUrl', label: 'URL de evidencia', type: 'url', required: false },
  ],
  columns: [{ key: 'nombre', label: 'Actividad' }, { key: 'categoria', label: 'Categoría' }, { key: 'organizacion', label: 'Organización' }, { key: 'fechaInicio', label: 'Inicio' }],
}

function statusClass(value) {
  if (['APROBADA', 'ACTIVO', 'APROBADO', 'CURSANDO'].includes(value)) return 'status-positive'
  if (['RECHAZADA', 'INACTIVO', 'CANCELADA', 'REPROBADA', 'RETIRADO'].includes(value)) return 'status-negative'
  if (['PENDIENTE', 'EN_REVISION', 'MATRICULADA'].includes(value)) return 'status-warning'
  return 'status-neutral'
}

function App() {
  const [user, setUser] = useState(null)
  const [role, setRole] = useState('')
  const [active, setActive] = useState('inicio')
  const [loading, setLoading] = useState(true)
  const [loginMessage, setLoginMessage] = useState('')
  const [notice, setNotice] = useState(null)
  const [mobileOpen, setMobileOpen] = useState(false)

  const showNotice = useCallback((text, type = 'success') => {
    setNotice({ text, type })
    window.setTimeout(() => setNotice(null), 5200)
  }, [])
  const detect = useCallback(async (currentUser) => {
    setLoading(true)
    try {
      api.setTokenProvider(async () => currentUser.getIdToken())
      const currentRole = await identifyRole()
      setUser(currentUser)
      setRole(currentRole)
      setLoginMessage('')
      setActive('inicio')
    } catch (error) {
      await signOut(auth).catch(() => {})
      setUser(null); setRole('')
      setLoginMessage(errorMessage(error))
    } finally { setLoading(false) }
  }, [])

  useEffect(() => onAuthStateChanged(auth, (currentUser) => {
    if (currentUser) detect(currentUser)
    else { setUser(null); setRole(''); setLoading(false); api.setTokenProvider(null) }
  }), [detect])
  async function login() {
    const currentUser = auth.currentUser
    if (currentUser) await detect(currentUser)
  }
  async function logout() { await signOut(auth); setActive('inicio') }

  const activeItem = useMemo(() => {
    const groups = role === 'ADMINISTRADOR' ? adminGroups : role === 'DOCENTE' ? teacherNav : studentNav
    return groups.flatMap((group) => group.items).find((item) => item.id === active)
  }, [active, role])

  let content = null
  if (active === 'inicio') content = <Dashboard role={role} user={user} setActive={setActive} />
  else if (role === 'ADMINISTRADOR') {
    const resource = allAdminResources[active]
    if (active === 'auditoria') content = <AuditPage onNotice={showNotice} />
    else if (active === 'usuarios') content = <UserManagement onNotice={showNotice} />
    else if (active === 'perfil-admin') content = <AdminSocioProfile onNotice={showNotice} />
    else if (resource) content = <ResourcePage resource={resource} onNotice={showNotice} />
  } else if (role === 'ESTUDIANTE') content = <StudentSpace page={active} onNotice={showNotice} />
  else if (role === 'DOCENTE') content = <TeacherSpace page={active} onNotice={showNotice} />

  if (!user || !role) return <Login onLogin={login} loading={loading} message={loginMessage} onMessage={setLoginMessage} />

  return <div className="app-shell"><Sidebar role={role} active={active} setActive={setActive} user={user} onLogout={logout} open={mobileOpen} onClose={() => setMobileOpen(false)} /><main className="main-shell"><Topbar activeItem={activeItem} user={user} onMenu={() => setMobileOpen(true)} /><div className="workspace">{content || <div className="page-content"><div className="notice notice-warning">No hay una pantalla disponible para esta sección.</div></div>}</div></main>{notice && <div className={`toast toast-${notice.type}`} role="status">{notice.type === 'success' ? '✓' : '!'} {notice.text}</div>}</div>
}

const allAdminResources = Object.fromEntries(adminGroups.flatMap((group) => group.items.filter((item) => item.resource).map((item) => [item.id, item.resource])))
function AuditPage({ onNotice }) {
  const [filters, setFilters] = useState({ entidad: '', registroId: '', usuarioId: '', page: '0', size: '50' })
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const search = useCallback(async (event) => {
    if (event) { event.preventDefault(); setLoading(true) }
    const params = new URLSearchParams()
    Object.entries(filters).forEach(([key, value]) => { if (value !== '') params.set(key, value) })
    try { setResult(await api.get(`/api/admin/auditoria?${params}`)) }
    catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setLoading(false) }
  }, [filters, onNotice])
  useEffect(() => { void Promise.resolve().then(search) }, [search])
  const rows = result?.content || []
  return <div className="page-content"><div className="section-heading"><div><span className="eyebrow">ADMINISTRACIÓN · TRAZABILIDAD</span><h2>Auditoría del sistema</h2><p>Consulta quién modificó qué registro y revisa los valores anteriores y nuevos.</p></div></div><form className="panel filter-panel" onSubmit={search}><label>Entidad<input value={filters.entidad} onChange={(e) => setFilters({ ...filters, entidad: e.target.value })} placeholder="usuario, estudiante…" /></label><label>ID de registro<input value={filters.registroId} onChange={(e) => setFilters({ ...filters, registroId: e.target.value })} /></label><label>ID de usuario<input type="number" value={filters.usuarioId} onChange={(e) => setFilters({ ...filters, usuarioId: e.target.value })} /></label><label>Página<input type="number" min="0" value={filters.page} onChange={(e) => setFilters({ ...filters, page: e.target.value })} /></label><label>Tamaño<input type="number" min="1" max="100" value={filters.size} onChange={(e) => setFilters({ ...filters, size: e.target.value })} /></label><button className="button button-primary" disabled={loading}>{loading ? 'Consultando…' : 'Filtrar auditoría'}</button></form><section className="panel table-panel"><div className="table-scroll"><table><thead><tr><th>Fecha</th><th>Usuario actor</th><th>Entidad</th><th>ID registro</th><th>Operación</th><th>Antes</th><th>Después</th></tr></thead><tbody>{loading ? <tr><td colSpan="7" className="table-empty">Consultando auditoría…</td></tr> : rows.length === 0 ? <tr><td colSpan="7" className="table-empty">No hay cambios para los filtros seleccionados.</td></tr> : rows.map((row) => <tr key={row.id}><td>{displayValue(row.fecha)}</td><td>{displayValue(row.usuarioId)}</td><td>{displayValue(row.entidad)}</td><td>{displayValue(row.registroId)}</td><td>{displayValue(row.operacion)}</td><td>{displayValue(row.valoresAnteriores)}</td><td>{displayValue(row.valoresNuevos)}</td></tr>)}</tbody></table></div><div className="table-foot"><span>{result?.totalElements ?? 0} registros · página {Number(result?.number ?? 0) + 1} de {result?.totalPages ?? 0}</span><button className="text-button" onClick={search}>Actualizar ↻</button></div></section></div>
}

function AdminSocioProfile({ onNotice }) {
  const [students, setStudents] = useState([])
  const [studentId, setStudentId] = useState('')
  const [data, setData] = useState(null)
  const [values, setValues] = useState({})
  const [loading, setLoading] = useState(false)
  const [busy, setBusy] = useState(false)
  useEffect(() => { api.get('/api/admin/estudiantes').then(setStudents).catch((error) => onNotice(errorMessage(error), 'error')) }, [onNotice])
  async function load() {
    if (!studentId) return
    setLoading(true)
    try {
      const result = await api.get(`/api/admin/estudiantes/${studentId}/sociodemografico`)
      const profile = result || { municipioResidencia: '', estrato: '', situacionLaboral: '', requiereApoyo: false, observacion: '' }
      setData(profile)
      setValues({ ...profile })
    } catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setLoading(false) }
  }
  async function save(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const result = await api.put(`/api/admin/estudiantes/${studentId}/sociodemografico`, buildPayload({ fields: socioFields }, values))
      setData(result); setValues({ ...result })
      onNotice('Perfil sociodemográfico guardado.', 'success')
    } catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setBusy(false) }
  }
  return <div className="page-content"><div className="section-heading"><div><span className="eyebrow">ADMINISTRACIÓN · COMUNIDAD</span><h2>Perfiles sociodemográficos</h2><p>Consulta o actualiza el perfil de un estudiante; esta información no se elimina desde el CRUD.</p></div></div><section className="panel filter-panel"><label>Estudiante<select value={studentId} onChange={(event) => { setStudentId(event.target.value); setData(null) }}><option value="">Selecciona un estudiante…</option>{students.map((student) => <option key={student.id} value={student.id}>{student.codigoEstudiantil} · {student.nombre} {student.apellido}</option>)}</select></label><button className="button button-primary" disabled={!studentId || loading} onClick={load}>{loading ? 'Consultando…' : 'Consultar perfil'}</button></section>{data && <section className="panel data-panel"><form className="form-grid" onSubmit={save}>{socioFields.map((field) => <label key={field.name} className={field.type === 'textarea' ? 'field-wide' : ''}>{field.label}<FieldInput field={field} value={values[field.name]} onChange={(name, value) => setValues((old) => ({ ...old, [name]: value }))} /></label>)}<div className="field-wide modal-actions"><button className="button button-primary" disabled={busy}>{busy ? 'Guardando…' : 'Guardar perfil'}</button></div></form></section>}</div>
}

function UserManagement({ onNotice }) {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [roles, setRoles] = useState({})
  const [codes, setCodes] = useState({})
  const [busyId, setBusyId] = useState(null)
  const [query, setQuery] = useState('')
  const load = useCallback(async () => {
    try { setUsers(await api.get('/api/admin/usuarios')) }
    catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setLoading(false) }
  }, [onNotice])
  useEffect(() => { void Promise.resolve().then(load) }, [load])
  async function updateRole(id) {
    setBusyId(id)
    try {
      await api.patch(`/api/admin/usuarios/${id}/rol`, { rol: roles[id] || 'ESTUDIANTE', codigoDocente: codes[id] || null })
      onNotice('Rol actualizado; el cambio quedó registrado en auditoría.', 'success')
      await load()
    } catch (error) { onNotice(errorMessage(error), 'error') }
    finally { setBusyId(null) }
  }
  const filtered = users.filter((item) => `${item.correo} ${item.nombre} ${item.apellido} ${item.rol}`.toLowerCase().includes(query.toLowerCase()))
  return <div className="page-content"><div className="section-heading"><div><span className="eyebrow">ADMINISTRACIÓN · ACCESO</span><h2>Usuarios y roles</h2><p>Consulta las cuentas y cambia roles con trazabilidad. El usuario no puede cambiar su propio rol.</p></div></div><div className="notice notice-info">Al asignar DOCENTE se crea el perfil docente cuando hace falta. Los perfiles estudiantiles y su historial se conservan.</div><section className="panel table-panel"><div className="table-toolbar"><div><strong>Cuentas registradas</strong><span>{users.length} usuarios</span></div><label className="searchbox"><Icon name="search" /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Buscar usuario…" /></label></div><div className="table-scroll"><table><thead><tr><th>Usuario</th><th>Correo</th><th>Rol actual</th><th>Nuevo rol</th><th>Código docente</th><th></th></tr></thead><tbody>{loading ? <tr><td colSpan="6" className="table-empty">Cargando…</td></tr> : filtered.map((item) => <tr key={item.id}><td>{item.nombre} {item.apellido}</td><td>{item.correo}</td><td><span className="status-pill status-neutral">{item.rol}</span></td><td><select value={roles[item.id] || item.rol} onChange={(e) => setRoles({ ...roles, [item.id]: e.target.value })}>{enums.RolNombre.map((roleValue) => <option key={roleValue}>{roleValue}</option>)}</select></td><td><input className="inline-input" value={codes[item.id] || ''} placeholder="Opcional" onChange={(e) => setCodes({ ...codes, [item.id]: e.target.value })} /></td><td><button className="table-action" disabled={busyId === item.id || (roles[item.id] || item.rol) === item.rol} onClick={() => updateRole(item.id)}>{busyId === item.id ? 'Guardando…' : 'Guardar rol'}</button></td></tr>)}</tbody></table></div></section></div>
}

export default App
