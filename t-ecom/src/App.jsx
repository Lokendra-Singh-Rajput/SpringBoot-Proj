import React, { useState } from "react";
import Home from "./components/Home";
import Navbar from "./components/Navbar";
import Cart from "./components/Cart";
import AddProduct from "./components/AddProduct";
import Product from "./components/Product";
import {
  BrowserRouter,
  Routes,
  Route,
  useLocation,
} from "react-router-dom";
import { AppProvider } from "./Context/Context";
import { AuthProvider } from "./Context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import UpdateProduct from "./components/UpdateProduct";
import Order from "./components/Order";
import Login from "./components/Login";
import SearchResults from "./components/SearchResults";
import AiChatWidget from "./components/AiChatWidget";
import SemanticSearch from "./components/SemanticSearch";
import Footer from "./components/Footer";

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { ToastContainer } from "react-toastify";

function AppContent({ selectedCategory, handleCategorySelect }) {
  const location = useLocation();

  const hideNavbar = location.pathname === "/login";

  return (
    <>
      {!hideNavbar && (
        <Navbar onSelectCategory={handleCategorySelect} />
      )}

      <div className="min-vh-100 pb-5">
        <Routes>
          <Route path="/login" element={<Login />} />

          <Route
            path="/"
            element={
              <Home selectedCategory={selectedCategory} />
            }
          />

          
          <Route
            path="/add_product"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <AddProduct />
              </ProtectedRoute>
            }
          />

          <Route
            path="/product/update/:id"
            element={
              <ProtectedRoute requiredRole="ADMIN">
                <UpdateProduct />
              </ProtectedRoute>
            }
          />

          
          <Route path="/product" element={<Product />} />
          <Route path="/product/:id" element={<Product />} />

          
          <Route path="/cart" element={<Cart />} />
          <Route
            path="/search-results"
            element={<SearchResults />}
          />
          <Route path="/semantic-search" element={<SemanticSearch />} />

          
          <Route
            path="/orders"
            element={
              <ProtectedRoute>
                <Order />
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>

      {!hideNavbar && <Footer />}
      {!hideNavbar && <AiChatWidget />}
    </>
  );
}

function App() {
  const [selectedCategory, setSelectedCategory] = useState("");

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
  };

  return (
    <AuthProvider>
      <AppProvider>
        <BrowserRouter>
          <ToastContainer
            autoClose={2000}
            hideProgressBar={true}
          />

          <AppContent
            selectedCategory={selectedCategory}
            handleCategorySelect={handleCategorySelect}
          />
        </BrowserRouter>
      </AppProvider>
    </AuthProvider>
  );
}

export default App;