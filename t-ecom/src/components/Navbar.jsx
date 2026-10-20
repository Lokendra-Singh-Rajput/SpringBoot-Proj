import React, { useContext, useEffect, useRef, useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../Context/AuthContext";
import AppContext from "../Context/Context";

const Navbar = ({ onSelectCategory }) => {
  const { isLoggedIn, isAdmin, username, logout } = useAuth();
  const { cart } = useContext(AppContext);
  const navigate = useNavigate();
  const location = useLocation();

  
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("theme") || "dark-theme";
  });

  const [aiQuery, setAiQuery] = useState("");
  const [isNavCollapsed, setIsNavCollapsed] = useState(true);
  const navbarRef = useRef(null);

  
  useEffect(() => {
    document.body.className = theme;
    localStorage.setItem("theme", theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (prev === "dark-theme" ? "light-theme" : "dark-theme"));
  };

  
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (navbarRef.current && !navbarRef.current.contains(event.target)) {
        setIsNavCollapsed(true);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleLinkClick = () => {
    setIsNavCollapsed(true);
  };

  
  const handleAiSearch = (e) => {
    e.preventDefault();
    if (!aiQuery.trim()) return;
    navigate(`/semantic-search?q=${encodeURIComponent(aiQuery.trim())}`);
    setAiQuery("");
    setIsNavCollapsed(true);
  };

  return (
    <nav className="navbar navbar-expand-lg fixed-top navbar-custom shadow-sm py-2" ref={navbarRef}>
      <div className="container-fluid px-3 px-lg-4">
        
        <div className="d-flex align-items-center me-lg-4">
          <a
            className="navbar-brand fw-bold d-flex align-items-center gap-2 me-3"
            href="/"
            onClick={(e) => {
              e.preventDefault();
              if (onSelectCategory) onSelectCategory("");
              navigate("/");
            }}
            style={{ letterSpacing: "-0.5px", fontSize: "1.25rem" }}
          >
            <span
              style={{
                background: "linear-gradient(135deg, #c45b2c 0%, #e07a3f 100%)",
                color: "#fff",
                width: "32px",
                height: "32px",
                borderRadius: "8px",
                display: "inline-flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: "1rem",
                fontWeight: "bold",
                boxShadow: "0 2px 10px rgba(196, 91, 44, 0.35)",
              }}
            >
              L
            </span>
            <span>Lokendra<span style={{ color: "#e07a3f" }}>Store</span></span>
          </a>

          <div className="d-none d-lg-flex align-items-center gap-1">
            <a
              className={`nav-link px-2 py-1 rounded fw-medium ${location.pathname === "/" ? "active text-primary" : ""}`}
              href="/"
              onClick={(e) => {
                e.preventDefault();
                if (onSelectCategory) onSelectCategory("");
                navigate("/");
              }}
            >
              Catalog
            </a>

            <a
              className={`nav-link px-2 py-1 rounded fw-medium ${location.pathname === "/orders" ? "active text-primary" : ""}`}
              href="/orders"
              onClick={(e) => {
                e.preventDefault();
                navigate("/orders");
              }}
            >
              Orders
            </a>

            {isAdmin && (
              <a
                className="nav-link px-2 py-1 rounded fw-semibold text-primary"
                href="/add_product"
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/add_product");
                }}
              >
                <i className="bi bi-plus-circle me-1"></i> Add Product
              </a>
            )}
          </div>
        </div>

        
        <div className="flex-grow-1 mx-auto my-2 my-lg-0 px-lg-3" style={{ maxWidth: "580px" }}>
          <form onSubmit={handleAiSearch} className="w-100">
            <div className="search-input-group d-flex align-items-center">
              <span className="text-primary d-flex align-items-center" style={{ fontSize: "16px" }}>
                <i className="bi bi-stars"></i>
              </span>
              <input
                type="text"
                className="ai-search-input flex-grow-1"
                placeholder="Search products with AI (e.g. 'laptop for video editing' or 'wireless earbuds')..."
                value={aiQuery}
                onChange={(e) => setAiQuery(e.target.value)}
                style={{ fontSize: "13.5px" }}
              />
              <button
                type="submit"
                className="btn btn-sm text-white fw-semibold px-3 py-1"
                style={{
                  background: "linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)",
                  borderRadius: "9999px",
                  fontSize: "12.5px",
                  border: "none",
                  whiteSpace: "nowrap",
                }}
              >
                Search
              </button>
            </div>
          </form>
        </div>

        
        <button
          className="navbar-toggler border-0 d-lg-none ms-2"
          type="button"
          onClick={() => setIsNavCollapsed(!isNavCollapsed)}
          aria-expanded={!isNavCollapsed}
          aria-label="Toggle navigation"
        >
          <span className="navbar-toggler-icon"></span>
        </button>

        
        <div
          className={`${isNavCollapsed ? "d-none d-lg-flex" : "d-flex flex-column flex-lg-row"} align-items-lg-center gap-2 ms-lg-3 mt-2 mt-lg-0`}
        >
          
          <div className="d-flex d-lg-none flex-column gap-1 mb-2 pb-2 border-bottom">
            <a className="nav-link py-1" href="/" onClick={handleLinkClick}>Catalog</a>
            <a className="nav-link py-1" href="/orders" onClick={handleLinkClick}>Orders</a>
            {isAdmin && (
              <a className="nav-link py-1 text-primary fw-bold" href="/add_product" onClick={handleLinkClick}>
                + Add Product
              </a>
            )}
          </div>

          <div className="d-flex align-items-center gap-2">
            
            <button
              type="button"
              onClick={toggleTheme}
              className="btn btn-sm btn-outline-secondary d-flex align-items-center justify-content-center"
              style={{ width: "36px", height: "36px", borderRadius: "50%" }}
              title={theme === "dark-theme" ? "Switch to Light Mode" : "Switch to Dark Mode"}
            >
              {theme === "dark-theme" ? (
                <i className="bi bi-sun-fill text-warning fs-6"></i>
              ) : (
                <i className="bi bi-moon-stars-fill text-secondary fs-6"></i>
              )}
            </button>

            
            <a
              href="/cart"
              className="btn btn-sm btn-outline-secondary position-relative d-flex align-items-center gap-1 px-3 py-1"
              style={{
                borderRadius: "20px",
                borderColor: "var(--border-color)",
                color: "var(--text-primary)",
              }}
              onClick={(e) => {
                e.preventDefault();
                navigate("/cart");
                handleLinkClick();
              }}
            >
              <i className="bi bi-cart3 text-primary"></i>
              <span className="fw-medium">Cart</span>
              {cart && cart.length > 0 && (
                <span
                  className="badge rounded-pill ms-1"
                  style={{
                    background: "var(--primary-accent)",
                    color: "#ffffff",
                    fontSize: "11px",
                    padding: "2px 6px",
                  }}
                >
                  {cart.reduce((sum, item) => sum + (item.quantity || 1), 0)}
                </span>
              )}
            </a>

            
            {isLoggedIn ? (
              <div
                className="d-flex align-items-center gap-2 ms-1 ps-2 border-start"
                style={{ borderColor: "var(--border-color)" }}
              >
                <div className="d-none d-xl-flex flex-column text-end">
                  <span className="fw-semibold small" style={{ lineHeight: "1.2" }}>
                    {username}
                  </span>
                  {isAdmin ? (
                    <span className="badge bg-danger text-uppercase" style={{ fontSize: "9px" }}>
                      ADMIN
                    </span>
                  ) : (
                    <span className="text-muted" style={{ fontSize: "10px" }}>Customer</span>
                  )}
                </div>

                <button
                  type="button"
                  onClick={() => {
                    logout();
                    window.location.href = "/login";
                  }}
                  className="btn btn-sm btn-outline-danger d-flex align-items-center gap-1"
                  style={{ borderRadius: "8px" }}
                  title="Logout"
                >
                  <i className="bi bi-box-arrow-right"></i>
                  <span>Logout</span>
                </button>
              </div>
            ) : (
              <a
                href="/login"
                className="btn btn-sm btn-primary ms-1 px-3"
                style={{
                  borderRadius: "8px",
                  background: "linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)",
                  border: "none",
                }}
                onClick={(e) => {
                  e.preventDefault();
                  navigate("/login");
                  handleLinkClick();
                }}
              >
                Sign In
              </a>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;