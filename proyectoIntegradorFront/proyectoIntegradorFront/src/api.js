const baseUrl = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/+$/, '')
let tokenProvider = null

export class ApiError extends Error {
  constructor(status, message, body) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

export const api = {
  setTokenProvider(provider) {
    tokenProvider = provider
  },
  async request(method, path, body) {
    const token = tokenProvider ? await tokenProvider() : null
    const headers = { Accept: 'application/json' }
    if (body !== undefined) headers['Content-Type'] = 'application/json'
    if (token) headers.Authorization = `Bearer ${token}`
    let response
    try {
      response = await fetch(`${baseUrl}${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
      })
    } catch {
      throw new ApiError(0, `No fue posible conectar con la API (${baseUrl}).`, null)
    }
    if (response.status === 204) return null
    const contentType = response.headers.get('content-type') || ''
    const result = contentType.includes('application/json')
      ? await response.json().catch(() => null)
      : await response.text().catch(() => '')
    if (!response.ok) {
      const message = result?.message || result?.error || result?.detail || (typeof result === 'string' ? result : null)
      throw new ApiError(response.status, message || `Error HTTP ${response.status}`, result)
    }
    return result
  },
  async download(path, filename) {
    const token = tokenProvider ? await tokenProvider() : null
    const headers = token ? { Authorization: `Bearer ${token}` } : {}
    let response
    try {
      response = await fetch(`${baseUrl}${path}`, { headers })
    } catch {
      throw new ApiError(0, `No fue posible conectar con la API (${baseUrl}).`, null)
    }
    if (!response.ok) {
      const result = await response.text().catch(() => '')
      throw new ApiError(response.status, result || `Error HTTP ${response.status}`, result)
    }
    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = filename
    document.body.appendChild(link)
    link.click()
    link.remove()
    URL.revokeObjectURL(objectUrl)
  },
  get(path) { return this.request('GET', path) },
  post(path, body) { return this.request('POST', path, body) },
  put(path, body) { return this.request('PUT', path, body) },
  patch(path, body) { return this.request('PATCH', path, body) },
  delete(path) { return this.request('DELETE', path) },
}
