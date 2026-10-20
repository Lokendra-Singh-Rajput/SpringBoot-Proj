import React, { useContext, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AppContext from "../Context/Context";
import { useAuth } from "../Context/AuthContext";
import unplugged from "../assets/unplugged.png";
import "./home.css";

const CATEGORIES = [
  { name: "All",         icon: "🛍️" },
  { name: "Laptop",      icon: "💻" },
  { name: "Mobile",      icon: "📱" },
  { name: "Headphone",   icon: "🎧" },
  { name: "Electronics", icon: "⚡" },
  { name: "Fashion",     icon: "👗" },
  { name: "Toys",        icon: "🧸" },
];

const SkeletonCard = () => (
  <div className="col">
    <div className="shimmer-card" style={{ height: "340px" }}>
      <div className="shimmer-block" style={{ height: "200px" }} />
      <div style={{ padding: "16px 18px" }}>
        <div className="shimmer-block rounded mb-2" style={{ height: "12px", width: "35%" }} />
        <div className="shimmer-block rounded mb-2" style={{ height: "15px", width: "85%" }} />
        <div className="shimmer-block rounded mb-3" style={{ height: "15px", width: "50%" }} />
        <div className="shimmer-block rounded" style={{ height: "40px" }} />
      </div>
    </div>
  </div>
);

const Home = () => {
  const { data, isError, addToCart } = useContext(AppContext);
  const { isLoggedIn } = useAuth();
  const navigate = useNavigate();

  const [activeCategory, setActiveCategory] = useState("All");
  const [showToast, setShowToast]           = useState(false);
  const [toastProduct, setToastProduct]     = useState(null);
  const [addedIds, setAddedIds]             = useState(new Set());
  const [currentPage, setCurrentPage]       = useState(1);
  const [itemsPerPage, setItemsPerPage]     = useState(12);

  const baseUrl = import.meta.env.VITE_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

  useEffect(() => {
    setCurrentPage(1);
  }, [activeCategory]);

  
  useEffect(() => {
    let t;
    if (showToast) t = setTimeout(() => setShowToast(false), 3000);
    return () => clearTimeout(t);
  }, [showToast]);

  const handleAddToCart = (e, product) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isLoggedIn) {
      navigate("/login", {
        state: { returnUrl: "/", message: "Please sign in to add items to your cart." },
      });
      return;
    }
    addToCart(product);
    setToastProduct(product);
    setShowToast(true);
    setAddedIds((prev) => new Set(prev).add(product.id));
    setTimeout(() => {
      setAddedIds((prev) => {
        const s = new Set(prev);
        s.delete(product.id);
        return s;
      });
    }, 2000);
  };

  const filteredProducts =
    activeCategory === "All" ? data : data?.filter((p) => p.category === activeCategory);

  const totalItems = filteredProducts?.length || 0;
  const totalPages = Math.max(1, Math.ceil(totalItems / itemsPerPage));
  const safeCurrentPage = Math.min(Math.max(currentPage, 1), totalPages);
  const startIndex = (safeCurrentPage - 1) * itemsPerPage;
  const endIndex = Math.min(startIndex + itemsPerPage, totalItems);
  const paginatedProducts = filteredProducts?.slice(startIndex, endIndex) || [];

  const handlePageChange = (page) => {
    if (page < 1 || page > totalPages) return;
    setCurrentPage(page);
    const elem = document.getElementById("catalog-section");
    if (elem) {
      elem.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  const getPageNumbers = () => {
    const pages = [];
    if (totalPages <= 5) {
      for (let i = 1; i <= totalPages; i++) pages.push(i);
    } else {
      if (safeCurrentPage <= 3) {
        pages.push(1, 2, 3, 4, "...", totalPages);
      } else if (safeCurrentPage >= totalPages - 2) {
        pages.push(1, "...", totalPages - 3, totalPages - 2, totalPages - 1, totalPages);
      } else {
        pages.push(1, "...", safeCurrentPage - 1, safeCurrentPage, safeCurrentPage + 1, "...", totalPages);
      }
    }
    return pages;
  };

  const featured = data ? data.slice(0, Math.min(10, data.length)) : [];

  
  if (isError) {
    return (
      <div className="container d-flex justify-content-center align-items-center" style={{ minHeight: "80vh" }}>
        <div className="text-center">
          <img src={unplugged} alt="Error" width="90" className="mb-3 opacity-50" />
          <h4 style={{ color: "var(--text-primary)" }}>Unable to reach the catalog</h4>
          <p className="text-muted">Make sure the backend is running on port 8080.</p>
          <button className="btn btn-minimal-primary mt-2" onClick={() => window.location.reload()}>
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <>
      
      <div className="position-fixed top-0 end-0 p-3" style={{ zIndex: 1060, marginTop: "68px" }}>
        {showToast && toastProduct && (
          <div className="cart-toast">
            <div className="cart-toast-check">
              <i className="bi bi-check" />
            </div>
            <img
              src={`${baseUrl}/api/product/${toastProduct.id}/image`}
              alt={toastProduct.name}
              className="cart-toast-img"
              onError={(e) => { e.target.src = unplugged; }}
            />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div className="cart-toast-name text-truncate">{toastProduct.name}</div>
              <div className="cart-toast-price">₹{toastProduct.price?.toLocaleString("en-IN")}</div>
              <div style={{ fontSize: "11px", color: "var(--text-muted)" }}>Added to cart ✓</div>
            </div>
            <button
              onClick={() => setShowToast(false)}
              style={{
                background: "none",
                border: "none",
                color: "var(--text-muted)",
                cursor: "pointer",
                padding: "4px",
                alignSelf: "flex-start",
              }}
            >
              ✕
            </button>
          </div>
        )}
      </div>

      <div className="container py-4" style={{ marginTop: "70px" }}>

        
        <section className="hero-editorial">
          <div className="hero-warm-badge">
            <span className="hero-warm-dot" />
            Curated 2026 Collection
          </div>
          <h1 className="hero-editorial-headline">
            Curated technology &amp; design essentials for modern living.
          </h1>
          <p className="hero-editorial-subtext">
            Handpicked premium laptops, studio audio, mobile gear, and lifestyle tech. Guided by intelligent hybrid AI search.
          </p>

          <div className="hero-cta-wrapper">
            <button
              type="button"
              className="btn-ceramic-primary"
              onClick={() => {
                const el = document.getElementById("catalog-section");
                el?.scrollIntoView({ behavior: "smooth" });
              }}
            >
              EXPLORE CATALOG
            </button>
            <button
              type="button"
              className="btn-warm-ghost"
              onClick={() => {
                const searchInput = document.querySelector(".ai-search-input");
                searchInput?.focus();
              }}
            >
              <i className="bi bi-stars me-1" /> Ask AI Search
            </button>
          </div>

          <p className="hero-guarantee-note">
            100% Genuine Products · Free Express Delivery · 7-Day Returns
          </p>

          <div className="hero-metrics-strip">
            <div className="hero-metric-item">
              <span className="hero-metric-value">50+</span>
              <span className="hero-metric-label">Curated Products</span>
            </div>
            <div className="hero-metric-divider" />
            <div className="hero-metric-item">
              <span className="hero-metric-value">10k+</span>
              <span className="hero-metric-label">Satisfied Shoppers</span>
            </div>
            <div className="hero-metric-divider" />
            <div className="hero-metric-item">
              <span className="hero-metric-value">99.98%</span>
              <span className="hero-metric-label">On-Time Delivery</span>
            </div>
            <div className="hero-metric-badge">
              <div className="hero-ribbon-tag">VERIFIED</div>
              <div className="hero-badge-text">
                <strong>Curated Quality</strong>
                <small>2026 Tech Selection</small>
              </div>
            </div>
          </div>
        </section>

        
        {featured.length > 0 && (
          <div className="mb-5">
            <div className="section-header">
              <h2 className="section-title">
                <span className="section-title-dot" />
                Featured Picks
              </h2>
            </div>

            <div className="ticker-container">
              
              <div className="ticker-track">
                {[...featured, ...featured].map((product, idx) => (
                  <div
                    className="ticker-card"
                    key={`${product.id}-${idx}`}
                    onClick={() => navigate(`/product/${product.id}`)}
                  >
                    <div className="ticker-card-img-wrap">
                      <span className="ticker-card-badge">Featured</span>
                      <img
                        src={`${baseUrl}/api/product/${product.id}/image`}
                        alt={product.name}
                        onError={(e) => { e.target.src = unplugged; }}
                      />
                    </div>
                    <div className="ticker-card-body">
                      <div className="ticker-card-brand">{product.brand}</div>
                      <div className="ticker-card-name" title={product.name}>
                        {product.name}
                      </div>
                      <div className="ticker-card-footer">
                        <span className="ticker-card-price">
                          ₹{product.price?.toLocaleString("en-IN")}
                        </span>
                        <button
                          type="button"
                          className="ticker-card-add-btn"
                          onClick={(e) => handleAddToCart(e, product)}
                          disabled={!product.productAvailable || product.stockQuantity === 0}
                          title="Add to cart"
                        >
                          <i
                            className={`bi ${addedIds.has(product.id) ? "bi-check-lg" : "bi-cart-plus"}`}
                            style={{ fontSize: "14px" }}
                          />
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        
        <div className="mb-4">
          <div className="section-header mb-3">
            <h2 className="section-title">
              <span className="section-title-dot" />
              Categories
            </h2>
          </div>
          <div className="category-showcase">
            {CATEGORIES.map((cat) => (
              <button
                key={cat.name}
                type="button"
                className={`cat-pill-btn ${activeCategory === cat.name ? "active" : ""}`}
                onClick={() => setActiveCategory(cat.name)}
              >
                <span>{cat.icon}</span>
                <span>{cat.name}</span>
              </button>
            ))}
          </div>
        </div>

        
        <div id="catalog-section">
          <div className="results-bar">
            <h2 className="section-title">
              <span className="section-title-dot" />
              {activeCategory === "All" ? "All Products" : `${activeCategory} Products`}
            </h2>
            <div className="d-flex align-items-center gap-3">
              <span className="results-count">
                {totalItems === 0
                  ? "0 items"
                  : `Showing ${startIndex + 1}–${endIndex} of ${totalItems} products`}
              </span>
              {totalItems > 8 && (
                <select
                  className="catalog-per-page-select"
                  value={itemsPerPage}
                  onChange={(e) => {
                    setItemsPerPage(Number(e.target.value));
                    setCurrentPage(1);
                  }}
                  aria-label="Products per page"
                >
                  <option value={8}>8 / page</option>
                  <option value={12}>12 / page</option>
                  <option value={24}>24 / page</option>
                </select>
              )}
            </div>
          </div>

          {!data ? (
            
            <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
              {Array.from({ length: 8 }).map((_, i) => <SkeletonCard key={i} />)}
            </div>
          ) : !filteredProducts || filteredProducts.length === 0 ? (
            <div className="empty-state">
              <span className="empty-state-icon">📦</span>
              <h4 style={{ color: "var(--text-primary)" }}>No products in this category</h4>
              <p className="text-muted">Try selecting "All" or browse other categories.</p>
              <button
                type="button"
                className="btn btn-minimal-primary mt-2"
                onClick={() => setActiveCategory("All")}
              >
                View All Products
              </button>
            </div>
          ) : (
            <>
              <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
              {paginatedProducts.map((product) => {
                const { id, brand, name, price, productAvailable, stockQuantity, category } = product;
                const outOfStock = !productAvailable || stockQuantity === 0;

                return (
                  <div className="col" key={id}>
                    <div className="product-card">
                      
                      <div className="product-card-img-wrap">
                        <img
                          src={`${baseUrl}/api/product/${id}/image`}
                          alt={name}
                          onError={(e) => { e.target.src = unplugged; }}
                        />
                        
                        <div className="product-card-overlay">
                          <button
                            type="button"
                            className="product-card-view-btn"
                            onClick={() => navigate(`/product/${id}`)}
                          >
                            Quick View
                          </button>
                        </div>
                        
                        <div
                          style={{
                            position: "absolute",
                            top: "12px",
                            left: "12px",
                            background: "rgba(15, 23, 42, 0.75)",
                            color: "#ffffff",
                            fontSize: "10.5px",
                            fontWeight: 700,
                            padding: "3px 10px",
                            borderRadius: "9999px",
                            backdropFilter: "blur(6px)",
                          }}
                        >
                          {category}
                        </div>
                        
                        {outOfStock && (
                          <div
                            style={{
                              position: "absolute",
                              top: "12px",
                              right: "12px",
                              background: "#ef4444",
                              color: "#ffffff",
                              fontSize: "10px",
                              fontWeight: 700,
                              padding: "3px 9px",
                              borderRadius: "9999px",
                            }}
                          >
                            Sold Out
                          </div>
                        )}
                      </div>

                      
                      <div className="product-card-body">
                        <div className="product-card-brand">{brand}</div>
                        <Link to={`/product/${id}`} className="text-decoration-none">
                          <div className="product-card-name" title={name}>{name}</div>
                        </Link>

                        <div className="product-card-footer">
                          <span className="product-card-price">
                            ₹{price?.toLocaleString("en-IN")}
                          </span>
                          <span
                            className="product-card-stock"
                            style={{ color: outOfStock ? "#ef4444" : "#10b981" }}
                          >
                            {outOfStock ? "● Out of Stock" : `● ${stockQuantity} left`}
                          </span>
                        </div>

                        <button
                          type="button"
                          className="product-card-add-btn"
                          onClick={(e) => handleAddToCart(e, product)}
                          disabled={outOfStock}
                        >
                          {addedIds.has(id) ? (
                            <><i className="bi bi-check-lg" /> Added!</>
                          ) : !isLoggedIn ? (
                            <><i className="bi bi-lock" /> Sign in to Buy</>
                          ) : outOfStock ? (
                            "Out of Stock"
                          ) : (
                            <><i className="bi bi-cart-plus" /> Add to Cart</>
                          )}
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {totalPages > 1 && (
              <div className="catalog-pagination-wrap">
                <div className="catalog-pagination-info">
                  Page <span className="fw-bold" style={{ color: "var(--primary-accent)" }}>{safeCurrentPage}</span> of{" "}
                  <span className="fw-bold">{totalPages}</span>
                </div>
                <ul className="catalog-pagination-nav">
                  <li>
                    <button
                      type="button"
                      className="catalog-page-btn"
                      onClick={() => handlePageChange(safeCurrentPage - 1)}
                      disabled={safeCurrentPage === 1}
                      aria-label="Previous Page"
                    >
                      <i className="bi bi-chevron-left" />
                    </button>
                  </li>
                  {getPageNumbers().map((num, idx) => (
                    <li key={idx}>
                      {num === "..." ? (
                        <span className="catalog-page-ellipsis">…</span>
                      ) : (
                        <button
                          type="button"
                          className={`catalog-page-btn ${num === safeCurrentPage ? "active" : ""}`}
                          onClick={() => handlePageChange(num)}
                        >
                          {num}
                        </button>
                      )}
                    </li>
                  ))}
                  <li>
                    <button
                      type="button"
                      className="catalog-page-btn"
                      onClick={() => handlePageChange(safeCurrentPage + 1)}
                      disabled={safeCurrentPage === totalPages}
                      aria-label="Next Page"
                    >
                      <i className="bi bi-chevron-right" />
                    </button>
                  </li>
                </ul>
              </div>
            )}
          </>
        )}
        </div>

      </div>
    </>
  );
};

export default Home;