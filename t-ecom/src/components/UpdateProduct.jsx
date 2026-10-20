import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "../axios";
import { toast } from "react-toastify";

const CATEGORIES = [
  { value: "Laptop", label: "💻 Laptop" },
  { value: "Headphone", label: "🎧 Headphone" },
  { value: "Mobile", label: "📱 Mobile" },
  { value: "Electronics", label: "⚡ Electronics" },
  { value: "Toys", label: "🧸 Toys" },
  { value: "Fashion", label: "👗 Fashion" },
  { value: "Books", label: "📚 Books" },
  { value: "Sports", label: "⚽ Sports" },
  { value: "Home", label: "🏠 Home & Kitchen" },
];

const UpdateProduct = () => {
  const { id } = useParams();
  const navigate = useNavigate();

  const [product, setProduct] = useState({});
  const [image, setImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [imageChanged, setImageChanged] = useState(false);
  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);

  const [updateProduct, setUpdateProduct] = useState({
    id: null,
    name: "",
    description: "",
    brand: "",
    price: "",
    category: "",
    releaseDate: "",
    productAvailable: false,
    stockQuantity: "",
  });

  const baseUrl = import.meta.env.VITE_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const response = await axios.get(`${baseUrl}/api/product/${id}`);
        setProduct(response.data);
        setUpdateProduct(response.data);

        const responseImage = await axios.get(
          `${baseUrl}/api/product/${id}/image`,
          { responseType: "blob" }
        );
        const imageFile = new File(
          [responseImage.data],
          response.data.imageName || "product-image.jpg",
          { type: responseImage.data.type }
        );
        setImage(imageFile);
        setImagePreview(URL.createObjectURL(responseImage.data));
      } catch (error) {
        console.error("Error fetching product:", error);
        toast.error("Failed to load product data.");
      } finally {
        setFetching(false);
      }
    };

    fetchProduct();
  }, [id]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setUpdateProduct((prev) => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      setImage(file);
      setImagePreview(URL.createObjectURL(file));
      setImageChanged(true);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    const formData = new FormData();
    if (imageChanged && image) {
      formData.append("imageFile", image);
    } else {
      formData.append("imageFile", new Blob([], { type: "application/octet-stream" }), "empty");
    }
    formData.append(
      "product",
      new Blob([JSON.stringify(updateProduct)], { type: "application/json" })
    );

    try {
      await axios.put(`${baseUrl}/api/product/${id}`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      toast.success("Product updated successfully! ✅");
      navigate("/");
    } catch (error) {
      console.error("Error updating product:", error);
      toast.error("Failed to update product. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="container mt-5 pt-5">
        <div className="d-flex justify-content-center align-items-center" style={{ height: "300px" }}>
          <div className="text-center">
            <div className="spinner-border text-primary mb-3" role="status" style={{ width: "3rem", height: "3rem" }}>
              <span className="visually-hidden">Loading...</span>
            </div>
            <p className="text-muted">Loading product data…</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="container mt-5 pt-5 pb-5" style={{ maxWidth: "860px" }}>
      
      <div className="d-flex align-items-center justify-content-between mb-4">
        <div>
          <h2 className="fw-bold mb-1">✏️ Update Product</h2>
          <p className="text-muted mb-0">Editing: <strong>{product.name}</strong></p>
        </div>
        <button
          type="button"
          className="btn btn-outline-secondary btn-sm"
          onClick={() => navigate("/")}
        >
          ← Back to Catalog
        </button>
      </div>

      <form onSubmit={handleSubmit} noValidate>
        
        <div className="card-theme p-4 mb-3" style={{ borderRadius: "14px", border: "1px solid var(--border-color)", background: "var(--bg-surface)" }}>
          <p className="text-uppercase fw-semibold mb-3" style={{ fontSize: "11px", letterSpacing: "0.8px" }}>
            Basic Information
          </p>

          <div className="row g-3">
            <div className="col-md-6">
              <label htmlFor="name" className="form-label fw-semibold">Product Name <span className="text-danger">*</span></label>
              <input
                type="text"
                className="form-control"
                id="name"
                name="name"
                value={updateProduct.name}
                onChange={handleChange}
                placeholder="e.g. Sony WH-1000XM5"
                required
              />
            </div>

            <div className="col-md-6">
              <label htmlFor="brand" className="form-label fw-semibold">Brand <span className="text-danger">*</span></label>
              <input
                type="text"
                className="form-control"
                id="brand"
                name="brand"
                value={updateProduct.brand}
                onChange={handleChange}
                placeholder="e.g. Sony"
                required
              />
            </div>

            <div className="col-md-4">
              <label htmlFor="price" className="form-label fw-semibold">Price <span className="text-danger">*</span></label>
              <div className="input-group">
                <span className="input-group-text fw-bold">₹</span>
                <input
                  type="number"
                  className="form-control"
                  id="price"
                  name="price"
                  value={updateProduct.price}
                  onChange={handleChange}
                  placeholder="0.00"
                  min="0.01"
                  step="0.01"
                  required
                />
              </div>
            </div>

            <div className="col-md-4">
              <label htmlFor="stockQuantity" className="form-label fw-semibold">Stock Quantity <span className="text-danger">*</span></label>
              <input
                type="number"
                className="form-control"
                id="stockQuantity"
                name="stockQuantity"
                value={updateProduct.stockQuantity}
                onChange={handleChange}
                placeholder="0"
                min="0"
                required
              />
            </div>

            <div className="col-md-4">
              <label htmlFor="releaseDate" className="form-label fw-semibold">Release Date</label>
              <input
                type="date"
                className="form-control"
                id="releaseDate"
                name="releaseDate"
                value={updateProduct.releaseDate ? updateProduct.releaseDate.slice(0, 10) : ""}
                onChange={handleChange}
              />
            </div>

            <div className="col-md-8">
              <label htmlFor="category" className="form-label fw-semibold">Category <span className="text-danger">*</span></label>
              <select
                className="form-select"
                id="category"
                name="category"
                value={updateProduct.category}
                onChange={handleChange}
                required
              >
                <option value="">Select a category</option>
                {CATEGORIES.map((cat) => (
                  <option key={cat.value} value={cat.value}>{cat.label}</option>
                ))}
              </select>
            </div>

            <div className="col-md-4 d-flex align-items-end pb-1">
              <div className="form-check form-switch">
                <input
                  className="form-check-input"
                  type="checkbox"
                  role="switch"
                  id="productAvailable"
                  name="productAvailable"
                  checked={updateProduct.productAvailable}
                  onChange={(e) =>
                    setUpdateProduct((prev) => ({ ...prev, productAvailable: e.target.checked }))
                  }
                  style={{ width: "2.5em", height: "1.4em" }}
                />
                <label className="form-check-label fw-semibold ms-2" htmlFor="productAvailable">
                  {updateProduct.productAvailable ? (
                    <span className="text-success">● In Stock</span>
                  ) : (
                    <span className="text-danger">● Out of Stock</span>
                  )}
                </label>
              </div>
            </div>
          </div>
        </div>

        
        <div className="card-theme p-4 mb-3" style={{ borderRadius: "14px", border: "1px solid var(--border-color)", background: "var(--bg-surface)" }}>
          <p className="text-uppercase fw-semibold mb-3" style={{ fontSize: "11px", letterSpacing: "0.8px" }}>
            Product Description
          </p>
          <textarea
            className="form-control"
            id="description"
            name="description"
            value={updateProduct.description}
            onChange={handleChange}
            placeholder="Describe the product features, specifications, and what makes it great…"
            rows="4"
            required
          />
        </div>

        
        <div className="card-theme p-4 mb-4" style={{ borderRadius: "14px", border: "1px solid var(--border-color)", background: "var(--bg-surface)" }}>
          <p className="text-uppercase fw-semibold mb-3" style={{ fontSize: "11px", letterSpacing: "0.8px" }}>
            Product Image
          </p>

          {imagePreview && (
            <div
              className="text-center mb-3 p-3 rounded"
              style={{
                background: "var(--bg-subtle)",
                border: "1px dashed var(--border-color)",
                borderRadius: "10px",
              }}
            >
              <img
                src={imagePreview}
                alt="Product preview"
                style={{
                  maxHeight: "200px",
                  maxWidth: "100%",
                  objectFit: "contain",
                  borderRadius: "8px",
                }}
              />
              <p className="text-muted small mt-2 mb-0">
                {imageChanged ? "✅ New image selected" : "Current product image"}
              </p>
            </div>
          )}

          <input
            className="form-control"
            type="file"
            id="imageFile"
            accept="image/png, image/jpeg, image/webp"
            onChange={handleImageChange}
          />
          <div className="form-text mt-1">
            Leave empty to keep the current image. Accepts PNG, JPG, WEBP.
          </div>
        </div>

        
        <div className="d-flex gap-3">
          <button
            type="submit"
            className="btn px-4 py-2 fw-semibold"
            disabled={loading}
            style={{
              background: "linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)",
              color: "#ffffff",
              border: "none",
              borderRadius: "10px",
              minWidth: "160px",
            }}
          >
            {loading ? (
              <>
                <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true" />
                Updating…
              </>
            ) : (
              "✅ Update Product"
            )}
          </button>

          <button
            type="button"
            className="btn btn-outline-secondary px-4 py-2"
            style={{ borderRadius: "10px" }}
            onClick={() => navigate("/")}
            disabled={loading}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default UpdateProduct;