import { defineStore } from 'pinia';
import api from 'src/services/api';

export type UserRole = 'ATTENDEE' | 'ORGANIZER' | 'ADMIN';

interface User {
  id: number | null;
  name: string;
  email: string;
  role: UserRole;
}

interface AuthState {
  token: string | null;
  user: User | null;
}

function getInitialUser(): User | null {
  const userStr = localStorage.getItem('user');
  if (!userStr) {
    return null;
  }
  try {
    return JSON.parse(userStr) as User;
  } catch (e) {
    console.error('Failed to parse user from localStorage', e);
    // Clear invalid user data
    localStorage.removeItem('user');
    return null;
  }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: localStorage.getItem('token'),
    user: getInitialUser(),
  }),

  getters: {
    isAuthenticated: (state) => !!state.token && !!state.user,
    role: (state): UserRole | null => (state.user ? state.user.role : null),
    isAdmin(): boolean {
      return this.role === 'ADMIN';
    },
    isOrganizer(): boolean {
      return this.role === 'ORGANIZER';
    },
    isAttendee(): boolean {
      return this.role === 'ATTENDEE';
    },
  },

  actions: {
    setAuth(token: string, user: User | null) {
      this.token = token;
      this.user = user;
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(user));
    },

    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    },

    async login(email: string, password: string) {
      try {
        const response = await api.post('/auth/login', { email, password });
        // Backend returns { token, role } so we hydrate a minimal user locally
        const user: User = {
          id: response.data.user?.id ?? null,
          name: response.data.user?.name ?? email.split('@')[0],
          email: response.data.user?.email ?? email,
          role: response.data.user?.role ?? response.data.role,
        };
        this.setAuth(response.data.token, user);
      } catch (error) {
        console.error('Login failed:', error);
        throw error;
      }
    },

    async register(name: string, email: string, password: string, role: UserRole = 'ATTENDEE') {
      try {
        await api.post('/auth/register', { name, email, password, role });
        // Immediately log in to keep UX smooth
        await this.login(email, password);
      } catch (error) {
        console.error('Registration failed:', error);
        throw error;
      }
    },
  },
});
