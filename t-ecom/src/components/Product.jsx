import { useNavigate, useParams, Link } from "react-router-dom";
import { useContext, useEffect, useState } from "react";
import AppContext from "../Context/Context";
import { useAuth } from "../Context/AuthContext";
import axios from "../axios";
import { toast } from "react-toastify";
import unplugged from "../assets/unplugged.png";

const Product = () => {
  const { id } = useParams();
  const { addToCart, removeFromCart, refreshData } = useContext(AppContext);
  const { isAdmin, isLoggedIn } = useAuth();
  const [product, setProduct] = useState(null);
  const [similarProducts, setSimilarProducts] = useState([]);
  const [loadingSimilar, setLoadingSimilar] = useState(false);
  const [imageUrl, setImageUrl] = useState("");
  const navigate = useNavigate();
  const baseUrl = import.meta.env.VITE_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(`/api/product/${id}`);
        setProduct(response.data);
      } catch (error) {
        console.error("Error fetching product:", error);
      }
    };

    setImageUrl(`${baseUrl}/api/product/${id}/image`);
    fetchProduct();
    window.scrollTo({ top: 0, behavior: "smooth" });
  }, [id, baseUrl]);

  
  useEffect(() => {
    if (!product) return;

    const fetchSimilar = async () => {
      setLoadingSimilar(true);
      try {
        const queryText = `${product.name} ${product.category} ${product.brand}`;
        const res = await axios.get(
          `/api/products/semantic-search?query=${encodeURIComponent(queryText)}&topK=5`
        );
        
        const filtered = (res.data || []).filter((p) => p.id !== product.id).slice(0, 4);
        setSimilarProducts(filtered);
      } catch (err) {
        console.error("Failed to fetch similar products:", err);
      } finally {
        setLoadingSimilar(false);
      }
    };

    fetchSimilar();
  }, [product]);

  const deleteProduct = async () => {
    if (!window.confirm("Are you sure you want to delete this product?")) return;
    try {
      await axios.delete(`/api/product/${id}`);
      removeFromCart(id);
      toast.success("Product deleted successfully");
      refreshData();
      navigate("/");
    } catch (error) {
      console.error("Error deleting product:", error);
      toast.error(error.response?.data?.detail || "Failed to delete product. Please try again.");
    }
  };

  const handleEditClick = () => {
    navigate(`/product/update/${id}`);
  };

  const handlAddToCart = () => {
    if (!isLoggedIn) {
      navigate("/login", {
        state: { returnUrl: `/product/${id}`, message: "Please sign in to add items to your cart." },
      });
      return;
    }
    addToCart(product);
    toast.success("Product added to cart!");
  };

  if (!product) {
    return (
      <div className="container py-5 text-center" style={{ marginTop: "100px" }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading product...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="container py-5" style={{ marginTop: "70px" }}>
      
      <div className="card card-theme p-4 p-md-5 border-0 mb-5">
        <div className="row g-5 align-items-center">
          
          <div className="col-lg-6 text-center">
            <div
              className="p-4 rounded-4 d-flex align-items-center justify-content-center"
              style={{
                backgroundColor: "var(--bg-subtle)",
                minHeight: "420px",
                border: "1px solid var(--border-color)",
              }}
            >
              <img
                src={imageUrl}
                alt={product.name}
                className="img-fluid rounded-3"
                style={{ maxHeight: "380px", maxWidth: "100%", objectFit: "contain" }}
                onError={(e) => {
                  e.target.src = unplugged;
                }}
              />
            </div>
          </div>

          
          <div className="col-lg-6">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <span className="badge bg-primary px-3 py-2 rounded-pill fw-semibold text-uppercase" style={{ fontSize: "11px" }}>
                {product.category}
              </span>
              <small className="text-muted">
                <i className="bi bi-calendar3 me-1"></i>
                Listed: {new Date(product.releaseDate).toLocaleDateString("en-IN", { year: "numeric", month: "short", day: "numeric" })}
              </small>
            </div>

            <h1 className="fw-bold mb-1" style={{ fontSize: "2rem", letterSpacing: "-0.5px" }}>
              {product.name}
            </h1>
            <p className="text-muted fst-italic mb-4" style={{ fontSize: "1.05rem" }}>
              by <strong className="text-primary">{product.brand}</strong>
            </p>

            <div className="mb-4 p-3 rounded-3" style={{ backgroundColor: "var(--bg-subtle)", border: "1px solid var(--border-color)" }}>
              <h6 className="fw-bold mb-2 small text-uppercase text-muted">
                <i className="bi bi-card-text me-1"></i> Product Overview
              </h6>
              <p className="mb-0" style={{ lineHeight: "1.6", fontSize: "0.95rem" }}>
                {product.description}
              </p>
            </div>

            
            <div className="d-flex align-items-baseline gap-3 mb-4">
              <h2 className="fw-bold text-primary mb-0" style={{ fontSize: "2.2rem" }}>
                ₹{product.price?.toLocaleString("en-IN")}
              </h2>
              {product.stockQuantity > 0 ? (
                <span className="badge bg-success-subtle text-success border border-success-subtle px-3 py-2 rounded-pill fw-bold">
                  ● In Stock ({product.stockQuantity} available)
                </span>
              ) : (
                <span className="badge bg-danger-subtle text-danger border border-danger-subtle px-3 py-2 rounded-pill fw-bold">
                  ● Out of Stock
                </span>
              )}
            </div>

            
            <div className="d-grid mb-4">
              <button
                className="btn btn-lg fw-bold py-3 d-flex align-items-center justify-content-center gap-2"
                style={{
                  background: !product.productAvailable || product.stockQuantity === 0 ? undefined : "linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)",
                  color: "#fff",
                  border: "none",
                  borderRadius: "12px",
                }}
                onClick={handlAddToCart}
                disabled={!product.productAvailable || product.stockQuantity === 0}
              >
                <i className="bi bi-cart3 fs-5"></i>
                {!isLoggedIn
                  ? "Sign In to Buy"
                  : product.stockQuantity !== 0
                  ? "Add to Cart"
                  : "Out of Stock"}
              </button>
            </div>

            
            {isAdmin && (
              <div className="pt-3 border-top d-flex gap-2" style={{ borderColor: "var(--border-color)" }}>
                <button
                  className="btn btn-outline-primary d-flex align-items-center gap-1 px-4 py-2"
                  style={{ borderRadius: "8px" }}
                  type="button"
                  onClick={handleEditClick}
                >
                  <i className="bi bi-pencil-square"></i>
                  Edit Product
                </button>

                <button
                  className="btn btn-outline-danger d-flex align-items-center gap-1 px-4 py-2"
                  style={{ borderRadius: "8px" }}
                  type="button"
                  onClick={deleteProduct}
                >
                  <i className="bi bi-trash"></i>
                  Delete Product
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      
      {similarProducts && similarProducts.length > 0 && (
        <div className="mt-5">
          <div className="d-flex align-items-center justify-content-between mb-4">
            <div>
              <div className="d-flex align-items-center gap-2 mb-1">
                <span className="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 rounded-pill px-3 py-1 small">
                  ✨ AI Vector Match
                </span>
              </div>
              <h3 className="fw-bold mb-0" style={{ letterSpacing: "-0.5px" }}>
                Customers Also Looked At
              </h3>
            </div>
            <Link to="/" className="text-decoration-none fw-semibold small text-primary">
              View catalog →
            </Link>
          </div>

          <div className="row row-cols-1 row-cols-sm-2 row-cols-md-4 g-4">
            {similarProducts.map((simProduct) => (
              <div className="col" key={simProduct.id}>
                <div
                  className="card card-theme h-100 border-0"
                  style={{ cursor: "pointer", transition: "transform 0.2s ease, box-shadow 0.2s ease" }}
                  onClick={() => navigate(`/product/${simProduct.id}`)}
                  onMouseEnter={(e) => (e.currentTarget.style.transform = "translateY(-4px)")}
                  onMouseLeave={(e) => (e.currentTarget.style.transform = "translateY(0)")}
                >
                  <div
                    style={{
                      height: "170px",
                      background: "var(--bg-subtle)",
                      borderTopLeftRadius: "14px",
                      borderTopRightRadius: "14px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      overflow: "hidden",
                    }}
                  >
                    <img
                      src={`${baseUrl}/api/product/${simProduct.id}/image`}
                      alt={simProduct.name}
                      style={{ maxHeight: "140px", maxWidth: "90%", objectFit: "contain" }}
                      onError={(e) => {
                        e.target.src = unplugged;
                      }}
                    />
                  </div>
                  <div className="card-body p-3 d-flex flex-column">
                    <small className="text-muted text-uppercase fw-semibold" style={{ fontSize: "10px" }}>
                      {simProduct.brand}
                    </small>
                    <h6 className="fw-bold text-truncate my-1" title={simProduct.name} style={{ fontSize: "13.5px" }}>
                      {simProduct.name}
                    </h6>
                    <div className="mt-auto pt-2 d-flex justify-content-between align-items-center">
                      <span className="fw-bold text-primary" style={{ fontSize: "1rem" }}>
                        ₹{simProduct.price?.toLocaleString("en-IN")}
                      </span>
                      <span className="badge bg-secondary-subtle text-secondary small">
                        {simProduct.category}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Product;