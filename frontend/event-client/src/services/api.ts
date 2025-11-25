import axios from 'axios';
import { useAuthStore } from 'src/stores/auth-store';

// Use env-provided base URL so capacitor/mobile hits the right host
const api = axios.create({
  baseURL: process.env.API_BASE_URL || '/api',
});

// Add Authorization: Bearer <token> to outgoing requests
api.interceptors.request.use((config) => {
  const auth = useAuthStore();

  if (auth.token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${auth.token}`;
  }

  return config;
});

export default api;
