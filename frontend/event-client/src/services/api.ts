import axios from 'axios';
import { useAuthStore } from 'src/stores/auth-store';

const api = axios.create({
  baseURL: '/api', // goes through dev proxy
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
