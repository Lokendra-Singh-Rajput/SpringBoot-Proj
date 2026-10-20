import axios from "../axios";
import React, { useEffect, useState } from "react";
import { useAuth } from "../Context/AuthContext";
import { toast } from "react-toastify";

const Order = () => {
  const { isAdmin } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedOrder, setExpandedOrder] = useState(null);
  const [updatingId, setUpdatingId] = useState(null);

  const fetchOrders = async () => {
    try {
      const response = await axios.get("/api/orders");
      setOrders(response.data);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch orders. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const toggleOrderDetails = (orderId) => {
    setExpandedOrder(expandedOrder === orderId ? null : orderId);
  };

  const handleStatusChange = async (orderId, newStatus) => {
    setUpdatingId(orderId);
    try {
      await axios.patch(`/api/orders/${orderId}/status`, { status: newStatus });
      toast.success(`Order ${orderId} updated to ${newStatus}`);
      setOrders((prev) =>
        prev.map((ord) => (ord.orderId === orderId ? { ...ord, status: newStatus } : ord))
      );
    } catch (err) {
      console.error(err);
      toast.error("Failed to update order status.");
    } finally {
      setUpdatingId(null);
    }
  };

  const getStatusClass = (status) => {
    switch (status) {
      case "PLACED":
        return "bg-info text-dark";
      case "CONFIRMED":
        return "bg-primary text-white";
      case "SHIPPED":
        return "bg-warning text-dark";
      case "DELIVERED":
        return "bg-success text-white";
      case "CANCELLED":
        return "bg-danger text-white";
      default:
        return "bg-secondary text-white";
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat("en-IN", {
      style: "currency",
      currency: "INR",
      maximumFractionDigits: 2,
    }).format(amount);
  };

  const calculateOrderTotal = (items) => {
    return items.reduce((total, item) => total + item.totalPrice, 0);
  };

  if (loading) {
    return (
      <div className="container mt-5 pt-5 text-center py-5">
        <div className="spinner-border text-primary my-5" role="status">
          <span className="visually-hidden">Loading orders...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="container mt-5 pt-5">
        <div className="alert alert-danger" role="alert">
          {error}
        </div>
      </div>
    );
  }

  return (
    <div className="container py-4" style={{ marginTop: "75px" }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h2 className="fw-bold mb-1">
            {isAdmin ? "Admin Order Management" : "My Orders"}
          </h2>
          <p className="text-muted small mb-0">
            {isAdmin
              ? "View customer orders, tracking details, and update fulfillment status."
              : "Track your placed orders and fulfillment status in real-time."}
          </p>
        </div>

        {isAdmin && (
          <span className="badge bg-danger px-3 py-2 text-uppercase" style={{ fontSize: "11px" }}>
            <i className="bi bi-shield-lock-fill me-1"></i> Admin Privileges Active
          </span>
        )}
      </div>

      <div className="card shadow-sm border-0" style={{ borderRadius: "12px", overflow: "hidden" }}>
        <div
          className="card-header text-white d-flex justify-content-between align-items-center py-3"
          style={{ background: "linear-gradient(135deg, #1c1815 0%, #2b231e 100%)", borderBottom: "1px solid var(--border-color)" }}
        >
          <span className="fw-semibold">
            <i className="bi bi-box-seam me-2"></i>
            Total Orders: {orders.length}
          </span>
          <button
            onClick={fetchOrders}
            className="btn btn-sm btn-outline-light d-flex align-items-center gap-1"
          >
            <i className="bi bi-arrow-clockwise"></i> Refresh
          </button>
        </div>

        <div className="card-body p-0">
          <div className="table-responsive">
            <table className="table table-hover align-middle mb-0">
              <thead className="table-light">
                <tr className="small text-uppercase text-muted">
                  <th className="ps-3">Order ID</th>
                  <th>Customer Info</th>
                  <th>Date</th>
                  <th>Status</th>
                  <th>Items</th>
                  <th>Total</th>
                  <th className="text-end pe-3">Actions</th>
                </tr>
              </thead>

              <tbody>
                {orders.length === 0 ? (
                  <tr>
                    <td colSpan="7" className="text-center py-5 text-muted">
                      <i className="bi bi-inbox display-4 d-block mb-3 opacity-50"></i>
                      <h6>No orders found</h6>
                      <p className="small">When orders are placed, they will appear here.</p>
                    </td>
                  </tr>
                ) : (
                  orders.map((order) => (
                    <React.Fragment key={order.orderId}>
                      <tr>
                        <td className="ps-3">
                          <strong className="text-primary">{order.orderId}</strong>
                        </td>

                        <td>
                          <div className="fw-semibold small">{order.customerName}</div>
                          <small className="text-muted">{order.email}</small>
                        </td>

                        <td className="small text-muted">
                          {new Date(order.orderDate).toLocaleDateString("en-IN", {
                            year: "numeric",
                            month: "short",
                            day: "numeric",
                          })}
                        </td>

                        <td>
                          
                          {isAdmin ? (
                            <div className="d-flex align-items-center gap-1">
                              <select
                                className="form-select form-select-sm fw-bold"
                                style={{ minWidth: "125px", fontSize: "12px" }}
                                value={order.status}
                                disabled={updatingId === order.orderId}
                                onChange={(e) => handleStatusChange(order.orderId, e.target.value)}
                              >
                                <option value="PLACED">PLACED</option>
                                <option value="CONFIRMED">CONFIRMED</option>
                                <option value="SHIPPED">SHIPPED</option>
                                <option value="DELIVERED">DELIVERED</option>
                                <option value="CANCELLED">CANCELLED</option>
                              </select>
                              {updatingId === order.orderId && (
                                <span className="spinner-border spinner-border-sm text-primary"></span>
                              )}
                            </div>
                          ) : (
                            <span className={`badge ${getStatusClass(order.status)} px-2 py-1`}>
                              {order.status}
                            </span>
                          )}
                        </td>

                        <td>
                          <span className="order-items-badge">
                            {order.items.length} {order.items.length === 1 ? "item" : "items"}
                          </span>
                        </td>

                        <td>
                          <strong className="text-primary">
                            {formatCurrency(calculateOrderTotal(order.items))}
                          </strong>
                        </td>

                        <td className="text-end pe-3">
                          <button
                            className="btn btn-sm btn-outline-secondary"
                            style={{ borderRadius: "6px" }}
                            onClick={() => toggleOrderDetails(order.orderId)}
                          >
                            {expandedOrder === order.orderId ? (
                              <>
                                <i className="bi bi-chevron-up me-1"></i> Hide
                              </>
                            ) : (
                              <>
                                <i className="bi bi-chevron-down me-1"></i> Details
                              </>
                            )}
                          </button>
                        </td>
                      </tr>

                      {expandedOrder === order.orderId && (
                        <tr>
                          <td colSpan="7" className="p-0">
                            <div className="order-details-expanded p-3 border-top border-bottom" style={{ borderColor: "var(--border-color)" }}>
                              <h6 className="fw-bold mb-2 small text-uppercase text-muted">
                                Order Items Breakdown ({order.items.length})
                              </h6>

                              <table className="table table-sm table-bordered mb-0 order-details-table" style={{ borderRadius: "6px", overflow: "hidden" }}>
                                <thead className="small">
                                  <tr>
                                    <th>Product Name</th>
                                    <th className="text-center" style={{ width: "120px" }}>Quantity</th>
                                    <th className="text-end" style={{ width: "160px" }}>Subtotal</th>
                                  </tr>
                                </thead>

                                <tbody>
                                  {order.items.map((item, index) => (
                                    <tr key={index} className="small">
                                      <td className="fw-semibold">{item.productName}</td>
                                      <td className="text-center">{item.quantity}</td>
                                      <td className="text-end fw-semibold">
                                        {formatCurrency(item.totalPrice)}
                                      </td>
                                    </tr>
                                  ))}

                                  <tr className="order-details-total-row fw-bold">
                                    <td colSpan="2" className="text-end">
                                      Order Total:
                                    </td>
                                    <td className="text-end text-primary">
                                      {formatCurrency(calculateOrderTotal(order.items))}
                                    </td>
                                  </tr>
                                </tbody>
                              </table>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Order;