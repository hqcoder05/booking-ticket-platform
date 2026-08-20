import axios from 'axios';

// Create an Axios instance configured to proxy to the Spring Boot backend
export const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add interceptor to inject JWT token if it exists in local storage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Generic API response structure from Spring Boot ApiResponse<T>
export interface ApiResponse<T> {
  code: number;
  message: string;
  result: T;
  timestamp: string;
}
