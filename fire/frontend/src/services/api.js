import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
})

// Add response interceptor for better error handling
api.interceptors.response.use(
  response => response,
  error => Promise.reject(error)
)

export const userService = {
  login: (email, password) => 
    api.post('/api/users/login', { email, password }),
  
  register: (userData) => 
    api.post('/api/users/register', userData),

  getUserInterests: (userId) =>
    api.get(`/api/users/${userId}/interests`),

  updateUserInterests: (userId, districtIds) =>
    api.put(`/api/users/${userId}/interests`, districtIds)
}

export const regionService = {
  getAllProvinces: () => 
    api.get('/api/regions/provinces'),
  
  getDistrictsInProvince: (provinceName) => 
    api.get(`/api/regions/provinces/${provinceName}/districts`),

  getAllRegions: () =>
    api.get('/api/regions')
}

export const fireReportService = {
  getRecentReports: (userId, page = 0, size = 5) =>
    api.get(`/api/fire-reports/user/${userId}/recent?page=${page}&size=${size}`)
    .then(response => response.data),

  getFireAlerts: (userId) =>
    api.get(`/api/fire-reports/user/${userId}/alerts`)
    .then(response => response.data),

  getReportsByDateRange: (userId, startDate, endDate, page = 0, size = 5) =>
    api.get(`/api/fire-reports/user/${userId}/search`, {
      params: { startDate, endDate, page, size }
    }).then(response => response.data)
}

export default api