import axios from '../axios';
import React, { useState, useEffect } from 'react';
import { Modal, Button, Form, Toast, ToastContainer } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../Context/AuthContext';

const CheckoutPopup = ({ show, handleClose, cartItems, totalPrice, clearCart }) => {
  const baseUrl = import.meta.env.VITE_BASE_URL;
  const navigate = useNavigate();
  const { username } = useAuth();

  const [name, setName] = useState(username || '');
  const [email, setEmail] = useState('');

  useEffect(() => {
    if (username && !name) {
      setName(username);
    }
  }, [username]);
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');
  const [city, setCity] = useState('');
  const [pincode, setPincode] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('cod');

  const [validated, setValidated] = useState(false);
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  const [toastVariant, setToastVariant] = useState('success');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleConfirm = async (event) => {
    event.preventDefault();
    const form = event.currentTarget;

    if (form.checkValidity() === false) {
      event.stopPropagation();
      setValidated(true);
      return;
    }

    setValidated(true);
    setIsSubmitting(true);

    const orderItems = cartItems.map((item) => ({
      productId: item.id,
      quantity: item.quantity,
    }));

    const data = {
      customerName: name,
      email: email,
      items: orderItems,
    };

    try {
      await axios.post('/api/orders/place', data);

      setToastVariant('success');
      setToastMessage('🎉 Order placed successfully! Check your Orders tab.');
      setShowToast(true);

      localStorage.removeItem('cart');
      if (typeof clearCart === 'function') {
        clearCart();
      }
      setTimeout(() => {
        navigate('/orders');
      }, 1500);
    } catch (error) {
      console.error(error);
      setToastVariant('danger');
      setToastMessage('Failed to place order. Please try again.');
      setShowToast(true);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <>
      <Modal show={show} onHide={handleClose} centered size="lg">
        <Modal.Header closeButton>
          <Modal.Title className="fw-bold d-flex align-items-center gap-2">
            <i className="bi bi-bag-check text-primary"></i>
            <span>Complete Your Order</span>
          </Modal.Title>
        </Modal.Header>
        <Form noValidate validated={validated} onSubmit={handleConfirm}>
          <Modal.Body className="px-4 py-3">
            <div className="row g-4">
              
              <div className="col-md-5 border-end pe-md-4">
                <h6 className="fw-bold mb-3 small text-uppercase text-muted" style={{ letterSpacing: '0.5px' }}>
                  Order Summary ({cartItems.length} items)
                </h6>
                <div
                  className="checkout-items mb-3"
                  style={{ maxHeight: '220px', overflowY: 'auto' }}
                >
                  {cartItems.map((item) => (
                    <div key={item.id} className="d-flex align-items-center mb-3 pb-2 border-bottom">
                      <img
                        src={`${baseUrl}/api/product/${item.id}/image`}
                        alt={item.name}
                        className="me-2 rounded"
                        style={{ width: '50px', height: '50px', objectFit: 'contain', background: 'var(--bg-subtle)' }}
                      />
                      <div className="flex-grow-1 min-w-0">
                        <div className="small fw-bold text-truncate" title={item.name}>
                          {item.name}
                        </div>
                        <div className="small text-muted">
                          Qty: {item.quantity} × ₹{item.price?.toLocaleString('en-IN')}
                        </div>
                      </div>
                      <div className="small fw-bold ms-2">
                        ₹{(item.price * item.quantity).toFixed(0)}
                      </div>
                    </div>
                  ))}
                </div>

                <div className="p-3 rounded-3" style={{ background: 'var(--bg-subtle)', border: '1px solid var(--border-color)' }}>
                  <div className="d-flex justify-content-between small text-muted mb-1">
                    <span>Subtotal</span>
                    <span>₹{totalPrice.toFixed(2)}</span>
                  </div>
                  <div className="d-flex justify-content-between small text-muted mb-2">
                    <span>Shipping</span>
                    <span className="text-success fw-bold">FREE</span>
                  </div>
                  <hr className="my-2" style={{ borderColor: 'var(--border-color)' }} />
                  <div className="d-flex justify-content-between fw-bold fs-6">
                    <span>Total Amount</span>
                    <span className="text-primary">₹{totalPrice.toFixed(2)}</span>
                  </div>
                </div>
              </div>

              
              <div className="col-md-7 ps-md-4">
                <h6 className="fw-bold mb-3 small text-uppercase text-muted" style={{ letterSpacing: '0.5px' }}>
                  Shipping &amp; Delivery Details
                </h6>

                <div className="row g-2 mb-3">
                  <div className="col-sm-6">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">Full Name *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="text"
                        placeholder="John Doe"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                  <div className="col-sm-6">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">Email *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="email"
                        placeholder="john@example.com"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-sm-6">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">Phone Number *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="tel"
                        placeholder="+91 98765 43210"
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                  <div className="col-sm-6">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">PIN Code *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="text"
                        placeholder="110001"
                        value={pincode}
                        onChange={(e) => setPincode(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                </div>

                <div className="row g-2 mb-3">
                  <div className="col-sm-8">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">Street Address *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="text"
                        placeholder="Flat / House No., Landmark"
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                  <div className="col-sm-4">
                    <Form.Group>
                      <Form.Label className="small fw-semibold mb-1">City *</Form.Label>
                      <Form.Control
                        size="sm"
                        type="text"
                        placeholder="New Delhi"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                        required
                      />
                    </Form.Group>
                  </div>
                </div>

                
                <h6 className="fw-bold mt-4 mb-2 small text-uppercase text-muted" style={{ letterSpacing: '0.5px' }}>
                  Payment Method
                </h6>
                <div className="d-flex flex-column gap-2 mb-3">
                  <label
                    className={`p-2 rounded-3 border d-flex align-items-center justify-content-between cursor-pointer ${
                      paymentMethod === 'cod' ? 'border-primary bg-primary bg-opacity-10' : ''
                    }`}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="d-flex align-items-center gap-2 small">
                      <input
                        type="radio"
                        name="payment"
                        value="cod"
                        checked={paymentMethod === 'cod'}
                        onChange={(e) => setPaymentMethod(e.target.value)}
                      />
                      <span>💵 Cash on Delivery (COD)</span>
                    </div>
                    <span className="badge bg-secondary-subtle text-secondary small">Pay on arrival</span>
                  </label>

                  <label
                    className={`p-2 rounded-3 border d-flex align-items-center justify-content-between cursor-pointer ${
                      paymentMethod === 'upi' ? 'border-primary bg-primary bg-opacity-10' : ''
                    }`}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="d-flex align-items-center gap-2 small">
                      <input
                        type="radio"
                        name="payment"
                        value="upi"
                        checked={paymentMethod === 'upi'}
                        onChange={(e) => setPaymentMethod(e.target.value)}
                      />
                      <span>⚡ Instant UPI / QR Code</span>
                    </div>
                    <span className="badge bg-success-subtle text-success small">Instant</span>
                  </label>

                  <label
                    className={`p-2 rounded-3 border d-flex align-items-center justify-content-between cursor-pointer ${
                      paymentMethod === 'card' ? 'border-primary bg-primary bg-opacity-10' : ''
                    }`}
                    style={{ cursor: 'pointer' }}
                  >
                    <div className="d-flex align-items-center gap-2 small">
                      <input
                        type="radio"
                        name="payment"
                        value="card"
                        checked={paymentMethod === 'card'}
                        onChange={(e) => setPaymentMethod(e.target.value)}
                      />
                      <span>💳 Credit / Debit Card</span>
                    </div>
                    <span className="badge bg-secondary-subtle text-secondary small">All cards</span>
                  </label>
                </div>
              </div>
            </div>
          </Modal.Body>
          <Modal.Footer className="px-4">
            <Button variant="outline-secondary" onClick={handleClose} disabled={isSubmitting}>
              Cancel
            </Button>
            <Button
              variant="primary"
              type="submit"
              disabled={isSubmitting}
              style={{
                background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)',
                border: 'none',
                padding: '8px 24px',
                fontWeight: 600,
              }}
            >
              {isSubmitting ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  Placing Order...
                </>
              ) : (
                `Place Order • ₹${totalPrice.toFixed(0)}`
              )}
            </Button>
          </Modal.Footer>
        </Form>
      </Modal>

      <ToastContainer position="top-end" className="p-3" style={{ zIndex: 1070 }}>
        <Toast
          show={showToast}
          onClose={() => setShowToast(false)}
          delay={3000}
          autohide
          bg={toastVariant}
        >
          <Toast.Header closeButton>
            <strong className="me-auto">Order Notification</strong>
          </Toast.Header>
          <Toast.Body className={toastVariant === 'success' ? 'text-white' : ''}>
            {toastMessage}
          </Toast.Body>
        </Toast>
      </ToastContainer>
    </>
  );
};

export default CheckoutPopup;