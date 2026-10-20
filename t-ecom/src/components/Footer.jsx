import React from "react";
import { Link } from "react-router-dom";

const Footer = () => {
  return (
    <footer
      style={{
        borderTop: "1px solid var(--border-color)",
        background: "var(--bg-surface)",
        color: "var(--text-secondary)",
        padding: "54px 0 28px",
        marginTop: "80px",
      }}
    >
      <div className="container">
        <div className="row g-4 justify-content-between">
          
          <div className="col-lg-4 col-md-6">
            <Link
              to="/"
              className="text-decoration-none d-flex align-items-center gap-2 mb-3"
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
              <span className="fw-bold" style={{ color: "var(--text-primary)", fontSize: "1.2rem" }}>
                Lokendra<span style={{ color: "#e07a3f" }}>Store</span>
              </span>
            </Link>
            <p className="small mb-3" style={{ maxWidth: "340px", lineHeight: "1.6" }}>
              Your destination for quality electronics, laptops, mobiles, and lifestyle accessories. Curated products with instant search and dependable delivery.
            </p>
            <div className="d-flex align-items-center gap-3 text-muted small">
              <span><i className="bi bi-geo-alt me-1"></i>New Delhi, India</span>
              <span><i className="bi bi-envelope me-1"></i>support@lokendrastore.com</span>
            </div>
          </div>

          
          <div className="col-lg-2 col-md-3 col-6">
            <h6 className="fw-bold mb-3 small text-uppercase" style={{ color: "var(--text-primary)", letterSpacing: "0.5px" }}>
              Shop
            </h6>
            <ul className="list-unstyled small d-flex flex-column gap-2 mb-0">
              <li>
                <Link to="/" className="text-decoration-none" style={{ color: "var(--text-secondary)" }}>
                  All Products
                </Link>
              </li>
              <li>
                <Link to="/semantic-search" className="text-decoration-none" style={{ color: "var(--text-secondary)" }}>
                  AI Smart Search
                </Link>
              </li>
              <li>
                <Link to="/cart" className="text-decoration-none" style={{ color: "var(--text-secondary)" }}>
                  Cart
                </Link>
              </li>
              <li>
                <Link to="/orders" className="text-decoration-none" style={{ color: "var(--text-secondary)" }}>
                  Order History
                </Link>
              </li>
            </ul>
          </div>

          
          <div className="col-lg-2 col-md-3 col-6">
            <h6 className="fw-bold mb-3 small text-uppercase" style={{ color: "var(--text-primary)", letterSpacing: "0.5px" }}>
              Customer Care
            </h6>
            <ul className="list-unstyled small d-flex flex-column gap-2 mb-0">
              <li>
                <span style={{ color: "var(--text-secondary)", cursor: "default" }}>
                  Shipping &amp; Delivery
                </span>
              </li>
              <li>
                <span style={{ color: "var(--text-secondary)", cursor: "default" }}>
                  Returns &amp; Refunds
                </span>
              </li>
              <li>
                <span style={{ color: "var(--text-secondary)", cursor: "default" }}>
                  Terms &amp; Conditions
                </span>
              </li>
              <li>
                <span style={{ color: "var(--text-secondary)", cursor: "default" }}>
                  Privacy Policy
                </span>
              </li>
            </ul>
          </div>

          
          <div className="col-lg-3 col-md-6">
            <h6 className="fw-bold mb-3 small text-uppercase" style={{ color: "var(--text-primary)", letterSpacing: "0.5px" }}>
              Accepted Payments
            </h6>
            <div className="d-flex flex-wrap gap-2 mb-3">
              {["UPI", "Visa", "Mastercard", "RuPay", "Net Banking", "Cash on Delivery"].map((method) => (
                <span
                  key={method}
                  className="badge"
                  style={{
                    background: "var(--bg-subtle)",
                    color: "var(--text-primary)",
                    border: "1px solid var(--border-color)",
                    padding: "6px 10px",
                    fontWeight: 500,
                    fontSize: "11px",
                  }}
                >
                  {method}
                </span>
              ))}
            </div>
            <p className="small text-muted mb-0">
              <i className="bi bi-shield-lock-fill text-success me-1"></i>
              256-bit encrypted secure checkout.
            </p>
          </div>
        </div>

        <hr style={{ borderColor: "var(--border-color)", margin: "40px 0 20px" }} />

        
        <div className="d-flex flex-column flex-sm-row align-items-center justify-content-between gap-2 small text-muted">
          <div>
            © {new Date().getFullYear()} <strong>LokendraStore</strong>. All rights reserved.
          </div>
          <div className="d-flex gap-3">
            <span style={{ cursor: "default" }}>Privacy</span>
            <span>•</span>
            <span style={{ cursor: "default" }}>Terms</span>
            <span>•</span>
            <span style={{ cursor: "default" }}>Security</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
