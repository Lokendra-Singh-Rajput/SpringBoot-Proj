import React, { useState, useEffect } from 'react';
import axios from '../axios';
import { useNavigate, useSearchParams } from 'react-router-dom';

const SemanticSearch = () => {
  const [searchParams] = useSearchParams();
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [currentQuery, setCurrentQuery] = useState('');
  const navigate = useNavigate();
  const baseUrl = import.meta.env.VITE_BASE_URL || (import.meta.env.DEV ? "http://localhost:8080" : "");

  const executeSearch = async (searchTerm) => {
    if (!searchTerm.trim()) return;
    setLoading(true);
    setSearched(true);
    setCurrentQuery(searchTerm.trim());
    try {
      const res = await axios.get(`/api/products/semantic-search?query=${encodeURIComponent(searchTerm)}`);
      setResults(res.data);
    } catch (err) {
      console.error('Semantic search failed:', err);
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const q = searchParams.get('q');
    if (q) {
      executeSearch(q);
    } else {
      
      setSearched(false);
      setResults([]);
      setCurrentQuery('');
    }
  }, [searchParams]);

  return (
    <div className="container py-4" style={{ marginTop: '75px' }}>

      
      <div className="mb-4">
        <div className="d-flex align-items-center gap-2 mb-1">
          <span style={{ fontSize: '1.5rem' }}>✨</span>
          <h4 className="fw-bold mb-0">AI Semantic Search</h4>
        </div>
        <p className="text-muted small mb-0">
          Search by intent, not just keywords — powered by vector embeddings &amp; RAG.
          {' '}Use the <strong>search bar in the navbar</strong> to enter a new query.
        </p>
      </div>

      
      {loading && (
        <div className="text-center py-5">
          <div className="spinner-border text-primary mb-3" style={{ width: '2.5rem', height: '2.5rem' }} role="status" />
          <p className="text-muted small">Searching with AI…</p>
        </div>
      )}

      
      {!loading && !searched && (
        <div
          className="text-center py-5 rounded-4"
          style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border-color)' }}
        >
          <div style={{ fontSize: '3rem' }}>🔍</div>
          <h5 className="fw-semibold mt-3 mb-1">Start your AI search</h5>
          <p className="text-muted mb-0">
            Type something in the <strong>search bar above</strong> and press Search.
            <br />
            <span className="small opacity-75">e.g. "wireless headphones for gym under ₹3000"</span>
          </p>
        </div>
      )}

      
      {!loading && searched && results.length === 0 && (
        <div
          className="text-center py-5 rounded-4"
          style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border-color)' }}
        >
          <div style={{ fontSize: '3rem' }}>😕</div>
          <h5 className="fw-semibold mt-3 mb-1">No matches for "{currentQuery}"</h5>
          <p className="text-muted mb-3 small">
            Try a different description or refine your query in the navbar search bar.
          </p>
          <a href="/" className="btn btn-sm btn-outline-primary">
            ← Browse full catalog
          </a>
        </div>
      )}

      
      {!loading && results.length > 0 && (
        <>
          
          <div className="d-flex align-items-center justify-content-between mb-3 pb-2" style={{ borderBottom: '1px solid var(--border-color)' }}>
            <div>
              <span className="fw-semibold">{results.length} result{results.length !== 1 ? 's' : ''}</span>
              <span className="text-muted"> for </span>
              <span className="fw-semibold text-primary">"{currentQuery}"</span>
            </div>
            <a href="/" className="btn btn-sm btn-outline-secondary">← Catalog</a>
          </div>

          
          <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
            {results.map((product) => (
              <div key={product.id} className="col">
                <div
                  className="card h-100 card-theme border-0 position-relative"
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/product/${product.id}`)}
                >
                  
                  <div className="position-absolute top-0 start-0 m-2" style={{ zIndex: 2 }}>
                    <span className="badge bg-dark bg-opacity-75" style={{ fontSize: '10px' }}>
                      {product.category}
                    </span>
                  </div>

                  
                  <div
                    style={{
                      height: '160px',
                      overflow: 'hidden',
                      borderTopLeftRadius: '12px',
                      borderTopRightRadius: '12px',
                      backgroundColor: 'var(--bg-subtle)',
                    }}
                  >
                    <img
                      src={`${baseUrl}/api/product/${product.id}/image`}
                      alt={product.name}
                      className="w-100 h-100"
                      style={{ objectFit: 'cover' }}
                      onError={(e) => { e.target.style.display = 'none'; }}
                    />
                  </div>

                  
                  <div className="card-body d-flex flex-column p-3">
                    <small className="text-muted text-uppercase fw-semibold" style={{ fontSize: '11px', letterSpacing: '0.5px' }}>
                      {product.brand}
                    </small>
                    <h6 className="fw-bold my-1 text-truncate" title={product.name} style={{ fontSize: '14px' }}>
                      {product.name}
                    </h6>
                    <div className="mt-auto pt-2 d-flex align-items-baseline justify-content-between">
                      <h5 className="fw-bold mb-0 text-primary" style={{ fontSize: '1.05rem' }}>
                        ₹{product.price?.toLocaleString('en-IN')}
                      </h5>
                      {!product.productAvailable && (
                        <span className="badge bg-secondary" style={{ fontSize: '10px' }}>Out of Stock</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default SemanticSearch;
