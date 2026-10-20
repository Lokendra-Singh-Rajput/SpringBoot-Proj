import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [token, setToken]       = useState(() => localStorage.getItem("token") || null);
  const [role, setRole]         = useState(() => localStorage.getItem("role")  || null);
  const [username, setUsername] = useState(() => localStorage.getItem("username") || null);

  
  const login = ({ token, role, username }) => {
    localStorage.setItem("token",    token);
    localStorage.setItem("role",     role);
    localStorage.setItem("username", username);
    setToken(token);
    setRole(role);
    setUsername(username);
  };

  
  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("username");
    localStorage.removeItem("cart");
    setToken(null);
    setRole(null);
    setUsername(null);
  };

  const isAdmin     = role === "ADMIN";
  const isLoggedIn  = !!token;

  return (
    <AuthContext.Provider value={{ token, role, username, isAdmin, isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

export default AuthContext;
