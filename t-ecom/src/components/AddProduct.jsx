import React, { useState } from "react";
import axios from "../axios";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

const AddProduct = () => {
  const [product, setProduct] = useState({
    name: "",
    brand: "",
    description: "", 
    price: "",
    category: "",
    stockQuantity: "",
    releaseDate: "",
    productAvailable: false,
  });

  const baseUrl = import.meta.env.VITE_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

  const [image, setImage] = useState(null); 
  const [loading, setLoading] = useState(false);
  const [validated, setValidated] = useState(false);
  const [errors, setErrors] = useState({});
  const [showModal, setShowModal] = useState(false);
  const [aiPrompt, setAiPrompt] = useState("");
  const [generatingProduct, setGeneratingProduct] = useState(false);
  const [generatingDescription, setGeneratingDescription] = useState(false);
  const [generatingImage, setGeneratingImage] = useState(false);
  const [aiGeneratedImage, setAiGeneratedImage] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);

  const navigate = useNavigate();

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProduct({ ...product, [name]: value });
    
    
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: null
      });
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    setImage(file);
    setAiGeneratedImage(null); 
    
    
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => setImagePreview(e.target.result);
      reader.readAsDataURL(file);
      
      
      const validTypes = ['image/jpeg', 'image/png'];
      if (!validTypes.includes(file.type)) {
        setErrors({
          ...errors,
          image: "Please select a valid image file (JPEG or PNG)"
        });
      } else if (file.size > 5 * 1024 * 1024) { 
        setErrors({
          ...errors,
          image: "Image size should be less than 5MB"
        });
      } else {
        setErrors({
          ...errors,
          image: null
        });
      }
    } else {
      setImagePreview(null);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    
    if (!product.name.trim()) newErrors.name = "Product name is required";
    if (!product.brand.trim()) newErrors.brand = "Brand is required";
    
    
    
    
    if (!product.price) {
      newErrors.price = "Price is required";
    } else if (parseFloat(product.price) <= 0) {
      newErrors.price = "Price must be greater than zero";
    }
    
    
    if (!product.category) newErrors.category = "Please select a category";
    
    
    if (!product.stockQuantity) {
      newErrors.stockQuantity = "Stock quantity is required";
    } else if (parseInt(product.stockQuantity) < 0) {
      newErrors.stockQuantity = "Stock quantity cannot be negative";
    }
    
    
    if (!product.releaseDate) newErrors.releaseDate = "Release date is required";
    
    
    if (image) {
      
      const validTypes = ['image/jpeg', 'image/png'];
      if (!validTypes.includes(image.type)) {
        newErrors.image = "Please select a valid image file (JPEG or PNG)";
      } else if (image.size > 5 * 1024 * 1024) {
        newErrors.image = "Image size should be less than 5MB";
      }
    }
    
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const generateDescription = async () => {
    if (!product.name.trim() || !product.category) {
      toast.warning("Please enter product name and select a category first");
      return;
    }

    setGeneratingDescription(true);

    try {
      const response = await axios.post(
        `${baseUrl}/api/product/generate-description`,
        null,
        {
          params: {
            name: product.name,
            category: product.category
          }
        }
      );

      if (response.data) {
        setProduct({
          ...product,
          description: response.data
        });
        toast.success("Description generated successfully!");
      }
    } catch (error) {
      console.error("Error generating description:", error);
      if (error.response && error.response.data) {
        toast.error(`Error: ${error.response.data}`);
      } else {
        toast.error("Failed to generate description. Please try again.");
      }
    } finally {
      setGeneratingDescription(false);
    }
  };

  const generateImage = async () => {
    if (!product.name.trim() || !product.category || !product.description.trim()) {
      toast.warning("Please enter product name, category, and description first");
      return;
    }

    setGeneratingImage(true);

    try {
      const response = await axios.post(
        `${baseUrl}/api/product/generate-image`,
        null,
        {
          params: {
            name: product.name,
            category: product.category,
            description: product.description
          },
          responseType: 'arraybuffer' 
        }
      );

      if (response.data) {
        
        const blob = new Blob([response.data], { type: 'image/jpeg' });
        const imageUrl = URL.createObjectURL(blob);
        
        
        setAiGeneratedImage({
          blob: blob,
          url: imageUrl
        });
        setImagePreview(imageUrl);
        setImage(null); 
        
        toast.success("Image generated successfully!");
      }
    } catch (error) {
      console.error("Error generating image:", error);
      if (error.response && error.response.data) {
        
        const errorMessage = new TextDecoder().decode(error.response.data);
        toast.error(`Error: ${errorMessage}`);
      } else {
        toast.error("Failed to generate image. Please try again.");
      }
    } finally {
      setGeneratingImage(false);
    }
  };

  const submitHandler = (event) => {
    event.preventDefault();
    
    
    const form = event.currentTarget;
    setValidated(true);
    
    
    if (!validateForm() || !form.checkValidity()) {
      event.stopPropagation();
      return;
    }
    
    setLoading(true);
    const formData = new FormData();
    
    
    if (image) {
      formData.append("imageFile", image);
    } else if (aiGeneratedImage) {
      
      const file = new File([aiGeneratedImage.blob], "ai-generated-image.jpg", {
        type: "image/jpeg"
      });
      formData.append("imageFile", file);
    }
    
    
    formData.append(
      "product",
      new Blob([JSON.stringify(product)], { type: "application/json" })
    );

    axios
      .post(`${baseUrl}/api/product`, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      })
      .then(() => {
        toast.success('Product added successfully');
        
        setProduct({
          name: "",
          brand: "",
          description: "",
          price: "",
          category: "",
          stockQuantity: "",
          releaseDate: "",
          productAvailable: false,
        });
        setImage(null);
        setAiGeneratedImage(null);
        setImagePreview(null);
        setValidated(false);
        setErrors({});
        navigate('/');
      })
      .catch((error) => {
        console.error("Error adding product:", error);
        if (error.response && error.response.data) {
          
          setErrors(error.response.data);
        } else {
          toast.error('Error adding product')
        }
        setLoading(false); 
      });
  };

  const handleGenerate = async () => {
    if (!aiPrompt.trim()) {
      toast.warning("Please enter a product description");
      return;
    }
    
    setGeneratingProduct(true);
    
    try {
      const response = await axios.post(`${baseUrl}/api/product/generate-product?query=${encodeURIComponent(aiPrompt)}`);
      
      if (response.data) {
        const generatedProduct = response.data;
        
        setProduct({
          name: generatedProduct.name || "",
          brand: generatedProduct.brand || "",
          description: generatedProduct.description || "",
          price: generatedProduct.price || "",
          category: generatedProduct.category || "",
          stockQuantity: generatedProduct.stockQuantity || "",
          releaseDate: generatedProduct.releaseDate || "",
          productAvailable: generatedProduct.productAvailable || false,
        });
        toast.success('Product generated successfully!');
      }
      
      
      setShowModal(false);
      setAiPrompt("");
    } catch (error) {
      toast.error("Error generating product. Please try again.");
    } finally {
      setGeneratingProduct(false);
    }
  };

  
  const canGenerateDescription = product.name.trim() && product.category;
  const canGenerateImage = product.name.trim() && product.category && product.description.trim();

  return (
    <div className="container py-5" style={{ marginTop: '70px', maxWidth: '860px' }}>

      
      <div className="d-flex justify-content-between align-items-start mb-4">
        <div>
          <h3 className="fw-bold mb-1">➕ Add New Product</h3>
          <p className="text-muted small mb-0">Fill in the product details below. Fields marked <span className="text-danger">*</span> are required.</p>
        </div>
        <button
          type="button"
          className="btn btn-sm d-flex align-items-center gap-1 fw-semibold"
          style={{
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            color: '#fff',
            border: 'none',
            borderRadius: '8px',
            padding: '8px 16px',
          }}
          onClick={() => setShowModal(true)}
        >
          <i className="bi bi-robot me-1"></i> Generate with AI
        </button>
      </div>

      <div className="card card-theme border-0">
        <div className="card-body p-4">
          <form className="row g-4 needs-validation" noValidate validated={validated.toString()} onSubmit={submitHandler}>

            
            <div className="col-12">
              <p className="fw-semibold text-muted small text-uppercase mb-3" style={{ letterSpacing: '0.6px' }}>
                <i className="bi bi-info-circle me-1"></i> Basic Information
              </p>
              <div className="row g-3">
                <div className="col-md-6">
                  <label htmlFor="name" className="form-label fw-semibold">
                    Product Name <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    className={`form-control ${validated ? (errors.name ? 'is-invalid' : 'is-valid') : ''}`}
                    placeholder="e.g. Apple MacBook Pro 16"
                    onChange={handleInputChange}
                    value={product.name}
                    name="name"
                    id="name"
                    required
                  />
                  {errors.name && <div className="invalid-feedback">{errors.name}</div>}
                </div>

                <div className="col-md-6">
                  <label htmlFor="brand" className="form-label fw-semibold">
                    Brand <span className="text-danger">*</span>
                  </label>
                  <input
                    type="text"
                    name="brand"
                    className={`form-control ${validated ? (errors.brand ? 'is-invalid' : 'is-valid') : ''}`}
                    placeholder="e.g. Apple"
                    value={product.brand}
                    onChange={handleInputChange}
                    id="brand"
                    required
                  />
                  {errors.brand && <div className="invalid-feedback">{errors.brand}</div>}
                </div>

                <div className="col-md-4">
                  <label htmlFor="category" className="form-label fw-semibold">
                    Category <span className="text-danger">*</span>
                  </label>
                  <select
                    className={`form-select ${validated ? (errors.category ? 'is-invalid' : 'is-valid') : ''}`}
                    value={product.category}
                    onChange={handleInputChange}
                    name="category"
                    id="category"
                    required
                  >
                    <option value="">Select category…</option>
                    <option value="Laptop">💻 Laptop</option>
                    <option value="Headphone">🎧 Headphone</option>
                    <option value="Mobile">📱 Mobile</option>
                    <option value="Electronics">🔌 Electronics</option>
                    <option value="Toys">🧸 Toys</option>
                    <option value="Fashion">👗 Fashion</option>
                  </select>
                  {errors.category && <div className="invalid-feedback">{errors.category}</div>}
                </div>

                <div className="col-md-4">
                  <label htmlFor="price" className="form-label fw-semibold">
                    Price (₹) <span className="text-danger">*</span>
                  </label>
                  <div className="input-group">
                    <span className="input-group-text fw-bold">₹</span>
                    <input
                      type="number"
                      className={`form-control ${validated ? (errors.price ? 'is-invalid' : 'is-valid') : ''}`}
                      placeholder="0.00"
                      onChange={handleInputChange}
                      value={product.price}
                      name="price"
                      id="price"
                      min="0.01"
                      step="0.01"
                      required
                    />
                    {errors.price && <div className="invalid-feedback">{errors.price}</div>}
                  </div>
                </div>

                <div className="col-md-4">
                  <label htmlFor="stockQuantity" className="form-label fw-semibold">
                    Stock Quantity <span className="text-danger">*</span>
                  </label>
                  <input
                    type="number"
                    className={`form-control ${validated ? (errors.stockQuantity ? 'is-invalid' : 'is-valid') : ''}`}
                    placeholder="e.g. 50"
                    onChange={handleInputChange}
                    value={product.stockQuantity}
                    name="stockQuantity"
                    id="stockQuantity"
                    min="0"
                    required
                  />
                  {errors.stockQuantity && <div className="invalid-feedback">{errors.stockQuantity}</div>}
                </div>

                <div className="col-md-4">
                  <label htmlFor="releaseDate" className="form-label fw-semibold">
                    Release Date <span className="text-danger">*</span>
                  </label>
                  <input
                    type="date"
                    className={`form-control ${validated ? (errors.releaseDate ? 'is-invalid' : 'is-valid') : ''}`}
                    value={product.releaseDate}
                    name="releaseDate"
                    onChange={handleInputChange}
                    id="releaseDate"
                    required
                  />
                  {errors.releaseDate && <div className="invalid-feedback">{errors.releaseDate}</div>}
                </div>
              </div>
            </div>

            
            <div className="col-12">
              <hr style={{ borderColor: 'var(--border-color)' }} />
            </div>

            
            <div className="col-12">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <label htmlFor="description" className="form-label fw-semibold mb-0">
                  Description <span className="text-muted fw-normal">(Optional)</span>
                </label>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-primary d-flex align-items-center gap-1"
                  onClick={generateDescription}
                  disabled={!canGenerateDescription || generatingDescription}
                  title={!canGenerateDescription ? "Enter product name & category first" : "Generate description with AI"}
                >
                  {generatingDescription ? (
                    <><span className="spinner-border spinner-border-sm" role="status" />&nbsp;Generating…</>
                  ) : (
                    <><i className="bi bi-robot"></i>&nbsp;Generate with AI</>
                  )}
                </button>
              </div>
              <textarea
                className={`form-control ${validated && errors.description ? 'is-invalid' : ''}`}
                placeholder="Describe the product — materials, features, specs, use case…"
                value={product.description}
                name="description"
                onChange={handleInputChange}
                id="description"
                rows="4"
              />
              {errors.description && <div className="invalid-feedback">{errors.description}</div>}
              {!canGenerateDescription && (
                <div className="form-text">
                  <i className="bi bi-info-circle me-1"></i>
                  Fill in Name &amp; Category to unlock AI description generation.
                </div>
              )}
            </div>

            
            <div className="col-12">
              <hr style={{ borderColor: 'var(--border-color)' }} />
            </div>

            
            <div className="col-12">
              <div className="d-flex justify-content-between align-items-center mb-2">
                <label htmlFor="imageFile" className="form-label fw-semibold mb-0">
                  Product Image <span className="text-muted fw-normal">(Optional)</span>
                </label>
                <button
                  type="button"
                  className="btn btn-sm btn-outline-success d-flex align-items-center gap-1"
                  onClick={generateImage}
                  disabled={!canGenerateImage || generatingImage}
                  title={!canGenerateImage ? "Enter Name, Category & Description first" : "Generate image with AI"}
                >
                  {generatingImage ? (
                    <><span className="spinner-border spinner-border-sm" role="status" />&nbsp;Generating…</>
                  ) : (
                    <><i className="bi bi-image"></i>&nbsp;Generate with AI</>
                  )}
                </button>
              </div>

              <input
                className={`form-control ${validated && errors.image ? 'is-invalid' : ''}`}
                type="file"
                onChange={handleImageChange}
                id="imageFile"
                accept="image/png, image/jpeg"
              />
              {errors.image && <div className="invalid-feedback">{errors.image}</div>}
              <div className="form-text">
                Upload JPG or PNG (max 5 MB) — or use AI generation above.
              </div>
              {!canGenerateImage && (
                <div className="form-text mt-1">
                  <i className="bi bi-info-circle me-1"></i>
                  Fill in Name, Category &amp; Description to unlock AI image generation.
                </div>
              )}

              
              {imagePreview && (
                <div className="mt-3">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <small className="text-muted">
                      {aiGeneratedImage ? '🤖 AI Generated Image Preview' : '📁 Selected Image Preview'}
                    </small>
                    <button
                      type="button"
                      className="btn btn-sm btn-outline-danger d-flex align-items-center gap-1"
                      onClick={() => {
                        setImagePreview(null);
                        setImage(null);
                        setAiGeneratedImage(null);
                        document.getElementById('imageFile').value = '';
                      }}
                    >
                      <i className="bi bi-trash"></i> Remove
                    </button>
                  </div>
                  <div
                    className="rounded-3 p-3 d-flex align-items-center justify-content-center"
                    style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border-color)', minHeight: '160px' }}
                  >
                    <img
                      src={imagePreview}
                      alt="Preview"
                      className="img-fluid rounded-2"
                      style={{ maxHeight: '200px', maxWidth: '100%', objectFit: 'contain' }}
                    />
                  </div>
                </div>
              )}
            </div>

            
            <div className="col-12">
              <hr style={{ borderColor: 'var(--border-color)' }} />
            </div>

            
            <div className="col-12">
              <div className="d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-3">
                <div className="form-check form-switch d-flex align-items-center gap-2 mb-0">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    role="switch"
                    name="productAvailable"
                    id="productAvailable"
                    checked={product.productAvailable}
                    onChange={(e) => setProduct({ ...product, productAvailable: e.target.checked })}
                    style={{ width: '2.5em', height: '1.4em' }}
                  />
                  <label className="form-check-label fw-semibold" htmlFor="productAvailable">
                    {product.productAvailable ? (
                      <span className="text-success">✅ Available for sale</span>
                    ) : (
                      <span className="text-muted">⏸ Not yet available</span>
                    )}
                  </label>
                </div>

                <div className="d-flex gap-2">
                  <button
                    type="button"
                    className="btn btn-outline-secondary px-4"
                    onClick={() => {
                      setProduct({ name: '', brand: '', description: '', price: '', category: '', stockQuantity: '', releaseDate: '', productAvailable: false });
                      setImage(null); setAiGeneratedImage(null); setImagePreview(null); setValidated(false); setErrors({});
                    }}
                  >
                    Reset
                  </button>
                  {loading ? (
                    <button className="btn btn-primary px-5" type="button" disabled>
                      <span className="spinner-border spinner-border-sm me-2" role="status" />
                      Saving…
                    </button>
                  ) : (
                    <button
                      type="submit"
                      className="btn px-5 fw-semibold"
                      style={{
                        background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '8px',
                      }}
                    >
                      <i className="bi bi-plus-circle me-2"></i>Add Product
                    </button>
                  )}
                </div>
              </div>
            </div>

          </form>
        </div>
      </div>

      
      {showModal && (
        <div className="modal fade show" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header bg-primary text-white">
                <h5 className="modal-title">
                  <i className="bi bi-robot me-2"></i>
                  Generate Product with AI
                </h5>
                <button 
                  type="button" 
                  className="btn-close btn-close-white" 
                  onClick={() => setShowModal(false)}
                  disabled={generatingProduct}
                ></button>
              </div>
              <div className="modal-body">
                <p className="text-muted">Describe the product you want to create, and our AI will generate product details for you.</p>
                <div className="mb-3">
                  <label htmlFor="aiPrompt" className="form-label">Product Description</label>
                  <textarea
                    id="aiPrompt"
                    className="form-control"
                    rows="4"
                    placeholder="E.g., A premium gaming laptop with high-end graphics card, 32GB RAM, and 1TB SSD storage"
                    value={aiPrompt}
                    onChange={(e) => setAiPrompt(e.target.value)}
                    disabled={generatingProduct}
                  ></textarea>
                </div>
                <div className="form-text mb-3">
                  <i className="bi bi-info-circle me-1"></i>
                  The more detailed your description, the better the generated product will be.
                </div>
              </div>
              <div className="modal-footer">
                <button 
                  type="button" 
                  className="btn btn-secondary" 
                  onClick={() => setShowModal(false)}
                  disabled={generatingProduct}
                >
                  Cancel
                </button>
                <button 
                  type="button" 
                  className="btn btn-primary" 
                  onClick={handleGenerate}
                  disabled={generatingProduct || !aiPrompt.trim()}
                >
                  {generatingProduct ? (
                    <>
                      <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                      Generating...
                    </>
                  ) : (
                    <>
                      <i className="bi bi-lightning-fill me-1"></i>
                      Generate
                    </>
                  )}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AddProduct;