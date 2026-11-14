import { getApp, getApps, initializeApp } from 'firebase/app'
import { getAuth } from 'firebase/auth'

const config = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY,
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN,
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID,
  appId: import.meta.env.VITE_FIREBASE_APP_ID,
}

export const firebaseConfigured = Boolean(config.apiKey && config.authDomain && config.projectId && config.appId)
const app = getApps().length ? getApp() : initializeApp(firebaseConfigured ? config : { apiKey: 'missing', authDomain: 'missing.firebaseapp.com', projectId: 'missing', appId: 'missing' })
export const auth = getAuth(app)
