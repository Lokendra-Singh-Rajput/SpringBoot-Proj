import axios from "axios";

const getBaseUrl = () => {
  if (import.meta.env.VITE_BASE_URL) {
    return import.meta.env.VITE_BASE_URL;
  }
  return import.meta.env.DEV ? "http://localhost:8080" : "";
};

const API = axios.create({
  baseURL: getBaseUrl(),
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export default API;