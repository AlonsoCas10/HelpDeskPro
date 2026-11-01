import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL
})

let accessToken = null

export function setAccessToken(token) {
  accessToken = token
}

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})

export default apiClient